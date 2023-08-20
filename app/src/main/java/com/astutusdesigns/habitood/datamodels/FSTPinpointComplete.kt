package com.astutusdesigns.habitood.datamodels

import java.util.*

/**
 * Created by TMiller on 1/26/2018.
 */
class FSTPinpointComplete() {

    lateinit var teamId: String
    lateinit var pointId: String
    lateinit var date: Date

    constructor(tId: String,
                pId: String,
                date: Date = Date()) : this() {

        this.teamId = tId
        this.pointId = pId
        this.date = date
    }

}