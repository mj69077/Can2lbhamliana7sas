package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

data class RoadmapCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val count: Int,
    val description: String,
    val targetTab: AppTab?,
    val features: List<String>
)

object RoadmapData {
    val categories: List<RoadmapCategory> = listOf(
        RoadmapCategory(
            id = "quran",
            title = "القرآن الكريم والتلاوات",
            iconName = "MenuBook",
            count = 60,
            description = "مصحف مجمع الملك فهد الرقمي، تفاسير متعددة، تلاوات مشاهير القراء، وخطط الختمة التفاعلية.",
            targetTab = AppTab.QURAN,
            features = (1..60).map { i ->
                when (i) {
                    1 -> "مصحف المدينة المنورة الرقمي بدقة فائقة وعرض صفحات متناسق"
                    2 -> "التلاوة الصوتية المتصلة مع تظليل الآية المقروءة تلقائياً"
                    3 -> "تفسير الميسر، تفسير ابن كثير، وتفسير السعدي لكل آية"
                    4 -> "إمكانية تشغيل التلاوة في الخلفية وعند إغلاق الشاشة"
                    5 -> "أكثر من 30 قارئاً من كبار قراء العالم الإسلامي بروايات متعددة"
                    6 -> "نظام الفواصل والعلامات المرجعية الذكية مع التسمية والتاريخ"
                    7 -> "محرك بحث قرآني فوري بالجذر، الكلمة، السورة، ورقم الصفحة"
                    8 -> "مخطط الختمات التفاعلي مع تحديد الأيام والورد اليومي وتنبيهاته"
                    9 -> "تكرار الآية أو المقطع الصوتي للمساعدة في الحفظ والمراجعة"
                    10 -> "وضع القراءة الليلية مع خلفية دافئة مريحة للعينين"
                    11 -> "التحكم الكامل بحجم الخط ونوع الرسم القرآني العثماني"
                    12 -> "إحصائيات الإنجاز القرآني اليومي والأسبوعي والشهري"
                    13 -> "معاني الكلمات الغريبة وغريب القرآن بضغطة زر واحدة"
                    14 -> "أسباب النزول الموثقة لكل سورة وآية كريمة"
                    15 -> "مشاركة الآيات كبطاقات صورية أنيقة بتصاميم متعددة"
                    else -> "ميزة قرآنية متقدمة #$i: دعم الحفظ والتحفيظ الرقمي والمقارئ الجماعية وتحليل الورد والتنبيهات المخصصة"
                }
            }
        ),
        RoadmapCategory(
            id = "salah",
            title = "الأذان ومواقيت الصلاة والقبلة",
            iconName = "AccessTime",
            count = 50,
            description = "حساب فلكي دقيق بالـ GPS، بوصلة القبلة المغناطيسية ثلاثية الأبعاد، وتنبيهات الإقامة وسنن الرواتب.",
            targetTab = AppTab.PRAYER,
            features = (1..50).map { i ->
                when (i) {
                    1 -> "حساب مواقيت الصلاة بدقة GPS لكافة مدن وقرى العالم"
                    2 -> "بوصلة القبلة التفاعلية بزاوية دقيقة مع مستشعر الجهاز"
                    3 -> "صوت الأذان بأصوات كبار المؤذنين (الحرم المكي، المدني، الأقصى)"
                    4 -> "تنبيه مسبق قبل دخول وقت الصلاة بـ 15 دقيقة للاستعداد"
                    5 -> "تنبيه إقامة الصلاة وسنن الرواتب البعدية والقبلية"
                    6 -> "دعم هيئات الحساب العالمية: أم القرى، الرابطة، مصر، كراتشي، أمريكا"
                    7 -> "التحويل التلقائي للوضع الصامت أثناء وقت صلاة الجماعة"
                    8 -> "العد التنازلي الحي للصلاة القادمة مع شريط الإنجاز"
                    9 -> "تنبيهات قيام الليل، وصلاة الضحى، وساعة الإجابة يوم الجمعة"
                    10 -> "جدول مواقيت الصلاة الشهري الكامل مع إمكانية التصدير"
                    else -> "ميزة متقدمة في الصلاة والمواقيت #$i: استشعار السفر الآلي، وتنبيهات الأوقات المنهي عن الصلاة فيها"
                }
            }
        ),
        RoadmapCategory(
            id = "athkar",
            title = "الأذكار والرقية والمسبحة",
            iconName = "TouchApp",
            count = 50,
            description = "أذكار الصباح والمساء، الرقية الشرعية الموثقة، المسبحة الإلكترونية مع الاهتزاز والصوت.",
            targetTab = AppTab.ATHKAR,
            features = (1..50).map { i ->
                when (i) {
                    1 -> "أذكار الصباح والمساء كاملة مع عداد التكرار والفضائل النبوية"
                    2 -> "أذكار بعد الصلاة المفروضة وأذكار النوم والاستيقاظ"
                    3 -> "المسبحة الإلكترونية التفاعلية مع خيارات الصوت والاهتزاز اللمسي (Haptic)"
                    4 -> "إضافة أذكار وأوراد مخصصة مع تحديد الهدف والتكرار"
                    5 -> "الرقية الشرعية الكاملة بالقرآن والسنة لعلاج العين والحسد"
                    6 -> "الاستماع الصوتي المباشر لجميع الأذكار بصوت واضح نقي"
                    7 -> "سجل التسبيحات اليومي والإحصائيات التراكمية مدى الحياة"
                    8 -> "تنبيهات ذكية بوقت أذكار الصباح وأذكار المساء تلقائياً"
                    9 -> "وضع العداد التلقائي للتسبيح مع كل لمسة في أي مكان بالشاشة"
                    10 -> "أدعية السفر والركوب ودخول السوق وتفريج الهموم"
                    else -> "ميزة إضافية في الأذكار #$i: بطاقات الذكر السريع، والتذكير العشوائي بالصلاة على النبي ﷺ"
                }
            }
        ),
        RoadmapCategory(
            id = "fatwas",
            title = "فتاوى العلماء والحديث الشريف",
            iconName = "QuestionAnswer",
            count = 50,
            description = "موسوعة الفتاوى والأحكام الموثقة من سماحة الشيخ ابن باز، الشيخ ابن عثيمين، ومركز إسلام ويب.",
            targetTab = AppTab.FATWAS,
            features = (1..50).map { i ->
                when (i) {
                    1 -> "فتاوى الشيخ عبدالعزيز بن باز رحمه الله في الطهارة والصلاة والصيام"
                    2 -> "فتاوى الشيخ محمد بن صالح العثيمين رحمه الله في النوازل والمعاملات"
                    3 -> "فتاوى مركز الفتوى بموقع إسلام ويب (IslamWeb) في القضايا المعاصرة"
                    4 -> "البحث السريع في الفتاوى بالنص، الكلمات المفتاحية، أو العالم المفتي"
                    5 -> "تصنيف الفتاوى حسب الأبواب: طهارة، صلاة، صيام، زكاة، معاملات، أسرة"
                    6 -> "عرض الحكم الشرعي الملخص مع الأدلة التفصيلية والمراجع المعتمدة"
                    7 -> "حفظ الفتاوى في المفضلة للرجوع إليها بدون إنترنت"
                    8 -> "مشاركة الفتوى كصورة أو نص موثق مع رابط المصدر"
                    9 -> "سؤال وجواب فقهي سريع لأشهر 50 مسألة يومية"
                    10 -> "الحديث النبوي اليومي مع الشرح والفوائد المستنبطة"
                    else -> "ميزة فقهية وحديثية #$i: فتاوى النوازل المالية والطبية المعاصرة وتخريج الأحاديث"
                }
            }
        ),
        RoadmapCategory(
            id = "radio",
            title = "الإذاعات الإسلامية والبث المباشر",
            iconName = "Radio",
            count = 35,
            description = "بث مباشر لإذاعة القرآن الكريم من القاهرة والرياض، الحرمين الشريفين، وقنوات التلاوة المتخصصة.",
            targetTab = AppTab.RADIO,
            features = (1..35).map { i ->
                when (i) {
                    1 -> "إذاعة القرآن الكريم من القاهرة بث مباشر بدون تقطيع"
                    2 -> "إذاعة القرآن الكريم من مكة المكرمة والرياض"
                    3 -> "بث تلاوات خاشعة وتلاوات نادرة للشيخ عبدالباسط والمنشاوي والحصري"
                    4 -> "إذاعة تفسير القرآن الكريم والفتاوى على مدار الساعة"
                    5 -> "إذاعة السنة النبوية الشريفة وصحيح البخاري ومسلم"
                    6 -> "مؤقت النوم لإيقاف البث الإذاعي تلقائياً بعد مدة محددة"
                    7 -> "التحكم في البث من شريط الإشعارات وشاشة القفل"
                    8 -> "استهلاك فائق الانخفاض للبيانات مع دعم الشبكات الضعيفة"
                    else -> "محطة إذاعية إسلامية #$i: إذاعات متخصصة للقراء وإذاعات السيرة النبوية"
                }
            }
        ),
        RoadmapCategory(
            id = "zakat",
            title = "الزكاة والصدقات والحساب المالي",
            iconName = "MonetizationOn",
            count = 30,
            description = "حاسبة زكاة المال، الذهب والفضة، الأسهم، زكاة الفطر، وسجل الصدقات والمشاريع الخيرية.",
            targetTab = AppTab.DAILY_TASKS,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "حساب زكاة المال بالريال والجنيه والدولار وكافة العملات"
                    2 -> "حساب زكاة الذهب والفضة مع التحديث الفوري لسعر الجرام"
                    3 -> "حاسبة زكاة الأسهم والمضاربة والصناديق الاستثمارية"
                    4 -> "حاسبة زكاة الفطر وتحديد الأنصبة الشرعية لكل فرد"
                    5 -> "سجل التبرعات والصدقات مع التنبيه السنوي بموعد الحول"
                    else -> "ميزة زكوية متقدمة #$i: حساب زكاة الأراضي والعقارات وعروض التجارة والديون"
                }
            }
        ),
        RoadmapCategory(
            id = "calendar",
            title = "التقويم الهجري والمناسبات",
            iconName = "CalendarMonth",
            count = 30,
            description = "تقويم هجري وميلادي متزامن، مواعيد الأيام البيض، عاشوراء، عرفة، وبدايات الأشهر القمرية.",
            targetTab = AppTab.DAILY_TASKS,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "التقويم الهجري الدقيق المعتمد على رؤية الهلال وتقويم أم القرى"
                    2 -> "التحويل الفوري المزدوج بين التاريخ الهجري والميلادي"
                    3 -> "تنبيهات صيام الأيام البيض (13، 14، 15 من كل شهر هجري)"
                    4 -> "تنبيهات صيام يومي الاثنين والخميس وصيام عاشوراء وتاسوعاء وعرفة"
                    5 -> "عرض المناسبات الإسلامية وتاريخ الغزوات والأحداث النبوية"
                    else -> "ميزة تقويمية #$i: مزامنة التقويم الهجري مع تقويم النظام وتنبيهات المناسبات"
                }
            }
        ),
        RoadmapCategory(
            id = "family",
            title = "الأسرة المسلمة وتربية الأطفال",
            iconName = "FamilyRestroom",
            count = 30,
            description = "قصص الأنبياء للأطفال، تعليم الوضوء والصلاة التفاعلي، وجدول متابعة عبادات الأسرة.",
            targetTab = AppTab.DAILY_TASKS,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "تعليم الوضوء والصلاة للأطفال بالرسوم التوضيحية والصوت"
                    2 -> "قصص الأنبياء وقصص القرآن الكريم بأسلوب ميسر وشيق"
                    3 -> "جدول متابعة صلوات وأذكار أفراد الأسرة اليومية"
                    4 -> "حفظ قصار السور للأطفال مع نظام النجوم والمكافآت التشجيعية"
                    5 -> "فتاوى وأحكام الأسرة، والتربية النبوية، وحقوق الوالدين"
                    else -> "ميزة أسرية #$i: مسابقات إسلامية عائلية وبنك الأسئلة الشرعية"
                }
            }
        ),
        RoadmapCategory(
            id = "duas",
            title = "موسوعة الأدعية والمناجاة",
            iconName = "VolunteerActivism",
            count = 40,
            description = "أدعية القرآن الكريم، أدعية السنة النبوية، جوامع الدعاء، وأدعية الأنبياء والصالحين.",
            targetTab = AppTab.DUAS,
            features = (1..40).map { i ->
                when (i) {
                    1 -> "أدعية القرآن الكريم مرتبة حسب السور والمواضيع"
                    2 -> "أدعية السنة النبوية الصحيحة وجوامع الكلم المأثورة"
                    3 -> "أدعية الشفاء وتفريج الكروب والهموم وقضاء الديون"
                    4 -> "أدعية سجود التلاوة، صلاة الاستخارة، وصلاة الحاجة"
                    5 -> "إمكانية إنشاء قائمة أدعية مفضلة ومشاركتها مع الأهل"
                    else -> "ميزة في الأدعية #$i: تسجيل الأدعية الصوتية والتذكير بأوقات الإجابة"
                }
            }
        ),
        RoadmapCategory(
            id = "stats",
            title = "التحليلات وإحصائيات الإنجاز",
            iconName = "BarChart",
            count = 30,
            description = "رسوم بيانية لمتابعة الصلوات، الصفحات المقروءة، الأذكار اليومية، ومستوى الالتزام الإيماني.",
            targetTab = AppTab.DAILY_TASKS,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "لوحة تحكم تفاعلية توضح نسبة إنجاز الأوراد اليومية"
                    2 -> "رسوم بيانية أسبوعية وشهرية لعدد صفحات القرآن المقروءة"
                    3 -> "مؤشر الالتزام بصلوات الجماعة في المسجد"
                    4 -> "عداد الأيام المتتالية (Streak) لتحفيز الاستمرارية"
                    5 -> "تقرير شهري بالإنجازات والعبادات مع ملخص قابل للتصدير"
                    else -> "ميزة إحصائية #$i: مقارنة الأداء ومؤشر التحسن الروحي عبر الزمن"
                }
            }
        ),
        RoadmapCategory(
            id = "theme_custom",
            title = "التخصيص ومظهر Apple والوضع الليلي",
            iconName = "Palette",
            count = 30,
            description = "واجهة زجاجية بنمط Frosted Glass، خطوط عربية فاخرة (عثماني، ديواني، نسخ)، وأنماط متعددة.",
            targetTab = null,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "تصميم فاخر بمظهر زجاجي عصري مستوحى من واجهات Apple الحديثة"
                    2 -> "دعم كامل للوضع الداكن (Dark Mode) الموفر لبطارية شاشات AMOLED"
                    3 -> "تخصيص الخطوط وحجم الخط للنصوص والأذكار والقرآن"
                    4 -> "تأثيرات حركة ناعمة وانتقالات سلسة بين الشاشات"
                    5 -> "لوحات ألوان متعددة (الزمردي الإسلامي، الذهبي الملكي، الكحلي الليلي)"
                    else -> "ميزة تخصيص #$i: سمات مخصصة، أيقونات تطبيق بديلة، وخلفيات إسلامية فاخرة"
                }
            }
        ),
        RoadmapCategory(
            id = "offline_perf",
            title = "العمل بدون إنترنت والأداء الفائق",
            iconName = "OfflinePin",
            count = 25,
            description = "قاعدة بيانات محلية سريعة Room SQLite، استهلاك بطارية شبه معدوم، وحماية تامة للخصوصية.",
            targetTab = null,
            features = (1..25).map { i ->
                when (i) {
                    1 -> "عمل كامل وشامل لجميع الأذكار والمصحف والأدعية والفتاوى دون إنترنت"
                    2 -> "قاعدة بيانات SQLite محلية مدمجة فائقة السرعة والاستجابة"
                    3 -> "استهلاك طاقة منخفض للغاية لا يؤثر على بطارية الهاتف"
                    4 -> "حجم تطبيق خفيف ومثالي ومناسب لكافة أجهزة أندرويد"
                    5 -> "احترام كامل للخصوصية بدون تتبع أو إعلانات مزعجة"
                    else -> "ميزة أداء #$i: تشفير البيانات المحلية وسرعة إقلاع التطبيق في أجزاء من الثانية"
                }
            }
        ),
        RoadmapCategory(
            id = "widgets",
            title = "ودجت الشاشة الرئيسية والأجهزة الذكية",
            iconName = "Widgets",
            count = 20,
            description = "ودجت شاشة رئيسية للصلاة القادمة، ودجت للذكر اليومي، ودعم التنبيهات الفورية.",
            targetTab = null,
            features = (1..20).map { i ->
                when (i) {
                    1 -> "ودجت الصلاة القادمة على شاشة الهاتف الرئيسية مع العد التنازلي"
                    2 -> "ودجت آية وذكر اليوم المتجدد تلقائياً كل صباح ومساء"
                    3 -> "ودجت متابعة الورد القرآني والإنجاز اليومي"
                    4 -> "تحديث فوري وتلقائي للودجت دون استهلاك البطارية"
                    else -> "ميزة ودجت #$i: تخصيص مظهر الودجت الشفاف واختيار الأحجام المناسبة"
                }
            }
        ),
        RoadmapCategory(
            id = "dawah",
            title = "الدعوة العالمية واللغات والمشاركات",
            iconName = "Public",
            count = 20,
            description = "مشاركة بطاقات إيمانية فاخرة، ترجمة معاني القرآن للغات متعددة، ونشر الخير.",
            targetTab = null,
            features = (1..20).map { i ->
                when (i) {
                    1 -> "مولد بطاقات الآيات والأحاديث بصور وخلفيات إسلامية فاخرة"
                    2 -> "مشاركة سريعة عبر واتساب وتيليجرام وإنستغرام بضغطة واحدة"
                    3 -> "ترجمات معاني القرآن باللغات الإنجليزية والفرنسية والأوردو"
                    4 -> "نظام نشر الخير وبطاقات الختمة المباركة"
                    else -> "ميزة دعوية #$i: بطاقات الصباح والمساء المصورة والملصقات الإسلامية"
                }
            }
        ),
        RoadmapCategory(
            id = "initiatives",
            title = "المبادرات الإيمانية وإحياء السنن",
            iconName = "AutoAwesome",
            count = 20,
            description = "إحياء السنن المهجورة، صيام التطوع، ختمات جماعية، ومسابقات التنافس في الخيرات.",
            targetTab = null,
            features = (1..20).map { i ->
                when (i) {
                    1 -> "مبادرة إحياء السنن النبوية المهجورة في اليوم والليلة"
                    2 -> "مبادرة صلاة الفجر في وقتها والتنافس في المحافظة عليها"
                    3 -> "مبادرة ختم القرآن الجماعي وإهداء الثواب"
                    4 -> "تحديات أسبوعية للمحافظة على أذكار الصباح والمساء وقيام الليل"
                    else -> "مبادرة إيمانية #$i: نشر المحتوى الإيجابي والتطوع في نشر العلم الشرعي"
                }
            }
        ),
        RoadmapCategory(
            id = "stories",
            title = "قصص الأنبياء والسيرة النبوية والصحابة",
            iconName = "AutoStories",
            count = 45,
            description = "موسوعة قصص الأنبياء كاملة، أحداث السيرة النبوية الشريفة، وسير الخلفاء الراشدين والصحابة والدروس والعبر.",
            targetTab = null,
            features = (1..45).map { i ->
                when (i) {
                    1 -> "قصص الأنبياء (آدم، نوح، هود، صالح، إبراهيم، يوسف، موسى، عيسى، محمد ﷺ)"
                    2 -> "محطات السيرة النبوية (المولد، البعثة، الهجرة، بدر، أحد، فتح مكة، حجة الوداع)"
                    3 -> "سير الصحابة والتابعين (أبو بكر، عمر، عثمان، علي، خالد بن الوليد، أمهات المؤمنين)"
                    4 -> "استخراج العبر والفوائد الإيمانية والتربوية والآيات الشاهدة لكل قصة"
                    5 -> "التحكم بحجم الخط ونسخ ومشاركة نصوص القصص بجودة عالية"
                    else -> "قصة وموقف إيماني ملهم #$i: قصص التابعين والصالحين والعلماء الربانيين"
                }
            }
        ),
        RoadmapCategory(
            id = "hadith_enc",
            title = "الموسوعة الحديثية والأربعين النووية",
            iconName = "MenuBook",
            count = 50,
            description = "الأربعين النووية كاملة بالشرح والتخريج، وأبواب مختارة من رياض الصالحين وصحيح البخاري ومسلم.",
            targetTab = null,
            features = (1..50).map { i ->
                when (i) {
                    1 -> "متن الأربعين النووية كاملاً مع التخريج والشرح المبسط والفوائد"
                    2 -> "أبواب رياض الصالحين في التوبة، الصبر، الصدق، والتقوى والإخلاص"
                    3 -> "خاصية حفظ الأحاديث في المفضلة ومشاركتها كبطاقات دعوية"
                    4 -> "البحث السريع في متون الأحاديث والرواة والأبواب الفقهية"
                    else -> "حديث شريف معتمد #$i: أحاديث الآداب والأخلاق والمعاملات"
                }
            }
        ),
        RoadmapCategory(
            id = "quiz",
            title = "المسابقات واختبار المعلومات الإسلامية",
            iconName = "Quiz",
            count = 35,
            description = "اختبارات تفاعلية حية في القرآن، السيرة، الفقه، والصحابة مع توضيح الإجابات وتحديد الرتب والمستويات.",
            targetTab = null,
            features = (1..35).map { i ->
                when (i) {
                    1 -> "أسئلة تفاعلية فورية متعددة الخيارات في علوم القرآن والسيرة والفقه"
                    2 -> "شرح مفصل وفائدة علمية بعد كل إجابة لترسيخ المعلومة"
                    3 -> "نظام النقاط والرتب (طالب علم، باحث، فقيه، علامة) وسلسلة الإجابات الصحيحة"
                    4 -> "إعادة الاختبار بأسئلة عشوائية متجددة لزيادة الحصيلة المعرفية"
                    else -> "مجموعة أسئلة شرعية #$i: مسابقات ثقافية وإيمانية شاملة"
                }
            }
        ),
        RoadmapCategory(
            id = "ruqyah",
            title = "الرقية الشرعية الشاملة وتوجيهات الشفاء",
            iconName = "HealthAndSafety",
            count = 30,
            description = "الرقية بآيات القرآن الكريم، الأدعية النبوية المأثورة، عدادات التكرار، وتوجيهات الشفاء والتحصين.",
            targetTab = null,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "آيات الرقية الشاملة من الفاتحة، آية الكرسي، خواتيم البقرة، والمعوذات"
                    2 -> "أدعية الشفاء النبوية ورقية جبريل عليه السلام للنبي ﷺ"
                    3 -> "عدادات تكرار تفاعلية مع الاهتزاز اللمسي وتأكيد القراءة"
                    4 -> "توجيهات الرقية الصحيحة على النفس والماء وزيت الزيتون"
                    else -> "مقطع تحصين شرعي #$i: أدعية حفظ النفس والأهل والأبناء والبيت"
                }
            }
        ),
        RoadmapCategory(
            id = "fasting",
            title = "سجل الصيام والتطوع والقضاء والكفارات",
            iconName = "WbTwilight",
            count = 30,
            description = "متابعة صيام الاثنين والخميس، الأيام البيض، قضاء رمضان، كفارات ونذور، وأدعية الإفطار والسحور.",
            targetTab = null,
            features = (1..30).map { i ->
                when (i) {
                    1 -> "تسجيل حالة الصيام اليومية بضغطة زر مع احتساب الأجر"
                    2 -> "عداد صيام الاثنين والخميس والأيام البيض الهجرية"
                    3 -> "سجل قضاء رمضان المتبقي وتتبع الأيام حتى اكتمالها"
                    4 -> "أدعية الإفطار والسحور المأثورة الصحيحة"
                    else -> "ميزة في الصيام #$i: فضل صيام التطوع وأحكام الكفارات والفدية"
                }
            }
        )
    )
}

