package com.astutusdesigns.habitood.datamodels

import java.util.*

/**
 * Created by TMiller on 1/26/2018.
 */
class FSUPinpointComplete() {

    lateinit var teamId: String
    lateinit var userId: String
    lateinit var pointId: String
    lateinit var businessId: String
    lateinit var date: Date

    constructor(tId: String,
                uId: String,
                pId: String,
                bId: String,
                date: Date = Date()) : this() {

        this.teamId = tId
        this.userId = uId
        this.pointId = pId
        this.businessId = bId
        this.date = date
    }

}