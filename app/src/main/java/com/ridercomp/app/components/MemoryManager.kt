package com.ridercomp.app.components

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Rider",
    val preferredLanguage: String = "en-US"
)

@Entity(tableName = "conversation_logs")
data class ConversationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userQuery: String,
    val aiResponse: String,
    val summary: String? = null // Optional lightweight summary for context
)

// --- DAO ---

@Dao
interface MemoryDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreference(): UserPreference?

    @Insert
    suspend fun insertOrUpdatePreference(preference: UserPreference)

    @Insert
    suspend fun insertLog(log: ConversationLog)

    @Query("SELECT * FROM conversation_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<ConversationLog>
    
    @Query("SELECT * FROM conversation_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int): Flow<List<ConversationLog>>

    @Query("DELETE FROM conversation_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}

// --- Database ---

@Database(entities = [UserPreference::class, ConversationLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}

// --- Manager ---

class MemoryManager(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "rider_memory_db"
    ).build()

    private val dao = db.memoryDao()

    suspend fun saveConversation(userQuery: String, aiResponse: String, summary: String? = null) {
        val log = ConversationLog(userQuery = userQuery, aiResponse = aiResponse, summary = summary)
        dao.insertLog(log)
        
        // Keep DB lightweight, delete logs older than 7 days
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        dao.deleteOldLogs(sevenDaysAgo)
    }

    suspend fun getRecentContext(limit: Int = 5): List<ConversationLog> {
        return dao.getRecentLogs(limit).reversed() // Return chronologically
    }
    
    suspend fun getUserName(): String {
        return dao.getUserPreference()?.userName ?: "Rider"
    }
}
