package com.astutusdesigns.habitood.datamodels

import java.util.*

/**
 * Created by TMiller on 1/24/2018.
 */
class FSTeam constructor() {
    var businessId: String = ""                     // company primary id.
    var teamName: String = ""                       // name of the team.
    var corkBoardPosts: Int = 0                     // number of cork board messages belonging to a team.
    var teamLeaderKey: String? = null               // primary key of User who created Team.
    var teamLeaderName: String? = null              // display name of Team leader.
//    var secondaryLeaderKey: String? = null          // primary key of second Team Managers.
//    var secondaryLeaderName: String? = null         // display name of secondary Team Managers.
    var createDate = Date()                         // date of the team creation.
    var teamDeleted = false                         // true if team is deleted.
    var pinpointKeys = mutableListOf<String>()      // hold a reference to pinpoint keys. (3 is the max!)
    var noteOnSuccess = false                       // should add anonymous note prompt be displayed when user swipes right?
    var noteOnFailure = false                       // should add anonymous note prompt be displayed when user swipes left?

    constructor(t: RTeam) : this() {
        businessId = t.businessId
        teamName = t.teamName
        corkBoardPosts = t.corkBoardPosts
        teamLeaderKey = t.teamLeaderKey
        teamLeaderName = t.teamLeaderName
//        secondaryLeaderKey = t.secondaryLeaderKey
//        secondaryLeaderName = t.secondaryLeaderName
        createDate = t.createDate
        teamDeleted = t.teamDeleted
        pinpointKeys = t.pinpointKeys
        noteOnSuccess = t.noteOnSuccess
        noteOnFailure = t.noteOnFailure
    }
}