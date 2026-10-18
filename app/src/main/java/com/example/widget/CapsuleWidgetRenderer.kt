package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.R
import java.time.DayOfWeek
import java.util.Locale

/**
 * High-precision canvas renderer for the 3 capsule (1x2) calendar widgets:
 * 1. Day of Week Widget
 * 2. Gregorian Date Widget
 * 3. Hijri Date Widget
 *
 * Supports independent styling per widget (Dark glass, Pearl Light, or Calligraphy Pattern with color palette).
 */
object CapsuleWidgetRenderer {

    const val BITMAP_WIDTH = 580
    const val BITMAP_HEIGHT = 270

    // Arabic Days of Week (Standard pristine spelling)
    val DAY_NAMES_AR = mapOf(
        DayOfWeek.MONDAY to "الإثنين",
        DayOfWeek.TUESDAY to "الثلاثاء",
        DayOfWeek.WEDNESDAY to "الأربعاء",
        DayOfWeek.THURSDAY to "الخميس",
        DayOfWeek.FRIDAY to "الجمعة",
        DayOfWeek.SATURDAY to "السبت",
        DayOfWeek.SUNDAY to "الأحد"
    )

    // Gregorian Months in Arabic
    val GREGORIAN_MONTHS_AR = listOf(
        "يناير",    // 1
        "فبراير",   // 2
        "مارس",     // 3
        "أبريل",    // 4
        "مايو",      // 5
        "يونيو",    // 6
        "يوليو",    // 7
        "أغسطس",   // 8
        "سبتمبر",  // 9
        "أكتوبر",   // 10
        "نوفمبر",   // 11
        "ديسمبر"   // 12
    )

    // Hijri Months in Arabic
    val HIJRI_MONTHS_AR = listOf(
        "المحرم",    // 1
        "صفر",        // 2
        "ربيع الأول", // 3
        "ربيع الثاني", // 4
        "جمادى الأولى", // 5
        "جمادى الآخرة", // 6
        "رجب",        // 7
        "شعبان",      // 8
        "رمضان",     // 9
        "شوال",      // 10
        "ذو القعدة",   // 11
        "ذو الحجة"    // 12
    )

    fun resolveArabicTypeface(context: Context, fontPref: String?): Typeface {
        val fontRes = when (fontPref) {
            "taleed_thuluth", "taleed" -> R.font.taleed_thuluth
            "thuluth", "a_thuluth" -> R.font.a_thuluth
            "amoshref_thuluth" -> R.font.amoshref_thuluth
            "decotype_thuluth" -> R.font.decotype_thuluth
            "decotype_thuluth_ii" -> R.font.decotype_thuluth_ii
            "dima_thuluth" -> R.font.dima_thuluth
            "elsawaf_thuluth" -> R.font.elsawaf_thuluth
            "layla_thuluth" -> R.font.layla_thuluth
            "thulth_mazar" -> R.font.thulth_mazar
            "ruqaa" -> R.font.aref_ruqaa
            "scheherazade" -> R.font.scheherazade_new
            "amiri" -> R.font.amiri
            "cairo" -> R.font.cairo
            "tajawal" -> R.font.tajawal
            else -> R.font.taleed_thuluth
        }
        return try {
            ResourcesCompat.getFont(context, fontRes)
                ?: ResourcesCompat.getFont(context, R.font.taleed_thuluth)
                ?: ResourcesCompat.getFont(context, R.font.a_thuluth)
                ?: ResourcesCompat.getFont(context, R.font.amoshref_thuluth)
                ?: ResourcesCompat.getFont(context, R.font.amiri)
                ?: Typeface.create("serif", Typeface.BOLD)
        } catch (e: Exception) {
            Typeface.create("serif", Typeface.BOLD)
        }
    }

    fun resolveNumberTypeface(context: Context, fontPref: String?): Typeface {
        val fontRes = when (fontPref) {
            "oswald" -> R.font.oswald
            "bebas" -> R.font.bebas_neue
            "montserrat" -> R.font.montserrat
            "outfit" -> R.font.outfit
            "rubik" -> R.font.rubik
            "poppins" -> R.font.poppins
            else -> R.font.oswald
        }
        return try {
            ResourcesCompat.getFont(context, fontRes)
                ?: ResourcesCompat.getFont(context, R.font.oswald)
                ?: Typeface.create("sans-serif-condensed", Typeface.BOLD)
        } catch (e: Exception) {
            Typeface.create("sans-serif-condensed", Typeface.BOLD)
        }
    }

