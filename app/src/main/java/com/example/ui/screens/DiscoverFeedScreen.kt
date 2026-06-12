package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Artwork
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoverFeedScreen(
    viewModel: AuraViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAuctions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val artworks by viewModel.filteredArtworks.collectAsState()
    val favoriteStateMap by viewModel.favoriteStateMap.collectAsState()
    val cartItems by viewModel.cartItemsList.collectAsState()

    var columnCount by remember { mutableIntStateOf(2) } // Pinterest standard: 2
    var selectedArtworkForPeek by remember { mutableStateOf<Artwork?>(null) }
    var selectedArtworkForSaveCollection by remember { mutableStateOf<Artwork?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-fidelity Header Context
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AURA ART",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Curated ambient art marketplace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Grid Column configuration toggles
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { columnCount = 1 },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = "Immersive Full View",
                            tint = if (columnCount == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { columnCount = 2 },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Standard 2-Column Grid",
                            tint = if (columnCount == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { columnCount = 3 },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Compact Grid",
                            tint = if (columnCount == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Hero Mood Board snippet injected seamlessly
            MoodBoardModule()

            // Asymmetrical Staggered Infinite Feed
            if (artworks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Brush,
                            contentDescription = "No artworks found",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No visual matches. Try modifying your search parameters.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(artworks, key = { it.id }) { artwork ->
                        val isLiked = favoriteStateMap[artwork.id]?.isLiked == true
                        val isSaved = favoriteStateMap[artwork.id]?.isSavedToCollection == true
                        val isCurrentlyInCart = cartItems.any { it.artworkId == artwork.id }

                        ArtGridItem(
                            artwork = artwork,
                            isLiked = isLiked,
                            isSaved = isSaved,
                            isInCart = isCurrentlyInCart,
                            onClick = { onNavigateToDetail(artwork.id) },
                            onDoubleTap = { viewModel.doubleTapToLike(artwork.id) },
                            onLongPress = { selectedArtworkForPeek = artwork },
                            onSaveClick = { selectedArtworkForSaveCollection = artwork },
                            onAddCartClick = { viewModel.addArtworkToCart(artwork.id) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )

        // Long-Press IMMERSIVE PEEK DIALOG MOCKUP
        selectedArtworkForPeek?.let { artwork ->
            PeekArtworkDialog(
                artwork = artwork,
                onDismiss = { selectedArtworkForPeek = null },
                onNavigateToDetail = {
                    selectedArtworkForPeek = null
                    onNavigateToDetail(artwork.id)
                }
            )
        }

        // SAVE TO COLLECTION FOLDER SELECTION SHEET
        selectedArtworkForSaveCollection?.let { artwork ->
            SaveToCollectionDialog(
                artwork = artwork,
                onDismiss = { selectedArtworkForSaveCollection = null },
                onSave = { collectionName ->
                    viewModel.saveArtworkToCollection(artwork.id, collectionName)
                    selectedArtworkForSaveCollection = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Saved artwork to '$collectionName'")
                    }
                }
            )
        }
    }
}

@Composable
fun MoodBoardModule() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Collection icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Moodspace Curations",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Aura Bloom: Organic Shadows",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "12 selected items exploring ambient reflection coefficients and light-absorbing varnishes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&q=80&w=200",
                    contentDescription = "Moodboard Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ArtGridItem(
    artwork: Artwork,
    isLiked: Boolean,
    isSaved: Boolean,
    isInCart: Boolean,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    onSaveClick: () -> Unit,
    onAddCartClick: () -> Unit
) {
    var showQuickLikeParticle by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Variable Height layout based on portrait vs landscape orientation
    val cardHeight = when (artwork.orientation) {
        com.example.data.ArtOrientation.PORTRAIT -> 240.dp
        com.example.data.ArtOrientation.LANDSCAPE -> 150.dp
        com.example.data.ArtOrientation.SQUARE -> 190.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(artwork.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = {
                        onDoubleTap()
                        showQuickLikeParticle = true
                        coroutineScope.launch {
                            delay(800)
                            showQuickLikeParticle = false
                        }
                    },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                ) {
                    // Visual Art Showcase using Coil
                    AsyncImage(
                        model = artwork.imageUrl,
                        contentDescription = artwork.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top float indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Badge / Medium chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = artwork.medium.name.replace("_", " "),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Save to Collection Button Quick-trigger
                        IconButton(
                            onClick = { onSaveClick() },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isSaved) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Quick Save Board",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Bottom info overlap styling (Pinterest ambient look)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Price Badge
                            Text(
                                text = if (artwork.isAuction) "LIVE BID" else "$${artwork.priceUsd.toInt()}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Quick add cart
                            if (!artwork.isAuction) {
                                IconButton(
                                    onClick = { onAddCartClick() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isInCart) Icons.Default.ShoppingBag else Icons.Outlined.ShoppingBag,
                                        contentDescription = "Quick Cart",
                                        tint = if (isInCart) MaterialTheme.colorScheme.primary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Metadata block underneath
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = artwork.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            AsyncImage(
                                model = artwork.artist.avatarUrl,
                                contentDescription = artwork.artist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "@${artwork.artist.alias}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Quick Double-Tap Particle Animation Overlays
    AnimatedVisibility(
            visible = showQuickLikeParticle,
            enter = scaleIn(animationSpec = spring()) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun PeekArtworkDialog(
    artwork: Artwork,
    onDismiss: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    AsyncImage(
                        model = artwork.imageUrl,
                        contentDescription = artwork.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Text("RELEASE TO DISMISS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = artwork.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            AsyncImage(
                                model = artwork.artist.avatarUrl,
                                contentDescription = artwork.artist.name,
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = artwork.artist.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Visual palette display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        artwork.colors.forEach { paletteColor ->
                            Box(
                                modifier = Modifier
                                    .weight(paletteColor.weight)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(android.graphics.Color.parseColor(paletteColor.hex)))
                            )
                        }
                    }

                    Text(
                        text = artwork.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Button(
                        onClick = { onNavigateToDetail() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = "Deep view", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Immersive Canvas Space")
                    }
                }
            }
        }
    }
}

@Composable
fun SaveToCollectionDialog(
    artwork: Artwork,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val predefinedCollections = listOf("Living Room Inspiration", "Ambient Slates", "Cyberpunk Portfolios", "Main Favorites Board")
    var customCollectionName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Save to Curated Collection",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Distinguish public curation spaces from premium secret design boards.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedCollections) { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSave(name) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = "Folder", tint = MaterialTheme.colorScheme.primary)
                            Text(text = name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Divider()

                OutlinedTextField(
                    value = customCollectionName,
                    onValueChange = { customCollectionName = it },
                    label = { Text("New Custom Board") },
                    placeholder = { Text("E.g. Study Room Glazes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (customCollectionName.isNotEmpty()) {
                            IconButton(onClick = { onSave(customCollectionName) }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add board")
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
