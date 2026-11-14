package com.example.data.quran

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class SurahEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<SurahEditionDto>
)

@JsonClass(generateAdapter = true)
data class SurahEditionDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val ayahs: List<AyahDto>
)

@JsonClass(generateAdapter = true)
data class AyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int = 1,
    val hizbQuarter: Int = 1
)

@JsonClass(generateAdapter = true)
data class PageEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<PageEditionDto>
)

@JsonClass(generateAdapter = true)
data class PageEditionDto(
    val number: Int,
    val ayahs: List<PageAyahDto>
)

@JsonClass(generateAdapter = true)
data class PageAyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int = 1,
    val surah: SurahRefDto
)

@JsonClass(generateAdapter = true)
data class SurahRefDto(
    val number: Int
)

interface QuranRemoteApi {
    @GET("v1/surah/{surahNumber}/editions/quran-uthmani,en.sahih")
    suspend fun getSurahArabicAndEnglish(
        @Path("surahNumber") surahNumber: Int
    ): SurahEditionsResponse

    @GET("v1/page/{pageNumber}/editions/quran-uthmani,en.sahih")
    suspend fun getMushafPageArabicAndEnglish(
        @Path("pageNumber") pageNumber: Int
    ): PageEditionsResponse
}
