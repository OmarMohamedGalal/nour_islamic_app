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
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Unified Live Canvas Preview Renderer for ALL widgets in the Noor app.
 * Renders high-definition bitmaps for live previews in the WidgetsScreen customizer.
 *
 * Supports:
 * - "default" (The original authentic look of the widget)
 * - "image" (Calligraphy Pattern Art with custom Color Palette / Hue)
 * - "dark" (Frosted Dark Glassmorphism)
 * - "light" (Pearl Light Glassmorphism)
 * - Custom Arabic Calligraphy Fonts & Number Watermark Fonts
 */
object AllWidgetsRenderer {

    // Common helper to draw background card for non-capsule widgets
    private fun drawWidgetBackground(
        canvas: Canvas,
        rect: RectF,
        radius: Float,
        bgStyle: String,
        patternColor: Int,
        context: Context,
        defaultGradientColors: IntArray = intArrayOf(
            Color.parseColor("#2B2E38"),
            Color.parseColor("#1F222B"),
            Color.parseColor("#15171D")
        )
    ) {
        when (bgStyle) {
            "image", "pattern" -> {
                val r = Color.red(patternColor)
                val g = Color.green(patternColor)
                val b = Color.blue(patternColor)

                val colTop = Color.argb(255, (r * 1.3f).toInt().coerceIn(0, 255), (g * 1.3f).toInt().coerceIn(0, 255), (b * 1.3f).toInt().coerceIn(0, 255))
                val colMid = Color.argb(255, r, g, b)
                val colBottom = Color.argb(255, (r * 0.65f).toInt().coerceIn(0, 255), (g * 0.65f).toInt().coerceIn(0, 255), (b * 0.65f).toInt().coerceIn(0, 255))

                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(rect.left, rect.top, rect.right, rect.bottom, intArrayOf(colTop, colMid, colBottom), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)

                // Draw scattered calligraphy watermark letters
                canvas.save()
                val clipPath = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
                canvas.clipPath(clipPath)

                val patternFont = try {
                    ResourcesCompat.getFont(context, R.font.taleed_thuluth) ?: Typeface.create("serif", Typeface.BOLD)
                } catch (e: Exception) {
                    Typeface.create("serif", Typeface.BOLD)
                }

                val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = patternFont
                    color = Color.WHITE
                    alpha = 22
                    textSize = rect.height() * 0.35f
                    textAlign = Paint.Align.CENTER
                }

                canvas.drawText("ع", rect.left + rect.width() * 0.2f, rect.top + rect.height() * 0.45f, glyphPaint)
                canvas.drawText("ص", rect.left + rect.width() * 0.5f, rect.top + rect.height() * 0.75f, glyphPaint)
                canvas.drawText("ل", rect.left + rect.width() * 0.8f, rect.top + rect.height() * 0.4f, glyphPaint)

                // Upper Glossy Sheen
                val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(rect.centerX(), rect.top, rect.centerX(), rect.top + rect.height() * 0.45f, intArrayOf(Color.argb(40, 255, 255, 255), Color.TRANSPARENT), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
                }
                canvas.drawRect(RectF(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.45f), sheenPaint)
                canvas.restore()
            }
            "light" -> {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(Color.parseColor("#F9FAFC"), Color.parseColor("#E9ECF3"), Color.parseColor("#C1C5D3")),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
            }
            "dark" -> {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(Color.parseColor("#34363D"), Color.parseColor("#202228"), Color.parseColor("#111215")),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
            }
            else -> {
                // "default"
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        defaultGradientColors,
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
            }
        }

        // Glass Border Stroke
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(Color.argb(160, 255, 255, 255), Color.argb(40, 255, 255, 255)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }

