package com.example.data.network

import com.example.data.model.Fatwa
import com.example.data.model.FatwaCategory
import com.example.data.model.RulingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class IslamwebFatwa(
    val fatwaNumber: String,
    val title: String,
    val question: String,
    val answer: String,
    val summary: String = "",
    val categoryName: String = "فتاوى عامة",
    val date: String = "",
    val url: String = "",
    val category: FatwaCategory = FatwaCategory.ALL,
    val rulingType: RulingType = RulingType.PERMISSIBLE,
    val source: String = "مركز الفتوى - إسلام ويب"
) {
    fun toFatwa(): Fatwa {
        return Fatwa(
            question = if (title.isNotBlank()) "$title\n\n$question" else question,
            answer = answer,
            summary = summary.ifBlank { title },
            ruling = "فتوى رقم $fatwaNumber - إسلام ويب",
            rulingType = rulingType,
            scholar = "مركز الفتوى - إسلام ويب",
            source = if (url.isNotBlank()) url else "موقع إسلام ويب (فتوى رقم $fatwaNumber)",
            evidence = "مركز الفتوى بإشراف د. عبدالله الفقيه",
            category = category,
            tags = "إسلام ويب, $fatwaNumber, $categoryName, فتاوى أونلاين"
        )
    }
}

object IslamwebService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val curatedIslamwebFatwas: List<IslamwebFatwa> = listOf(
        IslamwebFatwa(
            fatwaNumber = "10842",
            title = "حكم الصلاة بالثوب الذي أصابه دم يسير أو نجاسة يسيرة",
            question = "ما حكم الصلاة بثوب أصابه قليل من الدم الناتج عن جرح أو قلع ضرس أو رعاف؟ هل تجزئ الصلاة به أم تجب إعادته؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nفإن الدم المسفوح نجس بإجماع العلماء لقوله تعالى: ﴿قُل لَّا أَجِدُ فِي مَا أُوحِيَ إِلَيَّ مُحَرَّمًا عَلَىٰ طَاعِمٍ يَطْعَمُهُ إِلَّا أَن يَكُونَ مَيْتَةً أَوْ دَمًا مَّسْفُوحًا﴾.\n\nولكن الشريعة الإسلامية مبنية على اليسر ورفع الحرج، لذلك عفا جمهور الفقهاء عن يسير الدم الذي يشق الاحتراز منه، كدم البثور والجروح الصغيرة وما يخرج من الأسنان ونحو ذلك.\n\nفإذا كان الدم يسيراً عرفاً فصلاتك صحيحة ولا إعادة عليك، والأحوط غسله خروجاً من الخلاف إن تيسر ذلك دون مشقة.",
            summary = "الدم اليسير المعفو عنه عرفاً كدم الجروح البسيطة لا يبطل الصلاة وتصح به الصلاة على الراجح.",
            categoryName = "الطهارة والصلاة",
            category = FatwaCategory.TAHARAH,
            rulingType = RulingType.PERMISSIBLE,
            date = "2001-11-20",
            url = "https://www.islamweb.net/ar/fatwa/10842"
        ),
        IslamwebFatwa(
            fatwaNumber = "2162",
            title = "حكم سجود السهو ومواضعه متى يكون قبل السلام ومتى بعده؟",
            question = "أشكل علي سجود السهو، فمتى يكون قبل السلام من الصلاة ومتى يكون بعد السلام؟ وما هي الحالات الموجبة له؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nسجود السهو مشروع لجبر النقص والخلل الحاصل في الصلاة بسبب السهو والنسيان، وأسبابه ثلاثة: الزيادة، والنقص، والشك.\n\nوضابط موضع السجود عند المحققين من أهل العلم:\n1. يكون قبل السلام في حالتين:\n   أ) إذا كان السهو عن نقص، كنقص التشهد الأول أو تسبيح الركوع.\n   ب) إذا كان عن شك ولم يترجح لديه شيء فبنى على اليقين (الأقل).\n\n2. يكون بعد السلام في حالتين:\n   أ) إذا كان السهو عن زيادة في الصلاة، كزيادة ركعة أو ركوع.\n   ب) إذا كان عن شك وتحرى وترجح لديه أحد الأمرين.\n\nوإن سجد للسهو كله قبل السلام أو بعده أجزأه وصحت صلاته لأن الأمر واسع.",
            summary = "يسجد قبل السلام للنقص والشك بلا ترجيح، وبعد السلام للزيادة والشك مع التحري والترجيح.",
            categoryName = "الصلاة وأحكامها",
            category = FatwaCategory.SALAH,
            rulingType = RulingType.RECOMMENDED,
            date = "2000-05-14",
            url = "https://www.islamweb.net/ar/fatwa/2162"
        ),
        IslamwebFatwa(
            fatwaNumber = "6520",
            title = "حكم استعمال بخاخ الربو وقطرة العين والأذن للصائم في نهار رمضان",
            question = "هل يفسد بخاخ الربو صيام المريض في نهار رمضان؟ وكذلك قطرة العين وقطرة الأذن وحقن الوريد والعضل؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\n1. بخاخ الربو: الصحيح من أقوال أهل العلم والمجامع الفقهية المعاصرة أنه لا يفطر الصائم؛ لأن الغاز المندفع منه يذهب إلى القصبة الهوائية والرئتين لتوسيع الشعب الهوائية ولا ينزل إلى المعدة، والقطرات التي قد تنزل تافهة جداً دون قدر المضمضة المعفو عنها.\n\n2. قطرة العين والأذن: الراجح أنها لا تفطر وإن وجد طعمها في الحلق؛ لأن العين والأذن ليستا منفذاً معتاداً للطعام والشراب.\n\n3. الحقن العلاجية (العضل والوريد): غير المفطرة تجوز، أما الإبر المغذية التي تقوم مقام الطعام والشراب فإنها تفطر.",
            summary = "بخاخ الربو وقطرة العين والأذن والحقن غير المغذية لا تفطر الصائم وصيامه صحيح.",
            categoryName = "الصيام والمعاصرة",
            category = FatwaCategory.SIYAM,
            rulingType = RulingType.PERMISSIBLE,
            date = "2001-03-08",
            url = "https://www.islamweb.net/ar/fatwa/6520"
        ),
        IslamwebFatwa(
            fatwaNumber = "44132",
            title = "حكم المعاملات والتداول بالعملات الرقمية المشفرة (Bitcoin وغيرها)",
            question = "ما هو الموقف الشرعي في موقع إسلام ويب من التداول والشراء والبيع في العملات الرقمية الحديثة كالبيتكوين؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nالعملات الرقمية المشفرة (كالبيتكوين ونحوها) مسألة معاصرة نازلة اختلف فيها العلماء المعاصرون والمجامع الفقهية، والذي يترجح في الفتوى هو التوقف والمنع من المضاربة فيها والاستثمار بها للأسباب الآتية:\n\n1. شدة الغرر والمخاطرة والتقلبات العنيفة غير المنضبطة بقواعد الاقتصاد الحقيقي.\n2. عدم وجود جهة ضامنة أو رقابية رسمية أو أصول عينية تدعمها في الواقع.\n3. شيوع الجهالة واستخدامها في بعض الأنشطة المحظورة والمضاربات الوهمية الشبيهة بالقمار.\n\nفمن كان يملك منها شيئاً فالأولى له التخلص منها والاستثمار في المجالات المباحة الواضحة الخالية من الغرر.",
            summary = "الترجيح الشرعي هو المنع والتحذير من المضاربة بالعملات الرقمية لغلبة الغرر والمخاطرة العالية.",
            categoryName = "المعاملات المالية المعاصرة",
            category = FatwaCategory.TRANSACTIONS,
            rulingType = RulingType.PROHIBITED,
            date = "2018-01-15",
            url = "https://www.islamweb.net/ar/fatwa/44132"
        ),
        IslamwebFatwa(
            fatwaNumber = "3127",
            title = "شروط زكاة المال وعروض التجارة وكيفية حساب النصاب الشرعي",
            question = "كيف يحسب المسلم زكاة أمواله النقدية والمدخرات ورأس مال التجارة؟ وما هو النصاب المعتبر اليوم؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nتجب الزكاة في الأموال النقدية وعروض التجارة إذا توافر شرطان أساسيان:\n1. بلوغ النصاب: وهو ما يعادل قيمة 85 جراماً من الذهب الخالص (عيار 24)، أو 595 جراماً من الفضة الخالصة، ويُقدّر بالأنفع للفقراء وهو نصاب الفضة في الغالب.\n2. مرور حول هجري كامل (سنة قمرية) على ملك النصاب.\n\nمقدار الزكاة الواجبة: ربع العشر، أي 2.5% من إجمالي المبلغ والمدخرات والأرباح.\n\nوطريقة الحساب: (المبلغ الإجمالي الخاضع للزكاة ÷ 40) = مقدار الزكاة الواجب إخراجها فوراً للفقراء والمستحقين.",
            summary = "تجب الزكاة بنسبة 2.5% عند بلوغ النصاب وحولان الحول الهجري الكامل.",
            categoryName = "الزكاة والصدقات",
            category = FatwaCategory.ZAKAH,
            rulingType = RulingType.OBLIGATORY,
            date = "2000-07-22",
            url = "https://www.islamweb.net/ar/fatwa/3127"
        ),
        IslamwebFatwa(
            fatwaNumber = "13569",
            title = "حكم صلاة الاستخارة وكيفيتها ودعاؤها المأثور وعلامات القبول",
            question = "ما هي الطريقة الصحيحة لصلاة الاستخارة؟ ومتى يُقال الدعاء؟ وهل يُشترط أن يرى المستخير رؤيا في المنام؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nصلاة الاستخارة سنة مؤكدة لكل من همّ بأمر مباح كالزواج أو السفر أو العمل أو الشراء.\n\nصفة صلاة الاستخارة:\n1. يصلي ركعتين من غير الفريضة بنية الاستخارة.\n2. بعد السلام من الركعتين يرفع يديه ويحمد الله ويثني عليه ويصلي على النبي ﷺ.\n3. يقرأ الدعاء النبوي المشهور: 'اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ...'.\n4. يسمي حاجته عند قوله: 'اللهم إن كنت تعلم أن هذا الأمر (ويسميه)...'.\n\nتنبيه مهم: لا يُشترط رؤيا في المنام بعد الاستخارة، بل يمضي المستخير في أمره مستعيناً بالله، فما تيسر وتسهل فهو الخير وما تعسر فالله يصرفه عنه.",
            summary = "ركعتان ثم الدعاء المأثور، ولا يشترط منام بل المضي في الأمر مع انشراح الصدر وتيسر الأسباب.",
            categoryName = "السنن والأذكار",
            category = FatwaCategory.SALAH,
            rulingType = RulingType.RECOMMENDED,
            date = "2002-02-10",
            url = "https://www.islamweb.net/ar/fatwa/13569"
        ),
        IslamwebFatwa(
            fatwaNumber = "27419",
            title = "ضوابط الحجاب الشرعي وشروطه الكاملة للمرأة المسلمة",
            question = "ما هي الشروط والضوابط الواجب توفرها في لباس وحجاب المرأة المسلمة أمام الرجال الأجانب؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nالحجاب فريضة شرعية ثابتة بكتاب الله وسنة رسوله ﷺ، ويشترط في لباس المرأة المسلمة أمام غير المحارم الشروط الآتية:\n1. أن يستوعب جميع البدن.\n2. ألا يكون زينة في نفسه تلفت أنظار الرجال.\n3. أن يكون صفيقاً ساتراً لا يشف عما تحته.\n4. أن يكون واسعاً فضفاضاً غير ضيق يصف معالم الجسد.\n5. ألا يكون مبخراً أو مطيباً عند الخروج.\n6. ألا يشبه ملابس الرجال ولا ملابس الكافرات الخاصة بهن.\n7. ألا يكون لباس شهرة وخيلاء.",
            summary = "شروط الحجاب الشرعي: ساتر لجميع البدن، فضفاض، غير شفاف، غير زينة بذاته، ولا معطراً.",
            categoryName = "المرأة والأسرة",
            category = FatwaCategory.WOMEN_FAMILY,
            rulingType = RulingType.OBLIGATORY,
            date = "2003-01-28",
            url = "https://www.islamweb.net/ar/fatwa/27419"
        ),
        IslamwebFatwa(
            fatwaNumber = "5182",
            title = "حكم قراءة الأذكار وسور التحصين بدون وضوء",
            question = "هل يجوز قراءة أذكار الصباح والمساء وآية الكرسي والمعوذات والرقية الشرعية لغير المتوضئ؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nيجوز للمسلم والمسلمة قراءة أذكار الصباح والمساء وسائر الأذكار النبوية بدون وضوء بإجماع العلماء.\n\nوكذلك يجوز قراءة الآيات القرآنية الواردة في الأذكار كآية الكرسي والإخلاص والمعوذتين عن ظهر قلب من غير مس للمصحف الورقي للمحدث حدثاً أصغر.\n\nبل ثبت عن عائشة رضي الله عنها قالت: 'كان النبي ﷺ يذكر الله على كل أحيانه' (رواه مسلم). فالذكر مشروع وطيب في سائر الأحوال.",
            summary = "جائز تماماً قراءة الأذكار وسور التحصين والرقية بدون وضوء، والوضوء أفضل وأكمل.",
            categoryName = "الرقية والأذكار",
            category = FatwaCategory.ATHKAR_RUQYAH,
            rulingType = RulingType.PERMISSIBLE,
            date = "2000-10-18",
            url = "https://www.islamweb.net/ar/fatwa/5182"
        ),
        IslamwebFatwa(
            fatwaNumber = "12845",
            title = "حكم شراء الذهب بالبطاقة الائتمانية والتقسيط أو الدفع المؤجل",
            question = "هل يجوز شراء الذهب والفضة باستخدام البطاقات الائتمانية أو الدفع المؤجل والتقسيط؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nمن شروط صحة بيع وشراء الذهب والفضة بالنقود الورقية حصول التقابض الفوري في مجلس العقد (يداً بيد)؛ لقول النبي ﷺ: 'الذهب بالذهب والفضة بالفضة... يداً بيد مثلاً بمثل' (رواه مسلم).\n\n1. بطاقة الخصم المباشر (Debit Card): يجوز الشراء بها إذا كان القيد المصرفي فورياً يدخل في حساب البائع مباشرة فيعتبر قبضاً حكمياً صحيحاً.\n2. الشراء بالتقسيط أو بطاقات الائتمان غير المغطاة (الدفع الآجل): محرم بالإجماع لوقوعه في ربا النسيئة لغياب التقابض الفوري.",
            summary = "يجوز الشراء بالدفع الفوري المباشر (القبض الفوري)، ويحرم بالتقسيط أو الدين لربا النسيئة.",
            categoryName = "المعاملات والتجارة",
            category = FatwaCategory.TRANSACTIONS,
            rulingType = RulingType.PROHIBITED,
            date = "2001-12-05",
            url = "https://www.islamweb.net/ar/fatwa/12845"
        ),
        IslamwebFatwa(
            fatwaNumber = "5023",
            title = "حكم المسح على الجوارب الرقيقة والخفين وشروطه ومدته",
            question = "هل يجوز المسح على الجوارب العادية المصنوعة من القطن أو الصوف؟ وما هي مدة المسح للمقيم والمسافر وشروطه؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nيجوز المسح على الجوارب والخفاف إذا توفرت الشروط الشرعية الآتية:\n1. أن يلبسهما على طهارة كاملة (بعد وضوء كامل بالماء).\n2. أن يكون الجورب ساتراً لمحل الفرض (الكعبين).\n3. أن يكون طاهراً في نفسه.\n\nمدة المسح:\n• للمقيم: يوم وليلة (24 ساعة) تبدأ من أول مسح بعد الحدث.\n• للمسافر: ثلاثة أيام بلياليها (72 ساعة).\n\nوصفة المسح: يبلل يديه بالماء ثم يمسح ظاهر الجورب (أعلاه) من أطراف الأصابع إلى الساق مرة واحدة.",
            summary = "يجوز المسح على الجوارب الساترة للمقيم يوماً وليلة وللمسافر ثلاثة أيام بلياليها بشرط لبسها على طهارة.",
            categoryName = "الطهارة والصلاة",
            category = FatwaCategory.TAHARAH,
            rulingType = RulingType.PERMISSIBLE,
            date = "2000-09-11",
            url = "https://www.islamweb.net/ar/fatwa/5023"
        ),
        IslamwebFatwa(
            fatwaNumber = "6841",
            title = "حكم الجمع بين الصلاتين بسبب المطر والبرد الشديد والمشقة",
            question = "متى يجوز جمع صلاتي المغرب والعشاء أو الظهر والعصر بسبب المطر والوحل والبرد في المسجد والبيت؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nيجوز الجمع بين المغرب والعشاء جمع تقديم في المسجد إذا وجد مطر يبل الثياب ويشق معه الذهاب إلى المسجد، أو وحل شديد أو ريح باردة شديدة تسبب حرجاً للمصلين؛ لحديث ابن عباس رضي الله عنهما: 'جمع رسول الله ﷺ بالمدينة بين الظهر والعصر والمغرب والعشاء من غير خوف ولا مطر'، قيل لابن عباس: ما أراد إلى ذلك؟ قال: 'أراد أن لا يحرج أمته'.\n\nأما الجمع في البيوت للمنفرد فالأصل ألا يجمع إلا إذا لحقته مشقة معتبرة في خروجه.",
            summary = "يجوز الجمع بين المغرب والعشاء تقديماً في المسجد للمطر الذي يبل الثياب دفعاً للحرج والمشقة.",
            categoryName = "الصلاة وأحكامها",
            category = FatwaCategory.SALAH,
            rulingType = RulingType.PERMISSIBLE,
            date = "2001-03-24",
            url = "https://www.islamweb.net/ar/fatwa/6841"
        ),
        IslamwebFatwa(
            fatwaNumber = "2414",
            title = "حكم فوائد البنوك الربوية والإيداع في الحسابات الجارية والاستثمارية",
            question = "ما حكم الفوائد التي تعطيها البنوك التجارية على حسابات التوفير والودائع؟ وماذا يفعل المسلم إذا نزلت في حسابه؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nفوائد البنوك الربوية محرمة شرعاً بإجماع المجامع الفقهية المعاصرة وهي من الربا الصريح المنهي عنه قطعا في قوله تعالى: ﴿وَأَحَلَّ اللَّهُ الْبَيْعَ وَحَرَّمَ الرِّبَا﴾.\n\nوالواجب على المسلم:\n1. نقل أمواله إلى مصارف إسلامية منضبطة بالضوابط الشرعية.\n2. التخلص من الفوائد الربوية المحصلة بصرفها في وجوه الخير والفقراء والمصالح العامة بنية التخلص منها لا بنية الصدقة والتقرب.\n3. التوبة والاستغفار عن أي تعامل ربوي سابق.",
            summary = "فوائد البنوك التقليدية ربا محرم بإجماع المجامع الفقهية، ويجب التخلص منها في وجوه الخير والفقراء.",
            categoryName = "المعاملات والتجارة",
            category = FatwaCategory.TRANSACTIONS,
            rulingType = RulingType.PROHIBITED,
            date = "2000-06-19",
            url = "https://www.islamweb.net/ar/fatwa/2414"
        ),
        IslamwebFatwa(
            fatwaNumber = "3884",
            title = "حكم صيام ست من شوال قبل قضاء ما فات من رمضان",
            question = "هل يجوز للمرأة أو المريض صيام الست من شوال قبل قضاء الأيام التي أفطرها في شهر رمضان بعذر؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nاختلف أهل العلم في هذه المسألة:\n• القول الأول (وهو الراجح والأحوط): أن الواجب تقديم قضاء رمضان قبل صيام الست من شوال؛ لقول النبي ﷺ: 'من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر' (رواه مسلم)، ومن بقي عليه أيام من رمضان فإنه لم يصم رمضان كاملاً حتى يقضيها.\n• القول الثاني: جواز التطوع بالست قبل القضاء إذا كان وقت القضاء موسعاً.\n\nوالأفضل والأبرأ للذمة البدء بالقضاء فوراً ثم صيام ما تيسر من الست في بقية شوال.",
            summary = "الراجح تقديم قضاء رمضان على الست من شوال ليحصل على أجر 'من صام رمضان ثم أتبعه ستاً'.",
            categoryName = "الصيام والمعاصرة",
            category = FatwaCategory.SIYAM,
            rulingType = RulingType.RECOMMENDED,
            date = "2000-08-30",
            url = "https://www.islamweb.net/ar/fatwa/3884"
        ),
        IslamwebFatwa(
            fatwaNumber = "7290",
            title = "حكم السفر بدون محرم للمرأة لأداء فريضة الحج أو الدراسة",
            question = "ما حكم سفر المرأة بغير محرم للحج الواجب أو لضرورة الدراسة والعلاج رفقة صحبة آمنة؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nالأصل الشرعي الثابت بالحديث الصحيح: 'لا تسافر المرأة إلا مع ذي محرم' (متفق عليه).\n\nواختلف الفقهاء في حج الفريضة الأول:\n• فذهب جمهور العلماء (الشافعية والمالكية) إلى جواز سفرها لحج الفريضة مع رفقة مأمونة من النساء الثقات إذا أمنت الطريق وتوفرت الحماية.\n• وذهب الحنابلة والأحناف إلى اشتراط المحرم حتى لحج الفريضة.\n\nأما السفر لغير الحج الواجب كالسياحة فإنه لا يجوز إلا بمحرم، وللدراسة أو العلاج للضرورة القصوى مع الأمن التام وبإذن الولي.",
            summary = "الأصل اشتراط المحرم، ورخص جمهور العلماء في حج الفريضة الأول مع رفقة نسائية مأمونة.",
            categoryName = "الحج والأسرة",
            category = FatwaCategory.HAJJ,
            rulingType = RulingType.CONDITIONAL,
            date = "2001-04-16",
            url = "https://www.islamweb.net/ar/fatwa/7290"
        ),
        IslamwebFatwa(
            fatwaNumber = "38190",
            title = "حكم الصلاة بالحذاء والنعلين في الأماكن المفتوحة والبر",
            question = "هل تجوز الصلاة بالنعال والأحذية في البر والملاعب؟ وهل ثبتت الصلاة بالحذاء عن النبي ﷺ؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nالصلاة بالنعال والخفاف جائزة ومستحبة في الأماكن المناسبة كالصحراء والمصليات المفتوحة الخالية من السجاد المفروش؛ لثبوت ذلك عن النبي ﷺ قولا وفعلا:\n\nقال أنس بن مالك رضي الله عنه حين سئل: 'أكان النبي ﷺ يصلي في نعليه؟ قال: نعم' (رواه البخاري ومسلم).\nوقال ﷺ: 'خالفوا اليهود فإنهم لا يصلون في نعالهم ولا خفافهم'.\n\nبشرط أن يتأكد المصلي من طهارة نعليه قبل الصلاة بالنظر فيهما، أما المساجد المفروشة بالسجاد فينبغي خلعها صيانة للمسجد ونظافته.",
            summary = "الصلاة بالنعلين سنة في الفضاء والبر عند طهارتهما، وتخلع في المساجد المفروشة صيانة للنظافة.",
            categoryName = "الصلاة والسنن",
            category = FatwaCategory.SALAH,
            rulingType = RulingType.RECOMMENDED,
            date = "2017-06-12",
            url = "https://www.islamweb.net/ar/fatwa/38190"
        ),
        IslamwebFatwa(
            fatwaNumber = "9341",
            title = "حكم التبرع بالأعضاء بعد الوفاة أو في حال الحياة ونقل الكلى",
            question = "ما هو الموقف الشرعي من التبرع بالأعضاء لنقلها إلى مريض محتاج بعد الموت الدماغي أو التبرع بكلية حال الحياة؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nقرر مجمع الفقه الإسلامي الدولي وهيئة كبار العلماء جواز التبرع بالأعضاء بالضوابط الآتية:\n\n1. التبرع حال الحياة: يجوز نقل عضو من إنسان حي إلى آخر كإحدى الكليتين بشرط ألا يترتب على النزع ضرر محقق على المتبرع، وأن يكون برضاه واختياره التام دون أي بيع أو مقابل مالي.\n2. التبرع بعد الوفاة: يجوز إذا أوصى المتبرع بذلك في حياته أو وافق ورثته بعد وفاته المعتبرة شرعاً، لإنقاذ نفس معصومة عملاً بقوله تعالى: ﴿وَمَنْ أَحْيَاهَا فَكَأَنَّمَا أَحْيَا النَّاسَ جَمِيعًا﴾.\n3. يحرم قطعاً بيع وشراء الأعضاء البشرية لكرامة الإنسان.",
            summary = "جائز بالضوابط الشرعية كوصية وتطوع لإنقاذ المرضى، ويحرم بيع الأعضاء قطعاً.",
            categoryName = "نوازل وقضايا طبية معاصرة",
            category = FatwaCategory.CONTEMPORARY,
            rulingType = RulingType.PERMISSIBLE,
            date = "2001-09-02",
            url = "https://www.islamweb.net/ar/fatwa/9341"
        ),
        IslamwebFatwa(
            fatwaNumber = "18260",
            title = "حكم قراءة القرآن للحائض والنفساء من الهاتف المحمول أو عن ظهر قلب",
            question = "هل يجوز للمرأة الحائض قراءة القرآن الكريم للامتحان أو الورد اليومي من شاشة الجوال أو حفظاً دون لمس المصحف؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nاختلف الفقهاء في قراءة الحائض للقرآن:\n• مذهب الجمهور (الأحناف والشافعية والحنابلة): المنع من قراءة القرآن مطلقاً.\n• مذهب الإمام مالك ورواية عن أحمد واختيار شيخ الإسلام ابن تيمية: جواز قراءة القرآن للحائض عن ظهر قلب أو من الهاتف المحمول دون مس المصحف الورقي المباشر، خصوصاً إذا خشيت نسيان القرآن أو لحاجة التعليم والمراجعة؛ لأنه لم يثبت حديث صريح صحيح يمنع الحائض من القراءة، ومدة الحيض تطول بخلاف الجنابة.\n\nوالقراءة من شاشات الهواتف والتطبيقات لا تُعد مساً للمصحف الشرعي فتجوز بلا حرج.",
            summary = "يجوز للحائض قراءة القرآن من الهاتف أو عن ظهر قلب للورد اليومي أو المراجعة دون مس المصحف الورقي.",
            categoryName = "المرأة والأسرة",
            category = FatwaCategory.WOMEN_FAMILY,
            rulingType = RulingType.PERMISSIBLE,
            date = "2002-06-15",
            url = "https://www.islamweb.net/ar/fatwa/18260"
        ),
        IslamwebFatwa(
            fatwaNumber = "8142",
            title = "حكم وضع العطور والمكياج والصائم في نهار رمضان",
            question = "هل يفسد الطيب والعطر والبخور والمكياج صيام المرأة أو الرجل في نهار شهر رمضان؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\n1. التطيب بالعطور والأدهان والروائح الطيبة: جائز للصائم ولا يفسد الصيام ولا يفطر به باتفاق العلماء؛ لأن الرائحة ليست جرماً يدخل الجوف.\n2. البخور: يكره استنشاق دخانه عمداً لأن دخان البخور له جرم قد ينفذ إلى الحلق والمعدة، أما مجرد شمه من غير استنشاق فلا يفطر.\n3. المكياج والكريمات والمراهم على الوجه والجلد: لا تفسد الصوم لأنها تمتص عن طريق مسام الجلد وليست أكلاً ولا شرباً.",
            summary = "العطور والكريمات لا تفطر الصائم، ويجتنب استنشاق دخان البخور عمداً.",
            categoryName = "الصيام والمعاصرة",
            category = FatwaCategory.SIYAM,
            rulingType = RulingType.PERMISSIBLE,
            date = "2001-05-18",
            url = "https://www.islamweb.net/ar/fatwa/8142"
        ),
        IslamwebFatwa(
            fatwaNumber = "11720",
            title = "صفة صلاة الوتر وعدد ركعاتها ووقتها ودعاء القنوت فيها",
            question = "كيف تصلى صلاة الوتر؟ وكم أقلها وأكثرها؟ ومتى يقنت المصلي فيها؟",
            answer = "الحمد لله والصلاة والسلام على رسول الله وعلى آله وصحبه أما بعد:\n\nصلاة الوتر سنة مؤكدة حث عليها النبي ﷺ بقوله: 'إن الله وتر يحب الوتر، فأوتروا يا أهل القرآن' (رواه الترمذي).\n\n1. وقتها: من بعد صلاة العشاء وسنتها إلى طلوع الفجر الثاني، وأفضل وقتها ثلث الليل الأخير.\n2. عدد ركعاتها: أقلها ركعة واحدة، وأدنى الكمال ثلاث ركعات يصلي ركعتين شفعاً ويسلم ثم يصلي ركعة ويسلم، أو يصلي ثلاثاً متصلة بتشهد واحد في الأخيرة.\n3. دعاء القنوت: يشرع في الركعة الأخيرة بعد الرفع من الركوع أو قبله، ويدعو بالدعاء المأثور: 'اللهم اهدني فيمن هديت...'.",
            summary = "الوتر سنة مؤكدة من ركعة إلى إحدى عشرة ركعة، وأدنى الكمال ثلاث، ووقته ممتد حتى طلوع الفجر.",
            categoryName = "الصلاة والسنن",
            category = FatwaCategory.SALAH,
            rulingType = RulingType.RECOMMENDED,
            date = "2001-10-30",
            url = "https://www.islamweb.net/ar/fatwa/11720"
        )
    )

    fun normalizeArabic(text: String): String {
        return text
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
            .replace("ـ", "")
            .lowercase()
            .trim()
    }

    suspend fun searchIslamweb(query: String): List<IslamwebFatwa> = withContext(Dispatchers.IO) {
        val raw = query.trim()
        if (raw.isBlank()) return@withContext curatedIslamwebFatwas

        // If user entered just a fatwa number (e.g. 535861 or 10842)
        if (raw.all { it.isDigit() }) {
            val direct = fetchFatwaByNumber(raw)
            if (direct.isSuccess) {
                return@withContext listOf(direct.getOrThrow())
            }
        }

        val onlineResults = mutableListOf<IslamwebFatwa>()
        try {
            val encodedWord = java.net.URLEncoder.encode(raw, "UTF-8")
            val searchUrl = "https://www.islamweb.net/ar/fatawa/?page=search&word=$encodedWord"
            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ar,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                if (html.isNotBlank()) {
                    val parsedList = parseIslamwebSearchResults(html)
                    onlineResults.addAll(parsedList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Local curated matching
        val normalizedQuery = normalizeArabic(raw)
        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.length > 1 }

        val localMatched = curatedIslamwebFatwas.filter { fatwa ->
            val normTitle = normalizeArabic(fatwa.title)
            val normQ = normalizeArabic(fatwa.question)
            val normAns = normalizeArabic(fatwa.answer)
            val normSum = normalizeArabic(fatwa.summary)
            val normCat = normalizeArabic(fatwa.categoryName)
            val combined = "$normTitle $normQ $normAns $normSum $normCat"

            if (tokens.isEmpty()) {
                combined.contains(normalizedQuery)
            } else {
                combined.contains(normalizedQuery) || tokens.any { token -> combined.contains(token) }
            }
        }.sortedByDescending { fatwa ->
            val normTitle = normalizeArabic(fatwa.title)
            val normQ = normalizeArabic(fatwa.question)
            var score = 0
            if (normTitle.contains(normalizedQuery)) score += 10
            if (normQ.contains(normalizedQuery)) score += 5
            tokens.forEach { t ->
                if (normTitle.contains(t)) score += 3
                if (normQ.contains(t)) score += 1
            }
            score
        }

        // Merge online and local results, avoiding duplicate fatwa numbers
        val combined = mutableListOf<IslamwebFatwa>()
        val seenNumbers = mutableSetOf<String>()

        // Put online results first if available
        for (item in onlineResults) {
            if (seenNumbers.add(item.fatwaNumber)) {
                combined.add(item)
            }
        }

        for (item in localMatched) {
            if (seenNumbers.add(item.fatwaNumber)) {
                combined.add(item)
            }
        }

        return@withContext combined
    }

    private fun parseIslamwebSearchResults(html: String): List<IslamwebFatwa> {
        val results = mutableListOf<IslamwebFatwa>()
        try {
            // Pattern to extract fatwa cards from Islamweb search result page
            val pattern = Pattern.compile(
                "<h2[^>]*>\\s*<a[^>]*href=[\"']/ar/fatwa/(\\d+)/?([^\"]*)[\"'][^>]*>([\\s\\S]*?)</a>\\s*</h2>\\s*<p[^>]*>([\\s\\S]*?)</p>",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(html)

            while (matcher.find()) {
                val fatwaNum = matcher.group(1)?.trim() ?: continue
                val slug = matcher.group(2)?.trim() ?: ""
                val rawTitle = matcher.group(3) ?: ""
                val rawSnippet = matcher.group(4) ?: ""

                val cleanTitle = cleanHtmlText(rawTitle)
                val cleanSnippet = cleanHtmlText(rawSnippet).replace("المزيد", "").trim()
                val url = "https://www.islamweb.net/ar/fatwa/$fatwaNum/$slug".trimEnd('/')

                val category = determineCategory("$cleanTitle $cleanSnippet")
                val rulingType = determineRulingType(cleanSnippet)

                results.add(
                    IslamwebFatwa(
                        fatwaNumber = fatwaNum,
                        title = cleanTitle.ifBlank { "فتوى رقم $fatwaNum" },
                        question = if (cleanSnippet.isNotBlank()) cleanSnippet else cleanTitle,
                        answer = if (cleanSnippet.isNotBlank()) cleanSnippet else "اضغط لعرض تفاصيل ونص الفتوى الكاملة من موقع إسلام ويب.",
                        summary = cleanSnippet.take(180),
                        categoryName = category.displayName,
                        date = "مركز الفتوى - إسلام ويب",
                        url = url,
                        category = category,
                        rulingType = rulingType
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    suspend fun fetchFatwaByNumber(fatwaNumber: String): Result<IslamwebFatwa> = withContext(Dispatchers.IO) {
        val cleanNumber = fatwaNumber.trim().filter { it.isDigit() }
        if (cleanNumber.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال رقم أو نص فتوى صحيح"))
        }

        val localMatch = curatedIslamwebFatwas.find { it.fatwaNumber == cleanNumber }
        
        val url = "https://www.islamweb.net/ar/fatwa/$cleanNumber/"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ar,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                if (html.isNotBlank()) {
                    val parsed = parseIslamwebHtml(html, cleanNumber, url)
                    if (parsed != null && parsed.answer.isNotBlank() && !parsed.answer.contains("لا يوجد فتوى بهذا الرقم")) {
                        return@withContext Result.success(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (localMatch != null) {
            return@withContext Result.success(localMatch)
        }

        Result.failure(Exception("تعذر جلب الفتوى رقم $cleanNumber من إسلام ويب. يرجى التأكد من اتصال الإنترنت أو البحث بكتابة اسم الموضوع."))
    }

    private fun parseIslamwebHtml(html: String, fatwaNumber: String, url: String): IslamwebFatwa? {
        try {
            var title = ""
            val h2Matcher = Pattern.compile("<h2[^>]*>([\\s\\S]*?)</h2>", Pattern.CASE_INSENSITIVE).matcher(html)
            if (h2Matcher.find()) {
                title = cleanHtmlText(h2Matcher.group(1) ?: "")
            }

            if (title.isBlank() || title.contains("محاور") || title.contains("خدمات")) {
                val titleMatcher = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE).matcher(html)
                if (titleMatcher.find()) {
                    title = titleMatcher.group(1)?.replace("- إسلام ويب", "")?.replace("مركز الفتوى", "")?.replace("- سعادة تمتد", "")?.trim() ?: ""
                }
            }

            var question = ""
            var answer = ""

            // Extract quest-fatwa blocks
            val blockPattern = Pattern.compile("<div[^>]*class=[\"']mainitem quest-fatwa[\"'][^>]*>([\\s\\S]*?)</div>", Pattern.CASE_INSENSITIVE)
            val blockMatcher = blockPattern.matcher(html)
            val blocks = mutableListOf<String>()
            while (blockMatcher.find()) {
                val text = cleanHtmlText(blockMatcher.group(1) ?: "")
                if (text.isNotBlank() && !text.contains("تم نسخ الرابط") && text.length > 20) {
                    blocks.add(text)
                }
            }

            if (blocks.size >= 2) {
                question = blocks[0]
                answer = blocks.subList(1, blocks.size).joinToString("\n\n")
            } else if (blocks.size == 1) {
                val singleBlock = blocks[0]
                if (singleBlock.contains("الإجاب")) {
                    val parts = singleBlock.split("الإجاب", limit = 2)
                    question = parts[0].trim()
                    answer = "الإجابة" + parts.getOrElse(1) { "" }.trim()
                } else {
                    answer = singleBlock
                }
            }

            // Fallback to long <p> tags if blocks were not found
            if (answer.isBlank()) {
                val pPattern = Pattern.compile("<p[^>]*>([\\s\\S]*?)</p>", Pattern.CASE_INSENSITIVE)
                val pMatcher = pPattern.matcher(html)
                val pList = mutableListOf<String>()
                while (pMatcher.find()) {
                    val pText = cleanHtmlText(pMatcher.group(1) ?: "")
                    if (pText.length > 35 && !pText.contains("Español") && !pText.contains("Français") && !pText.contains("English") && !pText.contains("جميع الحقوق محفوظة")) {
                        pList.add(pText)
                    }
                }
                if (pList.isNotEmpty()) {
                    answer = pList.joinToString("\n\n")
                }
            }

            if (title.isBlank() && question.isBlank() && answer.isBlank()) {
                return null
            }

            if (title.isBlank()) {
                title = "فتوى إسلام ويب رقم $fatwaNumber"
            }
            if (question.isBlank()) {
                question = title
            }
            if (answer.isBlank()) {
                answer = "يمكنك مطالعة نص الفتوى الكاملة عبر الرابط الرسمي لإسلام ويب: $url"
            }

            val category = determineCategory(title + " " + question + " " + answer)
            val rulingType = determineRulingType(answer)

            return IslamwebFatwa(
                fatwaNumber = fatwaNumber,
                title = title,
                question = question,
                answer = answer,
                summary = if (title.isNotBlank()) title else "فتوى إسلام ويب رقم $fatwaNumber",
                categoryName = category.displayName,
                date = "مركز الفتوى - إسلام ويب",
                url = url,
                category = category,
                rulingType = rulingType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun cleanHtmlText(html: String): String {
        return html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun determineCategory(text: String): FatwaCategory {
        val t = normalizeArabic(text)
        return when {
            t.contains("وضوء") || t.contains("طهاره") || t.contains("جنابه") || t.contains("غسل") || t.contains("مسح") || t.contains("جورب") || t.contains("خف") -> FatwaCategory.TAHARAH
            t.contains("صلاه") || t.contains("سجود") || t.contains("مسجد") || t.contains("ركوع") || t.contains("اذان") || t.contains("وتر") || t.contains("استخاره") -> FatwaCategory.SALAH
            t.contains("صيام") || t.contains("رمضان") || t.contains("افطار") || t.contains("مفطر") || t.contains("شوال") || t.contains("بخاخ") -> FatwaCategory.SIYAM
            t.contains("زكاه") || t.contains("صدقه") || t.contains("نصاب") || t.contains("فقراء") -> FatwaCategory.ZAKAH
            t.contains("حج") || t.contains("عمره") || t.contains("احرام") || t.contains("طواف") || t.contains("سعي") -> FatwaCategory.HAJJ
            t.contains("بيع") || t.contains("شراء") || t.contains("ربا") || t.contains("بنك") || t.contains("قرض") || t.contains("تجاره") || t.contains("ذهب") || t.contains("عملات") -> FatwaCategory.TRANSACTIONS
            t.contains("مراه") || t.contains("زواج") || t.contains("طلاق") || t.contains("حجاب") || t.contains("رضاع") || t.contains("اسره") || t.contains("حائض") -> FatwaCategory.WOMEN_FAMILY
            t.contains("ذكر") || t.contains("دعاء") || t.contains("رقيه") || t.contains("عين") || t.contains("سحر") || t.contains("تحصين") -> FatwaCategory.ATHKAR_RUQYAH
            t.contains("انترنت") || t.contains("عملات") || t.contains("طب") || t.contains("تبرع") || t.contains("معاصر") -> FatwaCategory.CONTEMPORARY
            else -> FatwaCategory.ALL
        }
    }

    private fun determineRulingType(text: String): RulingType {
        val t = normalizeArabic(text)
        return when {
            t.contains("واجب") || t.contains("يجب") || t.contains("فرض") || t.contains("يلزم") -> RulingType.OBLIGATORY
            t.contains("سنه") || t.contains("مستحب") || t.contains("ينبغي") || t.contains("مشروع") -> RulingType.RECOMMENDED
            t.contains("حرام") || t.contains("محرم") || t.contains("لا يجوز") || t.contains("باطل") -> RulingType.PROHIBITED
            t.contains("مكروه") || t.contains("يكره") -> RulingType.DISLIKED
            t.contains("بشروط") || t.contains("تفصيل") || t.contains("اذا كان") -> RulingType.CONDITIONAL
            t.contains("جائز") || t.contains("مباح") || t.contains("لا حرج") || t.contains("يصح") -> RulingType.PERMISSIBLE
            else -> RulingType.GENERAL
        }
    }
}
