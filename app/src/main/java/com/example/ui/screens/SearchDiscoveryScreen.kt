package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArtMedium
import com.example.data.ArtOrientation
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchDiscoveryScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    val currentQueryText by viewModel.searchQueryText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val selectedPalette by viewModel.searchPaletteFilter.collectAsState()
    val selectedMedium by viewModel.searchMediumFilter.collectAsState()
    val selectedPriceMax by viewModel.searchPriceMaxFilter.collectAsState()
    val selectedOrientation by viewModel.searchOrientationFilter.collectAsState()
    val discoverUnsignedOnly by viewModel.discoverUnsignedArtistsOnly.collectAsState()

    var isCameraScanning by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All Regions") }

    val coroutineScope = rememberCoroutineScope()

    val recommendedPalettes = listOf(
        Pair("Ambient Teal", "#1A3A3A"),
        Pair("Bright Amber", "#D97706"),
        Pair("Matrix Emerald", "#10B981"),
        Pair("Deep Navy", "#0F172A"),
        Pair("Ultraviolet Glow", "#8B5CF6")
    )

    val regionalHubs = listOf("All Regions", "Pacific Northwest, USA", "Tokyo, Kanto", "Portland Studio", "London")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header Context
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aesthetic Discovery",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sync artwork parameters to dynamic room aesthetics",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (selectedPalette != null || selectedMedium != null || selectedPriceMax != null || selectedOrientation != null || discoverUnsignedOnly) {
                TextButton(onClick = { viewModel.clearAllFilters() }) {
                    Text("Clear All Filters")
                }
            }
        }

        // Search edit box with simulated parameters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.updateSearchQueryText(it)
                },
                placeholder = { Text("E.g. Seattle, Neon, Impressions...", fontSize = 13.sp) },
                prefix = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = {
                            searchText = ""
                            viewModel.updateSearchQueryText("")
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            // AI Camera scanning tool simulator
            IconButton(
                onClick = {
                    isCameraScanning = true
                    scanResultMessage = "Analyzing living room photo vectors..."
                    coroutineScope.launch {
                        delay(2000)
                        scanResultMessage = "Detected dominant shade: Ambient Teal (Hex #1A3A3A). Automatically sync'd palette filters!"
                        viewModel.selectPaletteFilter("#1A3A3A")
                        delay(2500)
                        isCameraScanning = false
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "AI Camera Lens Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Camera scanning feedback banner
        AnimatedVisibility(visible = isCameraScanning || scanResultMessage.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCameraScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Scan Done", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Text(text = scanResultMessage, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Palette Selector (Hex Interior Color Range matching)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Interior Palette Color Matches", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recommendedPalettes) { (name, hex) ->
                            val isSelected = selectedPalette.equals(hex, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    if (isSelected) viewModel.selectPaletteFilter(null)
                                    else viewModel.selectPaletteFilter(hex)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // High-fidelity Medium Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Structural Art Mediums", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ArtMedium.values().forEach { medium ->
                            val isSelected = selectedMedium == medium
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) viewModel.selectMediumFilter(null)
                                    else viewModel.selectMediumFilter(medium)
                                },
                                label = { Text(medium.name.replace("_", " "), fontSize = 11.sp) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // Dimensional Orientations
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Canvas Orientation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ArtOrientation.values().forEach { orientation ->
                            val isSelected = selectedOrientation == orientation
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) viewModel.selectOrientationFilter(null)
                                    else viewModel.selectOrientationFilter(orientation)
                                },
                                label = { Text(orientation.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Price point max bounds
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Maximum Valuation Cap", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        val maxPriceVal = selectedPriceMax
                        Text(
                            text = if (maxPriceVal == null) "All price structures" else "Under $${maxPriceVal.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = selectedPriceMax ?: 4000f,
                        onValueChange = { viewModel.selectPriceMaxFilter(it) },
                        valueRange = 200f..4000f,
                        steps = 19
                    )
                }
            }

            // Regional geo search mapping selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Global Regional Geolocation Hubs", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(regionalHubs) { region ->
                            val isSelected = selectedRegion == region
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedRegion = region }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Place",
                                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = region,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Algorithmic low exposure boost toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Boost", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Text("Discover Unsigned Artists Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "Prioritize low-exposure artists in the algorithmic pool to expand your collector collections.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = discoverUnsignedOnly,
                            onCheckedChange = { viewModel.toggleUnsignedArtists(it) }
                        )
                    }
                }
            }

            // Historical Queries Caching
            if (searchHistory.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Search History", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            searchHistory.forEach { history ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchText = history.queryText
                                            viewModel.updateSearchQueryText(history.queryText)
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History Icon",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = history.queryText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteHistoryItem(history.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
