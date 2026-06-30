package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SoloLevelingRepository(private val db: AppDatabase) {
    private val itemDao = db.unifiedItemDao()
    private val historyDao = db.historyRecordDao()
    private val profileDao = db.userProfileDao()

    val allActiveItems: Flow<List<UnifiedItem>> = itemDao.getAllActiveItems()
    val allItems: Flow<List<UnifiedItem>> = itemDao.getAllItems()
    val allHistoryRecords: Flow<List<HistoryRecord>> = historyDao.getAllHistoryRecords()
    val userProfileFlow: Flow<UserProfile?> = profileDao.getUserProfileFlow()

    // Profile actions
    suspend fun getUserProfile(): UserProfile {
        var profile = profileDao.getUserProfile()
        if (profile == null) {
            profile = UserProfile()
            profileDao.insertUserProfile(profile)
        }
        return profile
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.insertUserProfile(profile)
    }

    // Item actions
    suspend fun insertItem(item: UnifiedItem): Int {
        return itemDao.insertItem(item).toInt()
    }

    suspend fun updateItem(item: UnifiedItem) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(item: UnifiedItem) {
        itemDao.deleteItem(item)
    }

    suspend fun deleteItemById(id: Int) {
        itemDao.deleteItemById(id)
    }

    // Daily tracking history actions
    suspend fun getHistoryRecord(date: String): HistoryRecord {
        var record = historyDao.getHistoryRecordByDate(date)
        if (record == null) {
            record = HistoryRecord(date = date)
            historyDao.insertHistoryRecord(record)
        }
        return record
    }

    suspend fun insertOrUpdateHistory(record: HistoryRecord) {
        historyDao.insertHistoryRecord(record)
    }
}
