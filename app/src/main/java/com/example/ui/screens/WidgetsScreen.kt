package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.CalendarRepository
import com.example.data.repository.DuaaRepository
import com.example.ui.NoorDestination
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*
import com.example.widget.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class WidgetCategory(val titleAr: String, val titleEn: String, val icon: ImageVector) {
    ALL("الكل", "All", Icons.Filled.Widgets),
    CALENDAR("التقويم والكبسولة", "Calendar & Capsules", Icons.Filled.CalendarToday),
    PRAYER("الصلاة والمواقيت", "Prayer Times", Icons.Filled.AccessTime),
    QURAN_AZKAR("القرآن والأذكار", "Quran & Azkar", Icons.Filled.MenuBook)
}

enum class CapsuleWidgetType(val titleAr: String, val titleEn: String) {
    DAY("ودجت اليوم", "Day of Week"),
    GREGORIAN("ودجت التاريخ الميلادي", "Gregorian Date"),
    HIJRI("ودجت التاريخ الهجري", "Hijri Date")
}

data class HomeScreenWidgetInfo(
    val id: String,
    val category: WidgetCategory,
    val titleAr: String,
    val titleEn: String,
    val subtitle: String,
    val dimension: String,
    val providerClass: Class<*>,
    val capsuleType: CapsuleWidgetType? = null
)

val ALL_HOMESCREEN_WIDGETS = listOf(
    // 1. CALENDAR & CAPSULES (6)
    HomeScreenWidgetInfo(
        id = "capsule_day",
        category = WidgetCategory.CALENDAR,
        titleAr = "1. ودجت اليوم • Day of Week",
        titleEn = "Capsule Day of Week",
        subtitle = "يعرض اسم يوم الأسبوع بالخط العربي الفاخر والزخرفة الكبسولية المستقلة",
        dimension = "1x2",
        providerClass = CapsuleDayWidgetProvider::class.java,
        capsuleType = CapsuleWidgetType.DAY
    ),
    HomeScreenWidgetInfo(
        id = "capsule_gregorian",
        category = WidgetCategory.CALENDAR,
        titleAr = "2. ودجت التاريخ الميلادي • Gregorian Date",
        titleEn = "Capsule Gregorian Date",
        subtitle = "يعرض رقم اليوم بتدرج مائي زجاجي واسم الشهر الميلادي بخطوط مخصصة",
        dimension = "1x2",
        providerClass = CapsuleGregorianWidgetProvider::class.java,
        capsuleType = CapsuleWidgetType.GREGORIAN
    ),
    HomeScreenWidgetInfo(
        id = "capsule_hijri",
        category = WidgetCategory.CALENDAR,
        titleAr = "3. ودجت التاريخ الهجري • Hijri Date",
        titleEn = "Capsule Hijri Date",
        subtitle = "يعرض رقم وتاريخ اليوم الهجري والشهر الأصيل بتنسيق مائل مميز",
        dimension = "1x2",
        providerClass = CapsuleHijriWidgetProvider::class.java,
        capsuleType = CapsuleWidgetType.HIJRI
    ),
    HomeScreenWidgetInfo(
        id = "hijri_card",
        category = WidgetCategory.CALENDAR,
        titleAr = "4. بطاقة التاريخ الهجري الشاملة • Hijri Date Card",
        titleEn = "Full Hijri Date Card",
        subtitle = "عرض متكامل لليوم بالتاريخ الهجري والميلادي مع زخرفة كلاسيكية",
        dimension = "4x2",
        providerClass = HijriDateWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "ramadan_countdown",
        category = WidgetCategory.CALENDAR,
        titleAr = "5. عداد الإفطار وشهر رمضان • Ramadan Countdown",
        titleEn = "Ramadan & Iftar Countdown",
        subtitle = "حساب الساعات المتبقية لموعد الإفطار اليومي والعد التنازلي للشهر الفضيل",
        dimension = "4x2",
        providerClass = RamadanCountdownWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "islamic_holidays",
        category = WidgetCategory.CALENDAR,
        titleAr = "6. عداد المناسبات والأعياد الإسلامية • Islamic Holidays",
        titleEn = "Islamic Holidays Countdown",
        subtitle = "تتبع الأيام المتبقية لعيد الفطر، عيد الأضحى، المولد النبوي ورأس السنة الهجرية",
        dimension = "4x2",
        providerClass = IslamicHolidayWidgetProvider::class.java
    ),

    // 2. PRAYER TIMES & COMPASS (7)
    HomeScreenWidgetInfo(
        id = "next_prayer_arabic",
        category = WidgetCategory.PRAYER,
        titleAr = "7. بانر الصلاة القادمة بالخط العربي • Next Prayer Banner",
        titleEn = "Next Prayer Calligraphy Banner",
        subtitle = "عرض بارز للصلاة القادمة والوقت المتبقي بأسلوب خطي إسلامي عالي التباين",
        dimension = "4x1",
        providerClass = NextPrayerArabicWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "prayer_bar",
        category = WidgetCategory.PRAYER,
        titleAr = "8. شريط مواقيت الصلاة العريض • Prayer Times Bar",
        titleEn = "Horizontal Prayer Times Bar",
        subtitle = "شريط أفقي متكامل يعرض الصلوات الخمس مع تمييز الصلاة القادمة بأناقة",
        dimension = "4x1",
        providerClass = PrayerBarWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "prayer_list",
        category = WidgetCategory.PRAYER,
        titleAr = "9. قائمة مواقيت الصلاة المدمجة • Compact Prayer List",
        titleEn = "Compact Prayer List",
        subtitle = "قائمة رأسية لجميع صلوات اليوم مع مؤشر الوقت المتبقي لكل صلاة",
        dimension = "4x2",
        providerClass = PrayerListWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "next_prayer_card",
        category = WidgetCategory.PRAYER,
        titleAr = "10. بطاقة عداد الصلاة القادمة • Next Prayer Card",
        titleEn = "Next Prayer Countdown Card",
        subtitle = "بطاقة مربعة تظهر اسم الصلاة القادمة والعد التنازلي مع اسم مدينتك",
        dimension = "2x2",
        providerClass = NextPrayerWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "full_schedule",
        category = WidgetCategory.PRAYER,
        titleAr = "11. الجدول الكامل لمواقيت الصلوات • Full Prayer Schedule",
        titleEn = "Full Prayer Schedule",
        subtitle = "جدول يومي متكامل لجميع الصلوات مع التواريخ الهجرية والميلادية",
        dimension = "4x2",
        providerClass = FullScheduleWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "prayer_streak",
        category = WidgetCategory.PRAYER,
        titleAr = "12. متابع الالتزام اليومي بالصلوات • Prayer Streak Tracker",
        titleEn = "Daily Prayer Streak Tracker",
        subtitle = "متابعة إنجاز الصلوات الخمس يومياً ونسبة الالتزام مع أزرار التأكيد السريع",
        dimension = "4x2",
        providerClass = PrayerStreakWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "qibla_compass",
        category = WidgetCategory.PRAYER,
        titleAr = "13. بوصلة القبلة الرقمية الشريفة • Digital Qibla Compass",
        titleEn = "Digital Qibla Compass",
        subtitle = "بوصلة رقمية توضح درجة اتجاه اتجاه الكعبة المشرفة بالنسبة لموقعك الحالي",
        dimension = "2x2",
        providerClass = QiblaWidgetProvider::class.java
    ),

    // 3. QURAN & AZKAR (4)
    HomeScreenWidgetInfo(
        id = "quran_verse",
        category = WidgetCategory.QURAN_AZKAR,
        titleAr = "14. آية اليوم من القرآن الكريم • Quran Verse of the Day",
        titleEn = "Quran Verse of the Day",
        subtitle = "آية قرآنية ملهمة مع الترجمة (يمكنك النقر على الودجت لتبديل الآية فوراً)",
        dimension = "4x2",
        providerClass = QuranVerseWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "quran_continue",
        category = WidgetCategory.QURAN_AZKAR,
        titleAr = "15. متابعة قراءة القرآن الكريم • Quran Continue Reading",
        titleEn = "Quran Continue Reading",
        subtitle = "تظهر آخر سورة وآية تم الوقوف عليها مع إمكانية المتابعة بلمسة واحدة",
        dimension = "4x1",
        providerClass = QuranContinueWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "azkar_counter",
        category = WidgetCategory.QURAN_AZKAR,
        titleAr = "16. عداد التسبيح السريع للأذكار • Quick Tasbeeh Counter",
        titleEn = "Quick Tasbeeh Counter",
        subtitle = "عداد أذكار تفاعلي يتيح لك التسبيح وزيادة العداد مباشرة من الشاشة الرئيسية",
        dimension = "2x2",
        providerClass = AzkarCounterWidgetProvider::class.java
    ),
    HomeScreenWidgetInfo(
        id = "duaa_of_day",
        category = WidgetCategory.QURAN_AZKAR,
        titleAr = "17. دعاء اليوم المستجاب • Duaa of the Day",
        titleEn = "Duaa of the Day",
        subtitle = "دعاء مستجاب يومي من أدعية الكتاب والسنة المطهرة مع الترجمة والتصنيف",
        dimension = "4x2",
        providerClass = DuaaOfTheDayWidgetProvider::class.java
    )
)