    // 1. PRAYER TIMES BAR (4x1 aspect ratio)
    fun renderPrayerBar(
        context: Context,
        arabicFontKey: String?,
        numberFontKey: String?,
        bgStyle: String,
        patternColor: Int
    ): Bitmap {
        val width = 800
        val height = 220
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(12f, 12f, width - 12f, height - 12f)
        drawWidgetBackground(canvas, rect, 24f, bgStyle, patternColor, context)

        val arabicFont = CapsuleWidgetRenderer.resolveArabicTypeface(context, arabicFontKey)
        val numberFont = CapsuleWidgetRenderer.resolveNumberTypeface(context, numberFontKey)

        val isLight = (bgStyle == "light")
        val activeTextColor = if (isLight) Color.parseColor("#111318") else Color.WHITE
        val inactiveTextColor = if (isLight) Color.parseColor("#6A7080") else Color.parseColor("#9096A5")

        val prayers = listOf(
            Triple("الفجر", "05:12", false),
            Triple("الظهر", "12:15", false),
            Triple("العصر", "03:30", true), // Active
            Triple("المغرب", "06:05", false),
            Triple("العشاء", "07:35", false)
        )

        val colWidth = rect.width() / 5f
        for ((idx, item) in prayers.withIndex()) {
            val (name, time, isActive) = item
            val cx = rect.left + colWidth * idx + colWidth / 2f

            // Active glow pill
            if (isActive) {
                val pillRect = RectF(cx - colWidth * 0.42f, rect.top + 16f, cx + colWidth * 0.42f, rect.bottom - 16f)
                val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#33D4AF37")
                }
                val pillStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.parseColor("#D4AF37")
                }
                canvas.drawRoundRect(pillRect, 16f, 16f, pillPaint)
                canvas.drawRoundRect(pillRect, 16f, 16f, pillStroke)
            }

            // Prayer Name
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = arabicFont
                textSize = 28f
                color = if (isActive) Color.parseColor("#D4AF37") else inactiveTextColor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(name, cx, rect.top + 70f, namePaint)

