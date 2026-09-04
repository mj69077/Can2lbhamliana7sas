package com.example.data.local

import com.example.R
import com.example.data.model.GalleryCategory
import com.example.data.model.IslamicArtwork

object IslamicGalleryData {
    val allArtworks: List<IslamicArtwork> = listOf(
        IslamicArtwork(
            id = 1,
            title = "الكعبة المشرفة والمسجد الحرام",
            subtitle = "أول بيت وُضع للناس وقبلة المسلمين في مشارق الأرض ومغاربها",
            location = "مكة المكرمة، المملكة العربية السعودية",
            description = "المسجد الحرام هو أعظم مسجد في الإسلام ويقع في قلب مكة المكرمة. تتوسطه الكعبة المشرفة المغطاة بكسوة الحرير الأسود المزينة بآيات قرآنية مطرزة بخيوط الذهب والفضة الخالصة. الصلاة فيه تعدل مائة ألف صلاة فيما سواه.",
            category = GalleryCategory.HOLY_MOSQUES,
            localDrawableRes = R.drawable.ic_holy_kaaba,
            imageUrl = "https://images.unsplash.com/photo-1565552684305-7e90956b6b77?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("مكة", "الكعبة", "الحرم المكي", "قبلة المسلمين")
        ),
        IslamicArtwork(
            id = 2,
            title = "المسجد النبوي والقبة الخضراء",
            subtitle = "مسجد رسول الله ﷺ والروضة الشريفة روضة من رياض الجنة",
            location = "المدينة المنورة، المملكة العربية السعودية",
            description = "المسجد النبوي الشريف بناه النبي ﷺ في السنة الأولى للهجرة. يتميز بمآذنه الشامخة ومظلاته الهيدروليكية العملاقة وقبته الخضراء الشهيرة التي تعلو الحجرة النبوية المباركة والروضة الشريفة.",
            category = GalleryCategory.HOLY_MOSQUES,
            localDrawableRes = R.drawable.ic_medina_dome,
            imageUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("المدينة المنورة", "المسجد النبوي", "القبة الخضراء", "الروضة الشريفة")
        ),
        IslamicArtwork(
            id = 3,
            title = "المسجد الأقصى وقبة الصخرة المشرفة",
            subtitle = "مسرى رسول الله ﷺ وأولى القبلتين وثالث الحرمين الشريفين",
            location = "القدس الشريف، فلسطين",
            description = "المسجد الأقصى المبارك بكل ما دار عليه السور بمساحته البالغة 144 دونماً. تتوسطه قبة الصخرة المشرفة الذهبية الثمانية الأضلاع ذات الزخارف الفسيفسائية الأموية البديعة التي تعد من أروع ما أبدعته العمارة الإسلامية عبر التاريخ.",
            category = GalleryCategory.HOLY_MOSQUES,
            localDrawableRes = R.drawable.ic_aqsa_dome,
            imageUrl = "https://images.unsplash.com/photo-1578898887932-dce23a595ad4?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("القدس", "الأقصى", "قبة الصخرة", "فلسطين")
        ),
        IslamicArtwork(
            id = 4,
            title = "المصحف الشريف والخط العربي الملكي",
            subtitle = "كتاب الله المنزل باللسان العربي المبين على قلب خاتم الأنبياء",
            location = "مجمع الملك فهد لطباعة المصحف الشريف",
            description = "جمال الخط العربي والزخارف القرآنية العثمانية المذهبة، إتقان متوارث لأرقى فنون التذهيب والتخطيط في خدمة آيات الذكر الحكيم، حيث يلتقي سمو المعنى بجلال الخط والبيان.",
            category = GalleryCategory.CALLIGRAPHY,
            localDrawableRes = R.drawable.ic_quran_rehal,
            imageUrl = "https://images.unsplash.com/photo-1609599006353-e629aaabfeae?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("القرآن", "المصحف", "الخط العربي", "تذهيب")
        ),
        IslamicArtwork(
            id = 5,
            title = "قناديل السكينة والفوانيس الإسلامية",
            subtitle = "إضاءة بيوت الله وسكينة الليالي المباركة ونفحات الذكر",
            location = "العمارة الأندلسية والمملوكية",
            description = "القناديل النحاسية المزخرفة المفرغة بنقوش هندسية دقيقة تبعث أضواء دافئة وظلالاً روحانية عابقة بخشوع السحر وجمال الطاعة في محراب العبادة.",
            category = GalleryCategory.WALLPAPERS,
            localDrawableRes = R.drawable.ic_lantern_glow,
            imageUrl = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("قنديل", "فانوس", "رمضان", "روحانيات")
        ),
        IslamicArtwork(
            id = 6,
            title = "جامع الشيخ زايد الكبير",
            subtitle = "صرح إسلامي معاصر يجمع نقاء الرخام الأبيض وجمال الزخرفة العالمية",
            location = "أبوظبي، الإمارات العربية المتحدة",
            description = "من أكبر المساجد في العالم وأجملها تصميماً؛ يضم 82 قبة مكسوة بالرخام الأبيض النقي، وثريات مذهبة بكريستال شواروفسكي، وأكبر سجادة يدوية منسوجة في العالم مع بحيرات عاكسة تأسر الألباب.",
            category = GalleryCategory.HISTORIC_ARCHITECTURE,
            localDrawableRes = R.drawable.ic_mosque_hero,
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("أبوظبي", "جامع الشيخ زايد", "رخام", "عمارة إسلامية")
        ),
        IslamicArtwork(
            id = 7,
            title = "جامع قرطبة الكبير وعقود الأندلس",
            subtitle = "أعظم روائع الحضارة الأندلسية بأقواسه المزدوجة الموشاة بالأحمر والأبيض",
            location = "قرطبة، الأندلس",
            description = "شيد عام 785م ويعد من روائع الفن المعماري الإسلامي بفضل غابة أعمدته الرخامية ذات الـ 856 عموداً وأقواسه المحدوة ثنائية اللون ومحرابه المذهب الذي لا نظير له في العالم.",
            category = GalleryCategory.HISTORIC_ARCHITECTURE,
            localDrawableRes = R.drawable.ic_mosque_hero,
            imageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("قرطبة", "الأندلس", "محراب", "تاريخ إسلامي")
        ),
        IslamicArtwork(
            id = 8,
            title = "المسبحة الملكية والتسبيح الخالص",
            subtitle = "«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ»",
            location = "تراث الذكر والتسبيح",
            description = "عقد من الأحجار الكريمة والزمرد والذهب المصقول، يصحب الذاكرين في ساعات الخلوة برب العالمين واستغفار الأسحار وتكرار الباقيات الصالحات.",
            category = GalleryCategory.WALLPAPERS,
            localDrawableRes = R.drawable.ic_launcher_foreground,
            imageUrl = "https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?q=80&w=1200&auto=format&fit=crop",
            tags = listOf("تسبيح", "مسبحة", "ذكر", "خشوع")
        )
    )
}
