package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import coil.compose.AsyncImage
import com.example.data.ArtMedium
import com.example.data.CustomCommission
import com.example.data.MockArtData
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArtistStudioScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val artist = MockArtData.artists[0] // Elianna Vance profile
    val commissions by viewModel.commissions.collectAsState()

    var activeSubTab by remember { mutableStateOf("Studio Live") } // Studio Live | Commissions | Analytics | Blueprints
    var csvText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }

    // Stream State
    var isBroadcasting by remember { mutableStateOf(false) }
    var streamTipResponseCount by remember { mutableIntStateOf(0) }
    val flyingHearts = remember { mutableStateListOf<Offset>() }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Artist Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        AsyncImage(
                            model = artist.avatarUrl,
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = artist.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified Artist Checkmark", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Text(text = "@${artist.alias} • ${artist.geolocationRegion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    text = artist.bio,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Audio statement Intro Commentary bar (Feature 46)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Audio Intro Commentary", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(text = "Play Artist Statement Audio Intro", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "0:${artist.audioIntroDurationSec} sec", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Sub Tabs controller
        TabRow(
            selectedTabIndex = run {
                when (activeSubTab) {
                    "Studio Live" -> 0
                    "Commissions" -> 1
                    "Analytics" -> 2
                    else -> 3
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = activeSubTab == "Studio Live", onClick = { activeSubTab = "Studio Live" }) {
                Text("Studio Live", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "Commissions", onClick = { activeSubTab = "Commissions" }) {
                Text("Commissions", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "Analytics", onClick = { activeSubTab = "Analytics" }) {
                Text("Analytics", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "Blueprints", onClick = { activeSubTab = "Blueprints" }) {
                Text("Blueprints", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Active workspace
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (activeSubTab) {
                "Studio Live" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Podcasts, contentDescription = "Broadcast", tint = MaterialTheme.colorScheme.primary)
                            Text("Broadcasting Feed Studio Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Video stream simulator container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .drawBehind {
                                    // Custom particle animator of hearts flying up if tip jar is clicked!
                                    flyingHearts.forEach { offset ->
                                        drawCircle(
                                            color = Color.Red.copy(alpha = 0.7f),
                                            radius = 16f,
                                            center = offset
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBroadcasting) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&q=80&w=600",
                                    contentDescription = "Active broadcast video channel",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Red)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVE BROADCAST ONLINE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                if (streamTipResponseCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.82f))
                                            .padding(8.dp)
                                    ) {
                                        Text("TIP RECEIVED! +$10 FROM CO_LL***R", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { isBroadcasting = true },
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Active stream", tint = Color.White)
                                    }
                                    Text("Pre-configure camera nodes and go live", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
                                }
                            }
                        }

                        // Live stream tools
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isBroadcasting) {
                                        // Send flying particle
                                        streamTipResponseCount++
                                        flyingHearts.add(Offset(200f + (0..400).random(), 400f - (0..200).random()))
                                        coroutineScope.launch {
                                            delay(1500)
                                            streamTipResponseCount--
                                        }
                                    }
                                },
                                enabled = isBroadcasting,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Tip jar")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tip Artist ($10)")
                            }

                            OutlinedButton(
                                onClick = {
                                    isBroadcasting = false
                                    flyingHearts.clear()
                                },
                                enabled = isBroadcasting,
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Stop Broadcasting")
                            }
                        }

                        // Pint Catalog Items block
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Gray)
                            ) {
                                AsyncImage(model = MockArtData.artworks[0].imageUrl, contentDescription = "Pinned item", contentScale = ContentScale.Crop)
                            }
                            Column {
                                Text("Pinned Listing: Aetherial Rain", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Viewers see purchase button floating inside live frame.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                "Commissions" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Commission Management Workspace", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Release payouts securely upon milestone completion.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(commissions) { commission ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Buyer: ${commission.buyerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = commission.status, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(text = "Artwork Size & Budget: ${commission.sizeDescription} • Valuation $${commission.budgetUsd.toInt()}", fontSize = 11.sp)
                                        Text(text = "References: ${commission.referencesDescription}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        LinearProgressIndicator(
                                            progress = commission.currentMilestoneIndex.toFloat() / commission.milestonePaymentsCount,
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Completed milestones: ${commission.currentMilestoneIndex}/${commission.milestonePaymentsCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (commission.currentMilestoneIndex < commission.milestonePaymentsCount) {
                                                Button(
                                                    onClick = { viewModel.updateCommissionMilestone(commission.id) },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Approve Milestone", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Analytics" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Studio Metric Analytics Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        // Top summary counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(Pair("Portfolio Views", "4.2K"), Pair("Canvas Zooms", "1.1K"), Pair("Cart Actions", "124")).forEach { (label, count) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Zoom geometric click heatmap simulation (Feature 41)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Zoom Concentration Heatmap Nodes", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                // Dynamic heatmap canvas
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Gray)
                                ) {
                                    AsyncImage(model = MockArtData.artworks[0].imageUrl, contentDescription = "Art canvas", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                                    // Heatmap point layers overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color.Red.copy(alpha = 0.7f), Color.Yellow.copy(alpha = 0.3f), Color.Transparent)
                                                )
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-30).dp, y = 20.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color.Red.copy(alpha = 0.6f), Color.Transparent)
                                                )
                                            )
                                    )
                                }
                                Text("Dominant coordinates mapping brush high-density details under palette knife glazes.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                "Blueprints" -> {
                    var blueprintTab by remember { mutableStateOf("Architecture") } // Architecture | Schema | UI code | API code
                    var uploadProgressStep by remember { mutableIntStateOf(0) }
                    var progressStatusText by remember { mutableStateOf("Ready to simulate") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Enterprise Blueprint & Sync Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Decoupled Multi-Platform Media & State pipeline reference.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // Progress simulation timeline
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Interactive Ingestion Simulator", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Step ${uploadProgressStep}/3: $progressStatusText", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (uploadProgressStep < 3) {
                                        Button(
                                            onClick = {
                                                uploadProgressStep++
                                                progressStatusText = when (uploadProgressStep) {
                                                    1 -> "Uploading uncropped file raw bytes to Supabase storage bucket 'art-images-bucket'..."
                                                    2 -> "Resolved public URL: https://roqcztwhwonmfwbhwkuk.supabase.co/storage/v1/object/public/art-images-bucket/..."
                                                    3 -> "Synchronized Firebase real-time JSON node at path '/artworks/id_992' successfully!"
                                                    else -> "Upload complete"
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Next Step", fontSize = 10.sp)
                                        }
                                    } else {
                                        TextButton(onClick = {
                                            uploadProgressStep = 0
                                            progressStatusText = "Simulation reset"
                                        }) {
                                            Text("Reset", fontSize = 10.sp)
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = uploadProgressStep / 3f,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }

                        // Code selection buttons
                        ScrollableTabRow(
                            selectedTabIndex = when (blueprintTab) {
                                "Architecture" -> 0
                                "Schema" -> 1
                                "UI code" -> 2
                                else -> 3
                            },
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Architecture", "Schema", "UI code", "API code").forEach { t ->
                                Tab(
                                    selected = blueprintTab == t,
                                    onClick = { blueprintTab = t },
                                    text = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                when (blueprintTab) {
                                    "Architecture" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Data Synchronization Flow", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("1. Client uploads raw binary image payload to Supabase Bucket Storage.\n" +
                                                    "2. Supabase server processes upload, registers visual asset, and returns secure public image URL.\n" +
                                                    "3. Android client writes artwork object metadata (including resolved public image URL) directly to Firebase Realtime Database.\n" +
                                                    "4. Real-time synchronizer is notified, rendering artwork on discovery feeds.",
                                                fontSize = 11.sp, lineHeight = 16.sp)
                                        }
                                    }
                                    "Schema" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Firebase Database Nodes Configuration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            SelectionContainer {
                                                Text(
                                                    text = "{\n" +
                                                            "  \"artworks\": {\n" +
                                                            "    \"art_id_0\": {\n" +
                                                            "      \"id\": \"art_id_0\",\n" +
                                                            "      \"title\": \"Aetherial Blueprints\",\n" +
                                                            "      \"imageUrl\": \"https://roqcztwhwonmfwbhwkuk.supabase.co/storage/v1/object/public/art-images-bucket/...\",\n" +
                                                            "      \"priceUsd\": 1250,\n" +
                                                            "      \"artistId\": \"artist_990\"\n" +
                                                            "    }\n" +
                                                            "  }\n" +
                                                            "}",
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                    "UI code" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Jetpack Compose Pinterest Masonry Grid Code Slice", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            SelectionContainer {
                                                Text(
                                                    text = "@OptIn(ExperimentalFoundationApi::class)\n" +
                                                            "@Composable\n" +
                                                            "fun PinterestMasonryGrid(\n" +
                                                            "    artworks: List<Artwork>,\n" +
                                                            "    onItemClick: (String) -> Unit\n" +
                                                            ") {\n" +
                                                            "    LazyVerticalStaggeredGrid(\n" +
                                                            "        columns = StaggeredGridCells.Fixed(2),\n" +
                                                            "        modifier = Modifier.fillMaxWidth(),\n" +
                                                            "        horizontalArrangement = Arrangement.spacedBy(8.dp),\n" +
                                                            "        verticalItemSpacing = 8.dp\n" +
                                                            "    ) {\n" +
                                                            "        items(artworks) { artwork ->\n" +
                                                            "            Column(modifier = Modifier.clickable { onItemClick(artwork.id) }) {\n" +
                                                            "                AsyncImage(model = artwork.imageUrl, contentScale = ContentScale.Crop)\n" +
                                                            "                Text(text = artwork.title, fontWeight = FontWeight.Bold)\n" +
                                                            "            }\n" +
                                                            "        }\n" +
                                                            "    }\n" +
                                                            "}",
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                    "API code" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Retrofit Multi-Platform Pipeline Sync", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            SelectionContainer {
                                                Text(
                                                    text = "interface SupabaseStorageApi {\n" +
                                                            "    @Multipart\n" +
                                                            "    @POST(\"storage/v1/object/art-images-bucket/{fileName}\")\n" +
                                                            "    suspend fun uploadProductImage(\n" +
                                                            "        @Header(\"Authorization\") token: String,\n" +
                                                            "        @Path(\"fileName\") name: String,\n" +
                                                            "        @Part body: MultipartBody.Part\n" +
                                                            "    ): UploadResponse\n" +
                                                            "}",
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
                                                )
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
    }
}