@Composable
fun WidgetsScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val widgetPrefs = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(WidgetCategory.ALL) }
    var activeCustomizingWidget by remember { mutableStateOf<CapsuleWidgetType?>(null) }
    var activeColorizingWidgetInfo by remember { mutableStateOf<HomeScreenWidgetInfo?>(null) }

    // Intercept back gesture to return to Prayer screen smoothly
    BackHandler {
        viewModel.setDestination(NoorDestination.PRAYER)
    }

    val today = remember { LocalDate.now() }
    val hijri = remember { CalendarRepository.getHijriDate(today, -2) }

    // Real-time rendered bitmaps for the 3 capsule widgets
    val dayBitmap = remember(refreshTrigger) {
        CapsuleWidgetRenderer.renderDayOfWeek(context, today.dayOfWeek)
    }
    val gregBitmap = remember(refreshTrigger) {
        CapsuleWidgetRenderer.renderGregorian(context, today.dayOfMonth, today.monthValue)
    }
    val hijriBitmap = remember(refreshTrigger) {
        CapsuleWidgetRenderer.renderHijri(context, hijri.day, hijri.month)
    }

    // Filter widgets by selected category
    val filteredWidgets = remember(selectedCategory) {
        if (selectedCategory == WidgetCategory.ALL) {
            ALL_HOMESCREEN_WIDGETS
        } else {
            ALL_HOMESCREEN_WIDGETS.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP APP BAR / HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setDestination(NoorDestination.PRAYER) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GlassWhiteOverlayLight)
                            .border(1.dp, GlassBorderBrushSubtle, CircleShape)
                            .testTag("widgets_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HOME SCREEN WIDGETS",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = TextMuted
                        )
                        Text(
                            text = "ودجات الشاشة الرئيسية",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                }

                IconButton(
                    onClick = {
                        WidgetUpdater.updateAllWidgets(context)
                        refreshTrigger++
                        Toast.makeText(context, "تم تحديث جميع الودجات بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassWhiteOverlayLight)
                        .border(1.dp, GlassBorderBrushSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // CATEGORIES SELECTOR AT THE TOP
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(WidgetCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("widget_cat_${cat.name.lowercase()}"),
                        color = if (isSelected) AccentGold else Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentGold else GlassBorderColorSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) VoidBlack else PureWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.titleAr,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) VoidBlack else PureWhite
                            )
                        }
                    }
                }
            }
        }

        // INTRO BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassDarkElevated)
                    .background(GlassGradientHero)
                    .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22D4AF37)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Widgets,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جميع ودجات الشاشة الرئيسية (17 ودجت)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "استعرض ودجات التطبيق، خصص ألوانها مخصصاً أو افتراضياً واضغط 'إضافة للشاشة' للتثبيت المباشر.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // LIST ALL WIDGETS IN SELECTED CATEGORY
        items(filteredWidgets, key = { it.id }) { widgetInfo ->
            val savedColorInt = remember(refreshTrigger) {
                widgetPrefs.getInt("widget_color_${widgetInfo.id}", -1)
            }
            WidgetCardItem(
                info = widgetInfo,
                customColorInt = savedColorInt,
                dayBitmap = dayBitmap,
                gregBitmap = gregBitmap,
                hijriBitmap = hijriBitmap,
                onCustomize = {
                    widgetInfo.capsuleType?.let { type ->
                        activeCustomizingWidget = type
                    }
                },
                onCustomizeColor = {
                    activeColorizingWidgetInfo = widgetInfo
                },
                onPin = {
                    pinWidgetToHomeScreen(context, widgetInfo.providerClass)
                }
            )
        }
    }

    // MINI PAGE / CUSTOMIZATION DIALOG FOR A SPECIFIC CAPSULE WIDGET
    activeCustomizingWidget?.let { widgetType ->
        WidgetCustomizeMiniPage(
            widgetType = widgetType,
            onDismiss = { activeCustomizingWidget = null },
            onSaved = {
                refreshTrigger++
                WidgetUpdater.updateCapsuleWidgets(context)
                Toast.makeText(context, "تم حفظ وتطبيق مظهر ${widgetType.titleAr} بنجاح!", Toast.LENGTH_SHORT).show()
                activeCustomizingWidget = null
            }
        )
    }

    // COLOR CUSTOMIZE DIALOG FOR ANY WIDGET
    activeColorizingWidgetInfo?.let { wInfo ->
        val savedColor = remember { widgetPrefs.getInt("widget_color_${wInfo.id}", -1) }
        WidgetColorCustomizeDialog(
            widgetInfo = wInfo,
            initialColorInt = savedColor,
            onDismiss = { activeColorizingWidgetInfo = null },
            onSaveColor = { newColor ->
                widgetPrefs.edit().putInt("widget_color_${wInfo.id}", newColor).apply()
                refreshTrigger++
                WidgetUpdater.updateAllWidgets(context)
                Toast.makeText(context, "تم حفظ وتطبيق لون ${wInfo.titleAr} بنجاح!", Toast.LENGTH_SHORT).show()
                activeColorizingWidgetInfo = null
            }
        )
    }
}

