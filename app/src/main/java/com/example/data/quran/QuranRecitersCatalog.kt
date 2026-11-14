package com.example.data.quran

import java.util.Locale

data class ReciterOption(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val styleEn: String = "Murattal • Hafs",
    val styleAr: String = "مرتل • حفص عن عاصم",
    val surahServerBaseUrl: String = "https://server8.mp3quran.net/afs/",
    val ayahEditionId: String = "ar.alafasy"
) {
    fun surahMp3Url(surahNumber: Int): String {
        val padded = String.format(Locale.US, "%03d", surahNumber.coerceIn(1, 114))
        val base = if (surahServerBaseUrl.endsWith("/")) surahServerBaseUrl else "$surahServerBaseUrl/"
        return "${base}${padded}.mp3"
    }

    fun fallbackSurahMp3Url(surahNumber: Int): String {
        val padded = String.format(Locale.US, "%03d", surahNumber.coerceIn(1, 114))
        return "https://server8.mp3quran.net/afs/${padded}.mp3"
    }
}

object QuranRecitersCatalog {

    val reciters: List<ReciterOption> = listOf(
        ReciterOption(
            id = "ar.islamsobhi",
            nameEn = "Islam Sobhi",
            nameAr = "إسلام صبحي",
            styleEn = "Heartfelt Melodious • Hafs",
            styleAr = "تلاوة خاشعة • حفص عن عاصم",
            surahServerBaseUrl = "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.alafasy",
            nameEn = "Mishary Rashid Alafasy",
            nameAr = "مشاري راشد العفاسي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server8.mp3quran.net/afs/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdulbasitmurattal",
            nameEn = "Abdul Basit Abdul Samad (Murattal)",
            nameAr = "عبد الباسط عبد الصمد (مرتل)",
            styleEn = "Murattal • Classic Egyptian",
            styleAr = "المصحف المرتل",
            surahServerBaseUrl = "https://server7.mp3quran.net/basit/",
            ayahEditionId = "ar.abdulbasitmurattal"
        ),
        ReciterOption(
            id = "ar.abdulbasitmujawwad",
            nameEn = "Abdul Basit Abdul Samad (Mujawwad)",
            nameAr = "عبد الباسط عبد الصمد (مجود)",
            styleEn = "Mujawwad • Masterpiece",
            styleAr = "المصحف المجود",
            surahServerBaseUrl = "https://server13.mp3quran.net/basit_mjwd/",
            ayahEditionId = "ar.abdulbasitmurattal"
        ),
        ReciterOption(
            id = "ar.husary",
            nameEn = "Mahmoud Khalil Al-Husary",
            nameAr = "محمود خليل الحصري",
            styleEn = "Murattal • Tajweed Standard",
            styleAr = "المصحف المرتل",
            surahServerBaseUrl = "https://server13.mp3quran.net/husr/",
            ayahEditionId = "ar.husary"
        ),
        ReciterOption(
            id = "ar.husarymujawwad",
            nameEn = "Mahmoud Khalil Al-Husary (Mujawwad)",
            nameAr = "محمود خليل الحصري (مجود)",
            styleEn = "Mujawwad • Classic",
            styleAr = "المصحف المجود",
            surahServerBaseUrl = "https://server13.mp3quran.net/husr_mjwd/",
            ayahEditionId = "ar.husarymujawwad"
        ),
        ReciterOption(
            id = "ar.minshawi",
            nameEn = "Mohamed Siddiq Al-Minshawi",
            nameAr = "محمد صديق المنشاوي",
            styleEn = "Murattal • Reverent",
            styleAr = "المصحف المرتل",
            surahServerBaseUrl = "https://server10.mp3quran.net/minsh/",
            ayahEditionId = "ar.minshawi"
        ),
        ReciterOption(
            id = "ar.minshawimujawwad",
            nameEn = "Mohamed Siddiq Al-Minshawi (Mujawwad)",
            nameAr = "محمد صديق المنشاوي (مجود)",
            styleEn = "Mujawwad • Emotional",
            styleAr = "المصحف المجود",
            surahServerBaseUrl = "https://server11.mp3quran.net/minsh_mjwd/",
            ayahEditionId = "ar.minshawimujawwad"
        ),
        ReciterOption(
            id = "ar.mahermuaiqly",
            nameEn = "Maher Al-Muaiqly",
            nameAr = "ماهر المعيقلي",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server12.mp3quran.net/maher/",
            ayahEditionId = "ar.mahermuaiqly"
        ),
        ReciterOption(
            id = "ar.abdurrahmaansudais",
            nameEn = "Abdul Rahman Al-Sudais",
            nameAr = "عبد الرحمن السديس",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server11.mp3quran.net/sds/",
            ayahEditionId = "ar.abdurrahmaansudais"
        ),
        ReciterOption(
            id = "ar.saoodshuraym",
            nameEn = "Saud Al-Shuraim",
            nameAr = "سعود الشريم",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server7.mp3quran.net/shur/",
            ayahEditionId = "ar.saoodshuraym"
        ),
        ReciterOption(
            id = "ar.yasseraldossari",
            nameEn = "Yasser Al-Dosari",
            nameAr = "ياسر الدوسري",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server11.mp3quran.net/yasser/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.nasseralqatami",
            nameEn = "Nasser Al-Qatami",
            nameAr = "ناصر القطامي",
            styleEn = "Melodious • Riyadh",
            styleAr = "تلاوة خاشعة",
            surahServerBaseUrl = "https://server6.mp3quran.net/qtm/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.saadalghamdi",
            nameEn = "Saad Al-Ghamdi",
            nameAr = "سعد الغامدي",
            styleEn = "Murattal • Clear Cadence",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server7.mp3quran.net/s_gmd/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.ahmedajamy",
            nameEn = "Ahmed Al-Ajmi",
            nameAr = "أحمد بن علي العجمي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server10.mp3quran.net/ajm/",
            ayahEditionId = "ar.ahmedajamy"
        ),
        ReciterOption(
            id = "ar.faresabbad",
            nameEn = "Fares Abbad",
            nameAr = "فارس عباد",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server8.mp3quran.net/frs_a/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.shaatree",
            nameEn = "Abu Bakr Al-Shatri",
            nameAr = "أبو بكر الشاطري",
            styleEn = "Murattal • Warm Tone",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server11.mp3quran.net/shatri/",
            ayahEditionId = "ar.shaatree"
        ),
        ReciterOption(
            id = "ar.hazzaalbalushi",
            nameEn = "Hazza Al-Balushi",
            nameAr = "هزاع البلوشي",
            styleEn = "Tranquil Recitation",
            styleAr = "تلاوة هادئة خاشعة",
            surahServerBaseUrl = "https://server11.mp3quran.net/hazza/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.raadalkurdi",
            nameEn = "Raad Mohammad Al-Kurdi",
            nameAr = "رعد محمد الكردي",
            styleEn = "Dynamic & Reverent",
            styleAr = "تلاوة مؤثرة",
            surahServerBaseUrl = "https://server6.mp3quran.net/kurdi/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.idreesabkar",
            nameEn = "Idris Abkar",
            nameAr = "إدريس أبكر",
            styleEn = "Deep Emotional Maqam",
            styleAr = "تلاوة خاشعة مؤثرة",
            surahServerBaseUrl = "https://server6.mp3quran.net/abkr/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.khalidaljalil",
            nameEn = "Khalid Al-Jalil",
            nameAr = "خالد الجليل",
            styleEn = "Riyadh Grand Mosque",
            styleAr = "تلاوة خاشعة • الرياض",
            surahServerBaseUrl = "https://server10.mp3quran.net/jleel/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.mansouralsalimi",
            nameEn = "Mansour Al-Salimi",
            nameAr = "منصور السالمي",
            styleEn = "Calm Spiritual Recitation",
            styleAr = "تلاوة روحانية هادئة",
            surahServerBaseUrl = "https://server14.mp3quran.net/mansor/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.wadihalyamani",
            nameEn = "Wadih Al-Yamani",
            nameAr = "وديع اليمني",
            styleEn = "Clear Melodic Murattal",
            styleAr = "مرتل عذب الصوت",
            surahServerBaseUrl = "https://server6.mp3quran.net/wdee3/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.noreensiddiq",
            nameEn = "Noreen Mohammad Siddiq",
            nameAr = "نورين محمد صديق",
            styleEn = "Al-Duri 'an Abi 'Amr",
            styleAr = "رواية الدوري عن أبي عمرو",
            surahServerBaseUrl = "https://server16.mp3quran.net/nourin_siddig/Rewayat-Aldori-A-n-Abi-Amr/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muhammadayyoub",
            nameEn = "Muhammad Ayyub",
            nameAr = "محمد أيوب",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server8.mp3quran.net/ayyub/",
            ayahEditionId = "ar.muhammadayyoub"
        ),
        ReciterOption(
            id = "ar.muhammadjibreel",
            nameEn = "Muhammad Jibril",
            nameAr = "محمد جبريل",
            styleEn = "Egyptian Murattal",
            styleAr = "مرتل • مصر",
            surahServerBaseUrl = "https://server8.mp3quran.net/jbrl/",
            ayahEditionId = "ar.muhammadjibreel"
        ),
        ReciterOption(
            id = "ar.alijaber",
            nameEn = "Ali Jaber",
            nameAr = "علي جابر",
            styleEn = "Masjid Al-Haram Historic Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server11.mp3quran.net/a_jbr/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.bandarbaleela",
            nameEn = "Bandar Baleela",
            nameAr = "بندر بليلة",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server6.mp3quran.net/balilah/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdullahjuhany",
            nameEn = "Abdullah Awad Al-Juhany",
            nameAr = "عبد الله عواد الجهني",
            styleEn = "Masjid Al-Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server13.mp3quran.net/jhn/",
            ayahEditionId = "ar.abdullaah3awwaad"
        ),
        ReciterOption(
            id = "ar.salahbudair",
            nameEn = "Salah Al-Budair",
            nameAr = "صلاح البدير",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server6.mp3quran.net/s_bud/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.hudhaify",
            nameEn = "Ali Al-Hudhaify",
            nameAr = "علي بن عبد الرحمن الحذيفي",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server9.mp3quran.net/hthfi/",
            ayahEditionId = "ar.hudhaify"
        ),
        ReciterOption(
            id = "ar.abdulmohsenqasim",
            nameEn = "Abdulmohsen Al-Qasim",
            nameAr = "عبد المحسن القاسم",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server8.mp3quran.net/qasm/",
            ayahEditionId = "ar.abdulmuhsinalqasim"
        ),
        ReciterOption(
            id = "ar.abdullahbasfar",
            nameEn = "Abdullah Basfar",
            nameAr = "عبد الله بصفر",
            styleEn = "Tajweed Murattal",
            styleAr = "مرتل متقن",
            surahServerBaseUrl = "https://server6.mp3quran.net/bsfr/",
            ayahEditionId = "ar.abdullahbasfar"
        ),
        ReciterOption(
            id = "ar.salahbukhatir",
            nameEn = "Salah Bukhatir",
            nameAr = "صلاح بو خاطر",
            styleEn = "Serene Recitation",
            styleAr = "تلاوة ندية",
            surahServerBaseUrl = "https://server8.mp3quran.net/bu_khtr/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.hanirifai",
            nameEn = "Hani Ar-Rifai",
            nameAr = "هاني الرفاعي",
            styleEn = "Emotional Recitation",
            styleAr = "تلاوة خاشعة",
            surahServerBaseUrl = "https://server8.mp3quran.net/hani/",
            ayahEditionId = "ar.hanirifai"
        ),
        ReciterOption(
            id = "ar.khalifaaltunaiji",
            nameEn = "Khalifa Al-Tunaiji",
            nameAr = "خليفة الطنيجي",
            styleEn = "Crisp Tajweed • Murattal",
            styleAr = "مرتل • تجويد متقن",
            surahServerBaseUrl = "https://server12.mp3quran.net/tnjy/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.tawfeeqassayegh",
            nameEn = "Tawfeeq As-Sayegh",
            nameAr = "توفيق الصايغ",
            styleEn = "Melodic Murattal",
            styleAr = "تلاوة عذبة",
            surahServerBaseUrl = "https://server6.mp3quran.net/twfeeq/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muhammadluhaidan",
            nameEn = "Muhammad Al-Luhaidan",
            nameAr = "محمد اللحيدان",
            styleEn = "Powerful Recitation",
            styleAr = "تلاوة مؤثرة",
            surahServerBaseUrl = "https://server8.mp3quran.net/lhdan/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.adelkalbani",
            nameEn = "Adel Al-Kalbani",
            nameAr = "عادل الكلباني",
            styleEn = "Deep Baritone • Murattal",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server8.mp3quran.net/a_klb/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdullahmatrood",
            nameEn = "Abdullah Al-Matrood",
            nameAr = "عبد الله المطرود",
            styleEn = "Calm & Measured",
            styleAr = "تلاوة خاشعة",
            surahServerBaseUrl = "https://server8.mp3quran.net/mtrod/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdulwadoodhaneef",
            nameEn = "Abdulwadood Haneef",
            nameAr = "عبد الودود حنيف",
            styleEn = "Madinah Style Murattal",
            styleAr = "مرتل • المدينة",
            surahServerBaseUrl = "https://server8.mp3quran.net/wdod/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdulbarithubaity",
            nameEn = "Abdulbari Ath-Thubaity",
            nameAr = "عبد الباري الثبيتي",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server6.mp3quran.net/thubti/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.mahmoudalbanna",
            nameEn = "Mahmoud Ali Al-Banna",
            nameAr = "محمود علي البنا",
            styleEn = "Classic Egyptian School",
            styleAr = "المدرسة المصرية الأصيلة",
            surahServerBaseUrl = "https://server8.mp3quran.net/bna/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.mustafaismail",
            nameEn = "Mustafa Ismail",
            nameAr = "مصطفى إسماعيل",
            styleEn = "Classic Egyptian Reciter",
            styleAr = "المدرسة المصرية",
            surahServerBaseUrl = "https://server8.mp3quran.net/mustafa/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muhammadtablawi",
            nameEn = "Mohammad Al-Tablawi",
            nameAr = "محمد محمود الطبلاوي",
            styleEn = "Classic Egyptian Reciter",
            styleAr = "المدرسة المصرية",
            surahServerBaseUrl = "https://server12.mp3quran.net/tblawi/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.ibrahimalakhdar",
            nameEn = "Ibrahim Al-Akhdar",
            nameAr = "إبراهيم الأخضر",
            styleEn = "Chief Reciter of Madinah",
            styleAr = "شيخ قراء المسجد النبوي",
            surahServerBaseUrl = "https://server6.mp3quran.net/akdr/",
            ayahEditionId = "ar.ibrahimakhbar"
        ),
        ReciterOption(
            id = "ar.akramalaqmi",
            nameEn = "Akram Al-Alaqmi",
            nameAr = "أكرم العلقمي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server9.mp3quran.net/akrm/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.sahlyassin",
            nameEn = "Sahl Yassin",
            nameAr = "سهل ياسين",
            styleEn = "Murattal • Jeddah",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server6.mp3quran.net/shl/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.saberabdulhakam",
            nameEn = "Saber Abdul Hakam",
            nameAr = "صابر عبد الحكم",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server12.mp3quran.net/hkm/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdullahkhayat",
            nameEn = "Abdullah Khayat",
            nameAr = "عبد الله خياط",
            styleEn = "Historic Haram Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server12.mp3quran.net/kyat/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdulrashidsufi",
            nameEn = "Abdul Rashid Ali Sufi",
            nameAr = "عبد الرشيد صوفي",
            styleEn = "Master of Ten Qira'at",
            styleAr = "متقن القراءات العشر",
            surahServerBaseUrl = "https://server16.mp3quran.net/soufi/Rewayat-Hafs-A-n-Assem/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.aymansowaid",
            nameEn = "Dr. Ayman Suwayd",
            nameAr = "د. أيمن رشدي سويد",
            styleEn = "Tajweed Master • Instructional",
            styleAr = "المصحف المعلم بالتجويد",
            surahServerBaseUrl = "https://server8.mp3quran.net/ayman/",
            ayahEditionId = "ar.aymanswoaid"
        ),
        ReciterOption(
            id = "ar.omaralkazabri",
            nameEn = "Omar Al-Qazabri",
            nameAr = "عمر القزابري",
            styleEn = "Warsh 'an Nafi' • Casablanca",
            styleAr = "رواية ورش عن نافع",
            surahServerBaseUrl = "https://server9.mp3quran.net/omar_warsh/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.laayounelkouchi",
            nameEn = "Laayoun El Kouchi",
            nameAr = "العيون الكوشي",
            styleEn = "Warsh 'an Nafi' • Morocco",
            styleAr = "رواية ورش عن نافع",
            surahServerBaseUrl = "https://server11.mp3quran.net/koshi/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.yasseralmazroyee",
            nameEn = "Yasser Al-Mazroyee",
            nameAr = "ياسر المزروعي",
            styleEn = "Rowayat Ruways & Rawh",
            styleAr = "قراءة يعقوب الحضرمي",
            surahServerBaseUrl = "https://server9.mp3quran.net/mzroyee/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.zaki_daghistani",
            nameEn = "Zaki Daghistani",
            nameAr = "زكي داغستاني",
            styleEn = "Hijazi Traditional Maqam",
            styleAr = "التلاوة الحجازية",
            surahServerBaseUrl = "https://server9.mp3quran.net/zaki/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.abdulaziz_alahmad",
            nameEn = "Abdulaziz Al-Ahmad",
            nameAr = "عبد العزيز الأحمد",
            styleEn = "Heartfelt Murattal • Hafs",
            styleAr = "تلاوة خاشعة • حفص عن عاصم",
            surahServerBaseUrl = "https://server11.mp3quran.net/a_ahmed/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muhammad_abdulkarim",
            nameEn = "Muhammad Abdul-Kareem",
            nameAr = "محمد عبد الكريم",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server12.mp3quran.net/m_krm/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muhammad_almuhaisni",
            nameEn = "Muhammad Al-Muhaisny",
            nameAr = "محمد المحيسني",
            styleEn = "Emotional Recitation",
            styleAr = "تلاوة مؤثرة",
            surahServerBaseUrl = "https://server11.mp3quran.net/mhsny/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.ahmad_alhuthayfi",
            nameEn = "Ahmad Al-Hudhaify",
            nameAr = "أحمد بن علي الحذيفي",
            styleEn = "Al-Masjid An-Nabawi Imam",
            styleAr = "إمام المسجد النبوي",
            surahServerBaseUrl = "https://server8.mp3quran.net/ahmad_huth/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.khalid_alqahtani",
            nameEn = "Khalid Al-Qahtani",
            nameAr = "خالد القحطاني",
            styleEn = "Reverent Recitation",
            styleAr = "تلاوة خاشعة",
            surahServerBaseUrl = "https://server10.mp3quran.net/qht/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.nabil_arrifai",
            nameEn = "Nabil Ar-Rifai",
            nameAr = "نبيل الرفاعي",
            styleEn = "Murattal • Jeddah",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://server9.mp3quran.net/nabil/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.shirzad_taher",
            nameEn = "Shirzad Abdulrahman Taher",
            nameAr = "شيرزاد عبد الرحمن طاهر",
            styleEn = "Melodic Tajweed • Hafs",
            styleAr = "تلاوة متقنة عذبة",
            surahServerBaseUrl = "https://server12.mp3quran.net/shrz/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.peshawa_qadir",
            nameEn = "Peshawa Qadir Al-Kurdi",
            nameAr = "بيشه وا قادر الكردي",
            styleEn = "Tranquil Recitation • Hafs",
            styleAr = "تلاوة خاشعة • حفص عن عاصم",
            surahServerBaseUrl = "https://server16.mp3quran.net/peshawa/Rewayat-Hafs-A-n-Assem/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.badr_alturki",
            nameEn = "Badr Al-Turki",
            nameAr = "بدر التركي",
            styleEn = "Masjid Al-Haram Guest Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server10.mp3quran.net/badr/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.waleed_ashshamsan",
            nameEn = "Waleed Al-Shamsan",
            nameAr = "وليد الشمسان",
            styleEn = "Masjid Al-Haram Guest Imam",
            styleAr = "إمام المسجد الحرام",
            surahServerBaseUrl = "https://server16.mp3quran.net/shmsan/Rewayat-Hafs-A-n-Assem/",
            ayahEditionId = "ar.alafasy"
        )
    )

    fun reciterById(id: String): ReciterOption =
        reciters.firstOrNull { it.id == id } ?: reciters.first()
}
