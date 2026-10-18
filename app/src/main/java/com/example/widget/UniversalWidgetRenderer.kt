package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.CalculationMethod
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.Madhab
import com.example.data.model.PrayerType
import com.example.data.prayer.AstronomicalPrayerCalculator
import com.example.data.repository.CalendarRepository
import com.example.data.repository.DuaaRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Universal high-precision canvas renderer for all 17 widgets in Noor:
 * - 3 Capsule Calendar widgets (Day, Gregorian, Hijri)
 * - 7 Prayer & Qibla widgets (Prayer Bar, Next Prayer Banner, Next Prayer Pill, Prayer List, Full Schedule, Streak, Qibla)
 * - 4 Quran & Azkar widgets (Daily Ayah, Continue Reading, Tasbeeh Counter, Duaa of the Day)
 * - 3 Islamic Calendar Cards (Comprehensive Hijri Card, Upcoming Islamic Holiday, Ramadan Countdown)
 *
 * Implements luxury Islamic design with glassmorphism, authentic calligraphy, gold accents,
 * specular lighting, and seamless user color customization.
 */
object UniversalWidgetRenderer {

    const val WIDTH = 580
    const val HEIGHT = 270

    // Arabic Days
    val DAY_NAMES_AR = mapOf(
        java.time.DayOfWeek.MONDAY to "الإثنين",
        java.time.DayOfWeek.TUESDAY to "الثلاثاء",
        java.time.DayOfWeek.WEDNESDAY to "الأربعاء",
        java.time.DayOfWeek.THURSDAY to "الخميس",
        java.time.DayOfWeek.FRIDAY to "الجمعة",
        java.time.DayOfWeek.SATURDAY to "السبت",
        java.time.DayOfWeek.SUNDAY to "الأحد"
    )

