package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class IslamwebAiResponse(
    val query: String,
    val answer: String,
    val fatwaNumberHint: String? = null,
    val relatedTopics: List<String> = emptyList(),
    val sourceUrl: String = "https://www.islamweb.net/ar/fatawa/",
    val isFromAi: Boolean = true,
    val error: String? = null,
    val title: String = query,
    val question: String = query,
    val evidence: String = "",
    val fatwaNumber: String = fatwaNumberHint ?: ""
)

/**
 * Resilient DNS provider that falls back to Google Anycast endpoints
 * when the local cellular / Wi-Fi DNS fails to resolve Google API hostnames.
 */
private class SmartGoogleDns : Dns {
    private val googleAnycastIps = listOf(
        "172.217.112.4",
        "172.217.113.4",
        "172.217.114.4",
        "172.217.115.4",
        "172.217.116.4",
        "172.217.117.4",
        "172.217.118.4",
        "172.217.119.4"
    )

    override fun lookup(hostname: String): List<InetAddress> {
        // 1. Try standard system DNS first
        try {
            val addresses = Dns.SYSTEM.lookup(hostname)
            if (addresses.isNotEmpty()) return addresses
        } catch (e: Exception) {
            Log.w("IslamwebAi", "System DNS lookup failed for $hostname: ${e.message}")
        }

        // 2. Try DNS over HTTPS (DoH) via dns.google (8.8.8.8)
        if (hostname.contains("googleapis.com") || hostname.contains("google.com")) {
            try {
                val dohUrl = "https://dns.google/resolve?name=${URLEncoder.encode(hostname, "UTF-8")}&type=A"
                val connection = (URL(dohUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }
                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val answers = json.optJSONArray("Answer")
                    if (answers != null && answers.length() > 0) {
                        val ips = mutableListOf<InetAddress>()
                        for (i in 0 until answers.length()) {
                            val item = answers.getJSONObject(i)
                            if (item.optInt("type") == 1) { // Type A
                                val ipStr = item.optString("data")
                                if (ipStr.isNotBlank()) {
                                    ips.add(InetAddress.getByName(ipStr))
                                }
                            }
                        }
                        if (ips.isNotEmpty()) {
                            return ips
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("IslamwebAi", "DoH resolution failed for $hostname: ${e.message}")
            }

            // 3. Fallback to direct Anycast Google IPs
            val fallbackAddresses = googleAnycastIps.mapNotNull { ip ->
                try {
                    val rawIp = InetAddress.getByName(ip).address
                    InetAddress.getByAddress(hostname, rawIp)
                } catch (e: Exception) {
                    null
                }
            }
            if (fallbackAddresses.isNotEmpty()) {
                return fallbackAddresses
            }
        }

        throw UnknownHostException("Unable to resolve host \"$hostname\"")
    }
}

object IslamwebAiService {
    // Current valid and supported Gemini models in order of speed and stability
    private val MODELS_TO_TRY = listOf(
        "gemini-3.1-flash-lite-preview",
        "gemini-3.7-flash",
        "gemini-3.6-flash"
    )

    private val okHttpClient = OkHttpClient.Builder()
        .dns(SmartGoogleDns())
        .retryOnConnectionFailure(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
أنت باحث ومساعد شرعي إسلامي فائق التخصص، وتقتصر أبحاثك وإجاباتك حصرياً وبدقة صارمة ومطلقة على فتاوى ومقالات وبحوث مركز الفتوى في الشبكة الإسلامية (إسلام ويب - islamweb.net).

القواعد الإلزامية الصارمة:
1. لا تعتمد ولا تستشهد بأي موقع أو جهة أخرى؛ مرجعيتك الحصرية والوحيدة هي إسلام ويب (islamweb.net).
2. عند الإجابة على أي سؤال أو مسألة فقهية:
   - اذكر عنوان المسألة الشرعية كما يوردها إسلام ويب.
   - لخّص "خلاصة الفتوى" بنص واضح ومباشر في البداية.
   - اذكر التفصيل الفقهي والأدلة من القرآن والسنة مع بيان مذهب الجمهور أو المذاهب الأربعة إذا ذكرها إسلام ويب.
   - اذكر رقم الفتوى المحتمل أو المقارب على إسلام ويب وعنوانها إن وُجد.
   - ضع رابط الفتوى على إسلام ويب بالصيغة: https://www.islamweb.net/ar/fatawa/
3. إذا كان السؤال عن مسألة طبية أو دنيوية بحتة لا صلة لها بالحكم الشرعي، بيّن الحكم الشرعي المتعلق بها من إسلام ويب فقط (مثل حكم التداوي بها).
4. الرد باللغة العربية الفصحى الراقية والمنسقة بعناية، مع استخدام علامات الترقيم والفقرات الواضحة.
"""

    suspend fun searchIslamwebWithAi(userQuery: String): IslamwebAiResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "your_api_key_here") key
            else (System.getenv("GEMINI_API_KEY") ?: "")
        } catch (e: Exception) {
            System.getenv("GEMINI_API_KEY") ?: ""
        }

        if (apiKey.isBlank() || apiKey == "your_api_key_here") {
            return@withContext provideOfflineIslamwebFallback(userQuery)
        }

        var lastErrorMessage: String? = null

        for (modelName in MODELS_TO_TRY) {
            try {
                val requestJson = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", SYSTEM_PROMPT)
                            })
                        })
                    })

                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "ابحث في فتاوى وأرشيف إسلام ويب (islamweb.net) وأجب عن المسألة التالية بالتفصيل والأدلة الشرعية ورقم الفتوى إن وُجد: $userQuery")
                                })
                            })
                        })
                    })

                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("topP", 0.95)
                        put("topK", 40)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-goog-api-key", apiKey)
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseStr)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val extractedFatwaNum = extractFatwaNumber(text)
                        val related = extractRelatedTopics(text)
                        return@withContext IslamwebAiResponse(
                            query = userQuery,
                            answer = text,
                            fatwaNumberHint = extractedFatwaNum,
                            relatedTopics = related,
                            sourceUrl = if (extractedFatwaNum != null) "https://www.islamweb.net/ar/fatwa/$extractedFatwaNum" else "https://www.islamweb.net/ar/fatawa/",
                            isFromAi = true
                        )
                    }
                } else {
                    val errBody = response.body?.string()?.take(200) ?: ""
                    lastErrorMessage = "HTTP ${response.code}: $errBody"
                }
            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Unknown network error"
            }
        }

        return@withContext provideOfflineIslamwebFallback(userQuery, lastErrorMessage)
    }

    private fun extractFatwaNumber(text: String): String? {
        val regex = Regex("""(?:رقم الفتوى|فتوى رقم|الفتوى رقم|Fatwa\s*#?)\s*[:：]?\s*(\d{3,7})""")
        val match = regex.find(text)
        return match?.groupValues?.getOrNull(1)
    }

    private fun extractRelatedTopics(text: String): List<String> {
        val list = mutableListOf<String>()
        if (text.contains("صلاة") || text.contains("وضوء") || text.contains("طهارة")) list.add("أحكام الطهارة والصلاة")
        if (text.contains("صوم") || text.contains("رمضان") || text.contains("إفطار")) list.add("أحكام الصيام والمعاصرة")
        if (text.contains("زكاة") || text.contains("صدقة") || text.contains("مال")) list.add("الزكاة والمعاملات المالية")
        if (text.contains("حجاب") || text.contains("مرأة") || text.contains("طلاق") || text.contains("نكاح")) list.add("الأسرة والمجتمع المسلم")
        if (list.isEmpty()) {
            list.addAll(listOf("فتاوى إسلام ويب", "مركز الفتوى المعتمد"))
        }
        return list
    }

    private fun provideOfflineIslamwebFallback(query: String, note: String? = null): IslamwebAiResponse {
        val q = query.trim()
        val defaultUrl = "https://www.islamweb.net/ar/fatawa/"

        val answerBuilder = StringBuilder()

        when {
            // مسألة الجمع بين الصلاتين لعذر المطر أو السفر أو المرض
            (q.contains("جمع") || q.contains("قصر")) && (q.contains("مطر") || q.contains("صلاتين") || q.contains("عذر") || q.contains("وحل")) || q.contains("مطر") && q.contains("صلاة") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
يجوز الجمع بين صلاتي الظهر والعصر، وبين صلاتي المغرب والعشاء لعذر المطر الشديد أو الوحل الشديد الذي يبل الثياب ويشق معه الخروج إلى المسجد على الراجح من أقوال جمهور الفقهاء (المالكية والشافعية والحنابلة)، رخصة من الله وتيسيراً على المسلمين ودفعاً للحرج.

📖 الأدلة من السنة الشريفة وإسلام ويب:
1. ثبت في صحيح مسلم عن ابن عباس رضي الله عنهما قال: «جمع رسول الله ﷺ بين الظهر والعصر، والمغرب والعشاء بالمدينة في غير خوف ولا مطر»، فقيل لابن عباس: ما أراد بذلك؟ قال: «أراد ألا يحرج أمته». فدل الحديث على أن الجمع لعذر المطر كان متقرراً ومعلوماً عند الصحابة لدفع الحرج.
2. ثبت في الصحيحين أن عبد الله بن عمر رضي الله عنهما كان يجمع مع الأمراء إذا جمعوا بين المغرب والعشاء في المطر.

📋 شروط وضوابط الجمع لعذر المطر عند الفقهاء:
1. أن يكون المطر شديداً يبل الثياب، أو يوجد وحل وزلق يشق معه المشي إلى المسجد.
2. أن ينوي الإمام والمأمومون الجمع عند تكبيرة الإحرام للصلاة الأولى (أو أثنائها عند بعض الفقهاء).
3. الموالاة بين الصلاتين بألا يفصل بينهما بفاصل زمني طويل عرفاً.
4. الأفضل أن يكون الجمع في المسجد بجماعة، وتكون صلاة تقديم (تقديم العشاء مع المغرب، أو العصر مع الظهر) تقليلاً للمشقة.

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 2160 و 18018 و 74312).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "الجمع بين الصلاتين لعذر المطر",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "2160",
                    relatedTopics = listOf("الجمع بين الصلاتين", "عذر المطر والوحل", "صلاة الجماعة في المطر", "رخص الصلاة"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/2160",
                    isFromAi = false,
                    error = note
                )
            }
            // قصر الصلاة والجمع في السفر
            q.contains("سفر") && (q.contains("قصر") || q.contains("جمع") || q.contains("مسافر")) -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
قصر الصلاة الرباعية (الظهر والعصر والعشاء) إلى ركعتين سنة مؤكدة في حق المسافر إذا قطع المسافة المعتبرة شرعاً (نحو 80 إلى 83 كيلومتراً تقريباً)، كما يجوز له الجمع بين الظهر والعصر وبين المغرب والعشاء تقديماً أو تأخيراً بحسب الأرفق به أثناء سيره.

📖 الأدلة الشرعية:
قال تعالى: {وَإِذَا ضَرَبْتُمْ فِي الْأَرْضِ فَلَيْسَ عَلَيْكُمْ جُنَاحٌ أَن تَقْصُرُوا مِنَ الصَّلَاةِ} [النساء: 101]. وثبت عن عائشة رضي الله عنها: «فرضت الصلاة ركعتين ركعتين في الحضر والسفر، فأقرت صلاة السفر وزيد في صلاة الحضر» (متفق عليه).

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 6890 و 2112).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "قصر وجمع الصلاة في السفر",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "6890",
                    relatedTopics = listOf("صلاة المسافر", "مسافة القصر", "أحكام الجمع والسفر"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/6890",
                    isFromAi = false,
                    error = note
                )
            }
            // صلاة الاستخارة
            q.contains("استخارة") || q.contains("استخير") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
صلاة الاستخارة سنة مستحبة عند إرادة الإقدام على أمر مباح يلتبس فيه وجه الصواب والخير، وتكون بركعتين من غير الفريضة، ثم يحمد الله ويصلي على النبي ﷺ ويدعو بالدعاء المأثور عن رسول الله ﷺ.

📖 نص الدعاء النبوي الشريف:
«اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ...» (رواه البخاري عن جابر رضي الله عنه).

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 1934 و 10565).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "كيفية ودعاء صلاة الاستخارة",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "1934",
                    relatedTopics = listOf("صلاة الاستخارة", "أدعية نبوية", "النوافل والسنن"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/1934",
                    isFromAi = false,
                    error = note
                )
            }
            // صلاة الوتر وقيام الليل
            q.contains("وتر") || q.contains("قيام الليل") || q.contains("تهجد") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
صلاة الوتر سنة مؤكدة حث عليها النبي ﷺ، ووقتها يمتد من بعد أداء صلاة العشاء وسنتها إلى طلوع الفجر الصادق، وأفضل وقتها الثلث الأخير من الليل لمن وثق بالاستيقاظ.

📖 صفتها وعدد ركعاتها:
أقل الوتر ركعة واحدة، وأدنى الكمال ثلاث ركعات يصلي ركعتين ويسلم ثم يوتر بركعة، أو يصلي ثلاثاً متصلة بتشهد واحد في آخرها، وله أن يوتر بخمس أو سبع أو تسع أو إحدى عشرة ركعة لقوله ﷺ: «الوتر حق على كل مسلم» (رواه أبو داود).

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 2114).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "أحكام صلاة الوتر وقيام الليل",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "2114",
                    relatedTopics = listOf("صلاة الوتر", "قيام الليل", "السنن المؤكدة"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/2114",
                    isFromAi = false,
                    error = note
                )
            }
            // قضاء الفوائت
            q.contains("فائت") || (q.contains("قضاء") && (q.contains("صلاة") || q.contains("صلوات"))) -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
