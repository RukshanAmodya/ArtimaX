package com.example.data

import java.util.UUID

enum class ArtMedium {
    OIL_ON_CANVAS,
    DIGITAL_VECTOR,
    SURREAL_SCULPTURE,
    WATERCOLOR,
    ACRYLIC,
    MIXED_MEDIA
}

enum class ArtOrientation {
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

data class VisualPaletteColor(
    val name: String,
    val hex: String,
    val weight: Float // Percentage representation in the artwork
)

data class ArtworkMaterialLayer(
    val title: String,
    val description: String,
    val zoomLevelPercentage: Int
)

data class ArtistProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val alias: String,
    val avatarUrl: String,
    val bio: String,
    val statement: String,
    val audioIntroDurationSec: Int = 45,
    val rating: Float = 4.9f,
    val followerCount: Int = 12430,
    val totalSales: Int = 340,
    val geolocationRegion: String = "Pacific Northwest, USA",
    val acceptsCommissions: Boolean = true,
    val verifyStatus: Boolean = true
)

data class Artwork(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: ArtistProfile,
    val description: String,
    val imageUrl: String,
    val blurHash: String = "L69jXy~900oL00bI%M9F_3ofS4of", // Fallback progressive blur value
    val medium: ArtMedium,
    val orientation: ArtOrientation,
    val widthInches: Double,
    val heightInches: Double,
    val priceUsd: Double,
    val isAvailable: Boolean = true,
    val isDigitalLicenseAvailable: Boolean = false,
    val digitalLicensePriceUsd: Double = 49.0,
    val isAuction: Boolean = false,
    val auctionStartPriceUsd: Double = 0.0,
    val auctionCurrentBidUsd: Double = 0.0,
    val auctionReservePriceUsd: Double = 0.0,
    val auctionEndTimeEpochMs: Long = 0,
    val provenance: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val colors: List<VisualPaletteColor> = emptyList(),
    val materials: List<ArtworkMaterialLayer> = emptyList()
)

data class Bid(
    val id: String = UUID.randomUUID().toString(),
    val artworkId: String,
    val bidderHandle: String,
    val amountUsd: Double,
    val timestampEpochMs: Long,
    val isProxyBid: Boolean = false,
    val maxProxyAmountUsd: Double = 0.0
)

data class CustomCommission(
    val id: String = UUID.randomUUID().toString(),
    val artistId: String,
    val buyerName: String,
    val medium: ArtMedium,
    val sizeDescription: String,
    val referencesDescription: String,
    val budgetUsd: Double,
    val milestonePaymentsCount: Int = 3,
    val currentMilestoneIndex: Int = 0,
    val status: String = "Awaiting Artist Intake"
)

// In-Memory Seed Data for Outstanding Pinterest Visual Presentation
object MockArtData {
    val artists = listOf(
        ArtistProfile(
            name = "Elianna Vance",
            alias = "elianna_vance",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
            bio = "Contemporary Oil Impressionist mapping memory, architectural shadow, and ambient Pacific Northwest lighting environments.",
            statement = "My canvas captures the transient interplay of light and urban moisture. By overlaying dense glazes, the painting continuously shifts color profile depending on dynamic household illumination angles."
        ),
        ArtistProfile(
            name = "Kaito Shinomura",
            alias = "shinomura_studio",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
            bio = "Digital Vector Synthesist forging cybernetic architectural blueprints and procedural geometric color fields.",
            statement = "Vector art is the mathematics of light. I code the vertex clusters to enable infinite visual zoom capacity without loss of physical edge fidelity."
        ),
        ArtistProfile(
            name = "Marcus Vance",
            alias = "marcus_vance",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200",
            bio = "Biomorphic Clay & Bronze Sculptor exploring structural tension and kinetic structural balance.",
            statement = "Bronze represents solid fluid. My sculptures explore organic curvatures that appear weightless yet demand secure heavy ground anchors."
        )
    )

