package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SosDao {
    @Query("SELECT * FROM sos_records ORDER BY timestamp DESC")
    fun getAllSos(): Flow<List<SosEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSos(sos: SosEntity)

    @Update
    suspend fun updateSos(sos: SosEntity)

    @Query("UPDATE sos_records SET status = :status WHERE id = :id")
    suspend fun updateSosStatus(id: String, status: String)

    @Query("DELETE FROM sos_records WHERE id = :id")
    suspend fun deleteSos(id: String)

    @Query("DELETE FROM sos_records")
    suspend fun clearAll()
}
