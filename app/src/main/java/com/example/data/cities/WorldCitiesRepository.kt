package com.example.data.cities

import com.example.domain.prayer.CalculationMethod

data class CityOption(
    val nameEn: String,
    val nameAr: String,
    val countryEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val suggestedMethod: CalculationMethod = CalculationMethod.MWL
)

object WorldCitiesRepository {
    val cities: List<CityOption> = listOf(
        CityOption("Makkah", "مكة المكرمة", "Saudi Arabia", "السعودية", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityOption("Madinah", "المدينة المنورة", "Saudi Arabia", "السعودية", 24.4672, 39.6111, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityOption("Riyadh", "الرياض", "Saudi Arabia", "السعودية", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityOption("Jeddah", "جدة", "Saudi Arabia", "السعودية", 21.5433, 39.1728, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityOption("Dammam", "الدمام", "Saudi Arabia", "السعودية", 26.4207, 50.0888, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityOption("Dubai", "دبي", "UAE", "الإمارات", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.DUBAI),
        CityOption("Abu Dhabi", "أبوظبي", "UAE", "الإمارات", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.DUBAI),
        CityOption("Doha", "الدوحة", "Qatar", "قطر", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.QATAR),
        CityOption("Kuwait City", "الكويت", "Kuwait", "الكويت", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.KUWAIT),
        CityOption("Manama", "المنامة", "Bahrain", "البحرين", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.MWL),
        CityOption("Muscat", "مسقط", "Oman", "عُمان", 23.5880, 58.3829, "Asia/Muscat", CalculationMethod.MWL),
        CityOption("Jerusalem", "القدس", "Palestine", "فلسطين", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.EGYPT),
        CityOption("Amman", "عمّان", "Jordan", "الأردن", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.EGYPT),
        CityOption("Cairo", "القاهرة", "Egypt", "مصر", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPT),
        CityOption("Alexandria", "الإسكندرية", "Egypt", "مصر", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPT),
        CityOption("Damascus", "دمشق", "Syria", "سوريا", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.EGYPT),
        CityOption("Beirut", "بيروت", "Lebanon", "لبنان", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.EGYPT),
        CityOption("Baghdad", "بغداد", "Iraq", "العراق", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MWL),
        CityOption("Sana'a", "صنعاء", "Yemen", "اليمن", 15.3694, 44.1910, "Asia/Aden", CalculationMethod.UMM_AL_QURA),
        CityOption("Khartoum", "الخرطوم", "Sudan", "السودان", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPT),
        CityOption("Tunis", "تونس", "Tunisia", "تونس", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MWL),
        CityOption("Algiers", "الجزائر", "Algeria", "الجزائر", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MWL),
        CityOption("Casablanca", "الدار البيضاء", "Morocco", "المغرب", 33.5731, -7.5898, "Africa/Casablanca", CalculationMethod.MWL),
        CityOption("Rabat", "الرباط", "Morocco", "المغرب", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MWL),
        CityOption("Tripoli", "طرابلس", "Libya", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.EGYPT),
        CityOption("Istanbul", "إسطنبول", "Turkey", "تركيا", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.TURKEY),
        CityOption("Ankara", "أنقرة", "Turkey", "تركيا", 39.9334, 32.8597, "Europe/Istanbul", CalculationMethod.TURKEY),
        CityOption("Karachi", "كراتشي", "Pakistan", "باكستان", 24.8607, 67.0011, "Asia/Karachi", CalculationMethod.KARACHI),
        CityOption("Lahore", "لاهور", "Pakistan", "باكستان", 31.5204, 74.3587, "Asia/Karachi", CalculationMethod.KARACHI),
        CityOption("Islamabad", "إسلام آباد", "Pakistan", "باكستان", 33.6844, 73.0479, "Asia/Karachi", CalculationMethod.KARACHI),
        CityOption("Dhaka", "دكا", "Bangladesh", "بنغلاديش", 23.8103, 90.4125, "Asia/Dhaka", CalculationMethod.KARACHI),
        CityOption("New Delhi", "نيودلهي", "India", "الهند", 28.6139, 77.2090, "Asia/Kolkata", CalculationMethod.KARACHI),
        CityOption("Mumbai", "مومباي", "India", "الهند", 19.0760, 72.8777, "Asia/Kolkata", CalculationMethod.KARACHI),
        CityOption("Hyderabad", "حيدر آباد", "India", "الهند", 17.3850, 78.4867, "Asia/Kolkata", CalculationMethod.KARACHI),
        CityOption("Jakarta", "جاكرتا", "Indonesia", "إندونيسيا", -6.2088, 106.8456, "Asia/Jakarta", CalculationMethod.SINGAPORE),
        CityOption("Surabaya", "سورابايا", "Indonesia", "إندونيسيا", -7.2575, 112.7521, "Asia/Jakarta", CalculationMethod.SINGAPORE),
        CityOption("Kuala Lumpur", "كوالالمبور", "Malaysia", "ماليزيا", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethod.SINGAPORE),
        CityOption("Singapore", "سنغافورة", "Singapore", "سنغافورة", 1.3521, 103.8198, "Asia/Singapore", CalculationMethod.SINGAPORE),
        CityOption("Bandar Seri Begawan", "بندر سري بكاوان", "Brunei", "بروناي", 4.9031, 114.9398, "Asia/Brunei", CalculationMethod.SINGAPORE),
        CityOption("Kabul", "كابل", "Afghanistan", "أفغانستان", 34.5553, 69.2075, "Asia/Kabul", CalculationMethod.KARACHI),
        CityOption("Tashkent", "طشقند", "Uzbekistan", "أوزبكستان", 41.2995, 69.2401, "Asia/Tashkent", CalculationMethod.MWL),
        CityOption("Almaty", "ألماتي", "Kazakhstan", "كازاخستان", 43.2220, 76.8512, "Asia/Almaty", CalculationMethod.MWL),
        CityOption("Baku", "باكو", "Azerbaijan", "أذربيجان", 40.4093, 49.8671, "Asia/Baku", CalculationMethod.MWL),
        CityOption("London", "لندن", "United Kingdom", "المملكة المتحدة", 51.5074, -0.1278, "Europe/London", CalculationMethod.MWL),
        CityOption("Birmingham", "برمنغهام", "United Kingdom", "المملكة المتحدة", 52.4862, -1.8904, "Europe/London", CalculationMethod.MWL),
        CityOption("Manchester", "مانشستر", "United Kingdom", "المملكة المتحدة", 53.4808, -2.2426, "Europe/London", CalculationMethod.MWL),
        CityOption("Paris", "باريس", "France", "فرنسا", 48.8566, 2.3522, "Europe/Paris", CalculationMethod.UOIF),
        CityOption("Marseille", "مارسيليا", "France", "فرنسا", 43.2965, 5.3698, "Europe/Paris", CalculationMethod.UOIF),
        CityOption("Berlin", "برلين", "Germany", "ألمانيا", 52.5200, 13.4050, "Europe/Berlin", CalculationMethod.MWL),
        CityOption("Frankfurt", "فرانكفورت", "Germany", "ألمانيا", 50.1109, 8.6821, "Europe/Berlin", CalculationMethod.MWL),
        CityOption("Munich", "ميونخ", "Germany", "ألمانيا", 48.1351, 11.5820, "Europe/Berlin", CalculationMethod.MWL),
        CityOption("Amsterdam", "أمستردام", "Netherlands", "هولندا", 52.3676, 4.9041, "Europe/Amsterdam", CalculationMethod.MWL),
        CityOption("Brussels", "بروكسل", "Belgium", "بلجيكا", 50.8503, 4.3517, "Europe/Brussels", CalculationMethod.MWL),
        CityOption("Madrid", "مدريد", "Spain", "إسبانيا", 40.4168, -3.7038, "Europe/Madrid", CalculationMethod.MWL),
        CityOption("Cordoba", "قرطبة", "Spain", "إسبانيا", 37.8882, -4.7794, "Europe/Madrid", CalculationMethod.MWL),
        CityOption("Rome", "روما", "Italy", "إيطاليا", 41.9028, 12.4964, "Europe/Rome", CalculationMethod.MWL),
        CityOption("Stockholm", "ستوكهولم", "Sweden", "السويد", 59.3293, 18.0686, "Europe/Stockholm", CalculationMethod.MWL),
        CityOption("Oslo", "أوسلو", "Norway", "النرويج", 59.9139, 10.7522, "Europe/Oslo", CalculationMethod.MWL),
        CityOption("Moscow", "موسكو", "Russia", "روسيا", 55.7558, 37.6173, "Europe/Moscow", CalculationMethod.MWL),
        CityOption("Sarajevo", "سراييفو", "Bosnia", "البوسنة", 43.8563, 18.4131, "Europe/Sarajevo", CalculationMethod.MWL),
        CityOption("New York", "نيويورك", "USA", "أمريكا", 40.7128, -74.0060, "America/New_York", CalculationMethod.ISNA),
        CityOption("Los Angeles", "لوس أنجلوس", "USA", "أمريكا", 34.0522, -118.2437, "America/Los_Angeles", CalculationMethod.ISNA),
        CityOption("Chicago", "شيكاغو", "USA", "أمريكا", 41.8781, -87.6298, "America/Chicago", CalculationMethod.ISNA),
        CityOption("Houston", "هيوستن", "USA", "أمريكا", 29.7604, -95.3698, "America/Chicago", CalculationMethod.ISNA),
        CityOption("Dearborn", "ديربورن", "USA", "أمريكا", 42.3223, -83.1763, "America/Detroit", CalculationMethod.ISNA),
        CityOption("Washington D.C.", "واشنطن", "USA", "أمريكا", 38.9072, -77.0369, "America/New_York", CalculationMethod.ISNA),
        CityOption("Toronto", "تورونتو", "Canada", "كندا", 43.6532, -79.3832, "America/Toronto", CalculationMethod.ISNA),
        CityOption("Montreal", "مونتريال", "Canada", "كندا", 45.5017, -73.5673, "America/Toronto", CalculationMethod.ISNA),
        CityOption("Vancouver", "فانكوفر", "Canada", "كندا", 49.2827, -123.1207, "America/Vancouver", CalculationMethod.ISNA),
        CityOption("Sao Paulo", "ساو باولو", "Brazil", "البرازيل", -23.5505, -46.6333, "America/Sao_Paulo", CalculationMethod.MWL),
        CityOption("Buenos Aires", "بوينس آيرس", "Argentina", "الأرجنتين", -34.6037, -58.3816, "America/Argentina/Buenos_Aires", CalculationMethod.MWL),
        CityOption("Lagos", "لاغوس", "Nigeria", "نيجيريا", 6.5244, 3.3792, "Africa/Lagos", CalculationMethod.EGYPT),
        CityOption("Abuja", "أبوجا", "Nigeria", "نيجيريا", 9.0765, 7.3986, "Africa/Lagos", CalculationMethod.EGYPT),
        CityOption("Nairobi", "نيروبي", "Kenya", "كينيا", -1.2921, 36.8219, "Africa/Nairobi", CalculationMethod.MWL),
        CityOption("Johannesburg", "جوهانسبرغ", "South Africa", "جنوب أفريقيا", -26.2041, 28.0473, "Africa/Johannesburg", CalculationMethod.MWL),
        CityOption("Cape Town", "كيب تاون", "South Africa", "جنوب أفريقيا", -33.9249, 18.4241, "Africa/Johannesburg", CalculationMethod.MWL),
        CityOption("Dakar", "داكار", "Senegal", "السنغال", 14.7167, -17.4677, "Africa/Dakar", CalculationMethod.MWL),
        CityOption("Sydney", "سيدني", "Australia", "أستراليا", -33.8688, 151.2093, "Australia/Sydney", CalculationMethod.MWL),
        CityOption("Melbourne", "ملبورن", "Australia", "أستراليا", -37.8136, 144.9631, "Australia/Melbourne", CalculationMethod.MWL),
        CityOption("Tokyo", "طوكيو", "Japan", "اليابان", 35.6762, 139.6503, "Asia/Tokyo", CalculationMethod.MWL),
        CityOption("Seoul", "سيئول", "South Korea", "كوريا الجنوبية", 37.5665, 126.9780, "Asia/Seoul", CalculationMethod.MWL)
    )

    fun search(query: String): List<CityOption> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return cities
        return cities.filter {
            it.nameEn.contains(trimmed, ignoreCase = true) ||
                it.nameAr.contains(trimmed) ||
                it.countryEn.contains(trimmed, ignoreCase = true) ||
                it.countryAr.contains(trimmed)
        }
    }
}
