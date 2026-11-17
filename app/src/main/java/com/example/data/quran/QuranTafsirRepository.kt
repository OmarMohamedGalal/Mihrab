package com.example.data.quran

import android.content.Context
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File

enum class KsuTafsirEdition(
    val ksuCode: String,
    val quranComTafsirId: Int,
    val mirrorTafseerId: Int,
    val labelAr: String,
    val labelEn: String
) {
    AR_MUYASSAR(
        ksuCode = "ar_mu",
        quranComTafsirId = 16,
        mirrorTafseerId = 1,
        labelAr = "التفسير الميسر",
        labelEn = "Al-Muyassar"
    ),
    AR_SADI(
        ksuCode = "ar_saady",
        quranComTafsirId = 91,
        mirrorTafseerId = 3,
        labelAr = "تفسير السعدي",
        labelEn = "Al-Sa'di"
    ),
    AR_KATHIR(
        ksuCode = "ar_katheer",
        quranComTafsirId = 14,
        mirrorTafseerId = 4,
        labelAr = "تفسير ابن كثير",
        labelEn = "Ibn Kathir"
    ),
    EN_SAHEEH(
        ksuCode = "en_saheeh",
        quranComTafsirId = 20,
        mirrorTafseerId = 0,
        labelAr = "الترجمة الإنجليزية",
        labelEn = "English"
    )
}

@JsonClass(generateAdapter = true)
data class QuranComTafsirResponse(
    val tafsir: QuranComTafsirBody? = null
)

