package com.astutusdesigns.habitood.datamodels

import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.RankLevel
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

import java.io.Serializable
import java.util.HashMap

/**
 * FSUser model. Data in database will mirror this class.
 * Created by TMiller on 1/5/2018.
 */
class FSUser : Serializable {

    var userId = ""
    var email = ""
    var isActive = false
    var businessId: String? = null
    var fname = ""
    var lname = ""
    var profilePhotoUrl: String? = null
    var rankLevel = 1
    var ctlTeamLeading: String? = null
    var pinpointsRecorded: Int = 0

    fun getDisplayName(): String {
        return "$fname $lname"
    }

    enum class Rank {
        FrontlinePersonnel,
        CoreTeamLeader,
        Supervisor,
        Owner,
        BusinessAdmin,
        VendorAdmin,
        HabitoodAdmin
    }

    companion object {
        val RankLevelDescription: MutableMap<RankLevel, String> = HashMap()

        init {
            RankLevelDescription[RankLevel.FrontlinePersonnel] = HabitoodApp.instance.getString(R.string.frontline_personnel) //"Front-line Personnel"
            RankLevelDescription[RankLevel.CoreTeamLeader]     = HabitoodApp.instance.getString(R.string.core_team_leader) //"Core Team Leader"
            RankLevelDescription[RankLevel.Supervisor]         = HabitoodApp.instance.getString(R.string.supervisor) //"Supervisor"
            RankLevelDescription[RankLevel.BusinessAdmin]      = HabitoodApp.instance.getString(R.string.business_admin) // "Business Admin"
        }
    }

}
