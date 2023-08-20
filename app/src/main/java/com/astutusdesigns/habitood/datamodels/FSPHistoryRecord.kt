package com.astutusdesigns.habitood.datamodels

import java.util.*

/**
 * Created by timothy on 1/27/18.
 */
class FSPHistoryRecord(){

    lateinit var teamId: String
    lateinit var pointId: String
    lateinit var dateAdded: Date
    var dateRemoved: Date? = null

    constructor(teamId: String,
                pinpointId: String,
                dateAdded: Date = Date(),
                dateRemoved: Date? = null) : this() {

        this.teamId = teamId
        this.pointId = pinpointId
        this.dateAdded = dateAdded
        this.dateRemoved = dateRemoved
    }

}