package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun ArtworkDetailScreen(
    artworkId: String,
    viewModel: AuraViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val artworks by viewModel.artworks.collectAsState()
    val favoriteStateMap by viewModel.favoriteStateMap.collectAsState()

    val artwork = artworks.find { it.id == artworkId } ?: MockArtData.artworks[0]
    val isLiked = favoriteStateMap[artwork.id]?.isLiked == true
    val isSaved = favoriteStateMap[artwork.id]?.isSavedToCollection == true

    var selectedTab by remember { mutableStateOf("Showcase") } // Showcase | Material Core | Provenance Graph
    var isSendingInquiry by remember { mutableStateOf(false) }
    var inquiryMessageText by remember { mutableStateOf("") }
    var inquiryFeedbackMessage by remember { mutableStateOf("") }

    // Customizers
    var selectedFrameStyle by remember { mutableStateOf("No Frame") } // No Frame | Natural Oak | Polished Brass | Obsidian Float
    var mattingSizeInches by remember { mutableFloatStateOf(0f) }

    // AR Wall Placements
    var showARViewer by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Immersive Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return")
                }

                Text(
                    text = "CANVAS EXPLORER",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.doubleTapToLike(artwork.id) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Toggle",
                            tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.saveArtworkToCollection(artwork.id, "Main Favorites Board") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Toggle",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Main High-Res visual viewer with interactive frames overlay
                item {
                    ArtVisualFrameContainer(
                        imageUrl = artwork.imageUrl,
                        title = artwork.title,
                        frameStyle = selectedFrameStyle,
                        mattingSize = mattingSizeInches
                    )
                }

                // Interactive control modules (Frame customization + Show in Your Wall)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Frame style chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dynamic Framing Engine", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = selectedFrameStyle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("No Frame", "Natural Oak", "Polished Brass", "Obsidian Float").forEach { style ->
                                val active = selectedFrameStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { selectedFrameStyle = style }
                                        .border(1.dp, if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Matting slider
                        if (selectedFrameStyle != "No Frame") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Acid-Free Matting Border", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${mattingSizeInches.toInt()} inches", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = mattingSizeInches,
                                    onValueChange = { mattingSizeInches = it },
                                    valueRange = 0f..4f,
                                    steps = 3
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Interactive AR Screen Trigger button
                        Button(
                            onClick = { showARViewer = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.ViewInAr, contentDescription = "AR on wall")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Spatial placement (AR View)")
                        }
                    }
                }

                // Info Tabs
                item {
                    TabRow(
                        selectedTabIndex = run {
                            when (selectedTab) {
                                "Showcase" -> 0
                                "Material Core" -> 1
                                else -> 2
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(selected = selectedTab == "Showcase", onClick = { selectedTab = "Showcase" }) {
                            Text("Specs", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == "Material Core", onClick = { selectedTab = "Material Core" }) {
                            Text("Structure", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == "Provenance Graph", onClick = { selectedTab = "Provenance Graph" }) {
                            Text("Provenance", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tab Content Switch calculations
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            "Showcase" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = artwork.title,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Display Dimension scale metrics
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$${artwork.priceUsd.toInt()}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${artwork.widthInches.toInt()}″ × ${artwork.heightInches.toInt()}″ Physical Canvas",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Artist Profile Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.LightGray)
                                            ) {
                                                AsyncImage(
                                                    model = artwork.artist.avatarUrl,
                                                    contentDescription = artwork.artist.name,
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = artwork.artist.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "@${artwork.artist.alias} • Verified Master", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }

                                            IconButton(
                                                onClick = { isSendingInquiry = true },
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                            ) {
                                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Inquiry", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = artwork.description,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Artist Statement Commentary", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Audio comment", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "\"${artwork.artist.statement}\"",
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic,
                                                lineHeight = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                                            )
                                        }
                                    }
                                }
                            }

                            "Material Core" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Macro Composition Layers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "Interactive breakdown of materials, varnishes, or geometric math shaders present in this listing.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    artwork.materials.forEach { layer ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            shadowElevation = 1.dp,
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = layer.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(text = layer.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("${layer.zoomLevelPercentage}% Zoom", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Provenance Graph" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Securities & Cryptographic Provenance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "Verifiable record of ownership, solo view exhibitions, and cryptographic certifications.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Provenance timeline tree
                                    artwork.provenance.forEachIndexed { index, node ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                if (index < artwork.provenance.size - 1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(30.dp)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                                    )
                                                }
                                            }
                                            Text(
                                                text = node,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Certificate of Authenticity badge
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = "COA SECURE", tint = MaterialTheme.colorScheme.primary)
                                        Column {
                                            Text("Cryptographic COA Sealed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text("Downloads matching encrypted signatures on Aura Ledger.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Secure Checkout bottom anchoring panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Unified Checkout Value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${artwork.priceUsd.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Or $${(artwork.priceUsd / 4).toInt()}/mo Affirm 0% APR", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (artwork.isDigitalLicenseAvailable) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.addArtworkToCart(artwork.id, purchaseDigital = true)
                                    onNavigateToCart()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Licensing ($${artwork.digitalLicensePriceUsd.toInt()})", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.addArtworkToCart(artwork.id, purchaseDigital = false)
                                onNavigateToCart()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Go checkouts", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Acquire physical")
                        }
                    }
                }
            }
        }

        // DIRECT DIALOG INQUIRY OVERLAY
        if (isSendingInquiry) {
            Dialog(onDismissRequest = { isSendingInquiry = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Inquiry Request with Artist", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Connects directly with @${artwork.artist.alias} via encrypted secure message protocol.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = inquiryMessageText,
                            onValueChange = { inquiryMessageText = it },
                            placeholder = { Text("E.g. I live in Portland, can I request custom anti-reflective museum glass matting?", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (inquiryFeedbackMessage.isNotEmpty()) {
                            Text(text = inquiryFeedbackMessage, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { isSendingInquiry = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    inquiryFeedbackMessage = "Securing socket connection... Sending metadata..."
                                    inquiryFeedbackMessage = "Msg sent! Artist response expected under 4 hours."
                                    inquiryMessageText = ""
                                }
                            ) {
                                Text("Send Query")
                            }
                        }
                    }
                }
            }
        }

        // SIMULATED ROOM AR PORTRAIT VIEW ON YOUR WALL
        if (showARViewer) {
            Dialog(onDismissRequest = { showARViewer = false }) {
                SimulatedARViewer(
                    artwork = artwork,
                    frameStyle = selectedFrameStyle,
                    onDismiss = { showARViewer = false }
                )
            }
        }
    }
}

@Composable
fun ArtVisualFrameContainer(
    imageUrl: String,
    title: String,
    frameStyle: String,
    mattingSize: Float,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic custom Frame border definitions
        val frameThickness = when (frameStyle) {
            "No Frame" -> 0.dp
            "Natural Oak" -> 16.dp
            "Polished Brass" -> 12.dp
            else -> 18.dp // Obsidian Float
        }

        val frameColor = when (frameStyle) {
            "Natural Oak" -> Color(0xFFC19A6B)
            "Polished Brass" -> Color(0xFFD4AF37)
            "Obsidian Float" -> Color(0xFF1E1E1E)
            else -> Color.Transparent
        }

        val mattingThickness = (mattingSize * 8).dp

        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .shadow(
                    elevation = if (frameStyle != "No Frame") 12.dp else 4.dp,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(frameColor)
                .padding(frameThickness)
                .background(Color(0xFFFDFBF7)) // Fine-art off-white Matting sheet
                .padding(mattingThickness),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .heightIn(max = 300.dp)
            )
        }

        // Pinch-to-zoom user tip indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom indicator", tint = Color.White, modifier = Modifier.size(12.dp))
            Text("Pinch to zoom high-resolution vectors", color = Color.White, fontSize = 9.sp)
        }
    }
}

