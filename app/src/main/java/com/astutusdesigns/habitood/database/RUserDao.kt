package com.astutusdesigns.habitood.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.astutusdesigns.habitood.datamodels.RUser

@Dao
interface RUserDao {

    @Query("select * from RUser order by fname, lname")
    fun loadAllUsers(): List<RUser>

    @Query("select * from RUser where userId = :uid")
    fun loadUserById(uid: String): RUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(user: RUser)

    @Query("delete from RUser")
    fun deleteAll()

}