package com.astutusdesigns.habitood.datamodels

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import java.io.Serializable

/**
 * Created by TMiller on 1/24/2018.
 */
@Entity
class RPinpoint constructor() : Serializable {
    @PrimaryKey
    @NonNull
    var pinpointId: String = ""
    var pinpointTitle: String = ""
    var pinpointDescription: String = ""
    var deleted: Boolean = false

    /**
     * Create an RPinpoint from the firebase id and the pinpoint object retrieved from firebase.
     */
    constructor(id: String, p: FSPinpoint) : this() {
        pinpointId = id
        pinpointTitle = p.pinpointTitle ?: ""
        pinpointDescription = p.pinpointDescription ?: ""
        deleted = p.isDeleted
        HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).pinpointDao().insertOrReplacePinpoint(this)
    }

    /**
     * Get the FSPinpoint object from the RPinpoint object.
     */
    fun getFSPinpoint(): FSPinpoint? {
        return if(pinpointTitle.isEmpty()) null else FSPinpoint(this)
    }
}