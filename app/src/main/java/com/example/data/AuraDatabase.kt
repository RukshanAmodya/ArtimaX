package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val artworkId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val isSavedToCollection: Boolean = false,
    val collectionName: String = "Public Portfolio" // e.g. "Secret Board" or "Living Room Inspiration"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val artworkId: String,
    val isDigitalLicense: Boolean = false,
    val priceUsd: Double
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val queryText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bid_log")
data class BidLogEntity(
    @PrimaryKey val id: String,
    val artworkId: String,
    val bidderHandle: String,
    val amountUsd: Double,
    val timestamp: Long
)

@Dao
interface AuraDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE artworkId = :artId LIMIT 1")
    suspend fun getFavoriteById(artId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE artworkId = :artId")
    suspend fun deleteCartItem(artId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 15")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchQuery(id: Int)

    @Query("SELECT * FROM bid_log WHERE artworkId = :artId ORDER BY amountUsd DESC")
    fun getBidsForArtwork(artId: String): Flow<List<BidLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBidLog(bid: BidLogEntity)
}

@Database(
    entities = [FavoriteEntity::class, CartItemEntity::class, SearchHistoryEntity::class, BidLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun dao(): AuraDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getInstance(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_art_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
