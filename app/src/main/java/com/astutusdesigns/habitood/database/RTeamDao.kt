package com.astutusdesigns.habitood.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.astutusdesigns.habitood.datamodels.RTeam

/**
 * Created by TMiller on 1/24/2018.
 */
@Dao
interface RTeamDao {

    @Query("select * from RTeam order by teamName asc")
    fun loadAllTeams(): List<RTeam>

    @Query("select * from RTeam where teamDeleted = 0 order by teamName asc")
    fun loadNonDeletedTeams(): List<RTeam>

    @Query("select * from RTeam where teamId = :teamId")
    fun loadTeamById(teamId: String): RTeam

    @Query("select * from RTeam where teamName = :teamName")
    fun loadTeamByTeamName(teamName: String): RTeam

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceTeam(team: RTeam)

    @Delete
    fun deleteTeam(team: RTeam)

    @Query("delete from RTeam where teamId = :teamId")
    fun deleteTeamById(teamId: String)

    @Query("delete from RTeam")
    fun deleteAll()

}