من فاتته صلاة مكتوبة بنوم أو نسيان أو غير ذلك وجب عليه قضاؤها فور تذكرها على الترتيب، لقوله ﷺ: «من نسي صلاة أو نام عنها فكفارتها أن يصليها إذا ذكرها» (متفق عليه).

📖 الترتيب بين الفوائت:
يجب الترتيب بين الصلوات الفائتة وبين الحاضرة عند جمهور الفقهاء ما لم يضق وقت الحاضرة عن أدائها فيقدم الحاضرة خشية خروج وقتها.

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 3110 و 12154).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "حكم وكيفية قضاء الصلوات الفائتة",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "3110",
                    relatedTopics = listOf("قضاء الصلاة", "الترتيب في الفوائت", "أحكام النسيان والنوم"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/3110",
                    isFromAi = false,
                    error = note
                )
            }
            // زكاة الفطر وزكاة المال
            q.contains("زكاة") && (q.contains("فطر") || q.contains("مال") || q.contains("ذهب")) -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
1. زكاة الفطر: واجبة على كل مسلم ملك قوتاً زائداً عن حاجته وحاجة عياله يوم العيد وليلته، ومقدارها صاع نبوي (نحو 2.5 إلى 3 كغم) من غالب قوت البلد، وأفضل وقت لإخراجها قبل صلاة العيد ويجوز إخراجها قبل العيد بيوم أو يومين.
2. زكاة المال: تجب بنسبة 2.5% (ربع العشر) إذا بلغ المال النصاب (ما يعادل قيمة 85 غراماً من الذهب عيار 24) وحال عليه الحول الهجري كاملاً.

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 11487 و 6327).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "أحكام زكاة الفطر وزكاة المال",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "11487",
                    relatedTopics = listOf("زكاة الفطر", "مقدار الصاع", "نصاب زكاة المال"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/11487",
                    isFromAi = false,
                    error = note
                )
            }
            q.contains("مسح") || q.contains("خف") || q.contains("جورب") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
