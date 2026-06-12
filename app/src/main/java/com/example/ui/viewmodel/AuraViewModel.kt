package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class SearchFilterState(
    val query: String = "",
    val palette: String? = null,
    val medium: ArtMedium? = null,
    val maxPrice: Float? = null,
    val orientation: ArtOrientation? = null,
    val unsignedOnly: Boolean = false
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AuraDatabase.getInstance(application)
    private val dao = database.dao()

    // Dual-Sided Mode Switcher: "Buyer" vs "Artist"
    private val _userMode = MutableStateFlow("Buyer")
    val userMode: StateFlow<String> = _userMode.asStateFlow()

    fun setUserMode(mode: String) {
        _userMode.value = mode
    }

    // Active Art Feed
    private val _artworks = MutableStateFlow<List<Artwork>>(MockArtData.artworks)
    val artworks: StateFlow<List<Artwork>> = _artworks.asStateFlow()

    // Room Favorites (Likes & Curation)
    val favoriteStateMap: StateFlow<Map<String, FavoriteEntity>> = dao.getAllFavorites()
        .map { list -> list.associateBy { it.artworkId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Room Shopping Cart
    val cartItemsList: StateFlow<List<CartItemEntity>> = dao.getAllCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Search History
    val searchHistory: StateFlow<List<SearchHistoryEntity>> = dao.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consolidated Search Filtering State (Multi-factor)
    private val _searchFilters = MutableStateFlow(SearchFilterState())
    val searchFilters: StateFlow<SearchFilterState> = _searchFilters.asStateFlow()

    val searchQueryText: StateFlow<String> = _searchFilters
        .map { it.query }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val searchPaletteFilter: StateFlow<String?> = _searchFilters
        .map { it.palette }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val searchMediumFilter: StateFlow<ArtMedium?> = _searchFilters
        .map { it.medium }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val searchPriceMaxFilter: StateFlow<Float?> = _searchFilters
        .map { it.maxPrice }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val searchOrientationFilter: StateFlow<ArtOrientation?> = _searchFilters
        .map { it.orientation }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val discoverUnsignedArtistsOnly: StateFlow<Boolean> = _searchFilters
        .map { it.unsignedOnly }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateSearchQueryText(query: String) {
        _searchFilters.value = _searchFilters.value.copy(query = query)
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                dao.insertSearchQuery(SearchHistoryEntity(queryText = query))
            }
        }
    }

    fun selectPaletteFilter(hex: String?) {
        _searchFilters.value = _searchFilters.value.copy(palette = hex)
    }

    fun selectMediumFilter(medium: ArtMedium?) {
        _searchFilters.value = _searchFilters.value.copy(medium = medium)
    }

    fun selectPriceMaxFilter(maxPrice: Float?) {
        _searchFilters.value = _searchFilters.value.copy(maxPrice = maxPrice)
    }

    fun selectOrientationFilter(orientation: ArtOrientation?) {
        _searchFilters.value = _searchFilters.value.copy(orientation = orientation)
    }

    fun toggleUnsignedArtists(onlyUnsigned: Boolean) {
        _searchFilters.value = _searchFilters.value.copy(unsignedOnly = onlyUnsigned)
    }

    fun clearAllFilters() {
        _searchFilters.value = SearchFilterState()
    }

    // Filtered Artworks Feed (Asymmetrical matching combined via 2 flows only!)
    val filteredArtworks: StateFlow<List<Artwork>> = combine(
        _artworks,
        _searchFilters
    ) { artList, filters ->
        artList.filter { art ->
            val matchQuery = filters.query.isEmpty() ||
                    art.title.contains(filters.query, ignoreCase = true) ||
                    art.artist.name.contains(filters.query, ignoreCase = true) ||
                    art.tags.any { it.contains(filters.query, ignoreCase = true) }

            val matchPalette = filters.palette == null ||
                    art.colors.any { it.hex.equals(filters.palette, ignoreCase = true) }

            val matchMedium = filters.medium == null || art.medium == filters.medium

            val matchPrice = filters.maxPrice == null || art.priceUsd <= filters.maxPrice

            val matchOrientation = filters.orientation == null || art.orientation == filters.orientation

            val matchUnsigned = !filters.unsignedOnly || art.artist.totalSales < 400

            matchQuery && matchPalette && matchMedium && matchPrice && matchOrientation && matchUnsigned
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MockArtData.artworks)

    // Interactive Core Actions
    fun doubleTapToLike(artworkId: String) {
        viewModelScope.launch {
            val existing = dao.getFavoriteById(artworkId)
            if (existing != null) {
                dao.insertFavorite(existing.copy(isLiked = !existing.isLiked))
            } else {
                dao.insertFavorite(FavoriteEntity(artworkId = artworkId, isLiked = true))
            }
        }
    }

    fun saveArtworkToCollection(artworkId: String, collectionName: String) {
        viewModelScope.launch {
            val existing = dao.getFavoriteById(artworkId)
            if (existing != null) {
                dao.insertFavorite(existing.copy(isSavedToCollection = true, collectionName = collectionName))
            } else {
                dao.insertFavorite(FavoriteEntity(artworkId = artworkId, isSavedToCollection = true, collectionName = collectionName))
            }
        }
    }

    fun addArtworkToCart(artworkId: String, purchaseDigital: Boolean = false) {
        viewModelScope.launch {
            val art = _artworks.value.find { it.id == artworkId } ?: return@launch
            val price = if (purchaseDigital) art.digitalLicensePriceUsd else art.priceUsd
            dao.insertCartItem(CartItemEntity(artworkId = artworkId, isDigitalLicense = purchaseDigital, priceUsd = price))
        }
    }

    fun removeArtworkFromCart(artworkId: String) {
        viewModelScope.launch {
            dao.deleteCartItem(artworkId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            dao.clearCart()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            dao.deleteSearchQuery(id)
        }
    }

    // --- Auction Mode Features ---
    private val _bidsFlows = mutableMapOf<String, MutableStateFlow<List<BidLogEntity>>>()

    fun getBidsForArtwork(artworkId: String): StateFlow<List<Bid>> {
        val flow = _bidsFlows.getOrPut(artworkId) {
            val seedBids = listOf(
                BidLogEntity(UUID.randomUUID().toString(), artworkId, "anonymous_collector", 750.0, System.currentTimeMillis() - 600000),
                BidLogEntity(UUID.randomUUID().toString(), artworkId, "vance_curator", 800.0, System.currentTimeMillis() - 300000),
                BidLogEntity(UUID.randomUUID().toString(), artworkId, "solitary_bidder", 850.0, System.currentTimeMillis() - 50000)
            )
            MutableStateFlow(seedBids)
        }
        return flow.map { list ->
            list.map { Bid(it.id, it.artworkId, it.bidderHandle, it.amountUsd, it.timestamp) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun placeBid(artworkId: String, bidderHandle: String, amountUsd: Double): String {
        val currentArtList = _artworks.value
        val art = currentArtList.find { it.id == artworkId } ?: return "Artwork not found"
        if (amountUsd <= art.auctionCurrentBidUsd) {
            return "Bid must be higher than current bid ($${art.auctionCurrentBidUsd})"
        }

        // Soft-close Extension Check (If bid placed in final 60 seconds, expand end time by 2 minutes)
        var newEndTime = art.auctionEndTimeEpochMs
        val remainingMs = art.auctionEndTimeEpochMs - System.currentTimeMillis()
        val inFinal60Sec = remainingMs in 1..60000
        if (inFinal60Sec) {
            newEndTime = System.currentTimeMillis() + 120000 // Extension
        }

        // Update current artworks list
        _artworks.value = currentArtList.map {
            if (it.id == artworkId) {
                it.copy(
                    auctionCurrentBidUsd = amountUsd,
                    auctionEndTimeEpochMs = newEndTime
                )
            } else it
        }

        // Save Bid Log in state
        val flow = _bidsFlows.getOrPut(artworkId) { MutableStateFlow(emptyList()) }
        val newBidLog = BidLogEntity(
            id = UUID.randomUUID().toString(),
            artworkId = artworkId,
            bidderHandle = bidderHandle,
            amountUsd = amountUsd,
            timestamp = System.currentTimeMillis()
        )
        flow.value = (listOf(newBidLog) + flow.value).sortedByDescending { it.amountUsd }

        return if (inFinal60Sec) "Proxy trigger! Bid Met and End-Time Extended smoothly by 2 minutes!" else "Bid placed successfully"
    }

    // --- Commission & Studio Features (Artist Hub) ---
    private val _commissions = MutableStateFlow<List<CustomCommission>>(
        listOf(
            CustomCommission(
                artistId = "marcus_vance",
                buyerName = "Guggenheim Collector",
                medium = ArtMedium.SURREAL_SCULPTURE,
                sizeDescription = "Height: 6 feet, Indoor Cast Bronze",
                referencesDescription = "Biomorphic twisting rod matching 'Stretched Horizons' layout dynamics.",
                budgetUsd = 9500.0,
                status = "In Progress - Bronze Casting"
            )
        )
    )
    val commissions: StateFlow<List<CustomCommission>> = _commissions.asStateFlow()

    fun requestCustomCommission(commission: CustomCommission) {
        _commissions.value = _commissions.value + commission
    }

    fun updateCommissionMilestone(id: String) {
        _commissions.value = _commissions.value.map {
            if (it.id == id) {
                val nextIndex = (it.currentMilestoneIndex + 1).coerceAtMost(it.milestonePaymentsCount)
                val newStatus = if (nextIndex == it.milestonePaymentsCount) "Completed & Shipped Secure" else "Working on Milestone ${nextIndex + 1}"
                it.copy(currentMilestoneIndex = nextIndex, status = newStatus)
            } else it
        }
    }

    // Bulk Importer simulator
    fun simulateBulkCSVImport(jsonString: String): Int {
        // Mock parsing simple key-values
        val addedCount = 3
        val currentArt = _artworks.value
        val newWorks = listOf(
            Artwork(
                id = "bulk-${UUID.randomUUID()}",
                title = "Gilded Decay",
                artist = MockArtData.artists[0],
                description = "Imported physical canvas tracking leaf degradation layered with liquid gilding.",
                imageUrl = "https://images.unsplash.com/photo-1547891654-e66ed7edd96c?auto=format&fit=crop&q=80&w=800",
                medium = ArtMedium.OIL_ON_CANVAS,
                orientation = ArtOrientation.LANDSCAPE,
                widthInches = 16.0,
                heightInches = 20.0,
                priceUsd = 950.0,
                tags = listOf("Gold", "Modern", "Imported"),
                colors = listOf(VisualPaletteColor("Gilding Gold", "#ECC94B", 0.35f))
            ),
            Artwork(
                id = "bulk-${UUID.randomUUID()}",
                title = "Geometric Bloom",
                artist = MockArtData.artists[1],
                description = "Vector illustration tracing flower coordinates.",
                imageUrl = "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?auto=format&fit=crop&q=80&w=800",
                medium = ArtMedium.DIGITAL_VECTOR,
                orientation = ArtOrientation.SQUARE,
                widthInches = 24.0,
                heightInches = 24.0,
                priceUsd = 280.0,
                tags = listOf("Flower", "Vivid", "Imported"),
                colors = listOf(VisualPaletteColor("Neon Pink", "#EC4899", 0.5f))
            )
        )
        _artworks.value = currentArt + newWorks
        return addedCount
    }
}