    fun renderWidget(
        context: Context,
        widgetId: String,
        today: LocalDate = LocalDate.now(),
        hijriDay: Int = 26,
        hijriMonth: Int = 4,
        arabicFontKey: String = "taleed_thuluth",
        numberFontKey: String = "oswald",
        bgStyle: String = "default",
        patternColor: Int = Color.parseColor("#26364A")
    ): Bitmap {
        val effectiveArabicFont = if (arabicFontKey == "default") "taleed_thuluth" else arabicFontKey
        val effectiveNumberFont = if (numberFontKey == "default") "oswald" else numberFontKey

        return when (widgetId) {
            "capsule_day" -> CapsuleWidgetRenderer.renderDayOfWeek(
                context = context,
                dayOfWeek = today.dayOfWeek,
                customArabicFont = effectiveArabicFont,
                customBgStyle = if (bgStyle == "default") "image" else bgStyle,
                customPatternColor = patternColor
            )
            "capsule_gregorian" -> CapsuleWidgetRenderer.renderGregorian(
                context = context,
                dayOfMonth = today.dayOfMonth,
                monthNumber = today.monthValue,
                customArabicFont = effectiveArabicFont,
                customNumberFont = effectiveNumberFont,
                customBgStyle = if (bgStyle == "default") "image" else bgStyle,
                customPatternColor = patternColor
            )
            "capsule_hijri" -> CapsuleWidgetRenderer.renderHijri(
                context = context,
                hijriDay = hijriDay,
                hijriMonth = hijriMonth,
                customArabicFont = effectiveArabicFont,
                customNumberFont = effectiveNumberFont,
                customBgStyle = if (bgStyle == "default") "image" else bgStyle,
                customPatternColor = patternColor
            )
            "prayer_bar" -> renderClassicPrayerBar(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "next_prayer_arabic" -> renderClassicNextPrayerArabic(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "next_prayer_pill" -> renderClassicNextPrayerPill(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "prayer_list" -> renderClassicPrayerList(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "full_schedule" -> renderClassicFullSchedule(context, today, hijriDay, hijriMonth, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "prayer_streak" -> renderClassicPrayerStreak(context, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "qibla_compass" -> renderClassicQibla(context, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "quran_verse" -> renderClassicQuranVerse(context, effectiveArabicFont, bgStyle, patternColor)
            "quran_continue" -> renderClassicQuranContinue(context, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "azkar_counter" -> renderClassicAzkarCounter(context, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "duaa_of_the_day" -> renderClassicDuaa(context, effectiveArabicFont, bgStyle, patternColor)
            "hijri_date_card" -> renderClassicHijriDateCard(context, today, hijriDay, hijriMonth, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "islamic_holiday" -> renderClassicIslamicHoliday(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            "ramadan_countdown" -> renderClassicRamadanCountdown(context, today, effectiveArabicFont, effectiveNumberFont, bgStyle, patternColor)
            else -> CapsuleWidgetRenderer.renderDayOfWeek(context, today.dayOfWeek, customArabicFont = effectiveArabicFont, customBgStyle = bgStyle, customPatternColor = patternColor)
        }
    }

    private fun getArabicTypeface(context: Context, key: String): Typeface {
        return CapsuleWidgetRenderer.resolveArabicTypeface(context, if (key == "default") "taleed_thuluth" else key)
    }

    private fun getNumberTypeface(context: Context, key: String): Typeface {
        return CapsuleWidgetRenderer.resolveNumberTypeface(context, if (key == "default") "oswald" else key)
    }

    private fun getPrayerSchedule(context: Context, today: LocalDate): Pair<DayPrayerSchedule, String> {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", 21.4225f).toDouble()
        val lng = prefs.getFloat("longitude", 39.8262f).toDouble()
        val cityName = prefs.getString("city_name", "مكة المكرمة") ?: "مكة المكرمة"
        val methodId = prefs.getString("calculation_method", "umm_al_qura")
        val madhabId = prefs.getString("madhab", "standard")

        val method = CalculationMethod.entries.firstOrNull { it.id == methodId } ?: CalculationMethod.UMM_AL_QURA
        val madhab = Madhab.entries.firstOrNull { it.id == madhabId } ?: Madhab.STANDARD

        val schedule = AstronomicalPrayerCalculator.calculateSchedule(
            date = today,
            latitude = lat,
            longitude = lng,
            cityName = cityName,
            method = method,
            madhab = madhab
        )
        return Pair(schedule, cityName)
    }

    /**
     * Draws the authentic luxury glass card background tinted with the user's custom color.
     */
    private fun drawCardBackground(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        bgStyle: String,
        tintColor: Int,
        context: Context
    ) {
        when (bgStyle) {
            "image", "pattern" -> {
                drawCalligraphyPattern(canvas, rect, radius, tintColor, context)
            }
            "light" -> {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(Color.parseColor("#FAFBFD"), Color.parseColor("#ECEFF5"), Color.parseColor("#D9DEEB"), Color.parseColor("#C4C9D8")),
                        floatArrayOf(0f, 0.38f, 0.74f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                drawBevelStroke(canvas, rect, radius, false)
            }
            "dark" -> {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(Color.parseColor("#262932"), Color.parseColor("#1C1E26"), Color.parseColor("#14161B"), Color.parseColor("#0B0C0F")),
                        floatArrayOf(0f, 0.35f, 0.75f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                drawBevelStroke(canvas, rect, radius, true)
            }
            else -> {
                // "default" / Classic Sleek Dark Luxury Glass tinted with custom color & gold sheen
                val tr = Color.red(tintColor)
                val tg = Color.green(tintColor)
                val tb = Color.blue(tintColor)

                val cTop = Color.argb(255, (22 + tr * 0.28f).toInt().coerceIn(0, 255), (26 + tg * 0.28f).toInt().coerceIn(0, 255), (36 + tb * 0.28f).toInt().coerceIn(0, 255))
                val cMid = Color.argb(255, (14 + tr * 0.14f).toInt().coerceIn(0, 255), (16 + tg * 0.14f).toInt().coerceIn(0, 255), (22 + tb * 0.14f).toInt().coerceIn(0, 255))
                val cBottom = Color.argb(255, (8 + tr * 0.07f).toInt().coerceIn(0, 255), (9 + tg * 0.07f).toInt().coerceIn(0, 255), (12 + tb * 0.07f).toInt().coerceIn(0, 255))

                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(cTop, cMid, cBottom),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)

                // Top glossy sheen
                canvas.save()
                val clipPath = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
                canvas.clipPath(clipPath)

                val sheenHeight = rect.height() * 0.42f
                val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.centerX(), rect.top, rect.centerX(), rect.top + sheenHeight,
                        intArrayOf(Color.argb(45, 255, 255, 255), Color.argb(10, 255, 255, 255), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.6f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(RectF(rect.left, rect.top, rect.right, rect.top + sheenHeight), sheenPaint)

                // Subtle Islamic watermark star geometry in background
                drawSubtleIslamicStarWatermark(canvas, rect, tintColor)

                canvas.restore()

                drawBevelStroke(canvas, rect, radius, true)
            }
        }
    }

    private fun drawSubtleIslamicStarWatermark(canvas: Canvas, rect: RectF, tintColor: Int) {
        val cx = rect.right - 80f
        val cy = rect.bottom - 40f
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.WHITE
            alpha = 14
        }
        val r1 = 110f
        val r2 = 78f
        val points = 8
        val path = Path()
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) r1 else r2
            val angle = i * Math.PI / points - Math.PI / 2
            val x = (cx + radius * Math.cos(angle)).toFloat()
            val y = (cy + radius * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, starPaint)
    }

    private fun drawCalligraphyPattern(canvas: Canvas, rect: RectF, radius: Float, baseColor: Int, context: Context) {
        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)
        val colTop = Color.argb(255, (r * 1.32f).toInt().coerceIn(0, 255), (g * 1.32f).toInt().coerceIn(0, 255), (b * 1.32f).toInt().coerceIn(0, 255))
        val colBottom = Color.argb(255, (r * 0.68f).toInt().coerceIn(0, 255), (g * 0.68f).toInt().coerceIn(0, 255), (b * 0.68f).toInt().coerceIn(0, 255))
        val colDeep = Color.argb(255, (r * 0.45f).toInt().coerceIn(0, 255), (g * 0.45f).toInt().coerceIn(0, 255), (b * 0.45f).toInt().coerceIn(0, 255))

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(colTop, baseColor, colBottom, colDeep),
                floatArrayOf(0f, 0.35f, 0.72f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)

        canvas.save()
        val clipPath = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clipPath)

        val patternFont = try {
            ResourcesCompat.getFont(context, R.font.taleed_thuluth) ?: Typeface.create("serif", Typeface.BOLD)
        } catch (e: Exception) {
            Typeface.create("serif", Typeface.BOLD)
        }

        val patternPaintLight = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = patternFont
            color = Color.WHITE
            alpha = 24
            textAlign = Paint.Align.CENTER
        }
        val patternPaintDark = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = patternFont
            color = Color.BLACK
            alpha = 34
            textAlign = Paint.Align.CENTER
        }

        val calligGlyphs = listOf(
            Triple("ع", 130f, Pair(rect.left + 50f, rect.top + 75f)),
            Triple("ص", 120f, Pair(rect.left + 200f, rect.top + 85f)),
            Triple("م", 135f, Pair(rect.left + 285f, rect.top + 60f)),
            Triple("ل", 160f, Pair(rect.left + 455f, rect.top + 65f)),
            Triple("ح", 130f, Pair(rect.left + 75f, rect.bottom - 45f)),
            Triple("ق", 125f, Pair(rect.left + 255f, rect.bottom - 50f)),
            Triple("ي", 135f, Pair(rect.left + 435f, rect.bottom - 45f)),
            Triple("لا", 140f, Pair(rect.right - 40f, rect.centerY()))
        )

        for ((glyph, size, pos) in calligGlyphs) {
            patternPaintDark.textSize = size
            canvas.drawText(glyph, pos.first + 2f, pos.second + 2f, patternPaintDark)
            patternPaintLight.textSize = size
            canvas.drawText(glyph, pos.first, pos.second, patternPaintLight)
        }
        canvas.restore()

        drawBevelStroke(canvas, rect, radius, true)
    }

    private fun drawBevelStroke(canvas: Canvas, rect: RectF, radius: Float, isDark: Boolean) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                if (isDark) {
                    intArrayOf(Color.argb(175, 255, 255, 255), Color.argb(75, 255, 255, 255), Color.argb(25, 255, 255, 255), Color.argb(12, 255, 255, 255))
                } else {
                    intArrayOf(Color.argb(230, 255, 255, 255), Color.argb(140, 255, 255, 255), Color.argb(70, 180, 185, 200), Color.argb(35, 140, 145, 160))
                },
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    // =========================================================================
    // 1. PRAYER BAR (4x1 Ultra-Sleek Pill Bar)
    // =========================================================================
    private fun renderClassicPrayerBar(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = rect.height() / 2f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val activeTextColor = if (isLight) Color.parseColor("#0F141C") else Color.WHITE
        val inactiveTextColor = if (isLight) Color.parseColor("#707888") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)
        val numberFont = getNumberTypeface(context, numberFontKey)

        val (schedule, _) = getPrayerSchedule(context, today)
        val nextObligatory = schedule.getNextObligatoryPrayer(LocalTime.now())
        val activeType = nextObligatory.prayer.type

        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val prayers = listOf(
            Triple("الفجر", schedule.fajr.format(fmt), activeType == PrayerType.FAJR),
            Triple("الظهر", schedule.dhuhr.format(fmt), activeType == PrayerType.DHUHR),
            Triple("العصر", schedule.asr.format(fmt), activeType == PrayerType.ASR),
            Triple("المغرب", schedule.maghrib.format(fmt), activeType == PrayerType.MAGHRIB),
            Triple("العشاء", schedule.isha.format(fmt), activeType == PrayerType.ISHA)
        )

        val colWidth = rect.width() / 5f
        for (i in prayers.indices) {
            val (nameAr, time, isActive) = prayers[i]
            val cx = rect.left + colWidth * i + colWidth / 2f

            if (isActive) {
                // Glowing active highlight capsule
                val pillRect = RectF(cx - (colWidth * 0.44f), rect.top + 14f, cx + (colWidth * 0.44f), rect.bottom - 14f)
                val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        pillRect.left, pillRect.top, pillRect.right, pillRect.bottom,
                        intArrayOf(Color.argb(70, 212, 175, 55), Color.argb(40, 255, 255, 255), Color.argb(15, 255, 255, 255)),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(pillRect, 22f, 22f, pillPaint)

                val pillStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.parseColor("#E6D4AF37")
                }
                canvas.drawRoundRect(pillRect, 22f, 22f, pillStroke)

                // Illuminated Gold/White indicator dot
                val dotGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#80D4AF37")
                }
                canvas.drawCircle(cx, rect.top + 38f, 9f, dotGlow)

                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                canvas.drawCircle(cx, rect.top + 38f, 5.5f, dotPaint)
            } else {
                // Inactive subtle dot
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(90, 154, 160, 172) }
                canvas.drawCircle(cx, rect.top + 38f, 4f, dotPaint)
            }

            // Arabic Name in selected Arabic Calligraphy font
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = arabicFont
                color = if (isActive) Color.WHITE else inactiveTextColor
                textSize = 34f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(nameAr, cx, rect.top + 115f, namePaint)

            // Formatted Time in selected Number font
            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = numberFont
                color = if (isActive) (if (isLight) Color.BLACK else Color.WHITE) else inactiveTextColor
                textSize = 34f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(time, cx, rect.bottom - 46f, timePaint)
        }

        return bitmap
    }

    // =========================================================================
    // 2. NEXT PRAYER ARABIC BANNER (4x2 Majestic Luxury Banner)
    // =========================================================================
    private fun renderClassicNextPrayerArabic(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 34f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val goldColor = Color.parseColor("#D4AF37")
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)
        val numberFont = getNumberTypeface(context, numberFontKey)

        val (schedule, city) = getPrayerSchedule(context, today)
        val nextObligatory = schedule.getNextObligatoryPrayer(LocalTime.now())
        val diffMins = (nextObligatory.remainingSeconds / 60)
        val diffHours = diffMins / 60
        val countdownAr = if (diffHours > 0) "متبقي ${diffHours} س و ${diffMins % 60} د" else "متبقي ${diffMins} دقيقة"

        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val timeStr = nextObligatory.prayer.time.format(fmt)
        val amPm = if (nextObligatory.prayer.time.hour >= 12) "م" else "ص"

        // Top Row: "الصلاة القادمة • UPCOMING PRAYER" on right, City on left
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = mutedColor
            textSize = 21f
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.04f
        }
        canvas.drawText("الصلاة القادمة • UPCOMING PRAYER", rect.right - 36f, rect.top + 46f, headerPaint)

        val cityPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 21f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("📍 $city", rect.left + 36f, rect.top + 46f, cityPaint)

        // Right Section: Huge Majestic Arabic Calligraphy Name e.g. "صلاة العصر"
        val arabicNamePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            shader = LinearGradient(
                rect.right - 280f, rect.centerY() - 30f, rect.right - 30f, rect.centerY() + 30f,
                intArrayOf(Color.WHITE, Color.parseColor("#F5E6C8"), Color.parseColor("#E2C68A")),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            textSize = 72f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(nextObligatory.prayer.type.displayNameAr, rect.right - 36f, rect.centerY() + 25f, arabicNamePaint)

        // Left Section: Huge Digital Time in selected Number Font
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            color = whiteColor
            textSize = 86f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(timeStr, rect.left + 36f, rect.top + 140f, timePaint)

        val amPmPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = goldColor
            textSize = 26f
            textAlign = Paint.Align.LEFT
        }
        val timeWidth = timePaint.measureText(timeStr)
        canvas.drawText(amPm, rect.left + 42f + timeWidth, rect.top + 100f, amPmPaint)

        // Bottom Left: Glowing Countdown Pill
        val pillRect = RectF(rect.left + 36f, rect.bottom - 74f, rect.left + 240f, rect.bottom - 24f)
        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 212, 175, 55)
        }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillBg)

        val pillStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = Color.parseColor("#80D4AF37")
        }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillStroke)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(countdownAr, pillRect.centerX(), pillRect.centerY() + 7f, countPaint)

        // Bottom Right: English prayer name subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = mutedColor
            textSize = 22f
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.08f
        }
        canvas.drawText(nextObligatory.prayer.type.name, rect.right - 36f, rect.bottom - 42f, subPaint)

        return bitmap
    }

    // =========================================================================
    // 3. NEXT PRAYER COMPACT PILL (2x1 Card)
    // =========================================================================
    private fun renderClassicNextPrayerPill(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)
        val numberFont = getNumberTypeface(context, numberFontKey)

        val (schedule, city) = getPrayerSchedule(context, today)
        val nextObligatory = schedule.getNextObligatoryPrayer(LocalTime.now())
        val diffMins = (nextObligatory.remainingSeconds / 60)
        val diffHours = diffMins / 60
        val countdownStr = if (diffHours > 0) "in ${diffHours}h ${diffMins % 60}m" else "in ${diffMins}m"

        val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val timeStr = nextObligatory.prayer.time.format(fmt)

        // Top Row: NOOR • City
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 24f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.12f
        }
        canvas.drawText("NOOR • نُور", rect.left + 35f, rect.top + 52f, brandPaint)

        val cityPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(city, rect.right - 35f, rect.top + 52f, cityPaint)

        // Large Arabic Prayer Name with English subtitle
        val arNamePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 54f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(nextObligatory.prayer.type.displayNameAr, rect.left + 35f, rect.top + 130f, arNamePaint)

        val enNamePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = mutedColor
            textSize = 26f
            textAlign = Paint.Align.LEFT
        }
        val arWidth = arNamePaint.measureText(nextObligatory.prayer.type.displayNameAr)
        canvas.drawText("(${nextObligatory.prayer.type.name})", rect.left + 48f + arWidth, rect.top + 120f, enNamePaint)

        // Bottom Row: Time + Countdown Pill
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            color = whiteColor
            textSize = 36f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(timeStr, rect.left + 35f, rect.bottom - 36f, timePaint)

        // Countdown Pill
        val pillRect = RectF(rect.right - 190f, rect.bottom - 68f, rect.right - 35f, rect.bottom - 22f)
        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 255, 255, 255) }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillBg)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = whiteColor
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(countdownStr, pillRect.centerX(), pillRect.centerY() + 7f, countPaint)

        return bitmap
    }

    // =========================================================================
    // 4. COMPACT PRAYER LIST (2x2 Card)
    // =========================================================================
    private fun renderClassicPrayerList(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 30f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val activeTextColor = if (isLight) Color.parseColor("#10141A") else Color.WHITE
        val inactiveTextColor = if (isLight) Color.parseColor("#7A8292") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)
        val numberFont = getNumberTypeface(context, numberFontKey)

        val (schedule, _) = getPrayerSchedule(context, today)
        val nextObligatory = schedule.getNextObligatoryPrayer(LocalTime.now())
        val activeType = nextObligatory.prayer.type

        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val rows = listOf(
            Triple(PrayerType.FAJR, "الفجر • Fajr", schedule.fajr.format(fmt)),
            Triple(PrayerType.DHUHR, "الظهر • Dhuhr", schedule.dhuhr.format(fmt)),
            Triple(PrayerType.ASR, "العصر • Asr", schedule.asr.format(fmt)),
            Triple(PrayerType.MAGHRIB, "المغرب • Maghrib", schedule.maghrib.format(fmt)),
            Triple(PrayerType.ISHA, "العشاء • Isha", schedule.isha.format(fmt))
        )

        val rowHeight = (rect.height() - 20f) / 5f
        for (i in rows.indices) {
            val (type, label, time) = rows[i]
            val ry = rect.top + 10f + rowHeight * i
            val isActive = type == activeType

            if (isActive) {
                val hRect = RectF(rect.left + 14f, ry + 2f, rect.right - 14f, ry + rowHeight - 2f)
                val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 212, 175, 55) }
                canvas.drawRoundRect(hRect, 10f, 10f, hPaint)
            }

            // Dot
            val dotCx = rect.left + 35f
            val dotCy = ry + rowHeight / 2f
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isActive) Color.parseColor("#D4AF37") else Color.argb(90, 154, 160, 172)
            }
            canvas.drawCircle(dotCx, dotCy, if (isActive) 6f else 4f, dotPaint)

            // Name
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = if (isActive) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                color = if (isActive) activeTextColor else inactiveTextColor
                textSize = 27f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(label, dotCx + 20f, ry + rowHeight * 0.68f, namePaint)

            // Time
            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = numberFont
                color = if (isActive) activeTextColor else inactiveTextColor
                textSize = 28f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(time, rect.right - 35f, ry + rowHeight * 0.68f, timePaint)
        }

        return bitmap
    }

    // =========================================================================
    // 5. FULL SCHEDULE TABLE CARD (4x3 Comprehensive Card)
    // =========================================================================
    private fun renderClassicFullSchedule(context: Context, today: LocalDate, hijriDay: Int, hijriMonth: Int, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)
        val numberFont = getNumberTypeface(context, numberFontKey)

        val (schedule, city) = getPrayerSchedule(context, today)
        val monthNameAr = CalendarRepository.HIJRI_MONTHS_AR.getOrElse(hijriMonth - 1) { "ربيع الثاني" }

        // Header: PRAYER SCHEDULE • City • Hijri Date
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = whiteColor
            textSize = 28f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.06f
        }
        canvas.drawText("PRAYER SCHEDULE • مواقيت الصلاة", rect.left + 35f, rect.top + 48f, brandPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = Color.parseColor("#D4AF37")
            textSize = 21f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("$city • $hijriDay $monthNameAr ١٤٤٨", rect.right - 35f, rect.top + 48f, subPaint)

        // 5 Column Cards: Fajr, Dhuhr, Asr, Maghrib, Isha
        val fmt = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)
        val items = listOf(
            Pair("الفجر", schedule.fajr.format(fmt)),
            Pair("الظهر", schedule.dhuhr.format(fmt)),
            Pair("العصر", schedule.asr.format(fmt)),
            Pair("المغرب", schedule.maghrib.format(fmt)),
            Pair("العشاء", schedule.isha.format(fmt))
        )

        val nextObligatory = schedule.getNextObligatoryPrayer(LocalTime.now())
        val activeType = nextObligatory.prayer.type
        val types = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)

        val colWidth = (rect.width() - 36f) / 5f
        val topOffset = rect.top + 72f
        val botOffset = rect.bottom - 18f

        for (i in items.indices) {
            val (nameAr, time) = items[i]
            val cx = rect.left + 18f + colWidth * i + colWidth / 2f
            val isActive = types[i] == activeType

            val cellRect = RectF(rect.left + 18f + colWidth * i + 4f, topOffset, rect.left + 18f + colWidth * (i + 1) - 4f, botOffset)
            val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isActive) Color.argb(45, 212, 175, 55) else Color.argb(20, 255, 255, 255)
            }
            canvas.drawRoundRect(cellRect, 16f, 16f, cellPaint)

            if (isActive) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.parseColor("#D4AF37")
                }
                canvas.drawRoundRect(cellRect, 16f, 16f, strokePaint)
            }

            val nPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = arabicFont
                color = if (isActive) Color.WHITE else mutedColor
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(nameAr, cx, cellRect.centerY() - 15f, nPaint)

            val tPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = numberFont
                color = if (isActive) Color.WHITE else mutedColor
                textSize = 30f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(time, cx, cellRect.centerY() + 35f, tPaint)
        }

        return bitmap
    }

    // =========================================================================
    // 6. PRAYER STREAK & TRACKER (2x2 Card)
    // =========================================================================
    private fun renderClassicPrayerStreak(context: Context, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val doneCount = prefs.getInt("prayers_done_count", 4)
        val f = prefs.getBoolean("fajr_done", true)
        val d = prefs.getBoolean("dhuhr_done", true)
        val a = prefs.getBoolean("asr_done", true)
        val m = prefs.getBoolean("maghrib_done", true)
        val i = prefs.getBoolean("isha_done", false)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("PRAYER TRACKER • متابعة الصلوات", rect.left + 35f, rect.top + 52f, labelPaint)

        val streakTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 40f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("أتممت $doneCount من ٥ صلوات اليوم", rect.left + 35f, rect.top + 115f, streakTextPaint)

        // 5 Prayer Status Dots Row
        val prayers = listOf("فجر" to f, "ظهر" to d, "عصر" to a, "مغرب" to m, "عشاء" to i)
        val colWidth = (rect.width() - 70f) / 5f
        for (idx in prayers.indices) {
            val (name, isDone) = prayers[idx]
            val cx = rect.left + 35f + colWidth * idx + colWidth / 2f

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDone) Color.parseColor("#34C759") else Color.argb(60, 255, 255, 255)
            }
            canvas.drawCircle(cx, rect.bottom - 75f, 15f, dotPaint)

            if (isDone) {
                val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✓", cx, rect.bottom - 69f, checkPaint)
            }

            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                typeface = arabicFont
                color = if (isDone) whiteColor else mutedColor
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(name, cx, rect.bottom - 32f, namePaint)
        }

        return bitmap
    }

    // =========================================================================
    // 7. SMART QIBLA COMPASS (2x2 Card)
    // =========================================================================
    private fun renderClassicQibla(context: Context, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val numberFont = getNumberTypeface(context, numberFontKey)
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("QIBLA COMPASS • اتجاه القبلة", rect.left + 35f, rect.top + 52f, labelPaint)

        val degPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            color = whiteColor
            textSize = 76f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("118°", rect.left + 35f, rect.top + 140f, degPaint)

        val dirPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = mutedColor
            textSize = 28f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("باتجاه الكعبة المشرفة بمكة المكرمة", rect.left + 35f, rect.bottom - 45f, dirPaint)

        // Kaaba Compass Dial on Right
        val iconCx = rect.right - 100f
        val iconCy = rect.centerY()
        val cPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(40, 212, 175, 55) }
        canvas.drawCircle(iconCx, iconCy, 58f, cPaint)

        val dialStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#80D4AF37")
        }
        canvas.drawCircle(iconCx, iconCy, 58f, dialStroke)

        val kPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            textSize = 44f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🕋", iconCx, iconCy + 15f, kPaint)

        return bitmap
    }

    // =========================================================================
    // 8. QURAN AYAH OF THE DAY (4x2 Ornate Card)
    // =========================================================================
    private fun renderClassicQuranVerse(context: Context, arabicFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val prefs = context.getSharedPreferences("quran_ayah_widget_prefs", Context.MODE_PRIVATE)
        val index = prefs.getInt("ayah_index", 0) % QuranVerseWidgetProvider.AYAT.size
        val ayahItem = QuranVerseWidgetProvider.AYAT[index]

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("﴿ آيَةُ اليَوْمِ ﴾ • ${ayahItem.surahReference}", rect.centerX(), rect.top + 48f, labelPaint)

        // Ornate Quranic Ayah in selected calligraphy font
        val ayahPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 38f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(ayahItem.arabicWithBrackets, rect.centerX(), rect.centerY() + 15f, ayahPaint)

        // Translation
        val transPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 21f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(ayahItem.translation, rect.centerX(), rect.bottom - 38f, transPaint)

        return bitmap
    }

    // =========================================================================
    // 9. QURAN CONTINUE READING (2x2 Card)
    // =========================================================================
    private fun renderClassicQuranContinue(context: Context, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val surah = prefs.getString("reading_surah", "Al-Kahf") ?: "Al-Kahf"
        val ayah = prefs.getInt("reading_ayah", 28)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("وِرْدُ التِّلاوَة • CONTINUED READING", rect.left + 35f, rect.top + 52f, labelPaint)

        val surahPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 46f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("سورة $surah", rect.left + 35f, rect.top + 120f, surahPaint)

        val ayahPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 26f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("الآية رقم $ayah • استكمال الورد", rect.left + 35f, rect.top + 170f, ayahPaint)

        // Continue Reading Button
        val btnRect = RectF(rect.right - 210f, rect.bottom - 75f, rect.right - 35f, rect.bottom - 25f)
        val btnBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D4AF37") }
        canvas.drawRoundRect(btnRect, 16f, 16f, btnBg)

        val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تابع القراءة ◀", btnRect.centerX(), btnRect.centerY() + 7f, btnTextPaint)

        return bitmap
    }

    // =========================================================================
    // 10. DIGITAL TASBEEH & AZKAR COUNTER (2x2 Card)
    // =========================================================================
    private fun renderClassicAzkarCounter(context: Context, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val numberFont = getNumberTypeface(context, numberFontKey)
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val prefs = context.getSharedPreferences("azkar_widget_prefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("azkar_count", 33)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", rect.centerX(), rect.top + 65f, titlePaint)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            color = Color.parseColor("#D4AF37")
            textSize = 76f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(count.toString(), rect.centerX(), rect.centerY() + 25f, countPaint)

        val tapPillRect = RectF(rect.centerX() - 120f, rect.bottom - 70f, rect.centerX() + 120f, rect.bottom - 24f)
        val tapPillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 255, 255, 255) }
        canvas.drawRoundRect(tapPillRect, 16f, 16f, tapPillBg)

        val tapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = whiteColor
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("+ اضغط للتسبيح", tapPillRect.centerX(), tapPillRect.centerY() + 7f, tapPaint)

        return bitmap
    }

    // =========================================================================
    // 11. DUAA OF THE DAY (4x2 Card)
    // =========================================================================
    private fun renderClassicDuaa(context: Context, arabicFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val duaa = DuaaRepository.DUAA_LIST.firstOrNull() ?: DuaaRepository.DUAA_LIST[0]

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("دُعَاءُ اليَوْمِ • ${duaa.category.titleAr}", rect.centerX(), rect.top + 48f, labelPaint)

        val duaaPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 38f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(duaa.arabicText, rect.centerX(), rect.centerY() + 15f, duaaPaint)

        val transPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(duaa.translation, rect.centerX(), rect.bottom - 38f, transPaint)

        return bitmap
    }

    // =========================================================================
    // 12. COMPREHENSIVE HIJRI DATE CARD (2x2 Card)
    // =========================================================================
    private fun renderClassicHijriDateCard(context: Context, today: LocalDate, hijriDay: Int, hijriMonth: Int, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val numberFont = getNumberTypeface(context, numberFontKey)
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val monthNameAr = CalendarRepository.HIJRI_MONTHS_AR.getOrElse(hijriMonth - 1) { "ربيع الثاني" }
        val dayNameAr = DAY_NAMES_AR[today.dayOfWeek] ?: "الخميس"
        val gregorianFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)

        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            color = Color.parseColor("#D4AF37")
            textSize = 86f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(hijriDay.toString(), rect.left + 35f, rect.top + 95f, numPaint)

        val myPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 34f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("$dayNameAr • $monthNameAr ١٤٤٨ هـ", rect.left + 35f, rect.top + 145f, myPaint)

        val gregPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 24f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(today.format(gregorianFmt), rect.left + 35f, rect.bottom - 42f, gregPaint)

        return bitmap
    }

    // =========================================================================
    // 13. UPCOMING ISLAMIC HOLIDAY (2x2 Card)
    // =========================================================================
    private fun renderClassicIslamicHoliday(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val holidays = CalendarRepository.getUpcomingHolidays(today, -2)
        val next = holidays.firstOrNull()
        val nameAr = next?.holiday?.nameAr ?: "المولد النبوي الشريف"
        val remaining = next?.daysRemaining ?: 14
        val dateStr = if (next != null) "${next.holiday.hijriDay} ${CalendarRepository.HIJRI_MONTHS_AR[next.holiday.hijriMonth - 1]} ١٤٤٨ هـ" else "١٢ ربيع الأول ١٤٤٨ هـ"

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("المناسبات الإسلامية • ISLAMIC EVENT", rect.left + 35f, rect.top + 52f, labelPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 42f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(nameAr, rect.left + 35f, rect.top + 120f, namePaint)

        val pillRect = RectF(rect.left + 35f, rect.top + 155f, rect.left + 220f, rect.top + 205f)
        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 212, 175, 55) }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillBg)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = whiteColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("متبقي $remaining يوماً", pillRect.centerX(), pillRect.centerY() + 8f, countPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 24f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(dateStr, rect.right - 35f, rect.bottom - 42f, datePaint)

        return bitmap
    }

    // =========================================================================
    // 14. RAMADAN COUNTDOWN (2x2 Card)
    // =========================================================================
    private fun renderClassicRamadanCountdown(context: Context, today: LocalDate, arabicFontKey: String, numberFontKey: String, bgStyle: String, patternColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(12f, 12f, (WIDTH - 12).toFloat(), (HEIGHT - 12).toFloat())
        val radius = 32f

        drawCardBackground(canvas, rect, radius, bgStyle, patternColor, context)

        val isLight = bgStyle == "light"
        val whiteColor = if (isLight) Color.parseColor("#12161E") else Color.WHITE
        val mutedColor = if (isLight) Color.parseColor("#646C7C") else Color.parseColor("#9AA0AC")
        val arabicFont = getArabicTypeface(context, arabicFontKey)

        val holidays = CalendarRepository.getUpcomingHolidays(today, -2)
        val ramadan = holidays.firstOrNull { it.holiday.hijriMonth == 9 }
        val daysLeft = ramadan?.daysRemaining ?: 128

        val (schedule, _) = getPrayerSchedule(context, today)
        val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#D4AF37")
            textSize = 22f
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("رَمَضَانُ المُبَارَك • RAMADAN", rect.left + 35f, rect.top + 52f, labelPaint)

        val statePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = whiteColor
            textSize = 44f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("اللهم بلّغنا رمضان المبارك 🌙", rect.left + 35f, rect.top + 120f, statePaint)

        val pillRect = RectF(rect.left + 35f, rect.top + 155f, rect.left + 240f, rect.top + 205f)
        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(45, 212, 175, 55) }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillBg)

        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            color = whiteColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("متبقي $daysLeft يوماً", pillRect.centerX(), pillRect.centerY() + 8f, countPaint)

        val iftarPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = Typeface.DEFAULT
            color = mutedColor
            textSize = 24f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("المغرب: ${schedule.maghrib.format(fmt)}", rect.right - 35f, rect.bottom - 42f, iftarPaint)

        return bitmap
    }
}