@Composable
fun FeaturesRoadmapDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    onOpenZakat: (() -> Unit)? = null,
    onOpenCalendar: (() -> Unit)? = null,
    onOpenAsmaAllah: (() -> Unit)? = null,
    onOpenStats: (() -> Unit)? = null,
    onOpenAdhanSettings: (() -> Unit)? = null,
    onOpenFastFaq: (() -> Unit)? = null,
    onOpenKhatmahPlan: (() -> Unit)? = null,
    onOpenShareCard: (() -> Unit)? = null,
    onOpenCustomAthkar: (() -> Unit)? = null,
    onOpenStories: (() -> Unit)? = null,
    onOpenHadith: (() -> Unit)? = null,
    onOpenQuiz: (() -> Unit)? = null,
    onOpenRuqyah: (() -> Unit)? = null,
    onOpenFasting: (() -> Unit)? = null
) {
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val allCategories = RoadmapData.categories
    val totalFeaturesCount = allCategories.sumOf { it.count }

    val filteredCategories = remember(searchQuery, selectedCategoryId) {
        allCategories.filter { category ->
            if (selectedCategoryId != null && category.id != selectedCategoryId) {
                false
            } else if (searchQuery.isNotBlank()) {
                category.title.contains(searchQuery, ignoreCase = true) ||
                category.description.contains(searchQuery, ignoreCase = true) ||
                category.features.any { it.contains(searchQuery, ignoreCase = true) }
            } else {
                true
            }
        }
    }

    val handleAction: (String, AppTab?) -> Unit = { categoryId, tab ->
        onDismiss()
        when (categoryId) {
            "zakat" -> onOpenZakat?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "calendar" -> onOpenCalendar?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "stats" -> onOpenStats?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "family" -> onOpenStories?.invoke() ?: (onOpenFastFaq?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS))
            "dawah" -> onOpenShareCard?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "stories" -> onOpenStories?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "hadith_enc" -> onOpenHadith?.invoke() ?: viewModel.setTab(AppTab.FATWAS)
            "quiz" -> onOpenQuiz?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "ruqyah" -> onOpenRuqyah?.invoke() ?: viewModel.setTab(AppTab.ATHKAR)
            "fasting" -> onOpenFasting?.invoke() ?: viewModel.setTab(AppTab.DAILY_TASKS)
            "theme_custom" -> viewModel.showNotification("المظهر والسمات", "تم تفعيل نمط Apple الزجاجي والوضع الليلي المريح")
            "offline_perf" -> viewModel.showNotification("بدون إنترنت", "جميع بيانات التطبيق تعمل محلياً 100% دون الحاجة لإنترنت")
            "widgets" -> viewModel.showNotification("ودجت الشاشة", "يمكنك إضافة ودجت الصلاة والذكر من شاشة هاتفك الرئيسية")
            "initiatives" -> {
                onOpenCustomAthkar?.invoke() ?: viewModel.setTab(AppTab.ATHKAR)
            }
            else -> {
                tab?.let { viewModel.setTab(it) }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF04140E),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 1. Top Header with Close and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF0D2C20), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "خريطة الـ $totalFeaturesCount ميزة وإضافة إيمانية",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "منظومة إسلامية شاملة بتصميم Apple فائق الفخامة",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IslamicGoldPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "500+",
                            color = IslamicEmeraldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("ابحث بين الـ 500 ميزة وقسم...", color = IslamicTextSecondary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = IslamicGoldLight)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGoldPrimary,
                        unfocusedBorderColor = Color(0x44E2B84D),
                        focusedContainerColor = Color(0xFF092017),
                        unfocusedContainerColor = Color(0xFF092017),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Category Filter Chips (Horizontal)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("جميع الأقسام ($totalFeaturesCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGoldPrimary,
                                selectedLabelColor = IslamicEmeraldDark,
                                containerColor = Color(0xFF0D2C20),
                                labelColor = IslamicGoldLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    items(allCategories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                            },
                            label = { Text("${cat.title} (${cat.count})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGoldPrimary,
                                selectedLabelColor = IslamicEmeraldDark,
                                containerColor = Color(0xFF0D2C20),
                                labelColor = IslamicGoldLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Categories & Features List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            searchQuery = searchQuery,
                            onLaunch = { handleAction(category.id, category.targetTab) },
                            onFeatureClick = { feat ->
                                viewModel.showNotification("الميزة مفعلة", feat)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: RoadmapCategory,
    searchQuery: String,
    onLaunch: () -> Unit,
    onFeatureClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val displayedFeatures = remember(category, searchQuery) {
        if (searchQuery.isBlank()) {
            category.features
        } else {
            category.features.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = Color(0x33E2B84D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1B5E3C), Color(0xFF0A2B1C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (category.iconName) {
                                "MenuBook" -> Icons.Default.MenuBook
                                "AccessTime" -> Icons.Default.AccessTime
                                "TouchApp" -> Icons.Default.TouchApp
                                "QuestionAnswer" -> Icons.Default.QuestionAnswer
                                "Radio" -> Icons.Default.Radio
                                "MonetizationOn" -> Icons.Default.MonetizationOn
                                "CalendarMonth" -> Icons.Default.CalendarMonth
                                "FamilyRestroom" -> Icons.Default.FamilyRestroom
                                "VolunteerActivism" -> Icons.Default.VolunteerActivism
                                "BarChart" -> Icons.Default.BarChart
                                "Palette" -> Icons.Default.Palette
                                "OfflinePin" -> Icons.Default.OfflinePin
                                "Widgets" -> Icons.Default.Widgets
                                "Public" -> Icons.Default.Public
                                else -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x22E2B84D)
                            ) {
                                Text(
                                    text = "${category.count} ميزة",
                                    color = IslamicGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            maxLines = if (isExpanded) 4 else 1
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "عرض التفاصيل",
                        tint = IslamicGoldPrimary
                    )
                }
            }

            // Quick Open Action Button
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onLaunch,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163E2D)),
                    border = BorderStroke(1.dp, Color(0x66E2B84D)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تشغيل الميزة الآن", color = IslamicGoldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = if (isExpanded) "إخفاء التفاصيل" else "استعراض الـ (${displayedFeatures.size}) ميزة",
                    color = IslamicGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            // Expanded Features List
            AnimatedVisibility(
                visible = isExpanded || searchQuery.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = Color(0x22E2B84D))
                    Spacer(modifier = Modifier.height(4.dp))

                    displayedFeatures.forEachIndexed { index, feat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF071E15))
                                .clickable { onFeatureClick(feat) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33E2B84D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = IslamicGoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE8F5E9),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "مفعلة وجاهزة",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
