# ArtimaX Enterprise Multi-Vendor E-Commerce Marketplace
## Clean Architecture & System Engineering Blueprint

This document specifies the professional technical blueprint, database schema, multi-platform media pipelines, and implementation code for the **ArtimaX** Android application.

---

## 1. System Engineering & Data Flow Architecture

The ArtimaX runtime combines **Supabase Storage** (optimized visual media CDN pipeline) with **Firebase Realtime Database** (real-time synchronized state tree) in a decoupled, offline-first client architecture.

```
+----------------------------------------------------------------------------+
|                          ArtimaX Android App Client                        |
+---------------------+---------------------------------+--------------------+
                      |                                 |
         [1] Upload Image (Multipart)        [3] Write Metadata (JSON Object)
                      |                                 |
                      v                                 v
+---------------------+-----+                   +-------+--------------------+
|  Supabase Storage Bucket  |                   |   Firebase Realtime DB     |
|  "art-images-bucket"      |                   |   (Real-time State Tree)   |
+---------------------+-----+                   +-------+--------------------+
                      |                                 |
             [2] Return Public URL                      | [4] Real-time Sync
                      |                                 |     To Connected
                      +-------------------------------->+     Browsers/Clients
```

### Decoupled Data Flow Sequence
1. **Media Ingestion**: The Artist/Seller selects a high-resolution, uncropped artwork scan within the Seller Console. The application splits the payload; the binary image file is streamed directly to Supabase Storage bucket `"art-images-bucket"` using the public `anon` credentials.
2. **CDN Delivery Resolution**: Supabase resolves the storage path, applies optimization matrices, and returns a persistent HTTPS public reference URL.
3. **Transactional Synchronization**: The active Android client attaches the resolved public image URL to the product metadata payload (which includes aspect ratios, title, description, material properties, split-vendor payout indices, and price points) and writes it to the designated Firebase database path under `/artworks/$id`.
4. **Real-time Discovery Propagation**: Any buyer client listening to the synchronized Firebase pipeline updates its local state cache, instantly rendering the new variable-height product card on their discovery Feed.

---

## 2. Database & State Schema Specifications

### A. Firebase Realtime Database JSON Schema Tree
```json
{
  "artworks": {
    "artwork_identifier_uuid": {
      "id": "artwork_identifier_uuid",
      "title": "Aetherial Whispers in Granite",
      "description": "An exploration of biomorphic fluid forms against rugged stone frameworks.",
      "imageUrl": "https://roqcztwhwonmfwbhwkuk.supabase.co/storage/v1/object/public/art-images-bucket/aetherial_whispers.jpg",
      "priceUsd": 1250.00,
      "widthInches": 36.0,
      "heightInches": 48.0,
      "medium": "OIL_ON_CANVAS",
      "orientation": "PORTRAIT",
      "artistId": "artist_vendor_elianna",
      "tags": ["ambient", "modern", "oil"],
      "digitalLicenseAvailable": true,
      "digitalLicensePriceUsd": 240.00,
      "creationTimestamp": 1782349810
    }
  },
  "users": {
    "user_buyer_uuid": {
      "id": "user_buyer_uuid",
      "displayName": "Marcus Vance",
      "shippingAddress": {
        "street": "1420 Pine Street",
        "city": "Seattle",
        "state": "WA",
        "zip": "98101",
        "country": "USA"
      }
    }
  },
  "orders": {
    "order_transaction_uuid": {
      "id": "order_transaction_uuid",
      "buyerId": "user_buyer_uuid",
      "lineItems": [
        {
          "artworkId": "artwork_identifier_uuid",
          "quantity": 1,
          "isDigitalLicense": false,
          "priceUsd": 1250.00,
          "vendorId": "artist_vendor_elianna"
        }
      ],
      "totals": {
        "subtotal": 1250.00,
        "tax": 110.00,
        "shipping": 80.00,
        "grandTotal": 1440.00
      },
      "splitSettlementLedger": {
        "platformCommissionUsd": 125.00,
        "vendorPayoutUsd": 1125.00,
        "escrowStatus": "RELEASED_TO_VENDORS"
      },
      "shippingTracker": {
        "carrier": "FragileExpress",
        "trackingNumber": "FE-99887755",
        "currentStatus": "In Transit - Custom Crate Protection Engaged"
      }
    }
  }
}
```

---

## 3. UI Component Implementation: Pinterest Masonry Grid

