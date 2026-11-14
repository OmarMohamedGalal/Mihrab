package com.example.data.azkar

object AzkarCatalog {

    val tasbeehPresets = listOf(
        TasbeehPreset(
            id = "subhanallah",
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            translationEn = "Glory be to Allah",
            targetCount = 33
        ),
        TasbeehPreset(
            id = "alhamdulillah",
            arabicText = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translationEn = "All praise is due to Allah",
            targetCount = 33
        ),
        TasbeehPreset(
            id = "allahuakbar",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translationEn = "Allah is the Greatest",
            targetCount = 34
        ),
        TasbeehPreset(
            id = "tahlil",
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illAllahu wahdahu la sharika lah...",
            translationEn = "None has the right to be worshipped except Allah alone; to Him belongs dominion and praise",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "astaghfirullah",
            arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal-'Azim wa atubu ilayh",
            translationEn = "I seek forgiveness from Allah the Almighty and repent to Him",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "subhanallahi_wabihamdihi",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            translationEn = "Glory be to Allah and praise Him",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "kalimatan",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            transliteration = "SubhanAllahi wa bihamdihi, SubhanAllahil-'Azim",
            translationEn = "Two words beloved to the Most Merciful, heavy on the scale",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "baqiyat",
            arabicText = "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ",
            transliteration = "Al-Baqiyat as-Salihat",
            translationEn = "Glory be to Allah, praise be to Allah, none is worthy of worship but Allah, Allah is Greatest",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "hawqalah",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "La hawla wa la quwwata illa billah",
            translationEn = "There is no might nor power except through Allah (a treasure of Paradise)",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "salawat",
            arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            transliteration = "As-Salat al-Ibrahimiyyah",
            translationEn = "O Allah, send prayers upon Muhammad and the family of Muhammad",
            targetCount = 100
        ),
        TasbeehPreset(
            id = "hasbunallah",
            arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            transliteration = "HasbunAllahu wa ni'mal-Wakil",
            translationEn = "Sufficient for us is Allah, and He is the best Disposer of affairs",
            targetCount = 33
        )
    )