@Composable
private fun WidgetCardItem(
    info: HomeScreenWidgetInfo,
    customColorInt: Int = -1,
    dayBitmap: android.graphics.Bitmap,
    gregBitmap: android.graphics.Bitmap,
    hijriBitmap: android.graphics.Bitmap,
    onCustomize: () -> Unit,
    onCustomizeColor: () -> Unit,
    onPin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(GlassDarkBase)
            .background(GlassGradientCard)
            .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("widget_card_${info.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0x22D4AF37),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = info.dimension,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = info.titleAr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = info.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget Live or Rich Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D1117))
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                when (info.id) {
                    "capsule_day" -> Image(
                        bitmap = dayBitmap.asImageBitmap(),
                        contentDescription = info.titleAr,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(580f / 270f)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    "capsule_gregorian" -> Image(
                        bitmap = gregBitmap.asImageBitmap(),
                        contentDescription = info.titleAr,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(580f / 270f)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    "capsule_hijri" -> Image(
                        bitmap = hijriBitmap.asImageBitmap(),
                        contentDescription = info.titleAr,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(580f / 270f)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    else -> NonCapsuleWidgetPreview(info = info, customColorInt = customColorInt)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (info.capsuleType != null) {
                    Button(
                        onClick = onCustomize,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = VoidBlack
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المظهر",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VoidBlack
                        )
                    }
                }

                Button(
                    onClick = onCustomizeColor,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (info.capsuleType == null) AccentGold else Color(0x33FFFFFF),
                        contentColor = if (info.capsuleType == null) VoidBlack else PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ColorLens,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (info.capsuleType == null) VoidBlack else PureWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "اللون",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (info.capsuleType == null) VoidBlack else PureWhite
                    )
                }

                OutlinedButton(
                    onClick = onPin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrushHighlight),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddBox,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = PureWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافة للشاشة",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetHomeScreenCard(
    isPill: Boolean = false,
    customColorInt: Int = -1,
    bgEnable: Boolean = false,
    bgFill: String = "gradient",
    bgColor1: Int = android.graphics.Color.parseColor("#3C4D64"),
    bgColor2: Int = android.graphics.Color.parseColor("#151C26"),
    bgDirection: String = "vertical",
    bgTexture: Boolean = false,
    bgRounded: Boolean = true,
    bgGlassify: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val enable = bgEnable || (customColorInt != -1)
    val fillType = if (customColorInt != -1 && !bgEnable) "solid" else bgFill
    val color1Val = if (customColorInt != -1 && !bgEnable) customColorInt else bgColor1
    val color2Val = if (customColorInt != -1 && !bgEnable) customColorInt else bgColor2
    
    val cornerShape = RoundedCornerShape(28.dp)
    
    val brush = remember(enable, fillType, color1Val, color2Val, bgDirection) {
        if (!enable) {
            androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    Color(0xEA252932),
                    Color(0xF216181F),
                    Color(0xF80B0C0F)
                )
            )
        } else if (fillType == "solid") {
            androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(color1Val), Color(color1Val))
            )
        } else {
            val start = when (bgDirection) {
                "horizontal" -> androidx.compose.ui.geometry.Offset(0f, 0f)
                "diagonal" -> androidx.compose.ui.geometry.Offset(0f, 0f)
                else -> androidx.compose.ui.geometry.Offset(0f, 0f)
            }
            val end = when (bgDirection) {
                "horizontal" -> androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f)
                "diagonal" -> androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                else -> androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
            }
            androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(color1Val), Color(color2Val)),
                start = start,
                end = end
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cornerShape)
            .background(brush)
            .border(
                1.2.dp,
                if (enable) Color(color1Val).copy(alpha = 0.8f) else Color(0x35FFFFFF),
                cornerShape
            )
    ) {
        // Glassify overlay over the ENTIRE widget
        if (enable && bgGlassify) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x1AFFFFFF))
                    .border(
                        1.2.dp,
                        Color.White.copy(alpha = 0.35f),
                        cornerShape
                    )
            )
        }

        // Texture overlay
        if (enable && bgTexture) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val texPaint = android.graphics.Paint().apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 3f
                    color = android.graphics.Color.argb(12, 255, 255, 255)
                    isAntiAlias = true
                }
                val step = size.height * 0.18f
                for (i in 0..5) {
                    val waveY = i * step
                    drawContext.canvas.nativeCanvas.drawCircle(size.width / 2f, -waveY, size.width * 0.7f, texPaint)
                    drawContext.canvas.nativeCanvas.drawCircle(size.width / 2f, size.height + waveY, size.width * 0.7f, texPaint)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isPill) PaddingValues(horizontal = 20.dp, vertical = 12.dp) else PaddingValues(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun NonCapsuleWidgetPreview(
    info: HomeScreenWidgetInfo,
    customColorInt: Int = -1,
    bgEnable: Boolean = false,
    bgFill: String = "gradient",
    bgColor1: Int = android.graphics.Color.parseColor("#3C4D64"),
    bgColor2: Int = android.graphics.Color.parseColor("#151C26"),
    bgDirection: String = "vertical",
    bgTexture: Boolean = false,
    bgRounded: Boolean = true,
    bgGlassify: Boolean = false
) {
    @Composable
    fun WidgetHomeScreenCard(
        isPill: Boolean = false,
        customColorInt: Int = -1,
        content: @Composable ColumnScope.() -> Unit
    ) {
        WidgetHomeScreenCard(
            isPill = isPill,
            customColorInt = customColorInt,
            bgEnable = bgEnable,
            bgFill = bgFill,
            bgColor1 = bgColor1,
            bgColor2 = bgColor2,
            bgDirection = bgDirection,
            bgTexture = bgTexture,
            bgRounded = bgRounded,
            bgGlassify = bgGlassify,
            content = content
        )
    }

    when (info.id) {
        "next_prayer_arabic" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "05:11", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "in 5h 42m", style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = Color(0xFF8E8E93))
                    }
                    Text(text = "الفجر", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
        "prayer_bar" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prayers = listOf("Fajr" to "05:11", "Dhuhr" to "12:50", "Asr" to "15:30", "Maghrib" to "18:05", "Isha" to "19:35")
                    prayers.forEachIndexed { idx, (pName, pTime) ->
                        val isNext = idx == 2
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isNext) AccentGold else Color(0x44FFFFFF))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = pName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal), color = if (isNext) Color.White else Color(0xFF8E8E93))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = pTime, style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal), color = if (isNext) Color.White else Color(0xFF8E8E93))
                        }
                    }
                }
            }
        }
        "prayer_list" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val prayers = listOf("Fajr" to "05:11", "Dhuhr" to "12:50", "Asr" to "15:30", "Maghrib" to "18:05", "Isha" to "19:35")
                    prayers.forEachIndexed { idx, (pName, pTime) ->
                        val isNext = idx == 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNext) Color(0x33D4AF37) else Color.Transparent)
                                .border(if (isNext) 1.dp else 0.dp, if (isNext) AccentGold.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isNext) Icons.Filled.AccessTimeFilled else Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isNext) AccentGold else Color(0xFF8E8E93)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = pName, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal), color = if (isNext) Color.White else Color(0xFF8E8E93))
                            }
                            Text(text = pTime, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal), color = if (isNext) Color.White else Color(0xFF8E8E93))
                        }
                    }
                }
            }
        }
        "next_prayer_card" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "NOOR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = Color(0xFF888888))
                    Text(text = "Makkah", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFAAAAAA))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "UPCOMING PRAYER", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "ASR", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "3:42 PM", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = Color(0xFFE2E2E2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "in 48m", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
        "full_schedule" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "PRAYER SCHEDULE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                    Text(text = "20 Rabi' al-Thani 1448 AH", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFAAAAAA))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val schedule = listOf("FAJR" to "05:11", "DHUHR" to "12:50", "ASR" to "03:30", "MAGHRIB" to "06:05", "ISHA" to "07:35")
                    schedule.forEachIndexed { idx, (pName, pTime) ->
                        val isNext = idx == 2
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = pName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = if (isNext) AccentGold else Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = pTime, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = if (isNext) AccentGold else Color(0xFFAAAAAA))
                        }
                    }
                }
            }
        }
        "prayer_streak" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "PRAYER LOG", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                    Text(text = "4 / 5 COMPLETED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = AccentGold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Fajr" to true, "Dhuhr" to true, "Asr" to true, "Maghrib" to true, "Isha" to false).forEach { (pName, done) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (done) AccentGold else Color(0x22FFFFFF))
                                    .border(1.dp, if (done) AccentGold else Color(0x44FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = pName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFAAAAAA))
                        }
                    }
                }
            }
        }
        "qibla_compass" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "QIBLA DIRECTION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                    Icon(imageVector = Icons.Filled.Explore, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "253° NW", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Towards Holy Kaaba, Makkah", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFFAAAAAA))
            }
        }
        "hijri_card" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "20", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "RABI' AL-THANI 1448 AH", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Thursday, October 1, 2026", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF888888))
                    }
                }
            }
        }
        "ramadan_countdown" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Text(text = "RAMADAN HUB", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "UNTIL IFTAR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "3h 18m", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Maghrib at 6:32 PM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF888888))
            }
        }
        "islamic_holidays" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Text(text = "ISLAMIC HOLIDAY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "EID AL-FITR", style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "in 45 days", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = AccentGold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "1 Shawwal 1448 AH", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color(0xFF888888))
            }
        }
        "quran_verse" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "﴿ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ﴾",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Our Lord, do not impose blame upon us if we have forgotten or erred",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = Color(0xFFC8C8CC),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        "quran_continue" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = "CONTINUE READING", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp), color = Color(0xFF888888))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Surah Al-Kahf • Ayah 10", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Icon(imageVector = Icons.Filled.MenuBook, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                }
            }
        }
        "azkar_counter" -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "سبحان الله وبحمده", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "33", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "+ Tap to count",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        "duaa_of_day" -> {
            WidgetHomeScreenCard(isPill = true, customColorInt = customColorInt) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DUAA OF THE DAY • MORNING",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                        color = AccentGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "« اللَّهُمَّ بَكِ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ »",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "O Allah, by You we enter the morning and by You we enter the evening...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFC8C8CC),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            WidgetHomeScreenCard(customColorInt = customColorInt) {
                Text(text = info.titleAr, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun WidgetColorCustomizeDialog(
    widgetInfo: HomeScreenWidgetInfo,
    initialColorInt: Int,
    onDismiss: () -> Unit,
    onSaveColor: (Int) -> Unit
) {
    val context = LocalContext.current
    val id = widgetInfo.id
    val prefs = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }

    var bgEnable by remember { mutableStateOf(prefs.getBoolean("widget_bg_enable_$id", initialColorInt != -1)) }
    var bgFill by remember { mutableStateOf(prefs.getString("widget_bg_fill_$id", if (initialColorInt != -1) "solid" else "gradient") ?: "gradient") }
    var bgColor1 by remember { mutableIntStateOf(prefs.getInt("widget_bg_color1_$id", if (initialColorInt != -1) initialColorInt else android.graphics.Color.parseColor("#3C4D64"))) }
    var bgColor2 by remember { mutableIntStateOf(prefs.getInt("widget_bg_color2_$id", if (initialColorInt != -1) initialColorInt else android.graphics.Color.parseColor("#151C26"))) }
    var bgDirection by remember { mutableStateOf(prefs.getString("widget_bg_direction_$id", "vertical") ?: "vertical") }
    val bgTexture = false
    val bgRounded = true
    var bgGlassify by remember { mutableStateOf(prefs.getBoolean("widget_bg_glassify_$id", false)) }

    var showColorPicker1 by remember { mutableStateOf(false) }
    var showColorPicker2 by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(26.dp)),
            color = SurfaceCardHigh,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlassGradientHero)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تخصيص ${widgetInfo.titleAr.substringAfter(". ")}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "Customize Widget Background & Appearance",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = PureWhite, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = GlassBorderColorSubtle)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview
                    item {
                        Text(
                            text = "المعاينة الحية الفورية (Live Widget Preview):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NonCapsuleWidgetPreview(
                            info = widgetInfo,
                            bgEnable = bgEnable,
                            bgFill = bgFill,
                            bgColor1 = bgColor1,
                            bgColor2 = bgColor2,
                            bgDirection = bgDirection,
                            bgTexture = bgTexture,
                            bgRounded = bgRounded,
                            bgGlassify = bgGlassify
                        )
                    }

                    // Background section title
                    item {
                        Text(
                            text = "خلفية الودجت المخصصة (Background)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentGold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 1. Enable Switch
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تفعيل الخلفية المخصصة (Enable):", color = PureWhite, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("تفعيل أو تعطيل تخصيص مظهر الخلفية", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(
                                checked = bgEnable,
                                onCheckedChange = { bgEnable = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentGold,
                                    checkedTrackColor = AccentGold.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }

                    if (bgEnable) {
                        // 2. Fill Type Selector
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("نوع التعبئة (Fill):", color = PureWhite, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("gradient" to "تدرج ألوان", "solid" to "لون موحد").forEach { (typeKey, label) ->
                                        val isSelected = bgFill == typeKey
                                        FontSelectChip(
                                            label = label,
                                            isSelected = isSelected,
                                            onClick = { bgFill = typeKey }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Color 1 Selector
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("اللون الأول (Color 1):", color = PureWhite, style = MaterialTheme.typography.bodyMedium)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(bgColor1))
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable { showColorPicker1 = true }
                                )
                            }
                        }

                        // 4. Color 2 Selector (Only if Gradient)
                        if (bgFill == "gradient") {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("اللون الثاني (Color 2):", color = PureWhite, style = MaterialTheme.typography.bodyMedium)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(bgColor2))
                                            .border(2.dp, Color.White, CircleShape)
                                            .clickable { showColorPicker2 = true }
                                    )
                                }
                            }

                            // 5. Direction Selector
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("اتجاه التدرج (Direction):", color = PureWhite, style = MaterialTheme.typography.bodyMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("vertical" to "عمودي", "horizontal" to "أفقي", "diagonal" to "قطري").forEach { (dirKey, label) ->
                                            val isSelected = bgDirection == dirKey
                                            FontSelectChip(
                                                label = label,
                                                isSelected = isSelected,
                                                onClick = { bgDirection = dirKey }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 8. Glassify Switch
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("تأثير زجاجي شفاف (Glassify)", color = PureWhite, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF007AFF),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "New",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("إضافة طبقة زجاجية متداخلة تضفي عمقاً ثلاثي الأبعاد", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(
                                    checked = bgGlassify,
                                    onCheckedChange = { bgGlassify = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AccentGold,
                                        checkedTrackColor = AccentGold.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = GlassBorderColorSubtle)

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            // Save all customized background properties
                            val editor = prefs.edit()
                            editor.putBoolean("widget_bg_enable_$id", bgEnable)
                            if (bgEnable) {
                                editor.putString("widget_bg_fill_$id", bgFill)
                                editor.putInt("widget_bg_color1_$id", bgColor1)
                                editor.putInt("widget_bg_color2_$id", bgColor2)
                                editor.putString("widget_bg_direction_$id", bgDirection)
                                editor.putBoolean("widget_bg_texture_$id", bgTexture)
                                editor.putBoolean("widget_bg_rounded_$id", bgRounded)
                                editor.putBoolean("widget_bg_glassify_$id", bgGlassify)
                            }
                            editor.apply()
                            
                            // Save primary color to legacy color key as fallback
                            onSaveColor(if (bgEnable) bgColor1 else -1)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = VoidBlack),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = VoidBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ وتطبيق المظهر", fontWeight = FontWeight.Bold, color = VoidBlack)
                    }

                    OutlinedButton(
                        onClick = {
                            // Restore all to defaults
                            val editor = prefs.edit()
                            editor.remove("widget_bg_enable_$id")
                            editor.remove("widget_bg_fill_$id")
                            editor.remove("widget_bg_color1_$id")
                            editor.remove("widget_bg_color2_$id")
                            editor.remove("widget_bg_direction_$id")
                            editor.remove("widget_bg_texture_$id")
                            editor.remove("widget_bg_rounded_$id")
                            editor.remove("widget_bg_glassify_$id")
                            editor.apply()
                            
                            onSaveColor(-1)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrushHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة للافتراضي", fontWeight = FontWeight.SemiBold, color = PureWhite)
                    }
                }
            }
        }
    }

    // Circular color picker 1 dialog
    if (showColorPicker1) {
        CustomColorPickerDialog(
            initialColor = bgColor1,
            onDismiss = { showColorPicker1 = false },
            onColorSelected = { selected ->
                bgColor1 = selected
                showColorPicker1 = false
            }
        )
    }

    // Circular color picker 2 dialog
    if (showColorPicker2) {
        CustomColorPickerDialog(
            initialColor = bgColor2,
            onDismiss = { showColorPicker2 = false },
            onColorSelected = { selected ->
                bgColor2 = selected
                showColorPicker2 = false
            }
        )
    }
}