The following Kotlin Jetpack Compose implementation builds a high-performance, responsive, asymmetrical masonry discovery feed utilizing Material 3 tokens.

```kotlin
package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Artwork
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.PinterestCharcoal
import com.example.ui.theme.PinterestSubtitles

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinterestMasonryGrid(
    artworks: List<Artwork>,
    onItemClick: (String) -> Unit,
    onItemLongPress: (Artwork) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2), // Pinterest dual grid standard
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(artworks, key = { it.id }) { artwork ->
            MasonryProductCard(
                artwork = artwork,
                onClick = { onItemClick(artwork.id) },
                onLongPress = { onItemLongPress(artwork) }
            )
        }
    }
}

@Composable
fun MasonryProductCard(
    artwork: Artwork,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic height determination matching image's native metadata
    val calculatedHeight = when (artwork.orientation) {
        com.example.data.ArtOrientation.PORTRAIT -> 250.dp
        com.example.data.ArtOrientation.LANDSCAPE -> 160.dp
        com.example.data.ArtOrientation.SQUARE -> 200.dp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(artwork.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), // Pinterest rounded aesthetics
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = artwork.imageUrl,
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Crop, // Preserving native proportions beautifully
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calculatedHeight)
                        .clip(RoundedCornerShape(16.dp))
                )
                
                // Active Brand Price Tag Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$${artwork.priceUsd.toInt()}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Metadata representation
        Text(
            text = artwork.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PinterestCharcoal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "@${artwork.artist.alias}",
            fontSize = 11.sp,
            color = PinterestSubtitles,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp, bottom = 4.dp)
        )
    }
}
```

---

## 4. API & Storage Repository Implementation

Complete API snippet deploying Kotlin and Retrofit-style wrappers to bridge Supabase and Firebase Database.

```kotlin
package com.example.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File

interface SupabaseStorageApi {
    @Multipart
    @POST("storage/v1/object/art-images-bucket/{fileName}")
    suspend fun uploadProductImage(
        @Header("Authorization") apiKeyToken: String,
        @Header("apikey") apiKey: String,
        @Path("fileName") fileName: String,
        @Part fileBody: MultipartBody.Part
    ): SupabaseUploadResponse
}

data class SupabaseUploadResponse(
    val Key: String,
    val Id: String
)

class ProductRepository {
    private val supabaseBaseUrl = "https://roqcztwhwonmfwbhwkuk.supabase.co/"
    private val supabasePublishableAnonKey = "sb_publishable_XcVPwjUbQfchjhJzcJlNAg__qgxnFKU"
    
    private val api: SupabaseStorageApi = Retrofit.Builder()
        .baseUrl(supabaseBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SupabaseStorageApi::class.java)

    private val firebaseDbInstance = FirebaseDatabase.getInstance()

    /**
     * Uploads physical painting scan or visual file to Supabase Object Storage
     * and persists standard Firebase transaction data synchronously.
     */
    suspend fun processAndRegisterNewArtwork(
        localImageFile: File,
        title: String,
        description: String,
        priceUsd: Double,
        artistId: String,
        mediumName: String,
        orientationName: String
    ): Boolean {
        return try {
            val uniqueFilename = "${System.currentTimeMillis()}_${localImageFile.name}"
            val requestFile = localImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", uniqueFilename, requestFile)
            
            // 1. Direct upload stream to Supabase Segment
            val uploadResponse = api.uploadProductImage(
                apiKeyToken = "Bearer $supabasePublishableAnonKey",
                apiKey = supabasePublishableAnonKey,
                fileName = uniqueFilename,
                fileBody = filePart
            )

            // Resolve Public CDN URL
            val publicImageCdnUrl = "${supabaseBaseUrl}storage/v1/object/public/art-images-bucket/$uniqueFilename"

            // 2. Build structured transactional metadata object
            val artworkId = "artwork_id_${System.currentTimeMillis()}"
            val artworkMetadata = mapOf(
                "id" to artworkId,
                "title" to title,
                "description" to description,
                "imageUrl" to publicImageCdnUrl,
                "priceUsd" to priceUsd,
                "artistId" to artistId,
                "medium" to mediumName,
                "orientation" to orientationName,
                "timestamp" to System.currentTimeMillis()
            )

            // 3. Secure synchronization node update in Firebase RTD using modern tasks API
            firebaseDbInstance.getReference("artworks")
                .child(artworkId)
                .setValue(artworkMetadata)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
```
