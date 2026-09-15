package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DuaaCategory
import com.example.data.model.DuaaItem
import com.example.data.repository.DuaaRepository
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*

@Composable
fun DuaaScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchResults = remember(uiState.duaaSearchQuery, uiState.selectedDuaaCategory) {
        DuaaRepository.searchDuaas(uiState.duaaSearchQuery, uiState.selectedDuaaCategory)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "SUPPLICATIONS & DUAAS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted
                )
                Text(
                    text = "الأدعية المأثورة",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = uiState.duaaSearchQuery,
                onValueChange = { viewModel.setDuaaSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duaa_search_field"),
                placeholder = { Text("Search by keywords, translation, or title...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = TextMuted)
                },
                trailingIcon = {
                    if (uiState.duaaSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setDuaaSearch("") }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
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

        // CATEGORY PILLS
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DuaaCategory.entries) { cat ->
                    val isSelected = cat == uiState.selectedDuaaCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PureWhite else SurfaceCard,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setDuaaCategory(cat) }
                            .testTag("duaa_cat_${cat.id}")
                    ) {
                        Text(
                            text = cat.titleEn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) VoidBlack else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // DUAA CARDS
        if (searchResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No supplications found for '${uiState.duaaSearchQuery}'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(searchResults, key = { it.id }) { duaa ->
                val isFav = uiState.favoriteDuaaIds.contains(duaa.id)

                DuaaCard(
                    duaa = duaa,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleDuaaFavorite(duaa) },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${duaa.titleEn}\n\n${duaa.arabicText}\n\n${duaa.translation}\n\n(${duaa.reference}) - Shared via Noor")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Duaa"))
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Duaa", "${duaa.arabicText}\n\n${duaa.translation}\n(${duaa.reference})")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Duaa copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun DuaaCard(
    duaa: DuaaItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("duaa_card_${duaa.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Category Pill, Title & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = duaa.category.titleEn.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) AccentGold else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = duaa.titleEn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ARABIC TEXT
            Text(
                text = duaa.arabicText,
                style = ArabicAzkarTextStyle,
                color = PureWhite,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRANSLITERATION
            Text(
                text = duaa.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ENGLISH TRANSLATION
            Text(
                text = duaa.translation,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // REFERENCE CITATION
            Text(
                text = duaa.reference,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
