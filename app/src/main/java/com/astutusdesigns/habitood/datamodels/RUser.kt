package com.astutusdesigns.habitood.datamodels

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase

@Entity
class RUser constructor() {

    @PrimaryKey
    @NonNull
    var userId: String = ""
    var businessId: String? = null
    var email: String = ""
    var fname: String = ""
    var lname: String = ""
    var profilePhotoUrl: String? = null
    var rankLevel: Int = 1
    var ctlTeamLeading: String? = null
    var pinpointsRecorded = 0

    constructor(user: FSUser) : this() {
        userId            = user.userId
        businessId        = user.businessId
        email             = user.email
        fname             = user.fname
        lname             = user.lname
        profilePhotoUrl   = user.profilePhotoUrl
        rankLevel         = user.rankLevel
        ctlTeamLeading    = user.ctlTeamLeading
        pinpointsRecorded = user.pinpointsRecorded
        HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).userDao().insertOrReplace(this)
    }

    fun toFSUser(): FSUser {
        val fsUser = FSUser()

        fsUser.userId = userId
        fsUser.fname = fname
        fsUser.lname = lname
        fsUser.email = email
        fsUser.businessId = businessId
        fsUser.profilePhotoUrl = profilePhotoUrl
        fsUser.ctlTeamLeading = ctlTeamLeading
        fsUser.pinpointsRecorded = pinpointsRecorded
//        fsUser.rank = when(rankLevel) {
//            1 -> FSUser.Rank.FrontlinePersonnel
//            2 -> FSUser.Rank.CoreTeamLeader
//            3 -> FSUser.Rank.Supervisor
//            4 -> FSUser.Rank.BusinessAdmin
//            5 -> FSUser.Rank.Owner
//            else -> FSUser.Rank.FrontlinePersonnel
//        }

        return fsUser
    }

}