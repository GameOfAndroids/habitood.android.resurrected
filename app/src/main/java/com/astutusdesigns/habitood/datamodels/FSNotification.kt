package com.astutusdesigns.habitood.datamodels

import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by TMiller on 1/31/2018.
 */
class FSNotification() {

    enum class MessageType { AddedToTeam, PinpointAdded, PinpointComplete, }

    @Exclude
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    var nid: String? = null
    lateinit var message: String
    lateinit var date: Date
    lateinit var type: MessageType

    constructor(message: String,
                msgType: MessageType,
                date: Date = Date()) : this() {
        this.message = message
        this.type = msgType
        this.date = date
    }

    fun simpleDate(): String {
        return dateFormatter.format(date)
    }

}