@Composable
private fun WidgetCustomizeMiniPage(
    widgetType: CapsuleWidgetType,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }

    // Resolve initial settings for this specific widget
    val initialArabicFont = remember {
        when (widgetType) {
            CapsuleWidgetType.DAY -> prefs.getString("capsule_day_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth") ?: "taleed_thuluth"
            CapsuleWidgetType.GREGORIAN -> prefs.getString("capsule_gregorian_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth") ?: "taleed_thuluth"
            CapsuleWidgetType.HIJRI -> prefs.getString("capsule_hijri_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth") ?: "taleed_thuluth"
        }
    }

    val initialNumberFont = remember {
        when (widgetType) {
            CapsuleWidgetType.DAY -> "oswald"
            CapsuleWidgetType.GREGORIAN -> prefs.getString("capsule_gregorian_number_font", null) ?: prefs.getString("capsule_number_font", "oswald") ?: "oswald"
            CapsuleWidgetType.HIJRI -> prefs.getString("capsule_hijri_number_font", null) ?: prefs.getString("capsule_number_font", "oswald") ?: "oswald"
        }
    }

    val initialBgStyle = remember {
        when (widgetType) {
            CapsuleWidgetType.DAY -> prefs.getString("capsule_day_bg_style", null) ?: prefs.getString("capsule_background_style", "image") ?: "image"
            CapsuleWidgetType.GREGORIAN -> prefs.getString("capsule_gregorian_bg_style", null) ?: prefs.getString("capsule_background_style", "image") ?: "image"
            CapsuleWidgetType.HIJRI -> prefs.getString("capsule_hijri_bg_style", null) ?: prefs.getString("capsule_background_style", "image") ?: "image"
        }
    }

    val initialPatternColor = remember {
        when (widgetType) {
            CapsuleWidgetType.DAY -> if (prefs.contains("capsule_day_pattern_color")) prefs.getInt("capsule_day_pattern_color", android.graphics.Color.parseColor("#26364A")) else android.graphics.Color.parseColor("#26364A")
            CapsuleWidgetType.GREGORIAN -> if (prefs.contains("capsule_gregorian_pattern_color")) prefs.getInt("capsule_gregorian_pattern_color", android.graphics.Color.parseColor("#5A3B33")) else android.graphics.Color.parseColor("#5A3B33")
            CapsuleWidgetType.HIJRI -> if (prefs.contains("capsule_hijri_pattern_color")) prefs.getInt("capsule_hijri_pattern_color", android.graphics.Color.parseColor("#26364A")) else android.graphics.Color.parseColor("#26364A")
        }
    }

    var selectedArabicFont by remember { mutableStateOf(initialArabicFont) }
    var selectedNumberFont by remember { mutableStateOf(initialNumberFont) }
    var selectedBgStyle by remember { mutableStateOf(initialBgStyle) }
    var selectedPatternColor by remember { mutableIntStateOf(initialPatternColor) }
    var currentHue by remember { mutableFloatStateOf(215f) }

    val today = remember { LocalDate.now() }
    val hijri = remember { CalendarRepository.getHijriDate(today, -2) }

    // Instant dynamic preview bitmap for the mini page
    val previewBitmap = remember(selectedArabicFont, selectedNumberFont, selectedBgStyle, selectedPatternColor) {
        when (widgetType) {
            CapsuleWidgetType.DAY -> CapsuleWidgetRenderer.renderDayOfWeek(
                context = context,
                dayOfWeek = today.dayOfWeek,
                customArabicFont = selectedArabicFont,
                customBgStyle = selectedBgStyle,
                customPatternColor = selectedPatternColor
            )
            CapsuleWidgetType.GREGORIAN -> CapsuleWidgetRenderer.renderGregorian(
                context = context,
                dayOfMonth = today.dayOfMonth,
                monthNumber = today.monthValue,
                customArabicFont = selectedArabicFont,
                customNumberFont = selectedNumberFont,
                customBgStyle = selectedBgStyle,
                customPatternColor = selectedPatternColor
            )
            CapsuleWidgetType.HIJRI -> CapsuleWidgetRenderer.renderHijri(
                context = context,
                hijriDay = hijri.day,
                hijriMonth = hijri.month,
                customArabicFont = selectedArabicFont,
                customNumberFont = selectedNumberFont,
                customBgStyle = selectedBgStyle,
                customPatternColor = selectedPatternColor
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(26.dp)),
            color = SurfaceCardHigh,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlassGradientHero)
            ) {
                // MINI PAGE HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تخصيص ${widgetType.titleAr}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "Customize ${widgetType.titleEn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = GlassBorderColorSubtle)

                // SCROLLABLE EDITING OPTIONS
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. LIVE DYNAMIC PREVIEW CARD
                    item {
                        Text(
                            text = "المعاينة الحية الفورية (Live Preview):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF090C11))
                                .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(580f / 270f)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }
                    }

                    // 2. ARABIC CALLIGRAPHY FONT SELECTION
                    item {
                        Text(
                            text = "نوع الخط العربي (Arabic Font):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val arabicFonts = listOf(
                            "taleed_thuluth" to "خط تليد ثلث (Taleed)",
                            "thuluth" to "خط الثلث الأصيل (Thuluth)",
                            "decotype_thuluth" to "ثلث ديكوتايب (Deco)",
                            "dima_thuluth" to "ثلث ديما (Dima)",
                            "elsawaf_thuluth" to "ثلث السواف (Elsawaf)",
                            "ruqaa" to "خط الرقعة الأصيل (Ruqaa)",
                            "scheherazade" to "النسخ العثماني (Naskh)",
                            "amiri" to "خط أميري (Amiri)",
                            "cairo" to "كايرو هندسي (Cairo)"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            arabicFonts.take(3).forEach { (fontKey, fontLabel) ->
                                val isSelected = selectedArabicFont == fontKey
                                FontSelectChip(
                                    label = fontLabel.substringBefore(" ("),
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedArabicFont = fontKey }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            arabicFonts.subList(3, 6).forEach { (fontKey, fontLabel) ->
                                val isSelected = selectedArabicFont == fontKey
                                FontSelectChip(
                                    label = fontLabel.substringBefore(" ("),
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedArabicFont = fontKey }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            arabicFonts.takeLast(3).forEach { (fontKey, fontLabel) ->
                                val isSelected = selectedArabicFont == fontKey
                                FontSelectChip(
                                    label = fontLabel.substringBefore(" ("),
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedArabicFont = fontKey }
                                )
                            }
                        }
                    }

                    // 3. NUMBER WATERMARK FONT (FOR GREGORIAN & HIJRI)
                    if (widgetType != CapsuleWidgetType.DAY) {
                        item {
                            Text(
                                text = "نوع خط الأرقام المائية (Number Watermark Font):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val numberFonts = listOf(
                                "oswald" to "Oswald",
                                "bebas" to "Bebas Neue",
                                "montserrat" to "Montserrat",
                                "outfit" to "Outfit",
                                "rubik" to "Rubik",
                                "poppins" to "Poppins"
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                numberFonts.take(3).forEach { (fontKey, fontLabel) ->
                                    val isSelected = selectedNumberFont == fontKey
                                    FontSelectChip(
                                        label = fontLabel,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedNumberFont = fontKey }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                numberFonts.drop(3).forEach { (fontKey, fontLabel) ->
                                    val isSelected = selectedNumberFont == fontKey
                                    FontSelectChip(
                                        label = fontLabel,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedNumberFont = fontKey }
                                    )
                                }
                            }
                        }
                    }

                    // 4. BACKGROUND STYLE SELECTION
                    item {
                        Text(
                            text = "نمط الخلفية (Background Style):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val bgStyles = listOf(
                            "image" to "🎨 نمط الزخرفة الخطية",
                            "dark" to "🪟 داكن زجاجي",
                            "light" to "🕊️ لؤلؤي فضي"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            bgStyles.forEach { (styleKey, styleLabel) ->
                                val isSelected = selectedBgStyle == styleKey
                                FontSelectChip(
                                    label = styleLabel,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedBgStyle = styleKey }
                                )
                            }
                        }
                    }

                    // 5. COLOR PALETTE FOR CALLIGRAPHY PATTERN (If Image Style)
                    if (selectedBgStyle == "image") {
                        item {
                            Text(
                                text = "لون خلفية الزخرفة (Color Palette):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val presetColors = listOf(
                                "بني ملكي" to android.graphics.Color.parseColor("#5A3B33"),
                                "أزرق كحلي" to android.graphics.Color.parseColor("#26364A"),
                                "أخضر زمردي" to android.graphics.Color.parseColor("#1E3A2D"),
                                "رمادي فحمي" to android.graphics.Color.parseColor("#262932"),
                                "عنابي فاخر" to android.graphics.Color.parseColor("#421E28"),
                                "ذهبي برونزي" to android.graphics.Color.parseColor("#3E321C")
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                presetColors.take(3).forEach { (name, colorInt) ->
                                    val isSelected = selectedPatternColor == colorInt
                                    ColorSelectChip(
                                        name = name,
                                        colorInt = colorInt,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedPatternColor = colorInt }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                presetColors.drop(3).forEach { (name, colorInt) ->
                                    val isSelected = selectedPatternColor == colorInt
                                    ColorSelectChip(
                                        name = name,
                                        colorInt = colorInt,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedPatternColor = colorInt }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Spectrum Slider
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassWhiteOverlayLight)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "طيف ألوان مخصص (Custom Hue):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PureWhite
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(selectedPatternColor))
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }
                                Slider(
                                    value = currentHue,
                                    onValueChange = { hue ->
                                        currentHue = hue
                                        val hsv = floatArrayOf(hue, 0.48f, 0.28f)
                                        val newColorInt = android.graphics.Color.HSVToColor(hsv)
                                        selectedPatternColor = newColorInt
                                    },
                                    valueRange = 0f..360f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentGold,
                                        activeTrackColor = AccentGold,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = GlassBorderColorSubtle)

                // BOTTOM ACTION BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            // Save specifically for this widget in SharedPreferences
                            val editor = prefs.edit()
                            when (widgetType) {
                                CapsuleWidgetType.DAY -> {
                                    editor.putString("capsule_day_arabic_font", selectedArabicFont)
                                    editor.putString("capsule_day_bg_style", selectedBgStyle)
                                    editor.putInt("capsule_day_pattern_color", selectedPatternColor)
                                }
                                CapsuleWidgetType.GREGORIAN -> {
                                    editor.putString("capsule_gregorian_arabic_font", selectedArabicFont)
                                    editor.putString("capsule_gregorian_number_font", selectedNumberFont)
                                    editor.putString("capsule_gregorian_bg_style", selectedBgStyle)
                                    editor.putInt("capsule_gregorian_pattern_color", selectedPatternColor)
                                }
                                CapsuleWidgetType.HIJRI -> {
                                    editor.putString("capsule_hijri_arabic_font", selectedArabicFont)
                                    editor.putString("capsule_hijri_number_font", selectedNumberFont)
                                    editor.putString("capsule_hijri_bg_style", selectedBgStyle)
                                    editor.putInt("capsule_hijri_pattern_color", selectedPatternColor)
                                }
                            }
                            editor.apply()
                            onSaved()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = VoidBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ وتطبيق المظهر", fontWeight = FontWeight.Bold, color = VoidBlack)
                    }

                    OutlinedButton(
                        onClick = {
                            when (widgetType) {
                                CapsuleWidgetType.DAY -> pinWidgetToHomeScreen(context, CapsuleDayWidgetProvider::class.java)
                                CapsuleWidgetType.GREGORIAN -> pinWidgetToHomeScreen(context, CapsuleGregorianWidgetProvider::class.java)
                                CapsuleWidgetType.HIJRI -> pinWidgetToHomeScreen(context, CapsuleHijriWidgetProvider::class.java)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrushHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.AddBox, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة للشاشة", fontWeight = FontWeight.SemiBold, color = PureWhite)
                    }
                }
            }
        }
    }
}