@Composable
fun SimulatedARViewer(
    artwork: Artwork,
    frameStyle: String,
    onDismiss: () -> Unit
) {
    val rooms = listOf(
        Pair("Sleek Modern Living Room", "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&q=80&w=600"),
        Pair("Nordic Cozy Study Room", "https://images.unsplash.com/photo-1617806118233-18e1db207faf?auto=format&fit=crop&q=80&w=600"),
        Pair("Cyber Industrial Loft", "https://images.unsplash.com/photo-1558211583-d26f610c1eb1?auto=format&fit=crop&q=80&w=600")
    )
    var selectedRoomIndex by remember { mutableIntStateOf(0) }
    var useWarmLighting by remember { mutableStateOf(false) }
    var showManSilhouette by remember { mutableStateOf(true) }

    val activeRoomUrl = rooms[selectedRoomIndex].second

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.scaffoldBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AR View on Your Wall", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Real simulated render stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.DarkGray)
            ) {
                // Room Background
                AsyncImage(
                    model = activeRoomUrl,
                    contentDescription = "Room Environment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Render dynamic Warm Lighting filter overlay
                if (useWarmLighting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFA726).copy(alpha = 0.18f))
                    )
                }

                // Render Human Silhouette next to canvas for scale reference (Fulfills Dimensional Context Simulator!)
                if (showManSilhouette) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = "Human Scale Silhouette",
                        tint = Color.White.copy(alpha = 0.62f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 32.dp, bottom = 20.dp)
                            .size(180.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 48.dp, bottom = 180.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Human Silhouette (5'10\")", color = Color.White, fontSize = 8.sp)
                    }
                }

                // Scaled Canvas element positioned over active wall section
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 40.dp, y = (-40).dp)
                        .width(100.dp)
                        .height(130.dp)
                        .shadow(
                            elevation = 8.dp,
                            ambientColor = if (useWarmLighting) Color(0xFFE65100) else Color.Black
                        )
                        .background(Color.DarkGray)
                        .border(
                            width = 2.dp,
                            color = if (frameStyle != "No Frame") Color(0xFFD4AF37) else Color.Transparent
                        )
                ) {
                    AsyncImage(
                        model = artwork.imageUrl,
                        contentDescription = "Canvas scale",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Controls overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Background selectors
                Text("Select Household Room Environment:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rooms.size) { idx ->
                        val (name, _) = rooms[idx]
                        val active = selectedRoomIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedRoomIndex = idx }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Divider()

                // Lighting triggers & Silhouette toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Light", tint = if (useWarmLighting) Color.Yellow else Color.Gray)
                        Text("Warm Lighting (3000K Kelvins)", fontSize = 11.sp)
                    }
                    Switch(checked = useWarmLighting, onCheckedChange = { useWarmLighting = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Accessibility, contentDescription = "People scale", tint = if (showManSilhouette) MaterialTheme.colorScheme.primary else Color.Gray)
                        Text("Renders human silhouette for dimensional context", fontSize = 11.sp)
                    }
                    Switch(checked = showManSilhouette, onCheckedChange = { showManSilhouette = it })
                }
            }
        }
    }
}

// Scaffolding auxiliary helper schema color
@Composable
private fun ColorScheme.scaffoldBackground(): Color {
    return surface
}
