package com.example.data.repository

import com.example.data.model.DuaaCategory
import com.example.data.model.DuaaItem

object DuaaRepository {

    val DUAA_LIST: List<DuaaItem> = listOf(
        // DISTRESS & ANXIETY
        DuaaItem(
            id = "d_distress_1",
            category = DuaaCategory.DISTRESS,
            titleEn = "Relief from Distress (Yunus's Prayer)",
            titleAr = "دعاء ذي النون لتفريج الكرب",
            arabicText = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "Laa ilaaha illaa Anta subhaanaka innee kuntu minaz-zaalimeen.",
            translation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            reference = "Surah Al-Anbiya 21:87"
        ),
        DuaaItem(
            id = "d_distress_2",
            category = DuaaCategory.DISTRESS,
            titleEn = "Anxiety and Sorrow",
            titleAr = "دعاء الهم والحزن والعجز",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allaahumma innee a'oozu bika minal-hammi wal-hazani, wal-'ajzi wal-kasali, wal-bukhli wal-jubni, wa dala'id-dayni wa ghalabatir-rijaal.",
            translation = "O Allah, I seek refuge in You from grief and sadness, from weakness and laziness, from miserliness and cowardice, from being heavily in debt and from being overpowered by men.",
            reference = "Sahih Al-Bukhari 6363"
        ),

        // FORGIVENESS
        DuaaItem(
            id = "d_forgive_1",
            category = DuaaCategory.FORGIVENESS,
            titleEn = "Supplication for Complete Forgiveness",
            titleAr = "دعاء طلب المغفرة والرحمة",
            arabicText = "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
            transliteration = "Rabbanagh-fir lee wa liwaalidayya wa lil-mu'mineena yawma yaqoomul-hisaab.",
            translation = "Our Lord, forgive me and my parents and the believers the Day the account is established.",
            reference = "Surah Ibrahim 14:41"
        ),
        DuaaItem(
            id = "d_forgive_2",
            category = DuaaCategory.FORGIVENESS,
            titleEn = "Abu Bakr's Prayer in Salah",
            titleAr = "دعاء أبي بكر في الصلاة",
            arabicText = "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ، وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
            transliteration = "Allaahumma innee zalamtu nafsee zulman katheeran, wa laa yaghfiruz-zunooba illaa Anta, faghfir lee maghfiratan min 'indika warhamnee, innaka Antal-Ghafoorur-Raheem.",
            translation = "O Allah, I have wronged myself greatly and none forgives sins except You, so grant me forgiveness from You and have mercy on me. Indeed, You are the Forgiving, the Merciful.",
            reference = "Sahih Al-Bukhari 834, Muslim 2705"
        ),

        // GRATITUDE & RIZQ
        DuaaItem(
            id = "d_gratitude_1",
            category = DuaaCategory.GRATITUDE,
            titleEn = "Good in this World and the Hereafter",
            titleAr = "جوامع الخير في الدنيا والآخرة",
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbanaa aatinaa fid-dunyaa hasanatanw wa fil-aakhirati hasanatanw wa qinaa 'azaaban-naar.",
            translation = "Our Lord, give us in this world that which is good and in the Hereafter that which is good, and save us from the punishment of the Fire.",
            reference = "Surah Al-Baqarah 2:201"
        ),
        DuaaItem(
            id = "d_gratitude_2",
            category = DuaaCategory.GRATITUDE,
            titleEn = "Solomon's Prayer of Gratitude",
            titleAr = "دعاء سليمان لشكر النعمة",
            arabicText = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ وَأَدْخِلْنِي بِرَحْمَتِكَ فِي عِبَادِكَ الصَّالِحِينَ",
            transliteration = "Rabbi awzi'neee an ashkura ni'matakal-lateee an'amta 'alayya wa 'alaa waalidayya wa an a'mala saalihan tardaahu wa adkhilnee birahmatika fee 'ibaadikas-saaliheen.",
            translation = "My Lord, enable me to be grateful for Your favor which You have bestowed upon me and upon my parents and to do righteousness of which You approve, and admit me by Your mercy into [the ranks of] Your righteous servants.",
            reference = "Surah An-Naml 27:19"
        ),

        // TRAVEL
        DuaaItem(
            id = "d_travel_1",
            category = DuaaCategory.TRAVEL,
            titleEn = "Duaa for Boarding a Vehicle / Travel",
            titleAr = "دعاء ركوب الدابة والسفر",
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            transliteration = "Subhaanal-lazee sakh-khara lanaa haazaa wa maa kunnaa lahoo muqrineen, wa innaaa ilaa Rabbinaa lamunqaliboon.",
            translation = "Exalted is He who has subjected this to us, and we could not have [otherwise] subdued it. And indeed we, to our Lord, will return.",
            reference = "Surah Az-Zukhruf 43:13-14"
        ),

        // HEALTH & HEALING
        DuaaItem(
            id = "d_health_1",
            category = DuaaCategory.HEALTH,
            titleEn = "Ayyoob's Supplication in Illness",
            titleAr = "دعاء أيوب عليه السلام للشفاء",
            arabicText = "أَنِّي مَسَّنِيَ الضُّرُّ وَأَنتَ أَرْحَمُ الرَّاحِمِينَ",
            transliteration = "Annee massaniyad-durru wa Anta Arhamur-raahimeen.",
            translation = "Indeed, adversity has touched me, and You are the Most Merciful of the merciful.",
            reference = "Surah Al-Anbiya 21:83"
        ),
        DuaaItem(
            id = "d_health_2",
            category = DuaaCategory.HEALTH,
            titleEn = "The Prophet's Ruqyah for the Sick",
            titleAr = "دعاء النبي ﷺ لعيادة المريض",
            arabicText = "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَاسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لاَ شِفَاءَ إِلاَّ شِفَاؤُكَ، شِفَاءً لاَ يُغَادِرُ سَقَمًا",
            transliteration = "Allaahumma Rabban-naasi azhibil-baas, ishfi wa Antash-Shaafee, laa shifaaa'a illaa shifaaa'uka, shifaaa'an laa yughaadiru saqamaa.",
            translation = "O Allah, Lord of mankind, remove the hardship and heal him, for You are the Healer. There is no healing except Your healing, a healing that leaves behind no disease.",
            reference = "Sahih Al-Bukhari 5742, Muslim 2191"
        ),

        // ISTIKHARA & GUIDANCE
        DuaaItem(
            id = "d_istikhara_1",
            category = DuaaCategory.GUIDANCE,
            titleEn = "Duaa al-Istikhara (Seeking Guidance)",
            titleAr = "دعاء صلاة الاستخارة",
            arabicText = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلاَ أَقْدِرُ، وَتَعْلَمُ وَلاَ أَعْلَمُ، وَأَنْتَ عَلاَّمُ الْغُيُوبِ",
            transliteration = "Allaahumma innee astakheeruka bi'ilmika, wa astaqdiruka biqudratika, wa as'aluka min fadlikal-'azeem, fa-innaka taqdiru wa laa aqdir, wa ta'lamu wa laa a'lam, wa Anta 'Allaamul-ghuyoob...",
            translation = "O Allah, I consult You through Your knowledge, and I seek ability through Your power, and I ask You from Your immense favor. For You have power and I lack power, and You know and I know not, and You are the Knower of the unseen...",
            reference = "Sahih Al-Bukhari 1162"
        ),

        // PARENTS & FAMILY
        DuaaItem(
            id = "d_family_1",
            category = DuaaCategory.FAMILY,
            titleEn = "Supplication for Parents",
            titleAr = "دعاء بر الوالدين",
            arabicText = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir-hamhumaa kamaa rabbayaanee sagheeraa.",
            translation = "My Lord, have mercy upon them as they brought me up [when I was] small.",
            reference = "Surah Al-Isra 17:24"
        ),
        DuaaItem(
            id = "d_family_2",
            category = DuaaCategory.FAMILY,
            titleEn = "Righteous Spouse and Offspring",
            titleAr = "دعاء صلاح الزوج والذرية",
            arabicText = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            transliteration = "Rabbanaa hab lanaa min azwaajinaa wa zurriyyaatinaa qurrata a'yuninw-waj'alnaa lil-muttaqeena imaamaa.",
            translation = "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us a leader for the righteous.",
            reference = "Surah Al-Furqan 25:74"
        )
    )

    fun searchDuaas(query: String, category: DuaaCategory = DuaaCategory.ALL): List<DuaaItem> {
        return DUAA_LIST.filter { item ->
            val matchCategory = (category == DuaaCategory.ALL || item.category == category)
            val matchQuery = query.isBlank() ||
                    item.titleEn.contains(query, ignoreCase = true) ||
                    item.titleAr.contains(query, ignoreCase = true) ||
                    item.translation.contains(query, ignoreCase = true) ||
                    item.arabicText.contains(query)
            matchCategory && matchQuery
        }
    }
}
