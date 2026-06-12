package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Artwork
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay

@Composable
fun AuctionHouseScreen(
    viewModel: AuraViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val artworks by viewModel.artworks.collectAsState()
    val auctionWorks = artworks.filter { it.isAuction }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auction Header context
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura Bidding House",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Text("LIVE SOCKETS", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "Real-time bidding feeds featuring anonymous handles and soft-close extensions.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (auctionWorks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No active auctions in this cycle.", fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(auctionWorks) { artwork ->
                    AuctionCard(
                        artwork = artwork,
                        viewModel = viewModel,
                        onClick = { onNavigateToDetail(artwork.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AuctionCard(
    artwork: Artwork,
    viewModel: AuraViewModel,
    onClick: () -> Unit
) {
    val bids by viewModel.getBidsForArtwork(artwork.id).collectAsState(initial = emptyList())

    // Bidding forms inputs
    var bidAmountText by remember { mutableStateOf("") }
    var bidderNameInput by remember { mutableStateOf("master_collector_99") }
    var setupProxyBidding by remember { mutableStateOf(false) }
    var proxyLimitText by remember { mutableStateOf("") }

    var feedbackMessage by remember { mutableStateOf("") }
    var countdownText by remember { mutableStateOf("00:00:00") }

    val reserveMet = artwork.auctionCurrentBidUsd >= artwork.auctionReservePriceUsd

    // Live WebSockets CountDown Timer Simulation
    LaunchedEffect(artwork.auctionEndTimeEpochMs) {
        while (true) {
            val totalRemainingMs = artwork.auctionEndTimeEpochMs - System.currentTimeMillis()
            if (totalRemainingMs <= 0) {
                countdownText = "AUCTION IN ESCROW CLOSED"
                break
            } else {
                val sec = (totalRemainingMs / 1000) % 60
                val min = (totalRemainingMs / (1000 * 60)) % 60
                val hours = (totalRemainingMs / (1000 * 60 * 60)) % 24
                countdownText = String.format("%02d : %02d : %02d", hours, min, sec)
            }
            delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual showcase row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = artwork.imageUrl,
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Countdown Timer floating overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.82f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer", tint = Color.Yellow, modifier = Modifier.size(12.dp))
                        Text(
                            text = countdownText,
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reserve price indicator floating banner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (reserveMet) Color(0xFF10B981) else Color(0xFFEF4444))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (reserveMet) "RESERVE MET" else "RESERVE NOT MET ($1000)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title & Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = artwork.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "by ${artwork.artist.name} (Provenance Verified)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Current Bid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${artwork.auctionCurrentBidUsd.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider()

                // Bid placing section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Live Bid Panel", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = bidderNameInput,
                            onValueChange = { bidderNameInput = it },
                            label = { Text("Your Handle") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )

                        OutlinedTextField(
                            value = bidAmountText,
                            onValueChange = { bidAmountText = it },
                            label = { Text("Valuation Bid ($)") },
                            placeholder = { Text("E.g. 900") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                    }

                    // Proxy details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = setupProxyBidding, onCheckedChange = { setupProxyBidding = it })
                            Text("Automatic Proxy Bidding", fontSize = 11.sp)
                        }

                        if (setupProxyBidding) {
                            OutlinedTextField(
                                value = proxyLimitText,
                                onValueChange = { proxyLimitText = it },
                                label = { Text("Max Limit ($)") },
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = TextStyle(fontSize = 11.sp)
                            )
                        }
                    }

                    // Bid placing button triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amount = bidAmountText.toDoubleOrNull()
                                if (amount == null) {
                                    feedbackMessage = "Invalid bid number format"
                                    return@Button
                                }
                                feedbackMessage = viewModel.placeBid(artwork.id, bidderNameInput, amount)
                                bidAmountText = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Gavel, contentDescription = "Place bid")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transmit Live Bid")
                        }

                        // Buy-it-now immediate path override (Feature 73)
                        OutlinedButton(
                            onClick = onClick,
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buy Out Canvas Now")
                        }
                    }

                    if (feedbackMessage.isNotEmpty()) {
                        Text(
                            text = feedbackMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Ticker soft-closed warning alerts (Feature 69)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Soft-close Alert", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Soft-close: Placed bids in the final 60 seconds automatically extend the clock by 2m.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Divider()

                // Encrypted handles real-time feed ticker lists
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Anonymous Socket Stream Bid Ticker", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    bids.take(4).forEach { bid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )

                                val visibleCharCount = 3
                                val maskedHandle = if (bid.bidderHandle.length > visibleCharCount) {
                                    bid.bidderHandle.take(visibleCharCount) + "***" + bid.bidderHandle.takeLast(visibleCharCount)
                                } else {
                                    "an***tor"
                                }
                                Text(text = maskedHandle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = "$${bid.amountUsd.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private class TextStyle(val fontSize: androidx.compose.ui.unit.TextUnit)