            // Active Indicator Dot
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isActive) Color.parseColor("#D4AF37") else Color.argb(60, 255, 255, 255)
            }
            canvas.drawCircle(cx, rect.top + 98f, if (isActive) 6f else 4f, dotPaint)

            // Time String
            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = numberFont
                textSize = 30f
                color = if (isActive) activeTextColor else inactiveTextColor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(time, cx, rect.top + 152f, timePaint)
        }

        return bitmap
    }

    // 2. QURAN VERSE OF THE DAY (4x2 aspect ratio)
    fun renderQuranVerse(
        context: Context,
        arabicFontKey: String?,
        bgStyle: String,
        patternColor: Int
    ): Bitmap {
        val width = 800
        val height = 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(12f, 12f, width - 12f, height - 12f)
        drawWidgetBackground(canvas, rect, 28f, bgStyle, patternColor, context, intArrayOf(Color.parseColor("#2D221E"), Color.parseColor("#1E1714"), Color.parseColor("#120D0B")))

        val arabicFont = CapsuleWidgetRenderer.resolveArabicTypeface(context, arabicFontKey)
        val isLight = (bgStyle == "light")
        val textColor = if (isLight) Color.parseColor("#111318") else Color.WHITE

        // Title Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 24f
            color = Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📖 آية اليوم من القرآن الكريم", rect.centerX(), rect.top + 52f, headerPaint)

        // Verse Text
        val verseText = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾"
        val versePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 44f
            color = textColor
            textAlign = Paint.Align.CENTER
            if (!isLight) setShadowLayer(8f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText(verseText, rect.centerX(), rect.centerY() + 10f, versePaint)

        // Surah Subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 24f
            color = if (isLight) Color.parseColor("#505565") else Color.parseColor("#A0A5B5")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("سورة الرعد • الآية 28", rect.centerX(), rect.bottom - 42f, subPaint)

        return bitmap
    }

    // 3. NEXT PRAYER ARABIC BANNER (3x2 aspect ratio)
    fun renderNextPrayerBanner(
        context: Context,
        arabicFontKey: String?,
        numberFontKey: String?,
        bgStyle: String,
        patternColor: Int
    ): Bitmap {
        val width = 600
        val height = 320
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(12f, 12f, width - 12f, height - 12f)
        drawWidgetBackground(canvas, rect, 28f, bgStyle, patternColor, context, intArrayOf(Color.parseColor("#223344"), Color.parseColor("#15222E"), Color.parseColor("#0A121A")))

        val arabicFont = CapsuleWidgetRenderer.resolveArabicTypeface(context, arabicFontKey)
        val numberFont = CapsuleWidgetRenderer.resolveNumberTypeface(context, numberFontKey)
        val isLight = (bgStyle == "light")
        val textColor = if (isLight) Color.parseColor("#111318") else Color.WHITE

        // Label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 26f
            color = Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("الصلاة القادمة • Next Prayer", rect.centerX(), rect.top + 52f, labelPaint)

        // Arabic Prayer Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 58f
            color = textColor
            textAlign = Paint.Align.CENTER
            if (!isLight) setShadowLayer(10f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText("صلاة العصر", rect.centerX(), rect.top + 132f, namePaint)

        // Time
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = numberFont
            textSize = 62f
            color = Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("03:30 PM", rect.centerX(), rect.top + 215f, timePaint)

        // Countdown Badge
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 22f
            color = if (isLight) Color.parseColor("#404555") else Color.parseColor("#C0C5D5")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("متبقي 01س 15د • مكة المكرمة", rect.centerX(), rect.bottom - 32f, badgePaint)

        return bitmap
    }

    // 4. COMPACT PRAYER LIST (2x3 aspect ratio)
    fun renderPrayerList(
        context: Context,
        arabicFontKey: String?,
        numberFontKey: String?,
        bgStyle: String,
        patternColor: Int
    ): Bitmap {
        val width = 450
        val height = 550
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(12f, 12f, width - 12f, height - 12f)
        drawWidgetBackground(canvas, rect, 28f, bgStyle, patternColor, context)

        val arabicFont = CapsuleWidgetRenderer.resolveArabicTypeface(context, arabicFontKey)
        val numberFont = CapsuleWidgetRenderer.resolveNumberTypeface(context, numberFontKey)
        val isLight = (bgStyle == "light")
        val textColor = if (isLight) Color.parseColor("#111318") else Color.WHITE

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 28f
            color = Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("مواقيت الصلاة اليومية", rect.centerX(), rect.top + 52f, titlePaint)

        val rows = listOf(
            Triple("الفجر", "05:12 AM", false),
            Triple("الظهر", "12:15 PM", false),
            Triple("العصر", "03:30 PM", true), // Active
            Triple("المغرب", "06:05 PM", false),
            Triple("العشاء", "07:35 PM", false)
        )

        val rowHeight = (rect.height() - 80f) / 5f
        for ((idx, item) in rows.withIndex()) {
            val (name, time, isActive) = item
            val y = rect.top + 75f + rowHeight * idx + rowHeight / 2f

            if (isActive) {
                val highlightRect = RectF(rect.left + 16f, y - rowHeight * 0.42f, rect.right - 16f, y + rowHeight * 0.42f)
                val hlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#33D4AF37") }
                canvas.drawRoundRect(highlightRect, 14f, 14f, hlPaint)
            }

            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = arabicFont
                textSize = 28f
                color = if (isActive) Color.parseColor("#D4AF37") else textColor
            }
            canvas.drawText(name, rect.left + 36f, y + 10f, namePaint)

            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = numberFont
                textSize = 28f
                color = if (isActive) Color.parseColor("#D4AF37") else textColor
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(time, rect.right - 36f, y + 10f, timePaint)
        }

        return bitmap
    }

    // Generic 2x2 Square Widget Preview Renderer (e.g. Qibla, Azkar, Duaa, Streak, etc.)
    fun renderGenericSquareWidget(
        context: Context,
        titleAr: String,
        bodyAr: String,
        highlightText: String,
        footerText: String,
        arabicFontKey: String?,
        numberFontKey: String?,
        bgStyle: String,
        patternColor: Int
    ): Bitmap {
        val width = 450
        val height = 450
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(12f, 12f, width - 12f, height - 12f)
        drawWidgetBackground(canvas, rect, 28f, bgStyle, patternColor, context)

        val arabicFont = CapsuleWidgetRenderer.resolveArabicTypeface(context, arabicFontKey)
        val numberFont = CapsuleWidgetRenderer.resolveNumberTypeface(context, numberFontKey)
        val isLight = (bgStyle == "light")
        val textColor = if (isLight) Color.parseColor("#111318") else Color.WHITE

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 26f
            color = Color.parseColor("#D4AF37")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleAr, rect.centerX(), rect.top + 56f, titlePaint)

        // Main Body / Highlight Text
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = numberFont
            textSize = 58f
            color = textColor
            textAlign = Paint.Align.CENTER
            if (!isLight) setShadowLayer(8f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText(highlightText, rect.centerX(), rect.centerY() - 10f, highlightPaint)

        // Body Text
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 28f
            color = textColor
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(bodyAr, rect.centerX(), rect.centerY() + 45f, bodyPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicFont
            textSize = 22f
            color = if (isLight) Color.parseColor("#505565") else Color.parseColor("#A0A5B5")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(footerText, rect.centerX(), rect.bottom - 36f, footerPaint)

        return bitmap
    }
}
