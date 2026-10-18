package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerState
import com.example.data.model.Ayah
import com.example.data.model.QuranReciter
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.components.ReciterSelectionDialog
import com.example.ui.theme.*
import java.util.Locale

private fun Int.toArabicNumerals(): String {
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { char ->
        if (char in '0'..'9') digits[char - '0'] else char
    }.joinToString("")
}

private fun formatAudioTime(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

@Composable
fun QuranScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back gesture when in reading mode to return to Surah selection directory
    BackHandler(enabled = uiState.isReadingMode) {
        viewModel.closeReadingMode()
    }

    LaunchedEffect(uiState.reciterErrorMessage, playerState.errorMessage) {
        val error = uiState.reciterErrorMessage ?: playerState.errorMessage
        if (error != null) {
            val res = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "تغيير القارئ",
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                viewModel.setShowReciterDialog(true)
            }
            viewModel.clearReciterErrorMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isReadingMode) {
            QuranMedinaReaderView(
                surah = uiState.selectedSurah,
                ayahs = uiState.currentAyahs,
                bookmarks = uiState.bookmarks,
                playerState = playerState,
                selectedReciter = uiState.selectedReciter,
                onOpenReciterPicker = { viewModel.setShowReciterDialog(true) },
                onBack = { viewModel.closeReadingMode() },
                onToggleBookmark = { viewModel.toggleBookmark(it) },
                onPlaySurah = { viewModel.playSurahAudio(uiState.selectedSurah) },
                onPauseAudio = { viewModel.audioPlayer.pause() },
                onResumeAudio = { viewModel.audioPlayer.resume() },
                onStopAudio = { viewModel.audioPlayer.stop() },
                onSeekAudio = { viewModel.audioPlayer.seekTo(it) },
                onRewindAudio = { viewModel.audioPlayer.rewind(10) },
                onForwardAudio = { viewModel.audioPlayer.forward(10) },
                onNextSurah = { viewModel.nextSurah() },
                onPreviousSurah = { viewModel.previousSurah() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            QuranSurahDirectoryView(
                surahs = uiState.quranSurahs,
                searchQuery = uiState.quranSearchQuery,
                lastRead = uiState.currentReadingPosition,
                bookmarks = uiState.bookmarks,
                onSearchChange = { viewModel.setQuranSearch(it) },
                onSelectSurah = { surahNumber, verse -> viewModel.loadSurah(surahNumber, verse) },
                modifier = Modifier.fillMaxSize()
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
        )
    }

    if (uiState.showReciterDialog) {
        ReciterSelectionDialog(
            selectedReciter = uiState.selectedReciter,
            onSelectReciter = { reciter ->
                viewModel.selectReciter(reciter)
            },
            onDismiss = { viewModel.setShowReciterDialog(false) }
        )
    }
}

/**
 * Surah Selection Directory Screen
 * 100% Arabic with Noto Sans Arabic typography
 */
@Composable
fun QuranSurahDirectoryView(
    surahs: List<Surah>,
    searchQuery: String,
    lastRead: com.example.data.db.ReadingHistoryEntity?,
    bookmarks: List<com.example.data.db.QuranBookmarkEntity>,
    onSearchChange: (String) -> Unit,
    onSelectSurah: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Surahs, 1: Bookmarks

    val filteredSurahs = remember(searchQuery, surahs) {
        if (searchQuery.isBlank()) surahs
        else surahs.filter {
            it.nameAr.contains(searchQuery) ||
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.number.toString() == searchQuery.trim()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TOP HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "كتاب الله العزيز",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = NotoSansArabicFontFamily
                        ),
                        color = PureWhite
                    )
                }

                // Decorative golden medallion
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8A651E), Color(0xFFD4AF37), Color(0xFFF9E8A2))
                            )
                        )
                        .border(1.dp, PureWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = "القرآن الكريم",
                        tint = VoidBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // CONTINUE READING HERO CARD
        if (lastRead != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(GlassDarkElevated)
                        .background(GlassGradientHero)
                        .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(22.dp))
                        .clickable { onSelectSurah(lastRead.surahNumber, lastRead.verseNumber) }
                        .testTag("continue_reading_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(WhiteSilverGradient)
                                    .border(1.dp, PureWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoStories,
                                    contentDescription = null,
                                    tint = VoidBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "متابعة التلاوة",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "سورة ${lastRead.surahNameAr}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AccentGold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الآية رقم ${lastRead.verseNumber.toArabicNumerals()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "فتح",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_field"),
                placeholder = { Text("ابحث عن اسم السورة أو رقمها...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "بحث", tint = TextMuted)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x18FFFFFF),
                    unfocusedContainerColor = Color(0x0EFFFFFF),
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = GlassBorderColorSubtle,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                )
            )
        }

        // TABS: SURAHS / BOOKMARKS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabPill(
                    title = "فهرس السور (${filteredSurahs.size.toArabicNumerals()})",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TabPill(
                    title = "العلامات المحفوظة (${bookmarks.size.toArabicNumerals()})",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }

        if (selectedTab == 0) {
            items(filteredSurahs, key = { it.number }) { surah ->
                SurahRowItem(
                    surah = surah,
                    onClick = { onSelectSurah(surah.number, 1) }
                )
            }
        } else {
            if (bookmarks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد علامات مرجعية محفوظة بعد.\nاضغط على أيقونة الإشارة المرجعية أثناء القراءة لحفظ الآية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(bookmarks, key = { it.id }) { bm ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassDarkBase)
                            .background(GlassGradientCard)
                            .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(18.dp))
                            .clickable { onSelectSurah(bm.surahNumber, bm.verseNumber) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سورة ${bm.surahNameAr}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AccentGold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الآية رقم ${bm.verseNumber.toArabicNumerals()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "محفوظ",
                                tint = AccentGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(WhiteSilverGradient)
                        .border(1.dp, PureWhite, shape)
                } else {
                    Modifier
                        .background(GlassDarkBase)
                        .background(GlassGradientCard)
                        .border(1.dp, GlassBorderBrushSubtle, shape)
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) VoidBlack else TextSecondary
        )
    }
}

