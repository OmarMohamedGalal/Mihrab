package com.example.data.quran

import com.example.data.local.QuranVerseEntity

data class SurahMeta(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val meaningEn: String,
    val ayahCount: Int,
    val isMakkan: Boolean,
    val startJuz: Int,
    val startPage: Int
)

data class JuzMeta(
    val number: Int,
    val nameAr: String,
    val startSurah: Int,
    val startAyah: Int,
    val startPage: Int
)

object QuranMetadata {
    const val TOTAL_MUSHAF_PAGES = 604

    val reciters: List<ReciterOption> = QuranRecitersCatalog.reciters

    val surahs: List<SurahMeta> = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", "The Opener", 7, true, 1, 1),
        SurahMeta(2, "البقرة", "Al-Baqarah", "The Cow", 286, false, 1, 2),
        SurahMeta(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, false, 3, 50),
        SurahMeta(4, "النساء", "An-Nisa", "The Women", 176, false, 4, 77),
        SurahMeta(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, false, 6, 106),
        SurahMeta(6, "الأنعام", "Al-An'am", "The Cattle", 165, true, 7, 128),
        SurahMeta(7, "الأعراف", "Al-A'raf", "The Heights", 206, true, 8, 151),
        SurahMeta(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, false, 9, 177),
        SurahMeta(9, "التوبة", "At-Tawbah", "The Repentance", 129, false, 10, 187),
        SurahMeta(10, "يونس", "Yunus", "Jonah", 109, true, 11, 208),
        SurahMeta(11, "هود", "Hud", "Hud", 123, true, 11, 221),
        SurahMeta(12, "يوسف", "Yusuf", "Joseph", 111, true, 12, 235),
        SurahMeta(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, false, 13, 249),
        SurahMeta(14, "إبراهيم", "Ibrahim", "Abraham", 52, true, 13, 255),
        SurahMeta(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, true, 14, 262),
        SurahMeta(16, "النحل", "An-Nahl", "The Bee", 128, true, 14, 267),
        SurahMeta(17, "الإسراء", "Al-Isra", "The Night Journey", 111, true, 15, 282),
        SurahMeta(18, "الكهف", "Al-Kahf", "The Cave", 110, true, 15, 293),
        SurahMeta(19, "مريم", "Maryam", "Mary", 98, true, 16, 305),
        SurahMeta(20, "طه", "Taha", "Ta-Ha", 135, true, 16, 312),
        SurahMeta(21, "الأنبياء", "Al-Anbya", "The Prophets", 112, true, 17, 322),
        SurahMeta(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, false, 17, 332),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, true, 18, 342),
        SurahMeta(24, "النور", "An-Nur", "The Light", 64, false, 18, 350),
        SurahMeta(25, "الفرقان", "Al-Furqan", "The Criterion", 77, true, 18, 359),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, true, 19, 367),
        SurahMeta(27, "النمل", "An-Naml", "The Ant", 93, true, 19, 377),
        SurahMeta(28, "القصص", "Al-Qasas", "The Stories", 88, true, 20, 385),
        SurahMeta(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, true, 20, 396),
        SurahMeta(30, "الروم", "Ar-Rum", "The Romans", 60, true, 21, 404),
        SurahMeta(31, "لقمان", "Luqman", "Luqman", 34, true, 21, 411),
        SurahMeta(32, "السجدة", "As-Sajdah", "The Prostration", 30, true, 21, 415),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, false, 21, 418),
        SurahMeta(34, "سبأ", "Saba", "Sheba", 54, true, 22, 428),
        SurahMeta(35, "فاطر", "Fatir", "Originator", 45, true, 22, 434),
        SurahMeta(36, "يس", "Ya-Sin", "Ya Sin", 83, true, 22, 440),
        SurahMeta(37, "الصافات", "As-Saffat", "Those Who Set the Ranks", 182, true, 23, 446),
        SurahMeta(38, "ص", "Sad", "The Letter Sad", 88, true, 23, 453),
        SurahMeta(39, "الزمر", "Az-Zumar", "The Troops", 75, true, 23, 458),
        SurahMeta(40, "غافر", "Ghafir", "The Forgiver", 85, true, 24, 467),
        SurahMeta(41, "فصلت", "Fussilat", "Explained in Detail", 54, true, 24, 477),
        SurahMeta(42, "الشورى", "Ash-Shuraa", "The Consultation", 53, true, 25, 483),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, true, 25, 489),
        SurahMeta(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, true, 25, 496),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, true, 25, 499),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, true, 26, 502),
        SurahMeta(47, "محمد", "Muhammad", "Muhammad", 38, false, 26, 507),
        SurahMeta(48, "الفتح", "Al-Fath", "The Victory", 29, false, 26, 511),
        SurahMeta(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, false, 26, 515),
        SurahMeta(50, "ق", "Qaf", "The Letter Qaf", 45, true, 26, 518),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, true, 26, 520),
        SurahMeta(52, "الطور", "At-Tur", "The Mount", 49, true, 27, 523),
        SurahMeta(53, "النجم", "An-Najm", "The Star", 62, true, 27, 526),
        SurahMeta(54, "القمر", "Al-Qamar", "The Moon", 55, true, 27, 528),
        SurahMeta(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, false, 27, 531),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, true, 27, 534),
        SurahMeta(57, "الحديد", "Al-Hadid", "The Iron", 29, false, 27, 537),
        SurahMeta(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, false, 28, 542),
        SurahMeta(59, "الحشر", "Al-Hashr", "The Exile", 24, false, 28, 545),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", "She That Is to Be Examined", 13, false, 28, 549),
        SurahMeta(61, "الصف", "As-Saf", "The Ranks", 14, false, 28, 551),
        SurahMeta(62, "الجمعة", "Al-Jumu'ah", "The Congregation", 11, false, 28, 553),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, false, 28, 554),
        SurahMeta(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, false, 28, 556),
        SurahMeta(65, "الطلاق", "At-Talaq", "The Divorce", 12, false, 28, 558),
        SurahMeta(66, "التحريم", "At-Tahrim", "The Prohibition", 12, false, 28, 560),
        SurahMeta(67, "الملك", "Al-Mulk", "The Sovereignty", 30, true, 29, 562),
        SurahMeta(68, "القلم", "Al-Qalam", "The Pen", 52, true, 29, 564),
        SurahMeta(69, "الحاقة", "Al-Haqqah", "The Reality", 52, true, 29, 566),
        SurahMeta(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, true, 29, 568),
        SurahMeta(71, "نوح", "Nuh", "Noah", 28, true, 29, 570),
        SurahMeta(72, "الجن", "Al-Jinn", "The Jinn", 28, true, 29, 572),
        SurahMeta(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, true, 29, 574),
        SurahMeta(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, true, 29, 575),
        SurahMeta(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, true, 29, 577),
        SurahMeta(76, "الإنسان", "Al-Insan", "The Man", 31, false, 29, 578),
        SurahMeta(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, true, 29, 580),
        SurahMeta(78, "النبأ", "An-Naba", "The Tidings", 40, true, 30, 582),
        SurahMeta(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", 46, true, 30, 583),
        SurahMeta(80, "عبس", "'Abasa", "He Frowned", 42, true, 30, 585),
        SurahMeta(81, "التكوير", "At-Takwir", "The Overthrowing", 29, true, 30, 586),
        SurahMeta(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, true, 30, 587),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, true, 30, 587),
        SurahMeta(84, "الانشقاق", "Al-Inshiqaq", "The Sundering", 25, true, 30, 589),
        SurahMeta(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, true, 30, 590),
        SurahMeta(86, "الطارق", "At-Tariq", "The Nightcomer", 17, true, 30, 591),
        SurahMeta(87, "الأعلى", "Al-A'la", "The Most High", 19, true, 30, 591),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, true, 30, 592),
        SurahMeta(89, "الفجر", "Al-Fajr", "The Dawn", 30, true, 30, 593),
        SurahMeta(90, "البلد", "Al-Balad", "The City", 20, true, 30, 594),
        SurahMeta(91, "الشمس", "Ash-Shams", "The Sun", 15, true, 30, 595),
        SurahMeta(92, "الليل", "Al-Layl", "The Night", 21, true, 30, 595),
        SurahMeta(93, "الضحى", "Ad-Duhaa", "The Morning Hours", 11, true, 30, 596),
        SurahMeta(94, "الشرح", "Ash-Sharh", "The Relief", 8, true, 30, 596),
        SurahMeta(95, "التين", "At-Tin", "The Fig", 8, true, 30, 597),
        SurahMeta(96, "العلق", "Al-'Alaq", "The Clot", 19, true, 30, 597),
        SurahMeta(97, "القدر", "Al-Qadr", "The Power", 5, true, 30, 598),
        SurahMeta(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, false, 30, 598),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, false, 30, 599),
        SurahMeta(100, "العاديات", "Al-'Adiyat", "The Courser", 11, true, 30, 599),
        SurahMeta(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, true, 30, 600),
        SurahMeta(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", 8, true, 30, 600),
        SurahMeta(103, "العصر", "Al-'Asr", "The Declining Day", 3, true, 30, 601),
        SurahMeta(104, "الهمزة", "Al-Humazah", "The Traducer", 9, true, 30, 601),
        SurahMeta(105, "الفيل", "Al-Fil", "The Elephant", 5, true, 30, 601),
        SurahMeta(106, "قريش", "Quraysh", "Quraysh", 4, true, 30, 602),
        SurahMeta(107, "الماعون", "Al-Ma'un", "The Small Kindnesses", 7, true, 30, 602),
        SurahMeta(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, true, 30, 602),
        SurahMeta(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, true, 30, 603),
        SurahMeta(110, "النصر", "An-Nasr", "The Divine Support", 3, false, 30, 603),
        SurahMeta(111, "المسد", "Al-Masad", "The Palm Fiber", 5, true, 30, 603),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, true, 30, 604),
        SurahMeta(113, "الفلق", "Al-Falaq", "The Daybreak", 5, true, 30, 604),
        SurahMeta(114, "الناس", "An-Nas", "Mankind", 6, true, 30, 604)
    )

    val juzList: List<JuzMeta> = listOf(
        JuzMeta(1, "الم", 1, 1, 1),
        JuzMeta(2, "سيقول", 2, 142, 22),
        JuzMeta(3, "تلك الرسل", 2, 253, 42),
        JuzMeta(4, "لن تنالوا", 3, 93, 62),
        JuzMeta(5, "والمحصنات", 4, 24, 82),
        JuzMeta(6, "لا يحب الله", 4, 148, 102),
        JuzMeta(7, "وإذا سمعوا", 5, 82, 121),
        JuzMeta(8, "ولو أننا", 6, 111, 142),
        JuzMeta(9, "قال الملأ", 7, 88, 162),
        JuzMeta(10, "واعلموا", 8, 41, 182),
        JuzMeta(11, "يعتذرون", 9, 93, 201),
        JuzMeta(12, "وما من دابة", 11, 6, 222),
        JuzMeta(13, "وما أبرئ", 12, 53, 242),
        JuzMeta(14, "ربما", 15, 1, 262),
        JuzMeta(15, "سبحان الذي", 17, 1, 282),
        JuzMeta(16, "قال ألم", 18, 75, 302),
        JuzMeta(17, "اقترب", 21, 1, 322),
        JuzMeta(18, "قد أفلح", 23, 1, 342),
        JuzMeta(19, "وقال الذين", 25, 21, 362),
        JuzMeta(20, "أمن خلق", 27, 56, 382),
        JuzMeta(21, "اتل ما أوحي", 29, 46, 402),
        JuzMeta(22, "ومن يقنت", 33, 31, 422),
        JuzMeta(23, "وما لي", 36, 28, 442),
        JuzMeta(24, "فمن أظلم", 39, 32, 462),
        JuzMeta(25, "إليه يرد", 41, 47, 482),
        JuzMeta(26, "حم", 46, 1, 502),
        JuzMeta(27, "قال فما خطبكم", 51, 31, 522),
        JuzMeta(28, "قد سمع الله", 58, 1, 542),
        JuzMeta(29, "تبارك الذي", 67, 1, 562),
        JuzMeta(30, "عم", 78, 1, 582)
    )

    fun surahByNumber(number: Int): SurahMeta = surahs.firstOrNull { it.number == number } ?: surahs.first()

    fun juzForPage(page: Int): JuzMeta =
        juzList.lastOrNull { it.startPage <= page } ?: juzList.first()

    fun surahsOnPage(page: Int): List<SurahMeta> {
        val exactStarts = surahs.filter { it.startPage == page }
        val continuing = surahs.lastOrNull { it.startPage < page }
        return buildList {
            if (continuing != null) {
                // Continuing Surah if the previous Surah hasn't already ended before this page
                val nextSurahStart = surahs.firstOrNull { it.number > continuing.number }?.startPage ?: 605
                if (nextSurahStart > page || exactStarts.isEmpty()) {
                    add(continuing)
                }
            }
            addAll(exactStarts)
        }.distinctBy { it.number }
    }

    /**
     * Bundled offline starter Surahs with exact Medina Mushaf page numbers so immediate reading
     * works offline without internet.
     */
    val bundledStarterVerses: List<QuranVerseEntity> = listOf(
        // Surah 1: Al-Fatihah (Page 1)
        QuranVerseEntity(1, 1, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", 1, 1),
        QuranVerseEntity(2, 1, 2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", 1, 1),
        QuranVerseEntity(3, 1, 3, "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", 1, 1),
        QuranVerseEntity(4, 1, 4, "مَـٰلِكِ يَوْمِ ٱلدِّينِ", "Sovereign of the Day of Recompense.", 1, 1),
        QuranVerseEntity(5, 1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", 1, 1),
        QuranVerseEntity(6, 1, 6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Guide us to the straight path -", 1, 1),
        QuranVerseEntity(7, 1, 7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", 1, 1),
        // Surah 103: Al-'Asr (Page 601)
        QuranVerseEntity(6177, 103, 1, "وَٱلْعَصْرِ", "By time,", 30, 601),
        QuranVerseEntity(6178, 103, 2, "إِنَّ ٱلْإِنسَـٰنَ لَفِى خُسْرٍ", "Indeed, mankind is in loss,", 30, 601),
        QuranVerseEntity(6179, 103, 3, "إِلَّا ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّـٰلِحَـٰتِ وَتَوَاصَوْا۟ بِٱلْحَقِّ وَتَوَاصَوْا۟ بِٱلصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", 30, 601),
        // Surah 108: Al-Kawthar (Page 602)
        QuranVerseEntity(6205, 108, 1, "إِنَّآ أَعْطَيْنَـٰكَ ٱلْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", 30, 602),
        QuranVerseEntity(6206, 108, 2, "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", 30, 602),
        QuranVerseEntity(6207, 108, 3, "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ", "Indeed, your enemy is the one cut off.", 30, 602),
        // Surah 110: An-Nasr (Page 603)
        QuranVerseEntity(6214, 110, 1, "إِذَا جَآءَ نَصْرُ ٱللَّهِ وَٱلْفَتْحُ", "When the victory of Allah has come and the conquest,", 30, 603),
        QuranVerseEntity(6215, 110, 2, "وَرَأَيْتَ ٱلنَّاسَ يَدْخُلُونَ فِى دِينِ ٱللَّهِ أَفْوَاجًا", "And you see the people entering into the religion of Allah in multitudes,", 30, 603),
        QuranVerseEntity(6216, 110, 3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَٱسْتَغْفِرْهُ ۚ إِنَّهُۥ كَانَ تَوَّابًۢا", "Then exalt [Him] with praise of your Lord and ask Forgiveness of Him. Indeed, He is ever Accepting of repentance.", 30, 603),
        // Surah 112: Al-Ikhlas (Page 604)
        QuranVerseEntity(6222, 112, 1, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", 30, 604),
        QuranVerseEntity(6223, 112, 2, "ٱللَّهُ ٱلصَّمَدُ", "Allah, the Eternal Refuge.", 30, 604),
        QuranVerseEntity(6224, 112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", 30, 604),
        QuranVerseEntity(6225, 112, 4, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ", "Nor is there to Him any equivalent.\"", 30, 604),
        // Surah 113: Al-Falaq (Page 604)
        QuranVerseEntity(6226, 113, 1, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", 30, 604),
        QuranVerseEntity(6227, 113, 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", 30, 604),
        QuranVerseEntity(6228, 113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", 30, 604),
        QuranVerseEntity(6229, 113, 4, "وَمِن شَرِّ ٱلنَّفَّـٰثَـٰتِ فِى ٱلْعُقَدِ", "And from the evil of the blowers in knots", 30, 604),
        QuranVerseEntity(6230, 113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", 30, 604),
        // Surah 114: An-Nas (Page 604)
        QuranVerseEntity(6231, 114, 1, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", 30, 604),
        QuranVerseEntity(6232, 114, 2, "مَلِكِ ٱلنَّاسِ", "The Sovereign of mankind,", 30, 604),
        QuranVerseEntity(6233, 114, 3, "إِلَـٰهِ ٱلنَّاسِ", "The God of mankind,", 30, 604),
        QuranVerseEntity(6234, 114, 4, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "From the evil of the retreating whisperer -", 30, 604),
        QuranVerseEntity(6235, 114, 5, "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ", "Who whispers [evil] into the breasts of mankind -", 30, 604),
        QuranVerseEntity(6236, 114, 6, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "From among the jinn and mankind.\"", 30, 604)
    )
}