@Composable
private fun FontSelectChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentGold.copy(alpha = 0.22f) else GlassWhiteOverlayLight)
            .border(
                1.dp,
                if (isSelected) AccentGold else GlassBorderColorSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) AccentGold else PureWhite,
            maxLines = 1
        )
    }
}

@Composable
private fun ColorSelectChip(
    name: String,
    colorInt: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(colorInt))
            .border(
                2.dp,
                if (isSelected) AccentGold else Color.White.copy(alpha = 0.25f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

private fun pinWidgetToHomeScreen(context: Context, providerClass: Class<*>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager?.isRequestPinAppWidgetSupported == true) {
            val component = ComponentName(context, providerClass)
            appWidgetManager.requestPinAppWidget(component, null, null)
            Toast.makeText(context, "تم إرسال طلب إضافة الودجت للشاشة الرئيسية", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "يمكنك إضافة الودجت من قائمة ودجات هاتفك", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "يمكنك إضافة الودجت من قائمة ودجات هاتفك", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun CustomColorPickerDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    var selectedColor by remember { mutableIntStateOf(initialColor) }
    
    val hsv = remember(initialColor) {
        val out = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor, out)
        out
    }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    LaunchedEffect(hue, saturation, value) {
        selectedColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(24.dp)),
            color = SurfaceCardHigh
        ) {
            Column(
                modifier = Modifier
                    .background(GlassGradientHero)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        selectedColor = initialColor
                    }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset", tint = PureWhite)
                    }
                    Text(
                        text = "اختر لوناً مخصصاً",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AccentGold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color(selectedColor))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor).copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "طيف الألوان (Hue):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGold,
                        activeTrackColor = AccentGold,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Text(
                    text = "الدرجة/التشبع (Saturation):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Text(
                    text = "السطوع (Brightness):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0.1f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = PureWhite,
                        activeTrackColor = PureWhite,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val swatches = listOf(
                    android.graphics.Color.parseColor("#5A3B33") to "بني",
                    android.graphics.Color.parseColor("#26364A") to "كحلي",
                    android.graphics.Color.parseColor("#1E3A2D") to "زمرد",
                    android.graphics.Color.parseColor("#262932") to "رمادي",
                    android.graphics.Color.parseColor("#421E28") to "عنابي",
                    android.graphics.Color.parseColor("#3E321C") to "ذهبي",
                    android.graphics.Color.parseColor("#645582") to "بنفسجي"
                )

                Text(
                    text = "العينات السريعة (Swatches):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable { selectedColor = android.graphics.Color.parseColor("#26364A") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    swatches.forEach { (colorInt, name) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    width = if (selectedColor == colorInt) 2.dp else 1.dp,
                                    color = if (selectedColor == colorInt) AccentGold else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorInt }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onColorSelected(selectedColor) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = VoidBlack),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تحديد", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrushHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