@Composable
fun SurahRowItem(
    surah: Surah,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassDarkBase)
            .background(GlassGradientCard)
            .border(1.dp, GlassBorderBrushSubtle, shape)
            .clickable { onClick() }
            .testTag("surah_item_${surah.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Number medallion
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, GlassBorderBrushSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toArabicNumerals(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${surah.nameAr}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${surah.revelationType.titleAr} • ${surah.totalVerses.toArabicNumerals()} آية • الجزء ${surah.juzStart.toArabicNumerals()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = surah.nameAr,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 0.sp
                ),
                color = AccentGold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Authentic Medina 1 Mushaf Reader
 * 100% Arabic interface with Noto Sans Arabic typography
 */
@Composable
fun QuranMedinaReaderView(
    surah: Surah,
    ayahs: List<Ayah>,
    bookmarks: List<com.example.data.db.QuranBookmarkEntity>,
    playerState: AudioPlayerState,
    selectedReciter: QuranReciter,
    onOpenReciterPicker: () -> Unit,
    onBack: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onPlaySurah: () -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onRewindAudio: () -> Unit,
    onForwardAudio: () -> Unit,
    onNextSurah: () -> Unit,
    onPreviousSurah: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isNightMode by remember { mutableStateOf(false) }
    var isMushafPageView by remember { mutableStateOf(true) }
    var textScaleFactor by remember { mutableStateOf(1.0f) }
    var selectedAyahForDetail by remember { mutableStateOf<Ayah?>(null) }

    val pageBgColor = if (isNightMode) Color(0xFF141519) else Color(0xFFFAF7EE)
    val pageTextColor = if (isNightMode) Color(0xFFEDE5D5) else Color(0xFF1B1917)
    val goldBorderColor = if (isNightMode) Color(0xFFB58E38) else Color(0xFFC49A3C)
    val goldInnerBorderColor = if (isNightMode) Color(0x66B58E38) else Color(0x66C49A3C)
    val subTextColor = if (isNightMode) Color(0xFF9E998E) else Color(0xFF7A7061)

    val isCurrentSurahPlaying = playerState.isPlaying && playerState.surahNumber == surah.number
    val isCurrentSurahLoading = playerState.isLoading && playerState.surahNumber == surah.number

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(pageBgColor)
    ) {
        // TOP APP CONTROLS TOOLBAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isNightMode) Color(0xFF181A20) else Color(0xFFF3ECE0))
                .border(width = 1.dp, color = goldBorderColor.copy(alpha = 0.4f), shape = RectangleShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back & Surah Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = if (isNightMode) PureWhite else VoidBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "سُورَةُ ${surah.nameAr}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isNightMode) PureWhite else VoidBlack
                        )
                        Text(
                            text = "${surah.revelationType.titleAr} • ${surah.totalVerses.toArabicNumerals()} آية • الجزء ${surah.juzStart.toArabicNumerals()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = subTextColor
                        )
                    }
                }

                // Controls: Font Size, Mode Toggle, Reciter, Play Full Surah
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Font scale
                    IconButton(
                        onClick = { if (textScaleFactor > 0.85f) textScaleFactor -= 0.1f },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("خط-", fontWeight = FontWeight.Bold, color = if (isNightMode) PureWhite else VoidBlack, fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = { if (textScaleFactor < 1.4f) textScaleFactor += 0.1f },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Text("خط+", fontWeight = FontWeight.Bold, color = if (isNightMode) PureWhite else VoidBlack, fontSize = 11.sp)
                    }

                    // Night mode toggle
                    IconButton(
                        onClick = { isNightMode = !isNightMode },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "المظهر",
                            tint = if (isNightMode) Color(0xFFF9D776) else Color(0xFF5D4037),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // View Mode Switcher
                    IconButton(
                        onClick = { isMushafPageView = !isMushafPageView },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isMushafPageView) Icons.Filled.FormatListNumberedRtl else Icons.Filled.MenuBook,
                            contentDescription = if (isMushafPageView) "عرض الآيات" else "عرض المصحف",
                            tint = goldBorderColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Reciter selector chip button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNightMode) Color(0x33FFFFFF) else Color(0x228A651E))
                            .border(1.dp, goldBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { onOpenReciterPicker() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.RecordVoiceOver,
                                contentDescription = "اختيار القارئ",
                                tint = goldBorderColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedReciter.shortNameAr,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isNightMode) PureWhite else VoidBlack,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // PLAY FULL SURAH BUTTON
                    IconButton(
                        onClick = {
                            if (isCurrentSurahPlaying) onPauseAudio()
                            else if (playerState.surahNumber == surah.number && !playerState.isLoading) onResumeAudio()
                            else onPlaySurah()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8A651E), Color(0xFFD4AF37), Color(0xFFF9E8A2))
                                )
                            )
                            .testTag("play_full_surah_button")
                    ) {
                        if (isCurrentSurahLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VoidBlack,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isCurrentSurahPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل السورة كاملة",
                                tint = VoidBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // MAIN QURAN READING CANVAS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentPadding = PaddingValues(bottom = if (playerState.isPlaying || playerState.isLoading) 160.dp else 90.dp)
            ) {
                item {
                    // MEDINA MUSHAF ORNATE DOUBLE BORDER FRAME
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(pageBgColor)
                            .border(2.dp, goldBorderColor, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, goldInnerBorderColor, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // MEDINA PAGE TOP HEADER STRIP
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الجُزْءُ ${(surah.juzStart).toArabicNumerals()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = subTextColor
                                    )

                                    // Center Islamic Star medallion
                                    Text(
                                        text = "۞",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = goldBorderColor
                                    )

                                    Text(
                                        text = "سُورَةُ ${surah.nameAr}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = subTextColor
                                    )
                                }

                                // MEDINA ORNATE SURAH HEADER BANNER
                                MedinaSurahHeaderBanner(
                                    surah = surah,
                                    isNightMode = isNightMode,
                                    onPlayFullSurah = onPlaySurah,
                                    isPlaying = isCurrentSurahPlaying,
                                    isLoading = isCurrentSurahLoading
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // BISMILLAH (Except Surah At-Tawbah 9)
                                if (surah.number != 9) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            style = ArabicQuranTextStyle.copy(
                                                fontSize = (26 * textScaleFactor).sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            color = pageTextColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // MUSHAF PAGE OR VERSE-BY-VERSE DISPLAY
                                if (isMushafPageView) {
                                    MedinaContinuousPageText(
                                        ayahs = ayahs,
                                        selectedAyah = selectedAyahForDetail,
                                        textScaleFactor = textScaleFactor,
                                        textColor = pageTextColor,
                                        goldColor = goldBorderColor,
                                        onSelectAyah = { selectedAyahForDetail = it }
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ayahs.forEach { ayah ->
                                            val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.verseNumber == ayah.verseNumber }
                                            AyahStudyItem(
                                                ayah = ayah,
                                                isBookmarked = isBookmarked,
                                                isNightMode = isNightMode,
                                                textScaleFactor = textScaleFactor,
                                                onToggleBookmark = { onToggleBookmark(ayah) },
                                                onPlaySurah = onPlaySurah
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // MEDINA PAGE FOOTER
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${surah.revelationType.titleAr} - ${surah.totalVerses.toArabicNumerals()} آية",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = subTextColor
                                    )

                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, goldBorderColor, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "سورة رقم ${surah.number.toArabicNumerals()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = goldBorderColor
                                        )
                                    }

                                    Text(
                                        text = "الترتيب: ${surah.number.toArabicNumerals()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = subTextColor
                                    )
                                }
                            }
                        }
                    }
                }

                // SURAH PREV / NEXT NAVIGATION BUTTONS
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (surah.number > 1) {
                            OutlinedButton(
                                onClick = onPreviousSurah,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isNightMode) PureWhite else VoidBlack
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(goldBorderColor))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("السورة السابقة", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (surah.number < 114) {
                            Button(
                                onClick = onNextSurah,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = goldBorderColor,
                                    contentColor = VoidBlack
                                )
                            ) {
                                Text("السورة التالية", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // POPUP AYAH DETAIL DRAWER
            if (selectedAyahForDetail != null) {
                val ayah = selectedAyahForDetail!!
                val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.verseNumber == ayah.verseNumber }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = if (playerState.isPlaying || playerState.isLoading) 120.dp else 16.dp, start = 12.dp, end = 12.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isNightMode) Color(0xFF1B1D24) else Color(0xFFFFFFFF))
                        .border(1.dp, goldBorderColor, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(goldBorderColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ayah.verseNumber.toArabicNumerals(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = VoidBlack
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "سورة ${surah.nameAr} • الآية رقم ${ayah.verseNumber.toArabicNumerals()}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isNightMode) PureWhite else VoidBlack
                                )
                            }

                            Row {
                                IconButton(onClick = { onToggleBookmark(ayah) }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "حفظ الإشارة",
                                        tint = goldBorderColor
                                    )
                                }
                                IconButton(onClick = { selectedAyahForDetail = null }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "إغلاق",
                                        tint = subTextColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ayah.arabicText,
                            style = ArabicQuranTextStyle.copy(
                                fontSize = 23.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 40.sp
                            ),
                            color = if (isNightMode) PureWhite else Color(0xFF1B1917),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // FULL SURAH AUDIO PLAYER DOCK
        if (playerState.isPlaying || playerState.isLoading) {
            FullSurahAudioPlayerDock(
                playerState = playerState,
                selectedReciter = selectedReciter,
                isNightMode = isNightMode,
                onPause = onPauseAudio,
                onResume = onResumeAudio,
                onStop = onStopAudio,
                onSeek = onSeekAudio,
                onRewind = onRewindAudio,
                onForward = onForwardAudio
            )
        }
    }
}

/**
 * Medina Ornate Surah Header Banner
 */
@Composable
fun MedinaSurahHeaderBanner(
    surah: Surah,
    isNightMode: Boolean,
    onPlayFullSurah: () -> Unit,
    isPlaying: Boolean,
    isLoading: Boolean
) {
    val goldGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF8B651B),
            Color(0xFFC79E3E),
            Color(0xFFFDEBB2),
            Color(0xFFC79E3E),
            Color(0xFF8B651B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(goldGradient)
            .border(2.dp, Color(0xFF6B4D10), RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isNightMode) Color(0xDD181A22) else Color(0xDDF8F3E6))
                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Revelation place (مكية / مدنية)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = surah.revelationType.titleAr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF8C6418)
                    )
                    Text(
                        text = "ترتيبها ${surah.number.toArabicNumerals()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF9E844D)
                    )
                }

                // Center: Surah Name
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "سُورَةُ ${surah.nameAr}",
                        style = TextStyle(
                            fontFamily = NotoSansArabicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = if (isNightMode) Color(0xFFFFDF88) else Color(0xFF2C1E07)
                    )
                }

                // Number of verses
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "آياتها ${surah.totalVerses.toArabicNumerals()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF8C6418)
                    )
                    Text(
                        text = "الجزء ${surah.juzStart.toArabicNumerals()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF9E844D)
                    )
                }
            }
        }
    }
}