    // Global / Default Fallbacks
    fun getBackgroundStyle(context: Context): String {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return prefs.getString("capsule_background_style", "image") ?: "image"
    }

    fun getPatternColor(context: Context): Int {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("capsule_pattern_color", Color.parseColor("#26364A"))
    }

    fun getArabicTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return resolveArabicTypeface(context, prefs.getString("capsule_arabic_font", "taleed_thuluth"))
    }

    fun getNumberTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return resolveNumberTypeface(context, prefs.getString("capsule_number_font", "oswald"))
    }

    // --- DAY OF WEEK WIDGET SPECIFIC PREFERENCES ---
    fun getDayBackgroundStyle(context: Context): String {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return prefs.getString("capsule_day_bg_style", null) ?: getBackgroundStyle(context)
    }

    fun getDayPatternColor(context: Context): Int {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val widgetColor = prefs.getInt("widget_color_capsule_day", -1)
        if (widgetColor != -1) return widgetColor
        return if (prefs.contains("capsule_day_pattern_color")) {
            prefs.getInt("capsule_day_pattern_color", Color.parseColor("#26364A"))
        } else {
            getPatternColor(context)
        }
    }

    fun getDayArabicTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val font = prefs.getString("capsule_day_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth")
        return resolveArabicTypeface(context, font)
    }

    // --- GREGORIAN WIDGET SPECIFIC PREFERENCES ---
    fun getGregorianBackgroundStyle(context: Context): String {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return prefs.getString("capsule_gregorian_bg_style", null) ?: getBackgroundStyle(context)
    }

    fun getGregorianPatternColor(context: Context): Int {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val widgetColor = prefs.getInt("widget_color_capsule_gregorian", -1)
        if (widgetColor != -1) return widgetColor
        return if (prefs.contains("capsule_gregorian_pattern_color")) {
            prefs.getInt("capsule_gregorian_pattern_color", Color.parseColor("#5A3B33"))
        } else {
            prefs.getInt("capsule_pattern_color", Color.parseColor("#5A3B33"))
        }
    }

    fun getGregorianArabicTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val font = prefs.getString("capsule_gregorian_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth")
        return resolveArabicTypeface(context, font)
    }

    fun getGregorianNumberTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val font = prefs.getString("capsule_gregorian_number_font", null) ?: prefs.getString("capsule_number_font", "oswald")
        return resolveNumberTypeface(context, font)
    }

    // --- HIJRI WIDGET SPECIFIC PREFERENCES ---
    fun getHijriBackgroundStyle(context: Context): String {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        return prefs.getString("capsule_hijri_bg_style", null) ?: getBackgroundStyle(context)
    }

    fun getHijriPatternColor(context: Context): Int {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val widgetColor = prefs.getInt("widget_color_capsule_hijri", -1)
        if (widgetColor != -1) return widgetColor
        return if (prefs.contains("capsule_hijri_pattern_color")) {
            prefs.getInt("capsule_hijri_pattern_color", Color.parseColor("#26364A"))
        } else {
            getPatternColor(context)
        }
    }

    fun getHijriArabicTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val font = prefs.getString("capsule_hijri_arabic_font", null) ?: prefs.getString("capsule_arabic_font", "taleed_thuluth")
        return resolveArabicTypeface(context, font)
    }

    fun getHijriNumberTypeface(context: Context): Typeface {
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val font = prefs.getString("capsule_hijri_number_font", null) ?: prefs.getString("capsule_number_font", "oswald")
        return resolveNumberTypeface(context, font)
    }

    /**
     * Draws the authentic Arabic Calligraphy Watermark Pattern Background (Image Style)
     * using the selected palette color, scattered translucent calligraphy letters,
     * directional specular bevels, and top-half glossy lens reflection.
     */
    private fun drawCalligraphyPatternBackground(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        baseColor: Int,
        context: Context
    ) {
        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)

        // Highlight shade (lighter & specular)
        val colTop = Color.argb(
            255,
            (r * 1.32f).toInt().coerceIn(0, 255),
            (g * 1.32f).toInt().coerceIn(0, 255),
            (b * 1.32f).toInt().coerceIn(0, 255)
        )
        val colMid = Color.argb(255, r, g, b)
        val colBottom = Color.argb(
            255,
            (r * 0.68f).toInt().coerceIn(0, 255),
            (g * 0.68f).toInt().coerceIn(0, 255),
            (b * 0.68f).toInt().coerceIn(0, 255)
        )
        val colDeep = Color.argb(
            255,
            (r * 0.45f).toInt().coerceIn(0, 255),
            (g * 0.45f).toInt().coerceIn(0, 255),
            (b * 0.45f).toInt().coerceIn(0, 255)
        )

        // 1. Base Gradient Fill
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top,
                rect.right, rect.bottom,
                intArrayOf(colTop, colMid, colBottom, colDeep),
                floatArrayOf(0f, 0.35f, 0.72f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)

        // 2. Artistic Calligraphy Watermark Glyphs inside clipped capsule
        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(rect, radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val patternFont = try {
            ResourcesCompat.getFont(context, R.font.taleed_thuluth)
                ?: ResourcesCompat.getFont(context, R.font.amoshref_thuluth)
                ?: ResourcesCompat.getFont(context, R.font.a_thuluth)
                ?: Typeface.create("serif", Typeface.BOLD)
        } catch (e: Exception) {
            Typeface.create("serif", Typeface.BOLD)
        }

        val patternPaintLight = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = patternFont
            color = Color.WHITE
            alpha = 24 // ~9.5% opacity for subtle elegance matching images
            textAlign = Paint.Align.CENTER
        }

        val patternPaintDark = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = patternFont
            color = Color.BLACK
            alpha = 34 // ~13% dark depth shadow
            textAlign = Paint.Align.CENTER
        }

        // Scattered authentic Arabic calligraphy letters & swashes across the capsule (Bigger & Majestic)
        val calligGlyphs = listOf(
            Triple("ع", 125f, Pair(rect.left + 50f, rect.top + 75f)),
            Triple("و", 95f, Pair(rect.left + 120f, rect.top + 50f)),
            Triple("ص", 115f, Pair(rect.left + 200f, rect.top + 85f)),
            Triple("م", 130f, Pair(rect.left + 285f, rect.top + 60f)),
            Triple("ك", 120f, Pair(rect.left + 375f, rect.top + 80f)),
            Triple("ل", 155f, Pair(rect.left + 455f, rect.top + 65f)),
            Triple("ن", 105f, Pair(rect.right - 45f, rect.top + 85f)),
            Triple("ح", 125f, Pair(rect.left + 75f, rect.bottom - 45f)),
            Triple("ر", 100f, Pair(rect.left + 165f, rect.bottom - 35f)),
            Triple("ق", 118f, Pair(rect.left + 255f, rect.bottom - 50f)),
            Triple("هـ", 110f, Pair(rect.left + 345f, rect.bottom - 38f)),
            Triple("ي", 128f, Pair(rect.left + 435f, rect.bottom - 45f)),
            Triple("ـ", 160f, Pair(rect.right - 60f, rect.bottom - 35f)),
            Triple("ء", 70f, Pair(rect.left + 40f, rect.centerY())),
            Triple("لا", 135f, Pair(rect.right - 40f, rect.centerY()))
        )

        for ((glyph, size, pos) in calligGlyphs) {
            patternPaintDark.textSize = size
            canvas.drawText(glyph, pos.first + 2f, pos.second + 2f, patternPaintDark)
            patternPaintLight.textSize = size
            canvas.drawText(glyph, pos.first, pos.second, patternPaintLight)
        }

        // 3. Glass Lens Reflection / Sheen (Upper ~44% curved glossy highlight)
        val sheenHeight = rect.height() * 0.44f
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.centerX(), rect.top,
                rect.centerX(), rect.top + sheenHeight,
                intArrayOf(
                    Color.argb(48, 255, 255, 255),
                    Color.argb(12, 255, 255, 255),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val sheenRect = RectF(rect.left, rect.top, rect.right, rect.top + sheenHeight)
        canvas.drawRect(sheenRect, sheenPaint)

        // 4. Bottom Ambient Light Bounce
        val bounceHeight = rect.height() * 0.22f
        val bouncePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.centerX(), rect.bottom,
                rect.centerX(), rect.bottom - bounceHeight,
                Color.argb(16, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(rect.left, rect.bottom - bounceHeight, rect.right, rect.bottom), bouncePaint)
        canvas.restore()

        // 5. Directional Glass Bevel Stroke (Specular reflection on top-left)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
            shader = LinearGradient(
                rect.left, rect.top,
                rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(175, 255, 255, 255),
                    Color.argb(75, 255, 255, 255),
                    Color.argb(25, 255, 255, 255),
                    Color.argb(12, 255, 255, 255)
                ),
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    /**
     * Draws standard 3D frosted glass capsule (Dark or Pearl Light).
     */
    private fun drawGlassCapsule(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        gradientColors: IntArray,
        gradientPositions: FloatArray,
        isDark: Boolean
    ) {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top,
                rect.right, rect.bottom,
                gradientColors,
                gradientPositions,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)

        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(rect, radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val sheenHeight = rect.height() * 0.44f
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.centerX(), rect.top,
                rect.centerX(), rect.top + sheenHeight,
                intArrayOf(
                    if (isDark) Color.argb(48, 255, 255, 255) else Color.argb(65, 255, 255, 255),
                    if (isDark) Color.argb(12, 255, 255, 255) else Color.argb(18, 255, 255, 255),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val sheenRect = RectF(rect.left, rect.top, rect.right, rect.top + sheenHeight)
        canvas.drawRect(sheenRect, sheenPaint)

        val bounceHeight = rect.height() * 0.22f
        val bouncePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.centerX(), rect.bottom,
                rect.centerX(), rect.bottom - bounceHeight,
                if (isDark) Color.argb(16, 255, 255, 255) else Color.argb(25, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(rect.left, rect.bottom - bounceHeight, rect.right, rect.bottom), bouncePaint)
        canvas.restore()

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
            shader = LinearGradient(
                rect.left, rect.top,
                rect.right, rect.bottom,
                if (isDark) {
                    intArrayOf(
                        Color.argb(165, 255, 255, 255),
                        Color.argb(70, 255, 255, 255),
                        Color.argb(22, 255, 255, 255),
                        Color.argb(10, 255, 255, 255)
                    )
                } else {
                    intArrayOf(
                        Color.argb(230, 255, 255, 255),
                        Color.argb(140, 255, 255, 255),
                        Color.argb(70, 180, 185, 200),
                        Color.argb(35, 140, 145, 160)
                    )
                },
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    /**
     * Widget 1: Day of the Week Widget (Image 1)
     */
    fun renderDayOfWeek(
        context: Context,
        dayOfWeek: DayOfWeek,
        customArabicFont: String? = null,
        customBgStyle: String? = null,
        customPatternColor: Int? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(14f, 14f, (BITMAP_WIDTH - 14).toFloat(), (BITMAP_HEIGHT - 14).toFloat())
        val radius = rect.height() / 2f

        val bgStyle = customBgStyle ?: getDayBackgroundStyle(context)
        val patternColor = customPatternColor ?: getDayPatternColor(context)

        when (bgStyle) {
            "image", "pattern" -> {
                drawCalligraphyPatternBackground(canvas, rect, radius, patternColor, context)
            }
            "light" -> {
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#F9FAFC"),
                        Color.parseColor("#E9ECF3"),
                        Color.parseColor("#D6DAE6"),
                        Color.parseColor("#C1C5D3")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.38f, 0.74f, 1f),
                    isDark = false
                )
            }
            else -> {
                // "dark"
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#3C4D64"),
                        Color.parseColor("#2E3B4D"),
                        Color.parseColor("#202835"),
                        Color.parseColor("#151C26")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.35f, 0.75f, 1f),
                    isDark = true
                )
            }
        }

        // Arabic Day Calligraphy
        val dayText = DAY_NAMES_AR[dayOfWeek] ?: "الخميس"
        val arabicFont = if (customArabicFont != null) resolveArabicTypeface(context, customArabicFont) else getDayArabicTypeface(context)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = if (bgStyle == "light") Color.parseColor("#15181E") else Color.WHITE
            textAlign = Paint.Align.CENTER
            if (bgStyle == "light") {
                setShadowLayer(4f, 0f, 2f, Color.argb(45, 0, 0, 0))
            } else {
                setShadowLayer(8f, 0f, 4f, Color.argb(190, 0, 0, 0))
            }
        }

        var targetSize = rect.height() * 0.48f
        textPaint.textSize = targetSize
        val maxAvailableWidth = rect.width() * 0.76f
        while (textPaint.measureText(dayText) > maxAvailableWidth && targetSize > 20f) {
            targetSize -= 1.5f
            textPaint.textSize = targetSize
        }

        val fontMetrics = textPaint.fontMetrics
        val textY = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f

        canvas.drawText(dayText, rect.centerX(), textY, textPaint)
        return bitmap
    }

    /**
     * Widget 2: Gregorian Date Widget (Image 2)
     */
    fun renderGregorian(
        context: Context,
        dayOfMonth: Int,
        monthNumber: Int,
        customArabicFont: String? = null,
        customNumberFont: String? = null,
        customBgStyle: String? = null,
        customPatternColor: Int? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(14f, 14f, (BITMAP_WIDTH - 14).toFloat(), (BITMAP_HEIGHT - 14).toFloat())
        val radius = rect.height() / 2f

        val bgStyle = customBgStyle ?: getGregorianBackgroundStyle(context)
        val patternColor = customPatternColor ?: getGregorianPatternColor(context)

        when (bgStyle) {
            "image", "pattern" -> {
                drawCalligraphyPatternBackground(canvas, rect, radius, patternColor, context)
            }
            "light" -> {
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#F9FAFC"),
                        Color.parseColor("#E9ECF3"),
                        Color.parseColor("#D6DAE6"),
                        Color.parseColor("#C1C5D3")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.38f, 0.74f, 1f),
                    isDark = false
                )
            }
            else -> {
                // "dark"
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#34363D"),
                        Color.parseColor("#27292F"),
                        Color.parseColor("#1B1C21"),
                        Color.parseColor("#111215")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.35f, 0.72f, 1f),
                    isDark = true
                )
            }
        }

        // Large Watermark Day Number (e.g. "07") with gradient glassy white color
        val dayStr = String.format(Locale.ENGLISH, "%02d", dayOfMonth)
        val numberFont = if (customNumberFont != null) resolveNumberTypeface(context, customNumberFont) else getGregorianNumberTypeface(context)

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            textSize = rect.height() * 0.94f
            textAlign = Paint.Align.CENTER
            if (bgStyle == "light") {
                color = Color.parseColor("#7D8292")
                alpha = 68
            } else {
                shader = LinearGradient(
                    rect.centerX(), rect.top + 20f,
                    rect.centerX(), rect.bottom - 20f,
                    intArrayOf(
                        Color.argb(220, 255, 255, 255),
                        Color.argb(135, 255, 255, 255),
                        Color.argb(55, 255, 255, 255)
                    ),
                    floatArrayOf(0.0f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
        }
        val wmMetrics = watermarkPaint.fontMetrics
        val wmY = rect.centerY() - (wmMetrics.ascent + wmMetrics.descent) / 2f
        canvas.drawText(dayStr, rect.centerX(), wmY, watermarkPaint)

        // Foreground Arabic Month Calligraphy (e.g. "أكتوبر")
        val monthIdx = (monthNumber - 1).coerceIn(0, 11)
        val monthText = GREGORIAN_MONTHS_AR[monthIdx]
        val arabicFont = if (customArabicFont != null) resolveArabicTypeface(context, customArabicFont) else getGregorianArabicTypeface(context)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = if (bgStyle == "light") Color.parseColor("#15181E") else Color.WHITE
            textAlign = Paint.Align.CENTER
            if (bgStyle == "light") {
                setShadowLayer(4f, 0f, 2f, Color.argb(45, 0, 0, 0))
            } else {
                setShadowLayer(10f, 0f, 4f, Color.argb(220, 0, 0, 0))
            }
        }

        var targetSize = rect.height() * 0.46f
        textPaint.textSize = targetSize
        val maxAvailableWidth = rect.width() * 0.76f
        while (textPaint.measureText(monthText) > maxAvailableWidth && targetSize > 20f) {
            targetSize -= 1.5f
            textPaint.textSize = targetSize
        }

        val fontMetrics = textPaint.fontMetrics
        val textY = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f

        canvas.drawText(monthText, rect.centerX(), textY, textPaint)
        return bitmap
    }

    /**
     * Widget 3: Hijri Date Widget (Image 3)
     */
    fun renderHijri(
        context: Context,
        hijriDay: Int,
        hijriMonth: Int,
        customArabicFont: String? = null,
        customNumberFont: String? = null,
        customBgStyle: String? = null,
        customPatternColor: Int? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(14f, 14f, (BITMAP_WIDTH - 14).toFloat(), (BITMAP_HEIGHT - 14).toFloat())
        val radius = rect.height() / 2f

        val bgStyle = customBgStyle ?: getHijriBackgroundStyle(context)
        val patternColor = customPatternColor ?: getHijriPatternColor(context)

        when (bgStyle) {
            "image", "pattern" -> {
                drawCalligraphyPatternBackground(canvas, rect, radius, patternColor, context)
            }
            "light" -> {
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#F9FAFC"),
                        Color.parseColor("#E9ECF3"),
                        Color.parseColor("#D6DAE6"),
                        Color.parseColor("#C1C5D3")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.38f, 0.74f, 1f),
                    isDark = false
                )
            }
            else -> {
                // "dark"
                drawGlassCapsule(
                    canvas = canvas,
                    rect = rect,
                    radius = radius,
                    gradientColors = intArrayOf(
                        Color.parseColor("#383C48"),
                        Color.parseColor("#2B2E38"),
                        Color.parseColor("#1F222B"),
                        Color.parseColor("#15171D")
                    ),
                    gradientPositions = floatArrayOf(0f, 0.35f, 0.75f, 1f),
                    isDark = true
                )
            }
        }

        // Large Watermark Hijri Day Number (e.g. "26") with the exact geometric number font
        val dayStr = String.format(Locale.ENGLISH, "%02d", hijriDay)
        val numberFont = if (customNumberFont != null) resolveNumberTypeface(context, customNumberFont) else getHijriNumberTypeface(context)

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = numberFont
            textSize = rect.height() * 0.94f
            textAlign = Paint.Align.CENTER
            if (bgStyle == "light") {
                color = Color.parseColor("#7D8292")
                alpha = 68
            } else {
                shader = LinearGradient(
                    rect.centerX(), rect.top + 20f,
                    rect.centerX(), rect.bottom - 20f,
                    intArrayOf(
                        Color.argb(220, 255, 255, 255),
                        Color.argb(135, 255, 255, 255),
                        Color.argb(55, 255, 255, 255)
                    ),
                    floatArrayOf(0.0f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
        }
        val wmMetrics = watermarkPaint.fontMetrics
        val wmY = rect.centerY() - (wmMetrics.ascent + wmMetrics.descent) / 2f
        canvas.drawText(dayStr, rect.centerX(), wmY, watermarkPaint)

        // Foreground Arabic Month Calligraphy (e.g. "ربيع الثاني")
        val monthIdx = (hijriMonth - 1).coerceIn(0, 11)
        val monthText = HIJRI_MONTHS_AR[monthIdx]
        val arabicFont = if (customArabicFont != null) resolveArabicTypeface(context, customArabicFont) else getHijriArabicTypeface(context)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = arabicFont
            color = if (bgStyle == "light") Color.parseColor("#15181E") else Color.WHITE
            textAlign = Paint.Align.CENTER
            if (bgStyle == "light") {
                setShadowLayer(4f, 0f, 2f, Color.argb(45, 0, 0, 0))
            } else {
                setShadowLayer(10f, 0f, 4f, Color.argb(220, 0, 0, 0))
            }
        }

        var targetSize = if (monthText.length > 8) rect.height() * 0.38f else rect.height() * 0.44f
        textPaint.textSize = targetSize
        val maxAvailableWidth = rect.width() * 0.76f
        while (textPaint.measureText(monthText) > maxAvailableWidth && targetSize > 20f) {
            targetSize -= 1.5f
            textPaint.textSize = targetSize
        }

        val fontMetrics = textPaint.fontMetrics
        val textY = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f

        canvas.drawText(monthText, rect.centerX(), textY, textPaint)
        return bitmap
    }
}
