package com.astutusdesigns.habitood.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.astutusdesigns.habitood.datamodels.RPinpoint

/**
 * Created by TMiller on 1/24/2018.
 */
@Dao
interface RPinpointDao {
    @Query("select * from RPinpoint order by pinpointTitle asc")
    fun loadAllPinpoints(): List<RPinpoint>

    @Query("select * from RPinpoint where pinpointId = :id")
    fun loadPinpointById(id: String): RPinpoint?

    @Query("select * from RPinpoint where deleted = 0 order by pinpointTitle asc")
    fun loadNonDeletedPinpoints(): List<RPinpoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplacePinpoint(pinpoint: RPinpoint)

    @Delete
    fun deletePinpoint(pinpoint: RPinpoint)

    @Query("delete from RPinpoint")
    fun deleteAll()
}