/**
 * Medina Continuous Page Flowing Holy Quran Text
 */
@Composable
fun MedinaContinuousPageText(
    ayahs: List<Ayah>,
    selectedAyah: Ayah?,
    textScaleFactor: Float,
    textColor: Color,
    goldColor: Color,
    onSelectAyah: (Ayah) -> Unit
) {
    val quranText = remember(ayahs, textScaleFactor, textColor, goldColor, selectedAyah) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isSelected = selectedAyah?.verseNumber == ayah.verseNumber

                pushStringAnnotation(tag = "AYAH", annotation = ayah.verseNumber.toString())

                if (isSelected) {
                    withStyle(style = SpanStyle(background = Color(0x38D4AF37))) {
                        append(ayah.arabicText)
                    }
                } else {
                    append(ayah.arabicText)
                }

                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = goldColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSansArabicFontFamily
                    )
                ) {
                    append("\uFD3E${ayah.verseNumber.toArabicNumerals()}\uFD3F")
                }
                append(" ")

                pop()
            }
        }
    }

    val style = ArabicQuranTextStyle.copy(
        fontSize = (25 * textScaleFactor).sp,
        lineHeight = (46 * textScaleFactor).sp,
        textAlign = TextAlign.Justify
    )

    ClickableText(
        text = quranText,
        style = style.copy(color = textColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        onClick = { offset ->
            quranText.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val vNum = annotation.item.toIntOrNull()
                    val clickedAyah = ayahs.firstOrNull { it.verseNumber == vNum }
                    if (clickedAyah != null) {
                        onSelectAyah(clickedAyah)
                    }
                }
        }
    )
}