@JsonClass(generateAdapter = true)
data class QuranComTafsirBody(
    val resource_id: Int = 0,
    val resource_name: String? = null,
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class QuranTafseerMirrorResponse(
    val tafseer_id: Int = 0,
    val tafseer_name: String? = null,
    val ayah_number: Int = 0,
    val text: String? = null
)

interface QuranComTafsirApi {
    @GET("api/v4/tafsirs/{tafsirId}/by_ayah/{verseKey}")
    suspend fun getTafsirByAyah(
        @Path("tafsirId") tafsirId: Int,
        @Path("verseKey") verseKey: String
    ): QuranComTafsirResponse
}

interface QuranTafseerMirrorApi {
    @GET("tafseer/{tafseerId}/{surahNumber}/{ayahNumber}")
    suspend fun getTafsirFromMirror(
        @Path("tafseerId") tafseerId: Int,
        @Path("surahNumber") surahNumber: Int,
        @Path("ayahNumber") ayahNumber: Int
    ): QuranTafseerMirrorResponse
}

class QuranTafsirRepository(
    appContext: Context,
    private val quranComTafsirApi: QuranComTafsirApi,
    private val mirrorApi: QuranTafseerMirrorApi
) {
    private val cacheRoot = File(appContext.filesDir, "quran_tafsir_v1").apply { mkdirs() }

    suspend fun getTafsir(
        surahNumber: Int,
        ayahNumber: Int,
        edition: KsuTafsirEdition,
        englishFallback: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        if (edition == KsuTafsirEdition.EN_SAHEEH) {
            return@withContext if (englishFallback.isNotBlank()) {
                Result.success(englishFallback)
            } else {
                Result.failure(IllegalStateException("English translation unavailable"))
            }
        }

        val cacheFile = File(File(cacheRoot, edition.ksuCode).apply { mkdirs() }, "${surahNumber}_${ayahNumber}.txt")
        if (cacheFile.exists() && cacheFile.length() > 4L) {
            val cached = runCatching { cacheFile.readText().trim() }.getOrNull()
            if (!cached.isNullOrBlank()) {
                return@withContext Result.success(cached)
            }
        }

        val verseKey = "$surahNumber:$ayahNumber"

        // Primary: Quran.com v4 Tafsirs (Tafsir Al-Muyassar 16 / Al-Sa'di 91 / Ibn Kathir 14)
        val primaryText = runCatching {
            val res = quranComTafsirApi.getTafsirByAyah(edition.quranComTafsirId, verseKey)
            QuranScriptFormatter.stripTranslationHtml(res.tafsir?.text.orEmpty())
        }.getOrNull()?.takeIf { it.isNotBlank() }

        if (primaryText != null) {
            runCatching { cacheFile.writeText(primaryText) }
            return@withContext Result.success(primaryText)
        }

        // Secondary: KSU / api.quran-tafseer.com Arabic Tafsir mirror
        if (edition.mirrorTafseerId > 0) {
            val mirrorText = runCatching {
                val res = mirrorApi.getTafsirFromMirror(edition.mirrorTafseerId, surahNumber, ayahNumber)
                QuranScriptFormatter.stripTranslationHtml(res.text.orEmpty())
            }.getOrNull()?.takeIf { it.isNotBlank() }

            if (mirrorText != null) {
                runCatching { cacheFile.writeText(mirrorText) }
                return@withContext Result.success(mirrorText)
            }
        }

        // Offline bundled Al-Muyassar starter entries (Al-Fatihah 1:1..7 & Al-Baqarah 2:1..7)
        if (edition == KsuTafsirEdition.AR_MUYASSAR) {
            val bundled = bundledMuyassarStarter[verseKey]
            if (!bundled.isNullOrBlank()) {
                return@withContext Result.success(bundled)
            }
        }

        Result.failure(IllegalStateException("التفسير غير متوفر دون اتصال لهذه الآية"))
    }

    companion object {
        val bundledMuyassarStarter: Map<String, String> = mapOf(
            "1:1" to "أبتدئ قراءة القرآن باسم الله مستعينًا به، (اللهِ) علم على الرب -تبارك وتعالى- المعبود بحق دون سواه، وهو أخص أسماء الله تعالى، ولا يُسمى به غيره سبحانه. (الرَّحْمَنِ) ذي الرحمة العامة الذي وسعت رحمته جميع الخلق، (الرَّحِيمِ) بالمؤمنين، وهما اسمان من أسماء الله تعالى، يتضمنان إثبات صفة الرحمة لله تعالى كما يليق بجلاله.",
            "1:2" to "الثناء على الله بصفاته التي كلُّها أوصاف كمال، وبنعمه الظاهرة والباطنة، الدينية والدنيوية، وفي ضمنه أَمْرٌ لعباده أن يحمدوه، فهو المستحق له وحده، وهو سبحانه المنشئ للخلق، القائم بأمورهم، المربي لجميع خلقه بنعمه، ولأوليائه بالإيمان والعمل الصالح.",
            "1:3" to "(الرَّحْمَنِ) الذي وسعت رحمته جميع الخلق، (الرَّحِيمِ) بالمؤمنين، وهما اسمان من أسماء الله تعالى.",
            "1:4" to "وهو سبحانه وحده مالك يوم القيامة، وهو يوم الجزاء على الأعمال. وفي قراءة المسلم لهذه الآية في كل ركعة من صلواته تذكير له باليوم الآخر، وحثٌّ له على الاستعداد بالعمل الصالح، والكف عن المعاصي والسيئات.",
            "1:5" to "إنا نخصك وحدك بالعبادة، ونستعين بك وحدك في جميع أمورنا، فالأمر كله بيدك، لا يملك منه أحد مثقال ذرة. وفي هذه الآية دليل على أن العبد لا يجوز له أن يصرف شيئًا من أنواع العبادة كالدعاء والاستغاثة والذبح والطواف إلا لله وحده.",
            "1:6" to "دُلَّنا وأرشدنا ووفقنا إلى الطريق المستقيم، وثبتنا عليه حتى نلقاك، وهو الإسلام، الذي هو الطريق الواضح الموصل إلى رضوان الله وإلى جنته، الذي دلّ عليه خاتم رسله وأنبيائه محمد صلى الله عليه وسلم، فلا سبيل إلى سعادة العبد إلا بالاستقامة عليه.",
            "1:7" to "طريق الذين أنعمت عليهم من النبيين والصدِّيقين والشهداء والصالحين، فهم أهل الهداية والاستقامة، ولا تجعلنا ممن سلك طريق المغضوب عليهم، الذين عرفوا الحق ولم يعملوا به، وهم اليهود، ومن كان على شاكلتهم، والضالين، وهم الذين لم يهتدوا، فضلوا الطريق، وهم النصارى، ومن اتبع سنتهم.",
            "2:1" to "هذه الحروف وغيرها من الحروف المقطَّعة في أوائل السور فيها إشارة إلى إعجاز القرآن؛ فقد وقع به تحدي المشركين، فعجزوا عن معارضته، وهو مركَّب من هذه الحروف التي تتكون منها لغة العرب. فدلَّ عجز العرب عن الإتيان بمثله -مع أنهم أفصح الناس- على أن القرآن وحي من الله.",
            "2:2" to "هذا القرآن هو الكتاب العظيم الذي لا شَكَّ أنه من عند الله، فلا يصح أن يرتاب فيه أحد لوضوحه، ينتفع به المتقون بالعلم النافع والعمل الصالح وهم الذين يخافون الله، ويتبعون أحكامه.",
            "2:3" to "الذين يصدِّقون بالغيب الذي لا تدركه حواسُّهم ولا عقولهم وحدها؛ لأنه لا يُعْرف إلا بوحي الله إلى رسله، مثل الإيمان بالملائكة، والجنة، والنار، وكل ما أخبر الله به أو أخبر به رسوله، والإيمان: كلمة جامعة للإقرار بالله وملائكته وكتبه ورسله واليوم الآخر والقدر خيره وشره، وتصديق الإقرار بالقول والعمل بالقلب واللسان والجوارح، وهم مع تصديقهم بالغيب يحافظون على أداء الصلاة في مواقيتها بأداء شروطها وفرائضها على الوجه المشروع في كتاب الله وسنة نبيه، ومما أعطيناهم من المال يخرجون صدقة أموالهم الواجبة والمستحبة.",
            "2:4" to "والذين يصدِّقون بما أُنزل إليك أيها الرسول من القرآن، وما أُنزل إليك من الحكمة، وهي السنة، وبكل ما أُنزل من قبلك على الرسل من كتب، كالتوراة والإنجيل وغيرهما، ويصدِّقون بدار الحياة بعد الموت وما فيها من الحساب والجزاء، تصديقًا بقلوبهم يظهر أثره على ألسنتهم وجوارحهم، وخص الآخرة؛ لأن الإيمان بها من أعظم البواعث على فعل الطاعات، واجتناب المحرمات، ومحاسبة النفس.",
            "2:5" to "أصحاب هذه الصفات يسيرون على نور من ربهم وبتوفيق من خالقهم وهاديهم، وهم الفائزون الذين أدركوا ما طلبوا، ونجوا من شر ما منه هربوا.",
            "2:6" to "إن الذين جحدوا ما أنزل إليك من ربك استكبارًا وطغيانًا، لن يقع منهم الإيمان، سواء أخوَّفتهم وحذرتهم من عذاب الله، أم تركت ذلك؛ لإصرارهم على باطلهم.",
            "2:7" to "طبع الله على قلوب هؤلاء وعلى سمعهم، وجعل على أبصارهم غطاءً؛ بسبب كفرهم وعنادهم من بعد ما تبين لهم الحق، فلم يوفقهم للهدى، ولهم عذاب شديد في نار جهنم."
        )
    }
}
