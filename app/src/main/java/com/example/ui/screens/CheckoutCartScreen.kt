package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CartItemEntity
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CheckoutCartScreen(
    viewModel: AuraViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val artworks by viewModel.artworks.collectAsState()
    val cartItems by viewModel.cartItemsList.collectAsState()

    val contextArtMap = artworks.associateBy { it.id }

    var selectedCrateOptions by remember { mutableStateOf("Standard Crate") } // Standard Crate ($0) | Solid Wooden Vault ($150)
    var insuranceOptIn by remember { mutableStateOf(true) }
    var handwrittenNoteText by remember { mutableStateOf("") }
    var promoCodeText by remember { mutableStateOf("") }
    var promoDiscountsUsd by remember { mutableFloatStateOf(0f) }

    var checkoutFeedbackMessage by remember { mutableStateOf("") }
    var isCheckingOut by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calculations
    val subtotal = cartItems.sumOf { it.priceUsd }
    val crateCost = if (selectedCrateOptions == "Solid Wooden Vault") 150.0 else 0.0
    val insuranceCost = if (insuranceOptIn) subtotal * 0.015 else 0.0 // 1.5% high value insurance
    val liveTaxRateAvalara = 0.088 // 8.8% Washington/Seattle sales taxes
    val calculatedTax = (subtotal + crateCost) * liveTaxRateAvalara
    val grandTotal = subtotal + crateCost + insuranceCost + calculatedTax - promoDiscountsUsd

    // Split breakdowns (Escrow calculations)
    val platformFeeCommission = subtotal * 0.10 // 10% platform fee
    val shippingProvFee = crateCost + (if (subtotal > 0) 80.0 else 0.0) // baseline freight cost
    val artistEarningsPayout = subtotal - platformFeeCommission

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cart screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return")
                }
                Text(
                    text = "Commercial Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (cartItems.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear Cart")
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text("Your Aura checkout cart is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onNavigateBack) {
                        Text("Explore Art Collection")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cart Items loop
                items(cartItems) { cartItem ->
                    val artwork = contextArtMap[cartItem.artworkId]
                    if (artwork != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Gray)
                                ) {
                                    AsyncImage(
                                        model = artwork.imageUrl,
                                        contentDescription = artwork.title,
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = artwork.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "by ${artwork.artist.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (cartItem.isDigitalLicense) "Safe Watermarked Digital License" else "Physical Solid Canvas",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${cartItem.priceUsd.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    IconButton(
                                        onClick = { viewModel.removeArtworkFromCart(cartItem.artworkId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Crate shipping selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fragile Freight Crate Protection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Standard Crate", "Solid Wooden Vault").forEach { crate ->
                                val active = selectedCrateOptions == crate
                                val price = if (crate == "Solid Wooden Vault") " (+$150)" else " (Free)"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .clickable { selectedCrateOptions = crate }
                                        .border(1.dp, if (active) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$crate$price",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Insurance opt-in (Feature 60)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("High-Value Fine Art Transit Insurance", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Full coverage transit protections against damage, scratch, or logistics loss (1.5% premium).", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = insuranceOptIn, onCheckedChange = { insuranceOptIn = it })
                        }
                    }
                }

                // Handwritten note & promo codes
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = handwrittenNoteText,
                            onValueChange = { handwrittenNoteText = it },
                            label = { Text("Gift wrapping handwritten note markdown", fontSize = 11.sp) },
                            placeholder = { Text("E.g. Happy Wedding Anniversary! Enjoy this Elianna oil canvas...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = promoCodeText,
                                onValueChange = { promoCodeText = it },
                                label = { Text("Aura promo coupon code", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (promoCodeText.equals("AURA10", ignoreCase = true)) {
                                        promoDiscountsUsd = (subtotal * 0.10).toFloat()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }

                // Escrow hold warnings (Feature 55)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Escrow Secure", tint = Color(0xFF0284C7))
                        Column {
                            Text("AURA ESGROW SECURITIES ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                            Text("Your total funds are held in state escrow. Release to artist occurs strictly after buyer confirmation delivery.", fontSize = 9.sp, color = Color(0xFF0369A1))
                        }
                    }
                }

                // Checkout valuation breakdown totals (Feature 56 split breakdown)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Official Commercial Invoicing breakdown", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Art Subtotal value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${subtotal.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        if (crateCost > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wooden crate protective container", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${crateCost.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        if (insuranceCost > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("High-value cargo transit insurance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${insuranceCost.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Washington Avalara computed sales tax", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${calculatedTax.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        if (promoDiscountsUsd > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Promotional stacked coupon discount (AURA10)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text("-$${promoDiscountsUsd.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Divider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Grand Total", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("$${grandTotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }

                        Divider()

                        // Split payout details (Feature 56)
                        Text("Securities Split-Pay Breakdown:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Artist Payout", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${artistEarningsPayout.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Platform Commission", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${platformFeeCommission.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Freight Logistics", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${shippingProvFee.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Klarna / Afterpay installments visualization (Feature 63)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFFFFB3C6)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Financing via Klarna. available", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC70039))
                                Text("Pay in 4 interest-free installments of $${(grandTotal / 4).toInt()} USD monthly.", fontSize = 9.sp, color = Color(0xFFC70039))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFA6C9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Klarna.", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Process buttons
                item {
                    Button(
                        onClick = {
                            isCheckingOut = true
                            checkoutFeedbackMessage = "Authorizing dynamic security token..."
                            coroutineScope.launch {
                                delay(1200)
                                checkoutFeedbackMessage = "Securing funds in Escrow contracts..."
                                delay(1000)
                                checkoutFeedbackMessage = "Success! Custom packing crates are scheduled. Invoices generated."
                                delay(2000)
                                viewModel.clearCart()
                                isCheckingOut = false
                                onNavigateBack()
                            }
                        },
                        enabled = !isCheckingOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCheckingOut) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.Payment, contentDescription = "Pay")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Secure Payment Settlement")
                        }
                    }
                }

                if (checkoutFeedbackMessage.isNotEmpty()) {
                    item {
                        Text(
                            text = checkoutFeedbackMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                }
            }
        }
    }
}