/**
 * Verse-by-verse Item
 */
@Composable
fun AyahStudyItem(
    ayah: Ayah,
    isBookmarked: Boolean,
    isNightMode: Boolean,
    textScaleFactor: Float,
    onToggleBookmark: () -> Unit,
    onPlaySurah: () -> Unit
) {
    val gold = Color(0xFFC49A3C)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isNightMode) Color(0x22FFFFFF) else Color(0x0C8A651E))
            .border(1.dp, gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(gold.copy(alpha = 0.2f))
                        .border(1.dp, gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.verseNumber.toArabicNumerals(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = gold
                    )
                }

                Row {
                    IconButton(onClick = onPlaySurah, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircleOutline,
                            contentDescription = "تشغيل السورة كاملة",
                            tint = gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ الإشارة",
                            tint = gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${ayah.arabicText} \uFD3E${ayah.verseNumber.toArabicNumerals()}\uFD3F",
                style = ArabicQuranTextStyle.copy(
                    fontSize = (24 * textScaleFactor).sp,
                    lineHeight = (42 * textScaleFactor).sp,
                    textAlign = TextAlign.Right
                ),
                color = if (isNightMode) PureWhite else Color(0xFF1B1917),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Full Surah Audio Player Dock
 */
@Composable
fun FullSurahAudioPlayerDock(
    playerState: AudioPlayerState,
    selectedReciter: QuranReciter,
    isNightMode: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Int) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit
) {
    val gold = Color(0xFFD4AF37)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp)
            .background(if (isNightMode) Color(0xF2161820) else Color(0xF7FAF5EA))
            .border(width = 1.dp, color = gold.copy(alpha = 0.5f), shape = RectangleShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Player Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8A651E), Color(0xFFD4AF37), Color(0xFFF9E8A2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VoidBlack,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = VoidBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سُورَةُ ${playerState.surahName} • تلاوة كاملة",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isNightMode) PureWhite else VoidBlack
                        )
                        Text(
                            text = "القارئ: ${selectedReciter.nameAr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNightMode) TextSecondary else Color(0xFF7A6F5E)
                        )
                    }
                }

                IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إيقاف",
                        tint = if (isNightMode) TextMuted else Color(0xFF9E9585)
                    )
                }
            }

            // Seek bar / Progress slider
            val currentPos = playerState.currentPositionMs.toFloat()
            val totalDur = playerState.durationMs.toFloat().coerceAtLeast(1f)
            val sliderValue = (currentPos / totalDur).coerceIn(0f, 1f)

            Slider(
                value = sliderValue,
                onValueChange = { frac ->
                    val targetMs = (frac * totalDur).toInt()
                    onSeek(targetMs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                colors = SliderDefaults.colors(
                    thumbColor = gold,
                    activeTrackColor = gold,
                    inactiveTrackColor = gold.copy(alpha = 0.25f)
                )
            )

            // Elapsed and Total Time Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatAudioTime(playerState.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNightMode) TextSecondary else Color(0xFF7A6F5E)
                )
                Text(
                    text = if (playerState.durationMs > 0) formatAudioTime(playerState.durationMs) else "--:--",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNightMode) TextSecondary else Color(0xFF7A6F5E)
                )
            }

            // Player Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRewind) {
                    Icon(
                        imageVector = Icons.Filled.Replay10,
                        contentDescription = "تأخير ١٠ ثوانٍ",
                        tint = if (isNightMode) PureWhite else VoidBlack
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        if (playerState.isPlaying) onPause() else onResume()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8A651E), Color(0xFFD4AF37), Color(0xFFF9E8A2))
                            )
                        )
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = VoidBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onForward) {
                    Icon(
                        imageVector = Icons.Filled.Forward10,
                        contentDescription = "تقديم ١٠ ثوانٍ",
                        tint = if (isNightMode) PureWhite else VoidBlack
                    )
                }
            }
        }
    }
}
