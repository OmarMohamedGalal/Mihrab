package com.example.data.azkar

import com.example.data.local.CustomDhikrEntity
import com.example.data.local.DhikrDao
import com.example.data.local.TasbeehStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

enum class AzkarCategory {
    MORNING, EVENING, AFTER_PRAYER, SLEEP_AND_DAILY, GENERAL
}

data class AzkarItem(
    val id: String,
    val category: AzkarCategory,
    val arabicText: String,
    val transliteration: String,
    val translationEn: String,
    val referenceEn: String,
    val referenceAr: String,
    val repeatTarget: Int
)

data class TasbeehPreset(
    val id: String,
    val arabicText: String,
    val transliteration: String,
    val translationEn: String,
    val targetCount: Int
)

data class DailyVerseItem(
    val arabicText: String,
    val translationEn: String,
    val referenceEn: String,
    val referenceAr: String
)

data class DailyHadithItem(
    val arabicText: String,
    val translationEn: String,
    val sourceEn: String,
    val sourceAr: String
)

class AzkarRepository(private val dhikrDao: DhikrDao) {

    fun observeCustomDhikrs(): Flow<List<CustomDhikrEntity>> = dhikrDao.observeCustomDhikrs()

    fun observeTodayStats(date: LocalDate = LocalDate.now()): Flow<TasbeehStatsEntity?> =
        dhikrDao.observeDayStats(date.toString())

    suspend fun saveCustomDhikr(arabic: String, meaning: String, target: Int) =
        withContext(Dispatchers.IO) {
            dhikrDao.upsertCustomDhikr(
                CustomDhikrEntity(
                    arabicText = arabic.trim(),
                    meaning = meaning.trim(),
                    targetCount = target.coerceIn(1, 10000)
                )
            )
        }

    suspend fun deleteCustomDhikr(item: CustomDhikrEntity) = withContext(Dispatchers.IO) {
        dhikrDao.deleteCustomDhikr(item)
    }

    suspend fun recordTodayTasbeehTotal(totalToday: Int, date: LocalDate = LocalDate.now()) =
        withContext(Dispatchers.IO) {
            dhikrDao.upsertDayStats(TasbeehStatsEntity(date.toString(), totalToday))
        }

