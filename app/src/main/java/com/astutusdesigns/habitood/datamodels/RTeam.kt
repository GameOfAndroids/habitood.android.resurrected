package com.astutusdesigns.habitood.datamodels

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import java.io.Serializable
import java.util.*

/**
 * Created by TMiller on 1/24/2018.
 */
@Entity
class RTeam constructor() : Serializable {
    @PrimaryKey
    @NonNull
    var teamId: String = ""                         // team primary id.
    @NonNull
    var businessId: String = ""                     // company primary id.
    @NonNull
    var teamName: String = ""                       // name of the team.
    var corkBoardPosts: Int = 0                     // number of cork board messages belonging to a team.
    var teamLeaderKey: String? = null               // primary key of User who created Team.
    var teamLeaderName: String? = null              // display name of Team leader.
    var secondaryLeaderKey: String? = null          // primary key of second core Team leader.
    var secondaryLeaderName: String? = null         // display name of secondary core Team leader.
    var createDate = Date()                         // date of the team creation.
    var teamDeleted: Boolean = false                // true if team is deleted.
    var pinpointKeys = mutableListOf<String>()      // hold a reference to pinpoint keys. (3 is the max!)
    var noteOnSuccess = false
    var noteOnFailure = false

    constructor(teamId: String, t: FSTeam) : this() {
        this.teamId = teamId
        businessId = t.businessId
        teamName = t.teamName
        corkBoardPosts = t.corkBoardPosts
        teamLeaderKey = t.teamLeaderKey ?: ""
        teamLeaderName = t.teamLeaderName ?: ""
        secondaryLeaderKey = t.secondaryLeaderKey ?: ""
        secondaryLeaderName = t.secondaryLeaderName ?: ""
        createDate = t.createDate
        teamDeleted = t.teamDeleted
        pinpointKeys = t.pinpointKeys
        noteOnSuccess = t.noteOnSuccess
        noteOnFailure = t.noteOnFailure
        HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().insertOrReplaceTeam(this)
    }

    fun getFSTeam(): FSTeam {
        return FSTeam(this)
    }
}