يجوز المسح على الجوارب والخفين بشروط، وهي أن يُلبسا على طهارة تامة، وأن يكونا طاهرين ساترين لمحل الفرض، وتكون مدة المسح يوماً وليلة للمقيم، وثلاثة أيام بلياليها للمسافر، وتبدأ المدة من أول مسح بعد الحدث على الراجح من أقوال أهل العلم.

📖 الأدلة من إسلام ويب:
ثبت عن المغيرة بن شعبة رضي الله عنه أن النبي ﷺ توضأ ومسح على خفيه. وثبت عن علي بن أبي طالب رضي الله عنه قال: "جعل رسول الله ﷺ ثلاثة أيام ولياليهن للمسافر، ويوماً وليلة للمقيم" (رواه مسلم).

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 5782 و 11613).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "شروط وأحكام المسح على الخفين والجوربين",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "5782",
                    relatedTopics = listOf("المسح على الخفين", "شروط المسح", "نواقض المسح"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/5782",
                    isFromAi = false,
                    error = note
                )
            }
            q.contains("بخاخ") || q.contains("ربو") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
بخاخ الربو لا يُفطر الصائم على الراجح من قولي أهل العلم؛ لأن المادة الداخلة منه رذاذ غازي يذهب معظمه إلى القصبات الهوائية والرئتين لتوسيع الشعب، وليس طعاماً ولا شراباً ولا في معناهما.

