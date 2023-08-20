package com.astutusdesigns.habitood.datamodels

/**
 * Created by TMiller on 1/30/2018.
 */
class FSUnreadNoteItem() {

    var userId: String? = null
    var corkBoardNoteId: String? = null

    constructor(userId: String,
                corkBoardNoteId: String) : this() {
        this.userId = userId
        this.corkBoardNoteId = corkBoardNoteId
    }

}