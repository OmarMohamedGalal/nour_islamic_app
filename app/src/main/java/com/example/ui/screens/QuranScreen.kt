package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun QuranScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            QuranReaderView(
                surah = uiState.selectedSurah,
                ayahs = uiState.currentAyahs,
                bookmarks = uiState.bookmarks,
                playerState = playerState,
                selectedReciter = uiState.selectedReciter,
                onOpenReciterPicker = { viewModel.setShowReciterDialog(true) },
                onBack = { viewModel.closeReadingMode() },
                onToggleBookmark = { viewModel.toggleBookmark(it) },
                onPlayAyah = { viewModel.playAyahAudio(it) },
                onPauseAudio = { viewModel.audioPlayer.pause() },
                onResumeAudio = { viewModel.audioPlayer.resume() },
                onStopAudio = { viewModel.audioPlayer.stop() },
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
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.nameAr.contains(searchQuery) ||
                    it.translation.contains(searchQuery, ignoreCase = true) ||
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
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THE NOBLE QURAN",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = TextMuted
                    )
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }
            }
        }

        // LAST READ HERO CARD - Frosted Glass with Obsidian Glow
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
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = VoidBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CONTINUE READING",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = lastRead.surahNameEn,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = lastRead.surahNameAr,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PureWhite,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ayah ${lastRead.verseNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Filled.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // SEARCH BAR - Frosted Glass
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_field"),
                placeholder = { Text("Search Surah name or number...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = TextMuted)
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
                    title = "Surahs (${filteredSurahs.size})",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                TabPill(
                    title = "Bookmarks (${bookmarks.size})",
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
                            text = "No saved bookmarks yet.\nTap the bookmark icon on any Ayah while reading.",
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = bm.surahNameEn,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = bm.surahNameAr,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PureWhite,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ayah ${bm.verseNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "Saved",
                                tint = PureWhite
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
                // Number Hexagon / Circle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, GlassBorderBrushSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surah.nameEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${surah.translation} • ${surah.totalVerses} Verses • ${surah.revelationType.titleEn}",
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
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.sp
                ),
                color = PureWhite,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun QuranReaderView(
    surah: Surah,
    ayahs: List<Ayah>,
    bookmarks: List<com.example.data.db.QuranBookmarkEntity>,
    playerState: AudioPlayerState,
    selectedReciter: QuranReciter,
    onOpenReciterPicker: () -> Unit,
    onBack: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onPlayAyah: (Ayah) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // TOP TOOLBAR - Frosted Glass Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassDarkElevated)
                .background(GlassGradientCard)
                .border(width = 1.dp, brush = GlassBorderBrushSubtle, shape = RectangleShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = surah.nameEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = surah.nameAr,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = "${surah.revelationType.titleEn} • ${surah.totalVerses} Ayahs • Juz ${surah.juzStart}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Reciter selector chip button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassDarkBase)
                        .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(12.dp))
                        .clickable { onOpenReciterPicker() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.RecordVoiceOver,
                            contentDescription = "Select Reciter",
                            tint = PureWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedReciter.shortNameAr,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // AYAH LIST
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SURAH HEADER HERO CARD
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(GlassDarkElevated)
                        .background(GlassGradientHero)
                        .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(22.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x18FFFFFF))
                                .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "سُورَة رقم ${surah.number}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Arabic Surah Name on a single line with generous margin
                        Text(
                            text = "سُورَةُ ${surah.nameAr}",
                            style = ArabicQuranTextStyle.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.sp
                            ),
                            color = PureWhite,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${surah.nameEn} • ${surah.translation}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${surah.revelationType.titleAr} (${surah.revelationType.titleEn}) • ${surah.totalVerses} آيات • الجزء ${surah.juzStart}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // RECITER UNAVAILABILITY BANNER
            if (!selectedReciter.isSurahAvailable(surah.number)) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassDarkElevated)
                            .background(GlassGradientHero)
                            .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeOff,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "القارئ غير متوفر فى تلك السورة",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "القارئ ${selectedReciter.nameAr} لم يُسجل أو يُصدر تلاوة سورة ${surah.nameAr} بعد. يمكنك التبديل لقارئ آخر للاستماع.",
                                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onOpenReciterPicker,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = VoidBlack),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اختيار قارئ آخر", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // BISMILLAH BANNER (except Surah At-Tawbah 9)
            if (surah.number != 9 && surah.number != 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassDarkBase)
                            .background(GlassGradientCard)
                            .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(20.dp))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = ArabicQuranTextStyle.copy(fontSize = 24.sp, textAlign = TextAlign.Center),
                            color = PureWhite
                        )
                    }
                }
            }

            items(ayahs, key = { "${it.surahNumber}_${it.verseNumber}" }) { ayah ->
                val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.verseNumber == ayah.verseNumber }
                val isPlayingCurrent = playerState.isPlaying && playerState.surahNumber == ayah.surahNumber && playerState.verseNumber == ayah.verseNumber
                val isLoadingCurrent = playerState.isLoading && playerState.surahNumber == ayah.surahNumber && playerState.verseNumber == ayah.verseNumber

                AyahCard(
                    ayah = ayah,
                    isBookmarked = isBookmarked,
                    isPlaying = isPlayingCurrent,
                    isLoading = isLoadingCurrent,
                    onToggleBookmark = { onToggleBookmark(ayah) },
                    onPlay = { onPlayAyah(ayah) }
                )
            }
        }

        // STICKY MINI AUDIO PLAYER BAR - Frosted Glass Player
        if (playerState.isPlaying || playerState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassDarkElevated)
                    .background(GlassGradientHero)
                    .border(width = 1.dp, brush = GlassBorderBrush, shape = RectangleShape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WhiteSilverGradient),
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
                                    imageVector = Icons.Filled.Audiotrack,
                                    contentDescription = "Playing",
                                    tint = VoidBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${playerState.surahName} • Ayah ${playerState.verseNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            val activeReciterName = if (playerState.reciterName.isNotEmpty()) {
                                playerState.reciterName
                            } else {
                                selectedReciter.nameAr
                            }
                            Text(
                                text = "القارئ: $activeReciterName",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (playerState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PureWhite,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else if (playerState.isPlaying) {
                            IconButton(onClick = onPauseAudio) {
                                Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause", tint = PureWhite)
                            }
                        } else {
                            IconButton(onClick = onResumeAudio) {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play", tint = PureWhite)
                            }
                        }
                        IconButton(onClick = onStopAudio) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Stop", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    onToggleBookmark: () -> Unit,
    onPlay: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (isPlaying) {
                    Modifier
                        .background(GlassDarkElevated)
                        .background(GlassGradientActive)
                        .border(1.dp, PureWhite, shape)
                } else {
                    Modifier
                        .background(GlassDarkBase)
                        .background(GlassGradientCard)
                        .border(1.dp, GlassBorderBrushSubtle, shape)
                }
            )
            .testTag("ayah_card_${ayah.verseNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Verse Header Bar: Verse Number pill & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) WhiteSilverGradient else SolidColor(Color(0x1CFFFFFF)))
                        .border(1.dp, if (isPlaying) SolidColor(PureWhite) else GlassBorderBrushSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.verseNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPlaying) VoidBlack else PureWhite
                    )
                }

                Row {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.size(32.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = PureWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Outlined.PlayCircleOutline,
                                contentDescription = "Play Recitation",
                                tint = if (isPlaying) PureWhite else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) PureWhite else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ARABIC TEXT (Calm, understated Uthmani style)
            Text(
                text = ayah.arabicText,
                style = ArabicQuranTextStyle,
                color = PureWhite,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRANSLITERATION
            Text(
                text = ayah.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ENGLISH TRANSLATION
            Text(
                text = ayah.englishTranslation,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = TextSecondary
            )
        }
    }
}