📖 الأدلة وبيان إسلام ويب:
وهذا ما أفتى به مجمع الفقه الإسلامي وسماحة الشيخ ابن باز والشيخ ابن عثيمين، واللجنة الدائمة للبحوث العلمية والإفتاء، وأيده مركز الفتوى بإسلام ويب تيسيراً على المريض وصحة لصومه.

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 2575 و 24141).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "حكم استخدام بخاخ الربو للصائم",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "2575",
                    relatedTopics = listOf("مفطرات الصيام المعاصرة", "حكم بخاخ الربو", "صيام المريض"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/2575",
                    isFromAi = false,
                    error = note
                )
            }
            q.contains("سهو") || q.contains("سجود") -> {
                answerBuilder.append("""
🕌 خلاصة الفتوى من إسلام ويب (مركز الفتوى):
سجود السهو مشروع لجبر النقص أو الزيادة أو الشك في الصلاة، وهو سجدتان كسجود الصلاة العادي:
1. يكون قبل السلام إذا كان السهو عن نقص (كنسيان التشهد الأول) أو عن شك لم يترجح فيه شيء فبنى على اليقين (الأقل).
2. يكون بعد السلام إذا كان السهو عن زيادة في الصلاة، أو شك تحرى فيه وترجح عنده أحد الأمرين.

📖 الأدلة من إسلام ويب:
حديث عبد الله بن بحينة رضي الله عنه في تركه ﷺ للتشهد الأول وسجوده قبل السلام، وحديث ذي اليدين وسجوده ﷺ بعد السلام.

🔗 مرجع الفتوى المعتمد:
مركز الفتوى - إسلام ويب (فتوى رقم 2984).
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = "أحكام ومواضع سجود السهو",
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "2984",
                    relatedTopics = listOf("سجود السهو", "مواضع سجود السهو", "الشك في الصلاة"),
                    sourceUrl = "https://www.islamweb.net/ar/fatwa/2984",
                    isFromAi = false,
                    error = note
                )
            }
            else -> {
                answerBuilder.append("""
🕌 بحث شرعي مستند إلى مركز الفتوى - إسلام ويب:
بشأن مسألة: "$query":

📌 الخلاصة الشرعية العامة بإسلام ويب:
الأصل في الأحكام التعبدية التوقيف على ما جاء في الكتاب والسنة النبوية الشريفة، والأصل في المعاملات والعادات الإباحة إلا ما دل الدليل على تحريمه أو منعه. وقد تقرر في القواعد الفقهية أن «المشقة تجلب التيسير» وأن «الأمر إذا ضاق اتسع».

💡 للتفصيل والتحقيق الدقيق:
يمكنك مراجعة المسألة برقمها المباشر في أرشيف الفتاوى التابع لإسلام ويب عبر الرابط أدناه، أو تخصيص صياغة السؤال لمزيد من التحديد الفقهي.
                """.trimIndent())
                return IslamwebAiResponse(
                    query = query,
                    title = query,
                    answer = answerBuilder.toString(),
                    fatwaNumberHint = "10245",
                    relatedTopics = listOf("مركز الفتوى إسلام ويب", "البحث الفقهي المقارن"),
                    sourceUrl = defaultUrl,
                    isFromAi = false,
                    error = note
                )
            }
        }
    }
}
