package com.example.data.repository

import com.example.data.model.AzkarCategory
import com.example.data.model.AzkarItem

object AzkarRepository {

    val AZKAR_LIST: List<AzkarItem> = listOf(
        // MORNING
        AzkarItem(
            id = "m_1",
            category = AzkarCategory.MORNING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahnaa wa asbahal-mulku lillaah, walhamdu lillaah, laa ilaaha illallaahu wahdahoo laa shareeka lah, lahul-mulku wa lahul-hamd, wa huwa 'alaa kulli shay'in qadeer.",
            translation = "We have entered the morning and the kingdom belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion and to Him belongs the praise, and He is over all things omnipotent.",
            targetCount = 1,
            reference = "Sahih Muslim 2723",
            benefit = "Affirmation of tawheed and gratitude for the new day."
        ),
        AzkarItem(
            id = "m_2",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ",
            transliteration = "Allaahumma bika asbahnaa, wa bika amsaynaa, wa bika nahyaa, wa bika namootu wa ilaykan-nushoor.",
            translation = "O Allah, by You we enter the morning and by You we enter the evening, by You we live and by You we die, and to You is the resurrection.",
            targetCount = 1,
            reference = "Sunan Abi Dawud 5068, At-Tirmidhi 3391",
            benefit = "Entrusting our life and return to Allah."
        ),
        AzkarItem(
            id = "m_3",
            category = AzkarCategory.MORNING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَـهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
            transliteration = "Allaahumma Anta Rabbee laa ilaaha illaa Anta, khalaqtanee wa anaa 'abduka, wa anaa 'alaa 'ahdika wa wa'dika mastata'tu, a'oozu bika min sharri maa sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bizanbee faghfir lee fa-innahoo laa yaghfiruz-zunooba illaa Anta.",
            translation = "O Allah, You are my Lord, there is no deity worthy of worship except You. You created me and I am Your servant, and I abide by Your covenant and promise as much as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me, and I admit my sin, so forgive me, for none forgives sins except You.",
            targetCount = 1,
            reference = "Sahih Al-Bukhari 6306 (Sayyid al-Istighfar)",
            benefit = "Whoever recites it with firm belief and dies that day will enter Paradise."
        ),
        AzkarItem(
            id = "m_4",
            category = AzkarCategory.MORNING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillaahil-lazee laa yadurru ma'as-mihee shay'un fil-ardi wa laa fis-samaaa'i wa Huwas-Samee'ul-'Aleem.",
            translation = "In the name of Allah, with whose name nothing on earth or in the heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            reference = "Abu Dawood 5088, At-Tirmidhi 3388",
            benefit = "Nothing will harm him until the evening."
        ),
        AzkarItem(
            id = "m_5",
            category = AzkarCategory.MORNING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "Subhaanallaahi wa bihamdih.",
            translation = "Glory be to Allah and all praise is for Him.",
            targetCount = 100,
            reference = "Sahih Muslim 2691",
            benefit = "Sins are forgiven even if they were like the foam of the sea."
        ),

        // EVENING
        AzkarItem(
            id = "e_1",
            category = AzkarCategory.EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsaynaa wa amsal-mulku lillaah, walhamdu lillaah, laa ilaaha illallaahu wahdahoo laa shareeka lah, lahul-mulku wa lahul-hamd, wa huwa 'alaa kulli shay'in qadeer.",
            translation = "We have reached the evening and the kingdom belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner.",
            targetCount = 1,
            reference = "Sahih Muslim 2723",
            benefit = "Evening gratitude and surrender to Allah's sovereign majesty."
        ),
        AzkarItem(
            id = "e_2",
            category = AzkarCategory.EVENING,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'oozu bikalimaatil-laahit-taammaati min sharri maa khalaq.",
            translation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            targetCount = 3,
            reference = "Sahih Muslim 2709",
            benefit = "Protection from harmful creatures and poisonous stings throughout the night."
        ),
        AzkarItem(
            id = "e_3",
            category = AzkarCategory.EVENING,
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَـهَ إِلاَّ أَنْتَ",
            transliteration = "Allaahumma 'aafinee fee badanee, Allaahumma 'aafinee fee sam'ee, Allaahumma 'aafinee fee basaree, laa ilaaha illaa Anta.",
            translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight. None has the right to be worshipped except You.",
            targetCount = 3,
            reference = "Abu Dawood 5090",
            benefit = "Supplication for physical, auditory, and visual wellness."
        ),

        // POST-PRAYER
        AzkarItem(
            id = "p_1",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ. اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
            transliteration = "Astaghfirullaah (3x). Allaahumma Antas-Salaamu wa minkas-salaam, tabaarakta yaa Zal-Jalaali wal-Ikraam.",
            translation = "I ask Allah for forgiveness (3 times). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
            targetCount = 1,
            reference = "Sahih Muslim 591",
            benefit = "Recited immediately after completing obligatory prayer."
        ),
        AzkarItem(
            id = "p_2",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "Subhaanallaah",
            translation = "Glory be to Allah",
            targetCount = 33,
            reference = "Sahih Muslim 597",
            benefit = "Tasbeeh after each salah."
        ),
        AzkarItem(
            id = "p_3",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdu lillaah",
            translation = "All praise is for Allah",
            targetCount = 33,
            reference = "Sahih Muslim 597",
            benefit = "Tahmeed after each salah."
        ),
        AzkarItem(
            id = "p_4",
            category = AzkarCategory.AFTER_PRAYER,
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah is the Greatest",
            targetCount = 33,
            reference = "Sahih Muslim 597",
            benefit = "Takbeer after each salah, concluding with the testimony of faith for 100."
        ),

        // SLEEP
        AzkarItem(
            id = "s_1",
            category = AzkarCategory.SLEEP,
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbee wada'tu jambee, wa bika arfa'uh, fa-in amsakta nafsee farhamhaa, wa in arsaltahaa fahfazhaa bimaa tahfazu bihee 'ibaadakas-saaliheen.",
            translation = "In Your name my Lord, I lie down, and by Your name I rise. If You hold back my soul, then have mercy upon it, and if You release it, then protect it with what You protect Your righteous slaves.",
            targetCount = 1,
            reference = "Sahih Al-Bukhari 6320, Muslim 2714",
            benefit = "Peace of mind and soul protection through the night."
        ),
        AzkarItem(
            id = "s_2",
            category = AzkarCategory.SLEEP,
            arabicText = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
            transliteration = "Allaahumma qinee 'azaabaka yawma tab'athu 'ibaadak.",
            translation = "O Allah, protect me from Your punishment on the Day You resurrect Your servants.",
            targetCount = 3,
            reference = "Abu Dawood 5045, At-Tirmidhi 3398",
            benefit = "Placed right hand under right cheek and recited 3 times."
        ),

        // WAKING
        AzkarItem(
            id = "w_1",
            category = AzkarCategory.WAKING,
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillaahil-lazee ahyaanaa ba'da maa amaatanaa wa ilayhin-nushoor.",
            translation = "All praise is for Allah who gave us life after having caused us to die, and to Him is the resurrection.",
            targetCount = 1,
            reference = "Sahih Al-Bukhari 6312",
            benefit = "First remembrance upon opening eyes in the morning."
        ),

        // PROTECTION
        AzkarItem(
            id = "pr_1",
            category = AzkarCategory.PROTECTION,
            arabicText = "حَسْبِيَ اللَّهُ لاَ إِلَـهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyal-laahu laa ilaaha illaa Huwa 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azeem.",
            translation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            reference = "Abu Dawood 5081",
            benefit = "Allah will suffice him in what grieves him of this world and the Hereafter."
        ),
        AzkarItem(
            id = "pr_2",
            category = AzkarCategory.PROTECTION,
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            transliteration = "Yaa Hayyu yaa Qayyoomu birahmatika astagheeth, aslih lee sha'nee kullah, wa laa takilnee ilaa nafsee tarfata 'ayn.",
            translation = "O Ever-Living, O Sustainer, by Your mercy I seek assistance. Rectify for me all of my affairs and do not leave me to myself for even the blink of an eye.",
            targetCount = 1,
            reference = "Al-Hakim 1/545, Sahih At-Targhib 661",
            benefit = "Complete reliance on Allah's guidance."
        )
    )

    fun getByCategory(category: AzkarCategory): List<AzkarItem> {
        return AZKAR_LIST.filter { it.category == category }
    }
}
