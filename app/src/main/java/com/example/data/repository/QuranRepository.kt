package com.example.data.repository

import android.content.Context
import com.example.NoorApplication
import com.example.data.model.Ayah
import com.example.data.model.QuranReciter
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object QuranRepository {

    val SURAHS: List<Surah> = listOf(
        Surah(1, "Al-Fatihah", "الفاتحة", "The Opener", RevelationType.MECCAN, 7, 1),
        Surah(2, "Al-Baqarah", "البقرة", "The Cow", RevelationType.MEDINAN, 286, 1),
        Surah(3, "Ali 'Imran", "آل عمران", "Family of Imran", RevelationType.MEDINAN, 200, 3),
        Surah(4, "An-Nisa", "النساء", "The Women", RevelationType.MEDINAN, 176, 4),
        Surah(5, "Al-Ma'idah", "المائدة", "The Table Spread", RevelationType.MEDINAN, 120, 6),
        Surah(6, "Al-An'am", "الأنعام", "The Cattle", RevelationType.MECCAN, 165, 7),
        Surah(7, "Al-A'raf", "الأعراف", "The Heights", RevelationType.MECCAN, 206, 8),
        Surah(8, "Al-Anfal", "الأنفال", "The Spoils of War", RevelationType.MEDINAN, 75, 9),
        Surah(9, "At-Tawbah", "التوبة", "The Repentance", RevelationType.MEDINAN, 129, 10),
        Surah(10, "Yunus", "يونس", "Jonah", RevelationType.MECCAN, 109, 11),
        Surah(11, "Hud", "هود", "Hud", RevelationType.MECCAN, 123, 11),
        Surah(12, "Yusuf", "يوسف", "Joseph", RevelationType.MECCAN, 111, 12),
        Surah(13, "Ar-Ra'd", "الرعد", "The Thunder", RevelationType.MEDINAN, 43, 13),
        Surah(14, "Ibrahim", "إبراهيم", "Abraham", RevelationType.MECCAN, 52, 13),
        Surah(15, "Al-Hijr", "الحجر", "The Rocky Tract", RevelationType.MECCAN, 99, 14),
        Surah(16, "An-Nahl", "النحل", "The Bee", RevelationType.MECCAN, 128, 14),
        Surah(17, "Al-Isra", "الإسراء", "The Night Journey", RevelationType.MECCAN, 111, 15),
        Surah(18, "Al-Kahf", "الكهف", "The Cave", RevelationType.MECCAN, 110, 15),
        Surah(19, "Maryam", "مريم", "Mary", RevelationType.MECCAN, 98, 16),
        Surah(20, "Taha", "طه", "Ta-Ha", RevelationType.MECCAN, 135, 16),
        Surah(21, "Al-Anbiya", "الأنبياء", "The Prophets", RevelationType.MECCAN, 112, 17),
        Surah(22, "Al-Hajj", "الحج", "The Pilgrimage", RevelationType.MEDINAN, 78, 17),
        Surah(23, "Al-Mu'minun", "المؤمنون", "The Believers", RevelationType.MECCAN, 118, 18),
        Surah(24, "An-Nur", "النور", "The Light", RevelationType.MEDINAN, 64, 18),
        Surah(25, "Al-Furqan", "الفرقان", "The Criterion", RevelationType.MECCAN, 77, 18),
        Surah(26, "Ash-Shu'ara", "الشعراء", "The Poets", RevelationType.MECCAN, 227, 19),
        Surah(27, "An-Naml", "النمل", "The Ant", RevelationType.MECCAN, 93, 19),
        Surah(28, "Al-Qasas", "القصص", "The Stories", RevelationType.MECCAN, 88, 20),
        Surah(29, "Al-'Ankabut", "العنكبوت", "The Spider", RevelationType.MECCAN, 69, 20),
        Surah(30, "Ar-Rum", "الروم", "The Romans", RevelationType.MECCAN, 60, 21),
        Surah(31, "Luqman", "لقمان", "Luqman", RevelationType.MECCAN, 34, 21),
        Surah(32, "As-Sajdah", "السجدة", "The Prostration", RevelationType.MECCAN, 30, 21),
        Surah(33, "Al-Ahzab", "الأحزاب", "The Combined Forces", RevelationType.MEDINAN, 73, 21),
        Surah(34, "Saba", "سبأ", "Sheba", RevelationType.MECCAN, 54, 22),
        Surah(35, "Fatir", "فاطر", "Originator", RevelationType.MECCAN, 45, 22),
        Surah(36, "Ya-Sin", "يس", "Ya-Sin", RevelationType.MECCAN, 83, 22),
        Surah(37, "As-Saffat", "الصافات", "Those Who Set The Ranks", RevelationType.MECCAN, 182, 23),
        Surah(38, "Sad", "ص", "The Letter Sad", RevelationType.MECCAN, 88, 23),
        Surah(39, "Az-Zumar", "الزمر", "The Troops", RevelationType.MECCAN, 75, 23),
        Surah(40, "Ghafir", "غافر", "The Forgiver", RevelationType.MECCAN, 85, 24),
        Surah(41, "Fussilat", "فصلت", "Explained in Detail", RevelationType.MECCAN, 54, 24),
        Surah(42, "Ash-Shura", "الشورى", "The Consultation", RevelationType.MECCAN, 53, 25),
        Surah(43, "Az-Zukhruf", "الزخرف", "The Ornaments of Gold", RevelationType.MECCAN, 89, 25),
        Surah(44, "Ad-Dukhan", "الدخان", "The Smoke", RevelationType.MECCAN, 59, 25),
        Surah(45, "Al-Jathiyah", "الجاثية", "The Crouching", RevelationType.MECCAN, 37, 25),
        Surah(46, "Al-Ahqaf", "الأحقاف", "The Wind-Curved Sandhills", RevelationType.MECCAN, 35, 26),
        Surah(47, "Muhammad", "محمد", "Muhammad", RevelationType.MEDINAN, 38, 26),
        Surah(48, "Al-Fath", "الفتح", "The Victory", RevelationType.MEDINAN, 29, 26),
        Surah(49, "Al-Hujurat", "الحجرات", "The Rooms", RevelationType.MEDINAN, 18, 26),
        Surah(50, "Qaf", "ق", "The Letter Qaf", RevelationType.MECCAN, 45, 26),
        Surah(51, "Adh-Dhariyat", "الذاريات", "The Winnowing Winds", RevelationType.MECCAN, 60, 26),
        Surah(52, "At-Tur", "الطور", "The Mount", RevelationType.MECCAN, 49, 27),
        Surah(53, "An-Najm", "النجم", "The Star", RevelationType.MECCAN, 62, 27),
        Surah(54, "Al-Qamar", "القمر", "The Moon", RevelationType.MECCAN, 55, 27),
        Surah(55, "Ar-Rahman", "الرحمن", "The Beneficent", RevelationType.MEDINAN, 78, 27),
        Surah(56, "Al-Waqi'ah", "الواقعة", "The Inevitable", RevelationType.MECCAN, 96, 27),
        Surah(57, "Al-Hadid", "الحديد", "The Iron", RevelationType.MEDINAN, 29, 27),
        Surah(58, "Al-Mujadila", "المجادلة", "The Pleading Woman", RevelationType.MEDINAN, 22, 28),
        Surah(59, "Al-Hashr", "الحشر", "The Exile", RevelationType.MEDINAN, 24, 28),
        Surah(60, "Al-Mumtahanah", "الممتحنة", "She That Is To Be Examined", RevelationType.MEDINAN, 13, 28),
        Surah(61, "As-Saf", "الصف", "The Ranks", RevelationType.MEDINAN, 14, 28),
        Surah(62, "Al-Jumu'ah", "الجمعة", "The Congregation, Friday", RevelationType.MEDINAN, 11, 28),
        Surah(63, "Al-Munafiqun", "المنافقون", "The Hypocrites", RevelationType.MEDINAN, 11, 28),
        Surah(64, "At-Taghabun", "التغابن", "The Mutual Disillusion", RevelationType.MEDINAN, 18, 28),
        Surah(65, "At-Talaq", "الطلاق", "The Divorce", RevelationType.MEDINAN, 12, 28),
        Surah(66, "At-Tahrim", "التحريم", "The Prohibition", RevelationType.MEDINAN, 12, 28),
        Surah(67, "Al-Mulk", "الملك", "The Sovereignty", RevelationType.MECCAN, 30, 29),
        Surah(68, "Al-Qalam", "القلم", "The Pen", RevelationType.MECCAN, 52, 29),
        Surah(69, "Al-Haqqah", "الحاقة", "The Reality", RevelationType.MECCAN, 52, 29),
        Surah(70, "Al-Ma'arij", "المعارج", "The Ascending Stairways", RevelationType.MECCAN, 44, 29),
        Surah(71, "Nuh", "نوح", "Noah", RevelationType.MECCAN, 28, 29),
        Surah(72, "Al-Jinn", "الجن", "The Jinn", RevelationType.MECCAN, 28, 29),
        Surah(73, "Al-Muzzammil", "المزمل", "The Enshrouded One", RevelationType.MECCAN, 20, 29),
        Surah(74, "Al-Muddaththir", "المدثر", "The Cloaked One", RevelationType.MECCAN, 56, 29),
        Surah(75, "Al-Qiyamah", "القيامة", "The Resurrection", RevelationType.MECCAN, 40, 29),
        Surah(76, "Al-Insan", "الإنسان", "The Man", RevelationType.MEDINAN, 31, 29),
        Surah(77, "Al-Mursalat", "المرسلات", "The Emissaries", RevelationType.MECCAN, 50, 29),
        Surah(78, "An-Naba", "النبأ", "The Tidings", RevelationType.MECCAN, 40, 30),
        Surah(79, "An-Nazi'at", "النازعات", "Those Who Drag Forth", RevelationType.MECCAN, 46, 30),
        Surah(80, "'Abasa", "عبس", "He Frowned", RevelationType.MECCAN, 42, 30),
        Surah(81, "At-Takwir", "التكوير", "The Overthrowing", RevelationType.MECCAN, 29, 30),
        Surah(82, "Al-Infitar", "الانفطار", "The Cleaving", RevelationType.MECCAN, 19, 30),
        Surah(83, "Al-Mutaffifin", "المطففين", "The Defrauding", RevelationType.MECCAN, 36, 30),
        Surah(84, "Al-Inshiqaq", "الانشقاق", "The Splitting Open", RevelationType.MECCAN, 25, 30),
        Surah(85, "Al-Buruj", "البروج", "The Mansions of the Stars", RevelationType.MECCAN, 22, 30),
        Surah(86, "At-Tariq", "الطارق", "The Morning Star", RevelationType.MECCAN, 17, 30),
        Surah(87, "Al-A'la", "الأعلى", "The Most High", RevelationType.MECCAN, 19, 30),
        Surah(88, "Al-Ghashiyah", "الغاشية", "The Overwhelming", RevelationType.MECCAN, 26, 30),
        Surah(89, "Al-Fajr", "الفجر", "The Dawn", RevelationType.MECCAN, 30, 30),
        Surah(90, "Al-Balad", "البلد", "The City", RevelationType.MECCAN, 20, 30),
        Surah(91, "Ash-Shams", "الشمس", "The Sun", RevelationType.MECCAN, 15, 30),
        Surah(92, "Al-Layl", "الليل", "The Night", RevelationType.MECCAN, 21, 30),
        Surah(93, "Ad-Duhaa", "الضحى", "The Morning Hours", RevelationType.MECCAN, 11, 30),
        Surah(94, "Ash-Sharh", "الشرح", "The Relief", RevelationType.MECCAN, 8, 30),
        Surah(95, "At-Tin", "التين", "The Fig", RevelationType.MECCAN, 8, 30),
        Surah(96, "Al-'Alaq", "العلق", "The Clot", RevelationType.MECCAN, 19, 30),
        Surah(97, "Al-Qadr", "القدر", "The Power", RevelationType.MECCAN, 5, 30),
        Surah(98, "Al-Bayyinah", "البينة", "The Clear Proof", RevelationType.MEDINAN, 8, 30),
        Surah(99, "Az-Zalzalah", "الزلزلة", "The Earthquake", RevelationType.MEDINAN, 8, 30),
        Surah(100, "Al-'Adiyat", "العاديات", "The Courser", RevelationType.MECCAN, 11, 30),
        Surah(101, "Al-Qari'ah", "القارعة", "The Calamity", RevelationType.MECCAN, 11, 30),
        Surah(102, "At-Takathur", "التكاثر", "The Rivalry in World Increase", RevelationType.MECCAN, 8, 30),
        Surah(103, "Al-'Asr", "العصر", "The Declining Day", RevelationType.MECCAN, 3, 30),
        Surah(104, "Al-Humazah", "الهمزة", "The Traducer", RevelationType.MECCAN, 9, 30),
        Surah(105, "Al-Fil", "الفيل", "The Elephant", RevelationType.MECCAN, 5, 30),
        Surah(106, "Quraysh", "قريش", "Quraysh", RevelationType.MECCAN, 4, 30),
        Surah(107, "Al-Ma'un", "الماعون", "The Small kindnesses", RevelationType.MECCAN, 7, 30),
        Surah(108, "Al-Kawthar", "الكوثر", "The Abundance", RevelationType.MECCAN, 3, 30),
        Surah(109, "Al-Kafirun", "الكافرون", "The Disbelievers", RevelationType.MECCAN, 6, 30),
        Surah(110, "An-Nasr", "النصر", "The Divine Support", RevelationType.MEDINAN, 3, 30),
        Surah(111, "Al-Masad", "المسد", "The Palm Fiber", RevelationType.MECCAN, 5, 30),
        Surah(112, "Al-Ikhlas", "الإخلاص", "The Sincerity", RevelationType.MECCAN, 4, 30),
        Surah(113, "Al-Falaq", "الفلق", "The Daybreak", RevelationType.MECCAN, 5, 30),
        Surah(114, "An-Nas", "الناس", "Mankind", RevelationType.MECCAN, 6, 30)
    )

    private var jsonRoot: JSONObject? = null
    private val surahCache = ConcurrentHashMap<String, List<Ayah>>()

    private val bismillahRegex = Regex("^[\\uFEFF\\u200E\\u200F\\s]*ب[َِّ]*س[ْ]*م[ِ]*\\s+[ٱا]للَّ?[ّ]*ه[ِ]*\\s+[ٱا]لرَّ?[ّ]*ح[ْ]*مَ?ٰ?ن[ِ]*\\s+[ٱا]لرَّ?[ّ]*ح[ِ]*ي[مِ]*[ِ]*[\\s،-]*")

    private fun cleanVerseText(surahNumber: Int, verseNumber: Int, rawArabic: String): String {
        if (surahNumber > 1 && verseNumber == 1) {
            val cleaned = bismillahRegex.replace(rawArabic, "").trim()
            if (cleaned.isNotEmpty()) return cleaned
        }
        return rawArabic
    }

    fun getAyahsForSurah(
        surahNumber: Int,
        context: Context? = null,
        reciter: QuranReciter = QuranReciter.ALAFASY
    ): List<Ayah> {
        val cacheKey = "$surahNumber-${reciter.id}"
        surahCache[cacheKey]?.let { return it }

        try {
            val ctx = context ?: try { NoorApplication.instance } catch (e: Exception) { null }
            val root = jsonRoot ?: run {
                if (ctx != null) {
                    val jsonStr = ctx.assets.open("quran_complete.json").bufferedReader().use { it.readText() }
                    val parsed = JSONObject(jsonStr)
                    jsonRoot = parsed
                    parsed
                } else null
            }

            val key = surahNumber.toString()
            if (root != null && root.has(key)) {
                val array = root.getJSONArray(key)
                val list = ArrayList<Ayah>(array.length())
                val surah = SURAHS.firstOrNull { it.number == surahNumber } ?: SURAHS[0]
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val vNum = item.getInt("n")
                    val arText = cleanVerseText(surahNumber, vNum, item.getString("a"))
                    val enText = item.getString("e")
                    list.add(
                        Ayah(
                            surahNumber = surahNumber,
                            verseNumber = vNum,
                            arabicText = arText,
                            englishTranslation = enText,
                            transliteration = "Ayah $vNum min Surah ${surah.nameEn}",
                            audioUrl = reciter.getAudioUrl(surahNumber, vNum)
                        )
                    )
                }
                surahCache[cacheKey] = list
                return list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return getFallbackAyahs(surahNumber, reciter)
    }

    private fun getFallbackAyahs(surahNumber: Int, reciter: QuranReciter = QuranReciter.ALAFASY): List<Ayah> {
        return when (surahNumber) {
            1 -> listOf(
                Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Bismillaahir-Rahmaanir-Raheem", reciter.getAudioUrl(1, 1)),
                Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "Alhamdu lillaahi Rabbil 'aalameen", reciter.getAudioUrl(1, 2)),
                Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Ar-Rahmaanir-Raheem", reciter.getAudioUrl(1, 3)),
                Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Maaliki Yawmid-Deen", reciter.getAudioUrl(1, 4)),
                Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "Iyyaaka na'budu wa lyyaaka nasta'een", reciter.getAudioUrl(1, 5)),
                Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Ihdinas-Siraatal-Mustaqeem", reciter.getAudioUrl(1, 6)),
                Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "Siraatal-lazeena an'amta 'alayhim ghayril-maghdoobi 'alayhim wa lad-daaaalleen", reciter.getAudioUrl(1, 7))
            )
            2 -> listOf(
                Ayah(2, 1, "الم", "Alif, Lam, Meem.", "Alif-Laaam-Meeem", "https://everyayah.com/data/Alafasy_128kbps/002001.mp3"),
                Ayah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", "Zaalikal-Kitaabu laa rayba feeh; hudal-lil-muttaqeen", "https://everyayah.com/data/Alafasy_128kbps/002002.mp3"),
                Ayah(2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", "Allazeena yu'minoona bilghaybi wa yuqeemoonas-salaata wa mimmaa razaqnaahum yunfiqoon", "https://everyayah.com/data/Alafasy_128kbps/002003.mp3"),
                Ayah(2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that could intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.", "Allahu laa ilaaha illaa Huwal-Hayyul-Qayyoom; laa ta'khuzuhu sinatunw-wa laa nawm; lahoo maa fis-samaawaati wa maa fil-ard; man zal-lazee yashfa'u 'indahoo illaa bi-iznih...", "https://everyayah.com/data/Alafasy_128kbps/002255.mp3"),
                Ayah(2, 285, "آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ ۚ وَقَالُوا سَمِعْنَا وَأَطَعْنَا ۖ غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ", "The Messenger has believed in what was revealed to him from his Lord, and [so have] the believers. All of them have believed in Allah and His angels and His books and His messengers, [saying], 'We make no distinction between any of His messengers.' And they say, 'We hear and we obey. [We seek] Your forgiveness, our Lord, and to You is the final destination.'", "Aamanar-Rasoolu bimaaa unzila ilayhi mir-Rabbihee wal-mu'minoon; kullun aamana billaahi wa malaaa'ikatihee wa Kutubihee wa Rusulihee...", "https://everyayah.com/data/Alafasy_128kbps/002285.mp3"),
                Ayah(2, 286, "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا ۚ رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنتَ مَوْلَانَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ", "Allah does not charge a soul except [with that within] its capacity. It will have [the consequence of] what [good] it has gained, and it will bear [the consequence of] what [evil] it has earned. 'Our Lord, do not impose blame upon us if we have forgotten or erred. Our Lord, and lay not upon us a burden like that which You laid upon those before us. Our Lord, and burden us not with that which we have no ability to bear. And pardon us; and forgive us; and have mercy upon us. You are our protector, so give us victory over the disbelieving people.'", "Laa yukalliful-laahu nafsan illaa wus'ahaa; lahaa maa kasabat wa 'alayhaa maktasabat; Rabbanaa laa tu'aakhiznaaa in naseenaaa aw akhtaana...", "https://everyayah.com/data/Alafasy_128kbps/002286.mp3")
            )
            18 -> listOf(
                Ayah(18, 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا", "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", "Alhamdu lillaahil-lazeee anzala 'alaa 'abdihil Kitaaba wa lam yaj'al lahoo 'iwajaa", "https://everyayah.com/data/Alafasy_128kbps/018001.mp3"),
                Ayah(18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward", "Qayyimal liyunzira ba'san shadeedam mil ladunhu wa yubashshiral mu'mineenal lazeena ya'maloonas saalihaati anna lahum ajran hasanaa", "https://everyayah.com/data/Alafasy_128kbps/018002.mp3"),
                Ayah(18, 3, "مَّاكِثِينَ فِيهِ أَبَدًا", "In which they will remain forever", "Maakitheena feehi abadaa", "https://everyayah.com/data/Alafasy_128kbps/018003.mp3"),
                Ayah(18, 10, "إِذْ أَوَى الْفِتْيَةُ إِلَى الْكَهْفِ فَقَالُوا رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا", "[Mention] when the youths retreated to the cave and said, 'Our Lord, grant us from Yourself mercy and prepare for us from our affair right guidance.'", "Iz awal fityatu ilal Kahfi faqaaloo Rabbanaaa aatinaa mil ladunka rahmatanw wa hayyi' lanaa min amrinaa rashadaa", "https://everyayah.com/data/Alafasy_128kbps/018010.mp3")
            )
            36 -> listOf(
                Ayah(36, 1, "يس", "Ya, Seen.", "Yaa-Seeen", "https://everyayah.com/data/Alafasy_128kbps/036001.mp3"),
                Ayah(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", "By the wise Qur'an,", "Wal-Qur-aanil-Hakeem", "https://everyayah.com/data/Alafasy_128kbps/036002.mp3"),
                Ayah(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Indeed you, [O Muhammad], are from among the messengers,", "Innaka laminal mursaleen", "https://everyayah.com/data/Alafasy_128kbps/036003.mp3"),
                Ayah(36, 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "On a straight path.", "'Alaa Siraatim Mustaqeem", "https://everyayah.com/data/Alafasy_128kbps/036004.mp3")
            )
            55 -> listOf(
                Ayah(55, 1, "الرَّحْمَٰنُ", "The Most Merciful", "Ar-Rahmaan", "https://everyayah.com/data/Alafasy_128kbps/055001.mp3"),
                Ayah(55, 2, "عَلَّمَ الْقُرْآنَ", "Taught the Qur'an,", "'Allamal-Qur'aan", "https://everyayah.com/data/Alafasy_128kbps/055002.mp3"),
                Ayah(55, 3, "خَلَقَ الْإِنسَانَ", "Created man,", "Khalaqal-insaan", "https://everyayah.com/data/Alafasy_128kbps/055003.mp3"),
                Ayah(55, 4, "عَلَّمَهُ الْبَيَانَ", "[And] taught him speech.", "'Allamahul-bayaan", "https://everyayah.com/data/Alafasy_128kbps/055004.mp3"),
                Ayah(55, 13, "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ", "So which of the favors of your Lord would you deny?", "Fabi-ayyi aalaaa'i Rabbikumaa tukazzibaan", "https://everyayah.com/data/Alafasy_128kbps/055013.mp3")
            )
            67 -> listOf(
                Ayah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", "Tabaarakal-lazee biyadihil-mulku wa Huwa 'alaa kulli shay'in Qadeer", "https://everyayah.com/data/Alafasy_128kbps/067001.mp3"),
                Ayah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -", "Allazee khalaqal-mawta wal-hayaata liyabluwakum ayyukum ahsanu 'amalaa; wa Huwal-'Azeezul-Ghafoor", "https://everyayah.com/data/Alafasy_128kbps/067002.mp3"),
                Ayah(67, 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", "[And] who created seven heavens in layers. You see not in the creation of the Most Merciful any inconsistency. So return [your] vision [to the sky]; do you see any breaks?", "Allazee khalaqa sab'a samaawaatin tibaaqaa; maa taraa fee khalqir-Rahmaani min tafaawut...", "https://everyayah.com/data/Alafasy_128kbps/067003.mp3")
            )
            112 -> listOf(
                Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, 'He is Allah, [who is] One,", "Qul Huwal-laahu Ahad", "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"),
                Ayah(112, 2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "Allaahus-Samad", "https://everyayah.com/data/Alafasy_128kbps/112002.mp3"),
                Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "Lam yalid wa lam yoolad", "https://everyayah.com/data/Alafasy_128kbps/112003.mp3"),
                Ayah(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.'", "Wa lam yakul-lahoo kufuwan ahad", "https://everyayah.com/data/Alafasy_128kbps/112004.mp3")
            )
            113 -> listOf(
                Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, 'I seek refuge in the Lord of daybreak", "Qul a'oozu bi Rabbil-falaq", "https://everyayah.com/data/Alafasy_128kbps/113001.mp3"),
                Ayah(113, 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "Min sharri maa khalaq", "https://everyayah.com/data/Alafasy_128kbps/113002.mp3"),
                Ayah(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "Wa min sharri ghaasiqin izaa waqab", "https://everyayah.com/data/Alafasy_128kbps/113003.mp3"),
                Ayah(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "Wa min sharrin-naffaathaati fil 'uqad", "https://everyayah.com/data/Alafasy_128kbps/113004.mp3"),
                Ayah(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.'", "Wa min sharri haasidin izaa hasad", "https://everyayah.com/data/Alafasy_128kbps/113005.mp3")
            )
            114 -> listOf(
                Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, 'I seek refuge in the Lord of mankind,", "Qul a'oozu bi Rabbin-naas", "https://everyayah.com/data/Alafasy_128kbps/114001.mp3"),
                Ayah(114, 2, "مَلِكِ النَّاسِ", "The Sovereign of mankind.", "Malikin-naas", "https://everyayah.com/data/Alafasy_128kbps/114002.mp3"),
                Ayah(114, 3, "إِلَٰهِ النَّاسِ", "The God of mankind,", "Ilaahin-naas", "https://everyayah.com/data/Alafasy_128kbps/114003.mp3"),
                Ayah(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "Min sharril-waswaasil-khannaas", "https://everyayah.com/data/Alafasy_128kbps/114004.mp3"),
                Ayah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers into the breasts of mankind -", "Allazee yuwaswisu fee sudoorin-naas", "https://everyayah.com/data/Alafasy_128kbps/114005.mp3"),
                Ayah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.'", "Minal-jinnati wan-naas", "https://everyayah.com/data/Alafasy_128kbps/114006.mp3")
            )
            else -> {
                val surah = SURAHS.firstOrNull { it.number == surahNumber } ?: SURAHS[0]
                // Generates initial verses for remaining surahs
                (1..minOf(surah.totalVerses, 5)).map { v ->
                    Ayah(
                        surahNumber = surahNumber,
                        verseNumber = v,
                        arabicText = if (v == 1 && surahNumber != 9) "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" else "آية ${v} من سورة ${surah.nameAr}",
                        englishTranslation = "Verse $v of Surah ${surah.nameEn}: Guidance and remembrance from Allah, the Lord of all worlds.",
                        transliteration = "Ayah $v min Surah ${surah.nameEn}",
                        audioUrl = reciter.getAudioUrl(surahNumber, v)
                    )
                }
            }
        }
    }
}