    companion object {
        val tasbeehPresets: List<TasbeehPreset> get() = AzkarCatalog.tasbeehPresets
        val azkarItems: List<AzkarItem> get() = AzkarCatalog.azkarItems

        val dailyVerses = listOf(
            DailyVerseItem(
                arabicText = "فَٱذْكُرُونِىٓ أَذْكُرْكُمْ وَٱشْكُرُوا۟ لِى وَلَا تَكْفُرُونِ",
                translationEn = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
                referenceEn = "Al-Baqarah 2:152",
                referenceAr = "البقرة ١٥٢"
            ),
            DailyVerseItem(
                arabicText = "أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
                translationEn = "Unquestionably, by the remembrance of Allah hearts are assured.",
                referenceEn = "Ar-Ra'd 13:28",
                referenceAr = "الرعد ٢٨"
            ),
            DailyVerseItem(
                arabicText = "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
                translationEn = "For indeed, with hardship will be ease. Indeed, with hardship will be ease.",
                referenceEn = "Ash-Sharh 94:5-6",
                referenceAr = "الشرح ٥-٦"
            ),
            DailyVerseItem(
                arabicText = "حَافِظُوا۟ عَلَى ٱلصَّلَوَٰتِ وَٱلصَّلَوٰةِ ٱلْوُسْطَىٰ وَقُومُوا۟ لِلَّهِ قَـٰنِتِينَ",
                translationEn = "Maintain with care the obligatory prayers and stand before Allah devoutly obedient.",
                referenceEn = "Al-Baqarah 2:238",
                referenceAr = "البقرة ٢٣٨"
            ),
            DailyVerseItem(
                arabicText = "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ ۚ وَٱللَّهُ بِمَا تَعْمَلُونَ بَصِيرٌ",
                translationEn = "And He is with you wherever you are. And Allah, of what you do, is Seeing.",
                referenceEn = "Al-Hadid 57:4",
                referenceAr = "الحديد ٤"
            ),
            DailyVerseItem(
                arabicText = "لَا يُكَلِّفُ ٱللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
                translationEn = "Allah does not charge a soul except [with that within] its capacity.",
                referenceEn = "Al-Baqarah 2:286",
                referenceAr = "البقرة ٢٨٦"
            ),
            DailyVerseItem(
                arabicText = "وَمَن يَتَوَكَّلْ عَلَى ٱللَّهِ فَهُوَ حَسْبُهُۥ",
                translationEn = "And whoever relies upon Allah — then He is sufficient for him.",
                referenceEn = "At-Talaq 65:3",
                referenceAr = "الطلاق ٣"
            ),
            DailyVerseItem(
                arabicText = "إِنَّ ٱللَّهَ مَعَ ٱلصَّـٰبِرِينَ",
                translationEn = "Indeed, Allah is with the patient.",
                referenceEn = "Al-Baqarah 2:153",
                referenceAr = "البقرة ١٥٣"
            ),
            DailyVerseItem(
                arabicText = "وَقُل رَّبِّ زِدْنِى عِلْمًا",
                translationEn = "And say, \"My Lord, increase me in knowledge.\"",
                referenceEn = "Taha 20:114",
                referenceAr = "طه ١١٤"
            ),
            DailyVerseItem(
                arabicText = "رَبَّنَآ ءَاتِنَا فِى ٱلدُّنْيَا حَسَنَةً وَفِى ٱلْـَٔاخِرَةِ حَسَنَةً وَقِنَا عَذَابَ ٱلنَّارِ",
                translationEn = "Our Lord, give us good in this world and good in the Hereafter, and protect us from the Fire.",
                referenceEn = "Al-Baqarah 2:201",
                referenceAr = "البقرة ٢٠١"
            ),
            DailyVerseItem(
                arabicText = "وَإِذَا سَأَلَكَ عِبَادِى عَنِّى فَإِنِّى قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ ٱلدَّاعِ إِذَا دَعَانِ",
                translationEn = "When My servants ask you concerning Me — indeed I am near. I respond to the call of the supplicant.",
                referenceEn = "Al-Baqarah 2:186",
                referenceAr = "البقرة ١٨٦"
            ),
            DailyVerseItem(
                arabicText = "وَمَن يَتَّقِ ٱللَّهَ يَجْعَل لَّهُۥ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
                translationEn = "Whoever fears Allah — He will make for him a way out and provide for him from where he does not expect.",
                referenceEn = "At-Talaq 65:2-3",
                referenceAr = "الطلاق ٢-٣"
            ),
            DailyVerseItem(
                arabicText = "إِنَّ رَحْمَتَ ٱللَّهِ قَرِيبٌ مِّنَ ٱلْمُحْسِنِينَ",
                translationEn = "Indeed, the mercy of Allah is near to the doers of good.",
                referenceEn = "Al-A'raf 7:56",
                referenceAr = "الأعراف ٥٦"
            ),
            DailyVerseItem(
                arabicText = "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
                translationEn = "And your Lord is going to give you, and you will be satisfied.",
                referenceEn = "Ad-Duhaa 93:5",
                referenceAr = "الضحى ٥"
            ),
            DailyVerseItem(
                arabicText = "إِنَّ ٱلصَّلَوٰةَ تَنْهَىٰ عَنِ ٱلْفَحْشَآءِ وَٱلْمُنكَرِ ۗ وَلَذِكْرُ ٱللَّهِ أَكْبَرُ",
                translationEn = "Indeed, prayer prohibits immorality and wrongdoing, and the remembrance of Allah is greater.",
                referenceEn = "Al-'Ankabut 29:45",
                referenceAr = "العنكبوت ٤٥"
            ),
            DailyVerseItem(
                arabicText = "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
                translationEn = "If you are grateful, I will surely increase you [in favor].",
                referenceEn = "Ibrahim 14:7",
                referenceAr = "إبراهيم ٧"
            ),
            DailyVerseItem(
                arabicText = "قُلْ يَـٰعِبَادِىَ ٱلَّذِينَ أَسْرَفُوا۟ عَلَىٰٓ أَنفُسِهِمْ لَا تَقْنَطُوا۟ مِن رَّحْمَةِ ٱللَّهِ",
                translationEn = "Say, \"O My servants who have transgressed against themselves, do not despair of the mercy of Allah.\"",
                referenceEn = "Az-Zumar 39:53",
                referenceAr = "الزمر ٥٣"
            ),
            DailyVerseItem(
                arabicText = "حَسْبُنَا ٱللَّهُ وَنِعْمَ ٱلْوَكِيلُ",
                translationEn = "Sufficient for us is Allah, and [He is] the best Disposer of affairs.",
                referenceEn = "Ali 'Imran 3:173",
                referenceAr = "آل عمران ١٧٣"
            )
        )

        val dailyHadiths = listOf(
            DailyHadithItem(
                arabicText = "أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
                translationEn = "The most beloved of deeds to Allah are those that are most consistent, even if they are small.",
                sourceEn = "Sahih al-Bukhari 6464",
                sourceAr = "صحيح البخاري ٦٤٦٤"
            ),
            DailyHadithItem(
                arabicText = "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ فَأَكْثِرُوا الدُّعَاءَ",
                translationEn = "The closest that a servant comes to his Lord is when he is prostrating, so increase your supplication.",
                sourceEn = "Sahih Muslim 482",
                sourceAr = "صحيح مسلم ٤٨٢"
            ),
            DailyHadithItem(
                arabicText = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ ثَقِيلَتَانِ فِي الْمِيزَانِ حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                translationEn = "Two words are light on the tongue, heavy on the scale, and beloved to the Most Merciful: SubhanAllahi wa bihamdihi, SubhanAllahil-'Azim.",
                sourceEn = "Sahih al-Bukhari 6406",
                sourceAr = "صحيح البخاري ٦٤٠٦"
            ),
            DailyHadithItem(
                arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
                translationEn = "The best among you are those who learn the Quran and teach it.",
                sourceEn = "Sahih al-Bukhari 5027",
                sourceAr = "صحيح البخاري ٥٠٢٧"
            )
        )

        fun verseForDate(date: LocalDate): DailyVerseItem =
            dailyVerses[(date.dayOfYear % dailyVerses.size)]

        fun hadithForDate(date: LocalDate): DailyHadithItem =
            dailyHadiths[(date.dayOfYear % dailyHadiths.size)]
    }
}