    val azkarItems = listOf(
        // ==================== MORNING AZKAR (أذكار الصباح) ====================
        AzkarItem(
            id = "m_kursi",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum...",
            translationEn = "Ayat al-Kursi: Allah — there is no deity except Him, the Ever-Living, the Sustainer of existence. Whoever recites it in the morning is protected until evening.",
            referenceEn = "Quran 2:255 • Al-Hakim 1/562",
            referenceAr = "آية الكرسي • البقرة ٢٥٥",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_quls",
            category = AzkarCategory.MORNING,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ... ۝ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ...",
            transliteration = "Surah Al-Ikhlas, Al-Falaq, and An-Nas (3 times each)",
            translationEn = "Reciting Al-Ikhlas, Al-Falaq, and An-Nas three times in the morning suffices you from everything.",
            referenceEn = "Abu Dawud 5082, Tirmidhi 3575",
            referenceAr = "المعوذات ثلاثاً • أبو داود والترمذي",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "m_sayyid",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduk...",
            translationEn = "Sayyid al-Istighfar: O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant... forgive me, for none forgives sins except You.",
            referenceEn = "Sahih al-Bukhari 6306",
            referenceAr = "سيد الاستغفار • صحيح البخاري ٦٣٠٦",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m1",
            category = AzkarCategory.MORNING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ",
            transliteration = "Asbahna wa asbahal-mulku lillah, walhamdu lillah...",
            translationEn = "We have reached the morning and all sovereignty belongs to Allah. Lord, I ask You for the good of this day and what follows it, and seek refuge from its evil.",
            referenceEn = "Sahih Muslim 2723",
            referenceAr = "صحيح مسلم ٢٧٢٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_bika",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
            transliteration = "Allahumma bika asbahna, wa bika amsayna, wa bika nahya, wa bika namutu wa ilaykan-nushur",
            translationEn = "O Allah, by You we enter the morning, by You we enter the evening, by You we live and die, and to You is the Resurrection.",
            referenceEn = "At-Tirmidhi 3391",
            referenceAr = "سنن الترمذي ٣٣٩١",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_fitrah",
            category = AzkarCategory.MORNING,
            arabicText = "أَصْبَحْنَا عَلَىٰ فِطْرَةِ الْإِسْلَامِ، وَعَلَىٰ كَلِمَةِ الْإِخْلَاصِ، وَعَلَىٰ دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَىٰ مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            transliteration = "Asbahna 'ala fitratil-Islam, wa 'ala kalimatil-ikhlas...",
            translationEn = "We rise upon the natural religion of Islam, the word of sincerity, the religion of our Prophet Muhammad ﷺ, and the way of our forefather Ibrahim.",
            referenceEn = "Ahmad 3/406 (Sahih)",
            referenceAr = "مسند أحمد",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_ushhiduka",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            transliteration = "Allahumma inni asbahtu ushhiduka wa ushhidu hamalata 'arshik...",
            translationEn = "O Allah, I have entered the morning calling You, Your Throne-bearers, Your angels, and all creation to witness that You alone are Allah and Muhammad is Your servant and messenger.",
            referenceEn = "Abu Dawud 5069",
            referenceAr = "سنن أبي داود ٥٠٦٩",
            repeatTarget = 4
        ),
        AzkarItem(
            id = "m_niamah",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            transliteration = "Allahumma ma asbaha bi min ni'matin aw bi-ahadin min khalqika faminka wahdak...",
            translationEn = "O Allah, whatever blessing has come to me or any of Your creation this morning is from You alone without partner; to You belongs all praise and gratitude.",
            referenceEn = "Abu Dawud 5073",
            referenceAr = "سنن أبي داود ٥٠٧٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_afini",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنْتَ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَٰهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari...",
            translationEn = "O Allah, grant health to my body, hearing, and sight. O Allah, I seek refuge in You from disbelief, poverty, and the punishment of the grave.",
            referenceEn = "Abu Dawud 5090",
            referenceAr = "سنن أبي داود ٥٠٩٠",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "m_afiyah",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي",
            transliteration = "Allahumma inni as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah...",
            translationEn = "O Allah, I ask You for pardon and well-being in this life and the Hereafter, in my religion, worldly affairs, family, and wealth. Conceal my faults and calm my fears.",
            referenceEn = "Abu Dawud 5074, Ibn Majah 3871",
            referenceAr = "سنن أبي داود وابن ماجه",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m4",
            category = AzkarCategory.MORNING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'...",
            translationEn = "In the name of Allah, with whose name nothing on earth or in the heaven can harm, and He is the All-Hearing, All-Knowing.",
            referenceEn = "Abu Dawud 5088, Tirmidhi 3388",
            referenceAr = "سنن أبي داود والترمذي",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "m_raditu",
            category = AzkarCategory.MORNING,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
            transliteration = "Raditu billahi rabba, wa bil-Islami dina, wa bi-Muhammadin nabiyya",
            translationEn = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
            referenceEn = "Ahmad 4/337, Abu Dawud 5072",
            referenceAr = "مسند أحمد وأبو داود",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "m3",
            category = AzkarCategory.MORNING,
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ",
            transliteration = "Ya Hayyu ya Qayyum birahmatika astaghith, aslih li sha'ni kullahu...",
            translationEn = "O Ever-Living, O Sustainer, in Your mercy I seek relief; rectify for me all of my affairs and do not leave me to myself even for the blink of an eye.",
            referenceEn = "Al-Hakim 1/545 (Hasan)",
            referenceAr = "المستدرك على الصحيحين",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "m_hasbi",
            category = AzkarCategory.MORNING,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "HasbiyAllahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azim",
            translationEn = "Sufficient for me is Allah; there is no deity except Him. Upon Him I rely, and He is the Lord of the Great Throne.",
            referenceEn = "Quran 9:129 • Abu Dawud 5081",
            referenceAr = "سنن أبي داود ٥٠٨١",
            repeatTarget = 7
        ),
        AzkarItem(
            id = "m_khalqihi",
            category = AzkarCategory.MORNING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "SubhanAllahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatih",
            translationEn = "Glory and praise be to Allah, as much as the number of His creation, His own pleasure, the weight of His Throne, and the ink of His words.",
            referenceEn = "Sahih Muslim 2726",
            referenceAr = "صحيح مسلم ٢٧٢٦",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "m2",
            category = AzkarCategory.MORNING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            translationEn = "Glory be to Allah and praise Him (sins are forgiven even if like the foam of the sea).",
            referenceEn = "Sahih Muslim 2692",
            referenceAr = "صحيح مسلم ٢٦٩٢",
            repeatTarget = 100
        ),

        // ==================== EVENING AZKAR (أذكار المساء) ====================
        AzkarItem(
            id = "e_kursi",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum...",
            translationEn = "Ayat al-Kursi: Whoever recites it in the evening is protected until morning.",
            referenceEn = "Quran 2:255 • Al-Hakim 1/562",
            referenceAr = "آية الكرسي • البقرة ٢٥٥",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e_quls",
            category = AzkarCategory.EVENING,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ... ۝ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ... ۝ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ...",
            transliteration = "Surah Al-Ikhlas, Al-Falaq, and An-Nas (3 times each)",
            translationEn = "Recite Al-Ikhlas, Al-Falaq, and An-Nas three times in the evening for complete protection.",
            referenceEn = "Abu Dawud 5082, Tirmidhi 3575",
            referenceAr = "المعوذات ثلاثاً • أبو داود والترمذي",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "e_sayyid",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Sayyid al-Istighfar: Allahumma Anta Rabbi la ilaha illa Anta...",
            translationEn = "The Master Supplication for Forgiveness — whoever recites it with certainty in the evening and dies that night enters Paradise.",
            referenceEn = "Sahih al-Bukhari 6306",
            referenceAr = "سيد الاستغفار • صحيح البخاري ٦٣٠٦",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e1",
            category = AzkarCategory.EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا",
            transliteration = "Amsayna wa amsal-mulku lillah, walhamdu lillah...",
            translationEn = "We have reached the evening and all sovereignty belongs to Allah. Lord, I ask You for the good of this night and what follows it, and seek refuge from its evil.",
            referenceEn = "Sahih Muslim 2723",
            referenceAr = "صحيح مسلم ٢٧٢٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e3",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
            transliteration = "Allahumma bika amsayna, wa bika asbahna, wa bika nahya, wa bika namutu wa ilaykal-masir",
            translationEn = "O Allah, by You we enter the evening, by You we enter the morning, by You we live, and by You we die, and to You is the final return.",
            referenceEn = "At-Tirmidhi 3391",
            referenceAr = "سنن الترمذي ٣٣٩١",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e_ushhiduka",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            transliteration = "Allahumma inni amsaytu ushhiduka wa ushhidu hamalata 'arshik...",
            translationEn = "O Allah, I have entered the evening calling You and Your creation to witness that You alone are Allah and Muhammad is Your messenger.",
            referenceEn = "Abu Dawud 5069",
            referenceAr = "سنن أبي داود ٥٠٦٩",
            repeatTarget = 4
        ),
        AzkarItem(
            id = "e_niamah",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ مَا أَمْسَىٰ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            transliteration = "Allahumma ma amsa bi min ni'matin aw bi-ahadin min khalqik...",
            translationEn = "O Allah, whatever blessing has reached me or any of Your creation this evening is from You alone; to You belongs all praise and thanks.",
            referenceEn = "Abu Dawud 5073",
            referenceAr = "سنن أبي داود ٥٠٧٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e_afiyah",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي",
            transliteration = "Allahumma inni as'alukal-'afwa wal-'afiyah fid-dunya wal-akhirah...",
            translationEn = "O Allah, I ask You for pardon and well-being in this world and the Hereafter, and protection in my faith, family, and wealth.",
            referenceEn = "Abu Dawud 5074",
            referenceAr = "سنن أبي داود ٥٠٧٤",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "e2",
            category = AzkarCategory.EVENING,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'udhu bikalimatillahit-tammati min sharri ma khalaq",
            translationEn = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            referenceEn = "Sahih Muslim 2708",
            referenceAr = "صحيح مسلم ٢٧٠٨",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "e_bismillah_harm",
            category = AzkarCategory.EVENING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'...",
            translationEn = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm.",
            referenceEn = "Abu Dawud 5088",
            referenceAr = "سنن أبي داود ٥٠٨٨",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "e_raditu",
            category = AzkarCategory.EVENING,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
            transliteration = "Raditu billahi rabba, wa bil-Islami dina, wa bi-Muhammadin nabiyya",
            translationEn = "I am pleased with Allah as Lord, Islam as religion, and Muhammad ﷺ as Prophet.",
            referenceEn = "Abu Dawud 5072",
            referenceAr = "سنن أبي داود ٥٠٧٢",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "e_hasbi",
            category = AzkarCategory.EVENING,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "HasbiyAllahu la ilaha illa Huwa, 'alayhi tawakkaltu...",
            translationEn = "Sufficient for me is Allah; there is no deity except Him. Upon Him I have relied.",
            referenceEn = "Abu Dawud 5081",
            referenceAr = "سنن أبي داود ٥٠٨١",
            repeatTarget = 7
        ),
        AzkarItem(
            id = "e_subhan",
            category = AzkarCategory.EVENING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            translationEn = "Glory be to Allah and praise Him.",
            referenceEn = "Sahih Muslim 2692",
            referenceAr = "صحيح مسلم ٢٦٩٢",
            repeatTarget = 100
        ),

        // ==================== AFTER PRAYER AZKAR (أذكار بعد الصلاة) ====================
        AzkarItem(
            id = "p1",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Astaghfirullah (3x). Allahumma Antas-Salam wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram",
            translationEn = "I seek forgiveness from Allah (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
            referenceEn = "Sahih Muslim 591",
            referenceAr = "صحيح مسلم ٥٩١",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p_tahlil",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
            transliteration = "La ilaha illAllahu wahdahu la sharika lah... Allahumma la mani'a lima a'tayt...",
            translationEn = "None has the right to be worshipped except Allah alone. O Allah, none can withhold what You give, and none can give what You withhold, nor can wealth benefit anyone against Your will.",
            referenceEn = "Bukhari 844, Muslim 593",
            referenceAr = "متفق عليه • البخاري ومسلم",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p_ainni",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "اللَّهُمَّ أَعِنِّي عَلَىٰ ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
            transliteration = "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik",
            translationEn = "O Allah, help me to remember You, to thank You, and to worship You in the best of manners.",
            referenceEn = "Abu Dawud 1522",
            referenceAr = "سنن أبي داود ١٥٢٢",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p2",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            translationEn = "Glory be to Allah (33 times after each obligatory prayer).",
            referenceEn = "Sahih Muslim 597",
            referenceAr = "صحيح مسلم ٥٩٧",
            repeatTarget = 33
        ),
        AzkarItem(
            id = "p3",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translationEn = "All praise is due to Allah (33 times after each obligatory prayer).",
            referenceEn = "Sahih Muslim 597",
            referenceAr = "صحيح مسلم ٥٩٧",
            repeatTarget = 33
        ),
        AzkarItem(
            id = "p4",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translationEn = "Allah is the Greatest (33 times after each obligatory prayer).",
            referenceEn = "Sahih Muslim 597",
            referenceAr = "صحيح مسلم ٥٩٧",
            repeatTarget = 33
        ),
        AzkarItem(
            id = "p_khatm100",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illAllahu wahdahu la sharika lah... (Completes 100)",
            translationEn = "Whoever says this to complete the hundred after prayer, their sins are forgiven even if like sea foam.",
            referenceEn = "Sahih Muslim 597",
            referenceAr = "تمام المائة • صحيح مسلم ٥٩٧",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p_kursi",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ... وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Ayat al-Kursi",
            translationEn = "Whoever recites Ayat al-Kursi immediately after every obligatory prayer, nothing stands between them and Paradise except death.",
            referenceEn = "An-Nasa'i al-Kubra 9928",
            referenceAr = "آية الكرسي • السنن الكبرى للنسائي",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p_quls",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            transliteration = "Al-Ikhlas, Al-Falaq & An-Nas (1x after Dhuhr/Asr/Isha, 3x after Fajr/Maghrib)",
            translationEn = "The Prophet ﷺ commanded reciting Al-Mu'awwidhat after every prayer.",
            referenceEn = "Abu Dawud 1523",
            referenceAr = "سنن أبي داود ١٥٢٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "p_fajr_ilm",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            transliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbalan",
            translationEn = "O Allah, I ask You for beneficial knowledge, pure wholesome provision, and accepted deeds (said after Fajr Salam).",
            referenceEn = "Ibn Majah 925",
            referenceAr = "بعد السلام من الفجر • ابن ماجه",
            repeatTarget = 1
        ),

        // ==================== SLEEP & DAILY AZKAR (أذكار النوم واليوم) ====================
        AzkarItem(
            id = "s_bismika",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translationEn = "Before sleep: In Your name, O Allah, I die and I live.",
            referenceEn = "Sahih al-Bukhari 6324",
            referenceAr = "عند النوم • صحيح البخاري ٦٣٢٤",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "s_janbi",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbi wada'tu janbi wa bika arfa'uh...",
            translationEn = "In Your name my Lord I lay my side down, and by You I raise it; if You keep my soul have mercy on it, and if You return it, protect it as You protect Your righteous servants.",
            referenceEn = "Bukhari 6320, Muslim 2714",
            referenceAr = "متفق عليه • البخاري ومسلم",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "s_aslamtu",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ",
            transliteration = "Allahumma aslamtu nafsi ilayk, wa fawwadtu amri ilayk...",
            translationEn = "O Allah, I submit my soul to You, entrust my affair to You, turn my face to You, and rely upon You in hope and awe. I believe in Your Book and Your Prophet.",
            referenceEn = "Bukhari 247, Muslim 2710",
            referenceAr = "متفق عليه • البخاري ومسلم",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "s_fatimah",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "سُبْحَانَ اللَّهِ (٣٣) • الْحَمْدُ لِلَّهِ (٣٣) • اللَّهُ أَكْبَرُ (٣٤)",
            transliteration = "SubhanAllah (33), Alhamdulillah (33), Allahu Akbar (34) before sleep",
            translationEn = "Tasbeeh of Fatimah before sleep: better for you than a servant.",
            referenceEn = "Bukhari 6318, Muslim 2727",
            referenceAr = "تسبيح النوم • البخاري ومسلم",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "s_wakeup",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdulillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur",
            translationEn = "Upon waking up: All praise is for Allah who gave us life after causing us to die, and to Him is the Resurrection.",
            referenceEn = "Sahih al-Bukhari 6312",
            referenceAr = "عند الاستيقاظ • صحيح البخاري",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "d_enter_mosque",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaftah li abwaba rahmatik",
            translationEn = "Entering the Masjid: O Allah, open for me the gates of Your mercy.",
            referenceEn = "Sahih Muslim 713",
            referenceAr = "دخول المسجد • صحيح مسلم ٧١٣",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "d_leave_mosque",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allahumma inni as'aluka min fadlik",
            translationEn = "Leaving the Masjid: O Allah, I ask You from Your bounty.",
            referenceEn = "Sahih Muslim 713",
            referenceAr = "الخروج من المسجد • صحيح مسلم",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "d_leave_home",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi, tawakkaltu 'alAllahi, wa la hawla wa la quwwata illa billah",
            translationEn = "Leaving Home: In the name of Allah, I place my trust in Allah; there is no might nor power except through Allah.",
            referenceEn = "Abu Dawud 5095, Tirmidhi 3426",
            referenceAr = "الخروج من المنزل • أبو داود والترمذي",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "d_enter_home",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَىٰ رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Rabbina tawakkalna",
            translationEn = "Entering Home: In the name of Allah we enter, in the name of Allah we leave, and upon our Lord we rely.",
            referenceEn = "Abu Dawud 5096",
            referenceAr = "دخول المنزل • أبو داود ٥٠٩٦",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "d_travel",
            category = AzkarCategory.SLEEP_AND_DAILY,
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ ۝ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            transliteration = "Subhanal-ladhi sakhkhara lana hadha wa ma kunna lahu muqrinin...",
            translationEn = "Travel Supplication: Glory to Him who subjected this to us, though we could never have controlled it ourselves, and surely to our Lord we shall return.",
            referenceEn = "Quran 43:13-14 • Muslim 1342",
            referenceAr = "دعاء السفر والركوب • صحيح مسلم",
            repeatTarget = 1
        ),

        // ==================== COMPREHENSIVE DUAS (الأدعية الجامعة) ====================
        AzkarItem(
            id = "g1",
            category = AzkarCategory.GENERAL,
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar",
            translationEn = "Our Lord, give us in this world good and in the Hereafter good, and protect us from the punishment of the Fire.",
            referenceEn = "Quran 2:201 • Sahih al-Bukhari 4522",
            referenceAr = "سورة البقرة ٢٠١ • صحيح البخاري",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g2",
            category = AzkarCategory.GENERAL,
            arabicText = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin",
            translationEn = "Dua of Prophet Yunus: There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            referenceEn = "Quran 21:87 • At-Tirmidhi 3505",
            referenceAr = "دعاء ذي النون • الأنبياء ٨٧",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "g3",
            category = AzkarCategory.GENERAL,
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliteration = "Allahumma innaka 'Afuwwun tuhibbul-'afwa fa'fu 'anni",
            translationEn = "O Allah, You are Pardoning and love to pardon, so pardon me.",
            referenceEn = "At-Tirmidhi 3513",
            referenceAr = "سنن الترمذي ٣٥١٣",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "g_steadfast",
            category = AzkarCategory.GENERAL,
            arabicText = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَىٰ دِينِكَ",
            transliteration = "Ya Muqallibal-qulub, thabbit qalbi 'ala dinik",
            translationEn = "O Turner of hearts, keep my heart steadfast upon Your religion.",
            referenceEn = "At-Tirmidhi 3522 (Sahih)",
            referenceAr = "سنن الترمذي ٣٥٢٢",
            repeatTarget = 3
        ),
        AzkarItem(
            id = "g_guidance",
            category = AzkarCategory.GENERAL,
            arabicText = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            transliteration = "Rabbana la tuzigh qulubana ba'da idh hadaytana wa hab lana min ladunka rahmah...",
            translationEn = "Our Lord, let not our hearts deviate after You have guided us, and grant us mercy from Yourself. Truly You are the Bestower.",
            referenceEn = "Quran 3:8",
            referenceAr = "سورة آل عمران ٨",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_parents",
            category = AzkarCategory.GENERAL,
            arabicText = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا • رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
            transliteration = "Rabbirhamhuma kama rabbayani saghira. Rabbanaghfir li wa liwalidayya...",
            translationEn = "My Lord, have mercy upon my parents as they raised me when I was small. Our Lord, forgive me, my parents, and the believers on the Day of Account.",
            referenceEn = "Quran 17:24 & 14:41",
            referenceAr = "الإسراء ٢٤ وإبراهيم ٤١",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_family",
            category = AzkarCategory.GENERAL,
            arabicText = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            transliteration = "Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj'alna lil-muttaqina imama",
            translationEn = "Our Lord, grant us from among our spouses and offspring comfort to our eyes and make us an example for the righteous.",
            referenceEn = "Quran 25:74",
            referenceAr = "سورة الفرقان ٧٤",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_anxiety",
            category = AzkarCategory.GENERAL,
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allahumma inni a'udhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal...",
            translationEn = "Relief from anxiety: O Allah, I seek refuge in You from worry and grief, helplessness and laziness, miserliness and cowardice, crushing debt and being overpowered.",
            referenceEn = "Sahih al-Bukhari 6369",
            referenceAr = "تفريج الهم • صحيح البخاري ٦٣٦٩",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_knowledge",
            category = AzkarCategory.GENERAL,
            arabicText = "رَّبِّ زِدْنِي عِلْمًا • اللَّهُمَّ انْفَعْنِي بِمَا عَلَّمْتَنِي، وَعَلِّمْنِي مَا يَنْفَعُنِي، وَزِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma. Allahummanfa'ni bima 'allamtani wa 'allimni ma yanfa'uni...",
            translationEn = "My Lord, increase me in knowledge. O Allah, benefit me with what You have taught me and teach me what will benefit me.",
            referenceEn = "Quran 20:114 • Tirmidhi 3599",
            referenceAr = "طه ١١٤ • سنن الترمذي",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_affairs",
            category = AzkarCategory.GENERAL,
            arabicText = "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي",
            transliteration = "Allahumma aslih li diniyal-ladhi huwa 'ismatu amri, wa aslih li dunyaya...",
            translationEn = "O Allah, set right my religion which is my safeguard, set right my worldly affairs in which is my livelihood, and set right my Hereafter to which is my return.",
            referenceEn = "Sahih Muslim 2720",
            referenceAr = "صحيح مسلم ٢٧٢٠",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_istikharah",
            category = AzkarCategory.GENERAL,
            arabicText = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ",
            transliteration = "Allahumma inni astakhiruka bi'ilmika, wa astaqdiruka biqudratik...",
            translationEn = "Dua al-Istikharah: O Allah, I seek Your guidance by Your knowledge and Your strength by Your power, and I ask You of Your immense bounty.",
            referenceEn = "Sahih al-Bukhari 1166",
            referenceAr = "دعاء الاستخارة • صحيح البخاري",
            repeatTarget = 1
        ),
        AzkarItem(
            id = "g_taqabbal",
            category = AzkarCategory.GENERAL,
            arabicText = "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ ۝ وَتُبْ عَلَيْنَا إِنَّكَ أَنتَ التَّوَّابُ الرَّحِيمُ",
            transliteration = "Rabbana taqabbal minna innaka Antas-Sami'ul-'Alim, wa tub 'alayna...",
            translationEn = "Our Lord, accept this from us; indeed You are the All-Hearing, the All-Knowing, and accept our repentance.",
            referenceEn = "Quran 2:127-128",
            referenceAr = "سورة البقرة ١٢٧-١٢٨",
            repeatTarget = 1
        )
    )
}