    val artworks = listOf(
        Artwork(
            id = "art-1",
            title = "Aetherial Rain",
            artist = artists[0],
            description = "A sweeping impressionistic landscape showing dynamic reflection coefficients on rain-slicked asphalt under twilight atmospheric skies.",
            imageUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.OIL_ON_CANVAS,
            orientation = ArtOrientation.PORTRAIT,
            widthInches = 36.0,
            heightInches = 48.0,
            priceUsd = 1850.0,
            provenance = listOf("Painted in Seattle Studio (2025)", "Exhibited at Urban Luminary Solo Show (2025)", "Certified Authentic by Elianna Vance"),
            tags = listOf("Twilight", "Seattle", "Impressions", "Atmospheric", "Reflections"),
            colors = listOf(
                VisualPaletteColor("Teal Twilight", "#1A3A3A", 0.45f),
                VisualPaletteColor("Amber Neon", "#D97706", 0.25f),
                VisualPaletteColor("Overcast Gray", "#6B7280", 0.20f),
                VisualPaletteColor("Slick Shadow", "#111827", 0.10f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Underdrawing", "Soft charcoal canvas outline drafting general urban focal points.", 10),
                ArtworkMaterialLayer("Deep Glaze Layer", "Three translucent layers of Prussian Blue and Linseed oil to capture light depth.", 50),
                ArtworkMaterialLayer("Impasto Highlights", "Thick palette knife strokes of lead-free Titanium White and Yellow Lake.", 90)
            )
        ),
        Artwork(
            id = "art-2",
            title = "Cybernetic Oasis",
            artist = artists[1],
            description = "An intricate procedural isometric composition displaying micro-blueprints of floating greenhouse architectures mapping solar geometries.",
            imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.DIGITAL_VECTOR,
            orientation = ArtOrientation.LANDSCAPE,
            widthInches = 24.0,
            heightInches = 24.0,
            priceUsd = 450.0,
            isDigitalLicenseAvailable = true,
            digitalLicensePriceUsd = 65.0,
            provenance = listOf("Programmed natively in SVG Vector Coordinate Engine (2026)", "Cryptographically signed NFT on Aura Chain"),
            tags = listOf("Procedural", "Isometric", "Cyberpunk", "Greenhouse", "Vector"),
            colors = listOf(
                VisualPaletteColor("Matrix Green", "#10B981", 0.40f),
                VisualPaletteColor("Obsidian Dark", "#0B0F19", 0.35f),
                VisualPaletteColor("Ultraviolet", "#8B5CF6", 0.15f),
                VisualPaletteColor("Cyber Yellow", "#F59E0B", 0.10f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Math Vector Grid", "Algorithmic layout calculated to perfect Fibonacci cell limits.", 10),
                ArtworkMaterialLayer("Color Field Shaders", "Smooth RGB color gradients locked with SVG rendering parameters.", 60)
            )
        ),
        Artwork(
            id = "art-3",
            title = "Stretched Horizons",
            artist = artists[2],
            description = "A biomorphic bronze outline exploring heavy vertical tension and minimalist balance dynamics.",
            imageUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.SURREAL_SCULPTURE,
            orientation = ArtOrientation.PORTRAIT,
            widthInches = 18.0,
            heightInches = 32.0,
            priceUsd = 3400.0,
            provenance = listOf("Cast at Fine Art Foundry Portland", "Exhibited at Modern Art Biennial (2026)"),
            tags = listOf("Bronze", "Minimalist", "Biomorphic", "Curves", "Tension"),
            colors = listOf(
                VisualPaletteColor("Bronze Patina", "#4B5320", 0.60f),
                VisualPaletteColor("Raw Brass Accent", "#B5A642", 0.30f),
                VisualPaletteColor("Ebony Base", "#121212", 0.10f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Plaster Model", "Original plaster maquette sculpted with clay files.", 20),
                ArtworkMaterialLayer("Lost Wax Cast", "Poured molten bronze alloy into high-temp ceramic cores.", 70),
                ArtworkMaterialLayer("Acid Patina", "Hand-apply hot copper-nitrate solutions using butane torches.", 100)
            )
        ),
        Artwork(
            id = "art-4",
            title = "Submerged Echoes",
            artist = artists[0],
            description = "An exploration of marine light shafts cutting through dense kelp forests in Puget Sound. Featuring high-contrast warm and cool brushstroke balances.",
            imageUrl = "https://images.unsplash.com/photo-1549887534-1541e9326642?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.OIL_ON_CANVAS,
            orientation = ArtOrientation.SQUARE,
            widthInches = 30.0,
            heightInches = 30.0,
            priceUsd = 1200.0,
            isAuction = true,
            auctionStartPriceUsd = 500.0,
            auctionCurrentBidUsd = 850.0,
            auctionReservePriceUsd = 1000.0,
            auctionEndTimeEpochMs = System.currentTimeMillis() + 10 * 60 * 1000 + 45000, // 10m 45s from now for auction demonstration !
            provenance = listOf("Puget Sound Series Solo Work (2025)"),
            tags = listOf("Marine", "Kelp", "SeaLight", "Oil", "Abstract"),
            colors = listOf(
                VisualPaletteColor("Marine Cerulean", "#0284C7", 0.50f),
                VisualPaletteColor("Deep Sea Abyss", "#0F172A", 0.30f),
                VisualPaletteColor("Sand Gold", "#EAB308", 0.12f),
                VisualPaletteColor("Kelp Olive", "#84CC16", 0.08f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Canvas Gesso", "Two coats of acrylic-latex sizing on Belgian linen.", 10),
                ArtworkMaterialLayer("Wet-on-Wet Paint", "Oil mixtures applied quickly to capture diffuse water shapes.", 80)
            )
        ),
        Artwork(
            id = "art-5",
            title = "Solitary Helix",
            artist = artists[2],
            description = "A twisting biomorphic iron work looping through gravity vectors to express structural solitude.",
            imageUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.SURREAL_SCULPTURE,
            orientation = ArtOrientation.PORTRAIT,
            widthInches = 12.0,
            heightInches = 28.0,
            priceUsd = 2100.0,
            provenance = listOf("Welded in Vance Studios (2026)"),
            tags = listOf("Sculpture", "Iron", "Helix", "Modern", "Organic"),
            colors = listOf(
                VisualPaletteColor("Forged Charcoal", "#1F2937", 0.75f),
                VisualPaletteColor("Rust Highlight", "#C2410C", 0.20f),
                VisualPaletteColor("Polished Steel", "#E5E7EB", 0.05f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Forging Core", "Hand-pounded wrought iron rod heated to 2200°F.", 50),
                ArtworkMaterialLayer("Bead Welding", "Seamless TIG-welded joint structures ground with high grits.", 90)
            )
        ),
        Artwork(
            id = "art-6",
            title = "Deconstructed City",
            artist = artists[1],
            description = "High-fidelity digital illustration dissecting architectural grids of Tokyo rail networks into abstract primary shapes.",
            imageUrl = "https://images.unsplash.com/photo-1501472312651-726afd116ff1?auto=format&fit=crop&q=80&w=800",
            medium = ArtMedium.DIGITAL_VECTOR,
            orientation = ArtOrientation.LANDSCAPE,
            widthInches = 30.0,
            heightInches = 20.0,
            priceUsd = 500.0,
            isDigitalLicenseAvailable = false,
            provenance = listOf("Deconstructionist Tokyo Collection (2025)"),
            tags = listOf("Cities", "Tokyo", "Abstraction", "Graphic", "Vector"),
            colors = listOf(
                VisualPaletteColor("Crimson Sun", "#DC2626", 0.35f),
                VisualPaletteColor("Grid Onyx", "#1E293B", 0.35f),
                VisualPaletteColor("Concrete Gray", "#94A3B8", 0.20f),
                VisualPaletteColor("Rail Neon", "#38BDF8", 0.10f)
            ),
            materials = listOf(
                ArtworkMaterialLayer("Rail Layers", "Complex geometric shapes styled in semi-transparent overlays.", 40),
                ArtworkMaterialLayer("Primary Fields", "High intensity red and black visual balancing blocks.", 80)
            )
        )
    )
}
