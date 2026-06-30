package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnifiedItemDao {
    @Query("SELECT * FROM unified_items WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveItems(): Flow<List<UnifiedItem>>

    @Query("SELECT * FROM unified_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<UnifiedItem>>

    @Query("SELECT * FROM unified_items WHERE id = :id")
    suspend fun getItemById(id: Int): UnifiedItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: UnifiedItem): Long

    @Update
    suspend fun updateItem(item: UnifiedItem)

    @Delete
    suspend fun deleteItem(item: UnifiedItem)

    @Query("DELETE FROM unified_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)
}

@Dao
interface HistoryRecordDao {
    @Query("SELECT * FROM history_records ORDER BY date ASC")
    fun getAllHistoryRecords(): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history_records WHERE date = :date")
    suspend fun getHistoryRecordByDate(date: String): HistoryRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(record: HistoryRecord)

    @Update
    suspend fun updateHistoryRecord(record: HistoryRecord)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)
}
