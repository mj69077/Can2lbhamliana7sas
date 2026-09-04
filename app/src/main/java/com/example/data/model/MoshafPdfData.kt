package com.example.data.model

data class MoshafEdition(
    val id: Int,
    val name: String,
    val description: String,
    val downloadLink: String,
    val riwayah: String,
    val colorAccent: String
)

object MoshafPdfData {
    val editions: List<MoshafEdition> = listOf(
        MoshafEdition(
            id = 1,
            name = "مصحف المدينة المنورة (الأزرق)",
            description = "النسخة الرسمية من مجمع الملك فهد لطباعة المصحف الشريف باللون الأزرق المعتمد",
            downloadLink = "https://archive.org/download/Quran-Kareem-Khawagah-The-Blue-Page-Quran/Quran-Kareem-Khawagah-The-Blue-Page-Quran.pdf",
            riwayah = "حفص عن عاصم",
            colorAccent = "#1976D2"
        ),
        MoshafEdition(
            id = 2,
            name = "مصحف المدينة المنورة (الأخضر)",
            description = "مصحف المدينة النبوية بالخط الكلاسيكي الأخضر الفاخر عالي الدقة",
            downloadLink = "https://archive.org/download/EQuran00001/E-Quran-00001.pdf",
            riwayah = "حفص عن عاصم",
            colorAccent = "#2E7D32"
        ),
        MoshafEdition(
            id = 3,
            name = "مصحف المدينة الجوامعي الكبير",
            description = "مصحف المدينة المنورة الجوامعي الكبير بصفحات واسعة وخط عريض واضح للقراءة",
            downloadLink = "https://archive.org/download/arabic-568335686835685363568q3an1/arabic-quran2.pdf",
            riwayah = "حفص عن عاصم",
            colorAccent = "#0288D1"
        ),
        MoshafEdition(
            id = 4,
            name = "مصحف التجويد الملون الميسر",
            description = "القرآن الكريم مع ترميز ألوان أحكام التجويد (المد، الغنة، الإخفاء، القلقلة)",
            downloadLink = "https://archive.org/download/bensaoud_gmail_20170308_0721/%D9%85%D8%B5%D8%AD%D9%81%20%D8%A7%D9%84%D8%AA%D8%AC%D9%88%D9%8A%D8%AF%20%D8%A7%D9%84%D9%85%D9%84%D9%88%D9%86.pdf",
            riwayah = "حفص عن عاصم",
            colorAccent = "#D32F2F"
        ),
        MoshafEdition(
            id = 5,
            name = "مصحف رواية ورش عن نافع",
            description = "المصحف الشريف برواية ورش عن نافع المدني من طريق الأزرق - طبعة المدينة المنورة",
            downloadLink = "https://archive.org/download/WARSHMADINAHE/WARSH__MADINAH.pdf",
            riwayah = "ورش عن نافع",
            colorAccent = "#E65100"
        ),
        MoshafEdition(
            id = 6,
            name = "مصحف رواية قالون عن نافع",
            description = "القرآن الكريم برواية قالون عن نافع - طبعة المدينة المنورة المعتمدة في بلاد المغرب وليبيا",
            downloadLink = "https://archive.org/download/0471Pdf_201804/0471%20%20%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%82%D8%B1%D8%A7%20%20%D8%A7%D9%88%D9%86%D9%84%D8%A7%D9%8A%D9%86%20%20%20%20%20pdf%20%20%20%20%20%D9%85%D8%B5%D8%AD%D9%81%20%D8%A7%D9%84%D9%85%D8%AF%D9%8A%D9%86%D8%A9%20%D8%A7%D9%84%D9%86%D8%A8%D9%88%D9%8A%D8%A9%20%D8%A8%D8%B1%D9%88%D8%A7%D9%8A%D8%A9%20%D9%82%D8%A7%D9%84%D9%88%D9%86%20%D8%B9%D9%86%20%D9%86%D8%A7%D9%81%D8%B9.pdf",
            riwayah = "قالون عن نافع",
            colorAccent = "#6A1B9A"
        ),
        MoshafEdition(
            id = 7,
            name = "مصحف الجوال واللوحي PDF",
            description = "نسخة مهيأة خصيصاً للهواتف الذكية مقسمة بوضوح لسهولة التصفح أثناء التنقل",
            downloadLink = "https://archive.org/download/EQuran00003/E-Quran-00003.pdf",
            riwayah = "حفص عن عاصم",
            colorAccent = "#00897B"
        ),
        MoshafEdition(
            id = 8,
            name = "مصحف بهامش تفسير الجلالين",
            description = "القرآن الكريم وبجانبه التفسير الميسر المعتمد للإمامين المحلي والسيوطي (تفسير الجلالين)",
            downloadLink = "https://archive.org/download/Quran25/Quran25.pdf",
            riwayah = "حفص مع التفسير",
            colorAccent = "#C2185B"
        )
    )
}
