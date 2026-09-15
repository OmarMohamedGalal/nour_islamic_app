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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerState
import com.example.data.model.Ayah
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*

@Composable
fun QuranScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()

    if (uiState.isReadingMode) {
        QuranReaderView(
            surah = uiState.selectedSurah,
            ayahs = uiState.currentAyahs,
            bookmarks = uiState.bookmarks,
            playerState = playerState,
            onBack = { viewModel.closeReadingMode() },
            onToggleBookmark = { viewModel.toggleBookmark(it) },
            onPlayAyah = { viewModel.playAyahAudio(it) },
            onPauseAudio = { viewModel.audioPlayer.pause() },
            onResumeAudio = { viewModel.audioPlayer.resume() },
            onStopAudio = { viewModel.audioPlayer.stop() },
            modifier = modifier
        )
    } else {
        QuranSurahDirectoryView(
            surahs = uiState.quranSurahs,
            searchQuery = uiState.quranSearchQuery,
            lastRead = uiState.currentReadingPosition,
            bookmarks = uiState.bookmarks,
            onSearchChange = { viewModel.setQuranSearch(it) },
            onSelectSurah = { surahNumber, verse -> viewModel.loadSurah(surahNumber, verse) },
            modifier = modifier
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
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
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

        // LAST READ HERO CARD
        if (lastRead != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectSurah(lastRead.surahNumber, lastRead.verseNumber) }
                        .testTag("continue_reading_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite),
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
                            Column {
                                Text(
                                    text = "CONTINUE READING",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = TextMuted
                                )
                                Text(
                                    text = "${lastRead.surahNameEn} (${lastRead.surahNameAr})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Ayah ${lastRead.verseNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

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

        // SEARCH BAR
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
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = BorderOutline,
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectSurah(bm.surahNumber, bm.verseNumber) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${bm.surahNameEn} (${bm.surahNameAr})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Ayah ${bm.verseNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "Saved",
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PureWhite else SurfaceCard,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) VoidBlack else TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SurahRowItem(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Number Hexagon / Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceLow)
                        .border(1.dp, BorderOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = surah.nameEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                    Text(
                        text = "${surah.translation} • ${surah.totalVerses} Verses • ${surah.revelationType.titleEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = surah.nameAr,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                color = PureWhite
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
            .background(VoidBlack)
    ) {
        // TOP TOOLBAR
        Surface(
            color = VoidBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = surah.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "${surah.revelationType.titleEn} • ${surah.totalVerses} Ayahs • Juz ${surah.juzStart}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = surah.nameAr,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                    color = PureWhite
                )
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
            // BISMILLAH BANNER (except Surah At-Tawbah 9)
            if (surah.number != 9 && surah.number != 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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

                AyahCard(
                    ayah = ayah,
                    isBookmarked = isBookmarked,
                    isPlaying = isPlayingCurrent,
                    onToggleBookmark = { onToggleBookmark(ayah) },
                    onPlay = { onPlayAyah(ayah) }
                )
            }
        }

        // STICKY MINI AUDIO PLAYER BAR
        if (playerState.isPlaying || playerState.isLoading) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceCardHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = "Playing",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${playerState.surahName} • Ayah ${playerState.verseNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Text(
                                text = "Reciter: Mishary Rashid Alafasy",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (playerState.isPlaying) {
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
    onToggleBookmark: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.verseNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPlaying) SurfaceCardHigh else SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlaying) PureWhite else BorderSubtle)
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
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceLow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.verseNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }

                Row {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Outlined.PlayCircleOutline,
                            contentDescription = "Play Recitation",
                            tint = if (isPlaying) PureWhite else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) AccentGold else TextSecondary,
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
