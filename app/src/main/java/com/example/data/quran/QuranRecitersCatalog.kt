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
            nameEn = "Ayman Rushdi Swaid",
            nameAr = "د. أيمن رشدي سويد",
            styleEn = "Tajweed Master • Instructional",
            styleAr = "المصحف المعلم بالتجويد",
            surahServerBaseUrl = "https://server15.mp3quran.net/a-rushdi/Almusshaf-Al-Mo-lim/",
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
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/shirzad-taher/r1/",
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
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/waleed-shamsan/r1/",
            ayahEditionId = "ar.alafasy"
        ),
        ReciterOption(
            id = "ar.muftah_alsaltany",
            nameEn = "Muftah Alsaltany",
            nameAr = "مفتاح السلطني",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/muftah-saltani/r3/"
        ),
        ReciterOption(
            id = "ar.abdulelah_bin_aoun",
            nameEn = "Abdulelah bin Aoun",
            nameAr = "عبدالإله بن عون",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulelah-aoun/r1/"
        ),
        ReciterOption(
            id = "ar.majed_al_zamil",
            nameEn = "Majed Al-Zamil",
            nameAr = "ماجد الزامل",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/majed-zamil/r1/"
        ),
        ReciterOption(
            id = "ar.maher_shakhashero",
            nameEn = "Maher Shakhashero",
            nameAr = "ماهر شخاشيرو",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/maher-shakhashiro/r1/"
        ),
        ReciterOption(
            id = "ar.mousa_bilal",
            nameEn = "Mousa Bilal",
            nameAr = "موسى بلال",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/musa-bilal/r1/"
        ),
        ReciterOption(
            id = "ar.hatem_fareed_alwaer",
            nameEn = "Hatem Fareed Alwaer",
            nameAr = "حاتم فريد الواعر",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/hatem-waer/r1/"
        ),
        ReciterOption(
            id = "ar.mahmood_al_rifai",
            nameEn = "Mahmood Al rifai",
            nameAr = "محمود الرفاعي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/mahmoud-rifai/r1/"
        ),
        ReciterOption(
            id = "ar.jamaan_alosaimi",
            nameEn = "Jamaan Alosaimi",
            nameAr = "جمعان العصيمي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/jamaan-osaimi/r1/"
        ),
        ReciterOption(
            id = "ar.yousef_bin_noah_ahmad",
            nameEn = "Yousef Bin Noah Ahmad",
            nameAr = "يوسف بن نوح أحمد",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/yousef-ahmad/r1/"
        ),
        ReciterOption(
            id = "ar.moeedh_alharthi",
            nameEn = "Moeedh Alharthi",
            nameAr = "معيض الحارثي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/moeedh-harthi/r1/"
        ),
        ReciterOption(
            id = "ar.ahmed_al_trabulsi",
            nameEn = "Ahmed Al-trabulsi",
            nameAr = "أحمد الطرابلسي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-tarabulsi/r2/"
        ),
        ReciterOption(
            id = "ar.ahmed_amer",
            nameEn = "Ahmed Amer",
            nameAr = "أحمد عامر",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-amer/r1/"
        ),
        ReciterOption(
            id = "ar.mohammed_osman_khan",
            nameEn = "Mohammed Osman Khan",
            nameAr = "محمد عثمان خان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/muhammad-khan/r1/"
        ),
        ReciterOption(
            id = "ar.abdulbadi_ghailan",
            nameEn = "Abdulbadi Ghailan",
            nameAr = "عبدالبديع غيلان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulbadi-ghailan/r1/"
        ),
        ReciterOption(
            id = "ar.yusuf_alaidroos",
            nameEn = "Yusuf ALaidroos",
            nameAr = "يوسف العيدروس",
            styleEn = "Hafs",
            styleAr = "حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/yousef-aidarous/r1/"
        ),
        ReciterOption(
            id = "ar.hassan_aldaghriri",
            nameEn = "Hassan Aldaghriri",
            nameAr = "حسن الدغريري",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/hassan-daghriri/r1/"
        ),
        ReciterOption(
            id = "ar.alijon_qori",
            nameEn = "Alijon Qori",
            nameAr = "عليجان قوري حمدان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/alijon-hamdan/r1/"
        ),
        ReciterOption(
            id = "ar.mohammed_al_zubaidi",
            nameEn = "Mohammed Al-Zubaidi",
            nameAr = "محمد الزبيدي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/muhammad-zubaidi/r1/"
        ),
        ReciterOption(
            id = "ar.abdulrahman_aloosi",
            nameEn = "Abdulrahman Aloosi",
            nameAr = "عبدالرحمن العوسي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulrahman-oosi/r1/"
        ),
        ReciterOption(
            id = "ar.rami_aldeais",
            nameEn = "Rami Aldeais",
            nameAr = "رامي الدعيس",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/rami-duais/r1/"
        ),
        ReciterOption(
            id = "ar.abdulrahman_al_majed",
            nameEn = "Abdulrahman Al-Majed",
            nameAr = "عبدالرحمن الماجد",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulrahman-majed/r1/"
        ),
        ReciterOption(
            id = "ar.dawood_hamza",
            nameEn = "Dawood Hamza",
            nameAr = "داود حمزة",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/dawood-hamza/r1/"
        ),
        ReciterOption(
            id = "ar.ahmed_al_swailem",
            nameEn = "Ahmed Al-Swailem",
            nameAr = "أحمد السويلم",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-suwailem/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_shaheen",
            nameEn = "Ahmad Shaheen",
            nameAr = "أحمد خليل شاهين",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-shaheen/r1/"
        ),
        ReciterOption(
            id = "ar.saad_almqren",
            nameEn = "Saad Almqren",
            nameAr = "سعد المقرن",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/saad-muqrin/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_al_nufais",
            nameEn = "Ahmad Al Nufais",
            nameAr = "أحمد النفيس",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-nufais/r1/"
        ),
        ReciterOption(
            id = "ar.omar_al_darweez",
            nameEn = "Omar Al Darweez",
            nameAr = "عمر الدريويز",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/omar-duraiwiz/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_deban",
            nameEn = "Ahmad Deban",
            nameAr = "أحمد ديبان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-deeban/r1/"
        ),
        ReciterOption(
            id = "ar.nathier_almalki",
            nameEn = "Nathier Almalki",
            nameAr = "نذير المالكي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/nadhir-maliki/r1/"
        ),
        ReciterOption(
            id = "ar.haitham_aldukhain",
            nameEn = "Haitham Aldukhain",
            nameAr = "هيثم الدخين",
            styleEn = "Hafs",
            styleAr = "حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/haitham-dukhain/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_issa_al_maasaraawi",
            nameEn = "Ahmad Issa Al Maasaraawi",
            nameAr = "أحمد عيسى المعصراوي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-masarawi/r1/"
        ),
        ReciterOption(
            id = "ar.hashim_abu_dalal",
            nameEn = "Hashim Abu Dalal",
            nameAr = "هاشم أبو دلال",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/hashim-abudalal/r1/"
        ),
        ReciterOption(
            id = "ar.fouad_alkhamery",
            nameEn = "Fouad Alkhamery",
            nameAr = "فؤاد الخامري",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/fouad-khamiri/r1/"
        ),
        ReciterOption(
            id = "ar.sayed_ahmad_hashemi",
            nameEn = "Sayed Ahmad Hashemi",
            nameAr = "سيد أحمد هاشمي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/sayed-hashemi/r1/"
        ),
        ReciterOption(
            id = "ar.mal_allah_abdulrhman_aljaber",
            nameEn = "Mal-Allah Abdulrhman Aljaber",
            nameAr = "مال الله عبدالرحمن الجابر",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/malallah-jaber/r1/"
        ),
        ReciterOption(
            id = "ar.salman_alsadeiq",
            nameEn = "Salman Alsadeiq",
            nameAr = "سلمان الصديق",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/salman-siddiq/r1/"
        ),
        ReciterOption(
            id = "ar.hasan_saleh",
            nameEn = "Hasan Saleh",
            nameAr = "حسن صالح",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/hassan-saleh/r1/"
        ),
        ReciterOption(
            id = "ar.abdulrahman_alshahhat",
            nameEn = "Abdulrahman Alshahhat",
            nameAr = "عبدالرحمن الشحات",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulrahman-shahat/r1/"
        ),
        ReciterOption(
            id = "ar.issa_omar_sanankoua",
            nameEn = "Issa Omar Sanankoua",
            nameAr = "عيسى عمر سناكو",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/issa-sanankoua/r1/"
        ),
        ReciterOption(
            id = "ar.saleh_alquraishi",
            nameEn = "Saleh Alquraishi",
            nameAr = "صالح القريشي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/saleh-quraishi/r1/"
        ),
        ReciterOption(
            id = "ar.saleh_alshamrani",
            nameEn = "Saleh Alshamrani",
            nameAr = "صالح الشمراني",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/saleh-shamrani/r1/"
        ),
        ReciterOption(
            id = "ar.faisal_al_hajry",
            nameEn = "Faisal Al-Hajry",
            nameAr = "فيصل الهاجري",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/faisal-hajri/r1/"
        ),
        ReciterOption(
            id = "ar.anas_alemadi",
            nameEn = "Anas Alemadi",
            nameAr = "أنس العمادي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/anas-emadi/r1/"
        ),
        ReciterOption(
            id = "ar.sayeed_ramadan",
            nameEn = "Sayeed Ramadan",
            nameAr = "سيد رمضان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/sayed-ramadan/r1/"
        ),
        ReciterOption(
            id = "ar.saleh_alsahood",
            nameEn = "Saleh Alsahood",
            nameAr = "صالح الصاهود",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/saleh-sahood/r1/"
        ),
        ReciterOption(
            id = "ar.saleh_al_habdan",
            nameEn = "Saleh Al-Habdan",
            nameAr = "صالح الهبدان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/saleh-habdan/r1/"
        ),
        ReciterOption(
            id = "ar.salah_alhashim",
            nameEn = "Salah Alhashim",
            nameAr = "صلاح الهاشم",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/salah-hashim/r1/"
        ),
        ReciterOption(
            id = "ar.mukhtar_al_haj",
            nameEn = "Mukhtar Al-Haj",
            nameAr = "مختار الحاج",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/mukhtar-haj/r1/"
        ),
        ReciterOption(
            id = "ar.adel_ryyan",
            nameEn = "Adel Ryyan",
            nameAr = "عادل ريان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/adel-rayyan/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_al_hawashi",
            nameEn = "Ahmad Al-Hawashi",
            nameAr = "أحمد الحواشي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-hawashi/r1/"
        ),
        ReciterOption(
            id = "ar.abdulmohsin_al_harthy",
            nameEn = "Abdulmohsin Al-Harthy",
            nameAr = "عبدالمحسن الحارثي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulmohsen-harthi/r1/"
        ),
        ReciterOption(
            id = "ar.abdulmohsin_al_obaikan",
            nameEn = "Abdulmohsin Al-Obaikan",
            nameAr = "عبدالمحسن العبيكان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulmohsen-obaikan/r1/"
        ),
        ReciterOption(
            id = "ar.abdulhadi_kanakeri",
            nameEn = "Abdulhadi Kanakeri",
            nameAr = "عبدالهادي أحمد كناكري",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulhadi-kanakeri/r1/"
        ),
        ReciterOption(
            id = "ar.abdulwali_al_arkani",
            nameEn = "Abdulwali Al-Arkani",
            nameAr = "عبدالولي الأركاني",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/abdulwali-arkani/r1/"
        ),
        ReciterOption(
            id = "ar.ali_hajjaj_alsouasi",
            nameEn = "Ali Hajjaj Alsouasi",
            nameAr = "علي حجاج السويسي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ali-souissi/r1/"
        ),
        ReciterOption(
            id = "ar.emad_hafez",
            nameEn = "Emad Hafez",
            nameAr = "عماد زهير حافظ",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/imad-hafez/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_saber",
            nameEn = "Ahmad Saber",
            nameAr = "أحمد صابر",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-saber/r1/"
        ),
        ReciterOption(
            id = "ar.neamah_al_hassan",
            nameEn = "Neamah Al-Hassan",
            nameAr = "نعمة الحسان",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/neama-hassan/r1/"
        ),
        ReciterOption(
            id = "ar.ahmad_nauina",
            nameEn = "Ahmad Nauina",
            nameAr = "أحمد نعينع",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/ahmad-naina/r1/"
        ),
        ReciterOption(
            id = "ar.yahya_hawwa",
            nameEn = "Yahya Hawwa",
            nameAr = "يحيى حوا",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/yahya-hawwa/r1/"
        ),
        ReciterOption(
            id = "ar.yousef_alshoaey",
            nameEn = "Yousef Alshoaey",
            nameAr = "يوسف الشويعي",
            styleEn = "Murattal • Hafs",
            styleAr = "مرتل • حفص عن عاصم",
            surahServerBaseUrl = "https://cdn.mp3quran.net/audio/yousef-shoaey/r1/"
        ),
    )

    fun reciterById(id: String): ReciterOption =
        reciters.firstOrNull { it.id == id } ?: reciters.first()
}
