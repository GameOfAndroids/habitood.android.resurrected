package com.astutusdesigns.habitood.models

import android.util.Log
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.*
import com.astutusdesigns.habitood.pagination.Paginator
import com.astutusdesigns.habitood.pagination.PaginatorCallback
import com.astutusdesigns.habitood.pagination.pagination_adapters.PinpointPaginationAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by timothy on 1/22/18.
 */
class FSPinpointModel private constructor() {

    interface PinpointCreationCallback {
        fun pinpointCreationFailed(exception: Exception?)
        fun pinpointWasCreated()
        fun pinpointExists()
    }

    interface TeamPinpointHistoryCallback {
        fun pinpointsDidDownload(points: List<RPinpoint>)
        fun pinpointDownloadFailed(ex: Exception?)
    }

    interface PinpointQueryCallback {
        fun pinpointRecordsDidDownload(records: List<FSPinpointRecord>)
        fun pinpointRecordsDownloadFailed(ex: Exception?)
    }

    private object Holder { val Instance = FSPinpointModel() }
    private val tag = "FSPinpointModel"
    private val fire = FirebaseFirestore.getInstance()
    val db: HabitoodDatabase? = HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext)

    companion object {
        val instance: FSPinpointModel by lazy { FSPinpointModel.Holder.Instance }
    }

    /**
     * This method will be used to download a pinpoint which should be saved to the local database.
     * @param pid pinpoint id
     */
    fun syncPinpoint(pid: String) {
        val bid = FSBusinessModel.instance.getPersistedBusinessProfile()?.businessId
        if(bid != null) {
            fire.collection("Businesses").document(bid).collection("Pinpoints").document(pid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot?.exists() == true) {
                        val fsp = documentSnapshot.toObject(FSPinpoint::class.java)
                        if(fsp != null)
                            RPinpoint(documentSnapshot.id, fsp)
                    }
                }
        }
    }

    /**
     * This method will configure a pinpoint paginator and return it for use.
     *  @param PaginatorCallback which will be used to return paginated results through.
     */
    fun getNonDeletedPinpointsPaginator(callback: PaginatorCallback<RPinpoint>): Paginator<RPinpoint> {
        val bid = FSBusinessModel.instance.getPersistedBusinessProfile()!!.businessId
        val query = fire.collection("Businesses")
            .document(bid)
            .collection("Pinpoints")
            .whereEqualTo("deleted", false)
            .orderBy("pinpointTitle")

        return Paginator<RPinpoint>(PinpointPaginationAdapter(), query, callback, pageSize = 25)
    }

    /**
     * This method will first search the local database for the pinpoint. If it isn't found, it will
     * search the online database for it. Once downloaded, it will be stored in the local db for fast recall.
     * @param pid pinpoint id which should be fetched.
     * @param callback anonymous closure method which will be called when the pinpoint download completes.
     */
    fun fetchPinpoint(pid: String, callback: (RPinpoint?, Exception?) -> Unit) {
        val db = HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).pinpointDao()
        val pinpoint = db.loadPinpointById(pid)
        if(pinpoint != null) {
            callback(pinpoint, null)
            return
        }

        val bid = FSBusinessModel.instance.getPersistedBusinessProfile()?.businessId
        if(bid != null) {
            fire.collection("Businesses").document(bid).collection("Pinpoints").document(pid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot?.exists() == true) {
                        val fsp = documentSnapshot.toObject(FSPinpoint::class.java)
                        if(fsp != null)
                            callback(RPinpoint(documentSnapshot.id, fsp), null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(this.toString(), "An error occurred downloading a pinpoint by id: ${exception.localizedMessage}")
                    callback(null, exception)
                }
        } else {
            callback(null, Exception("No business id provided!"))
        }
    }

    fun searchPinpointByTitle(title: String, callback: (RPinpoint?, Exception?) -> Unit) {
        val bid = FSBusinessModel.instance.getPersistedBusinessProfile()!!.businessId
        fire.collection("Businesses")
            .document(bid)
            .collection("Pinpoints")
            .whereEqualTo("pinpointTitle", title)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot ?: callback(null, null)
                querySnapshot ?: return@addOnSuccessListener
                if(!querySnapshot.isEmpty) {
                    val fsp = querySnapshot.documents[0].toObject(FSPinpoint::class.java)
                    if(fsp != null) {
                        val rp = RPinpoint(querySnapshot.documents[0].id, fsp)
                        callback(rp, null)
                    }
                } else
                    callback(null, null)
            }
            .addOnFailureListener { ex ->
                Log.e(this.toString(), "An error occurred querying for Pinpoint by Title: ${ex.localizedMessage}")
            }
    }

    fun createPinpoint(name: String, description: String, callback: PinpointCreationCallback) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        val user = FSUserModel.instance.getPersistedUserProfile()
        val data = HashMap<String, Any>()
        data["bid"] = business!!.businessId
        data["t"] = name
        data["d"] = description
        data["uid"] = user!!.userId

        CloudModel.instance.firebaseCloudFunction("create_new_pinpoint", data) { resp, err ->
            if(err != null)
                callback.pinpointCreationFailed(err)
            else {
                val response = resp!!["status"] as String
                if(response == "pinpoint_exists")
                    callback.pinpointExists()
                else if(response == "success")
                    callback.pinpointWasCreated()
            }
        }
    }

    fun updatePinpoint(pinpoint: RPinpoint, callback: (String?, Exception?) -> Unit) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        val pinpointRef = fire.collection("Businesses").document(business!!.businessId).collection("Pinpoints").document(pinpoint.pinpointId)
        pinpointRef.update("pinpointDescription", pinpoint.pinpointDescription)
            .addOnSuccessListener { callback("success", null) }
            .addOnFailureListener { e -> callback(null, e) }
    }

    fun recordPinpoint(r: FSPinpointRecord) {
        // get business.
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()

        // record reference.
        val recordRef = FirebaseFirestore.getInstance()
            .collection("Businesses")
            .document(business!!.businessId)
            .collection("PinpointRecords")
            .document()

        recordRef.set(r)
    }

    fun markTeamPinpointComplete(cp: FSTPinpointComplete, handler: (Map<String,Any>?, Exception?) -> Unit) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        val data = HashMap<String, Any>()
        data["bid"] = business!!.businessId
        data["tid"] = cp.teamId
        data["pid"] = cp.pointId

        CloudModel.instance.firebaseCloudFunction("pinpoint_completed", data, handler)
    }


    private fun shouldAddHistoryItem(item: FSPHistoryRecord?, p1: Date, p2: Date): Boolean {
        item ?: return false
        if(item.dateRemoved == null) {
            if(p1 <= item.dateAdded && p2 >= item.dateAdded) { return true }
            else if(p1 >= item.dateAdded) { return true }
        } else {
            if(p1 <= item.dateAdded) {
                if(p2 >= item.dateAdded) { return true }
                else if(p1 >= item.dateAdded && p1 <= item.dateRemoved) { return true }
            }
        }

        return false
    }

    fun getTeamPinpointHistory(t: RTeam, d1: Date, d2: Date, c: TeamPinpointHistoryCallback) {
        getTeamPinpointHistoryQuery(t.businessId, t.teamId, d1, d2, c)
    }

    fun getTeamPinpointHistory(bid: String, tid: String, from: Date, to: Date, c: TeamPinpointHistoryCallback) {
        getTeamPinpointHistoryQuery(bid, tid, from, to, c)
    }

    private fun getTeamPinpointHistoryQuery(bid: String, tid: String, from: Date, to: Date, c: TeamPinpointHistoryCallback) {
        fire.collection("Businesses")
            .document(bid)
            .collection("Teams")
            .document(tid)
            .collection("History")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val pidsAssigned = ArrayList<String>()
                querySnapshot?.documents?.forEach { doc ->
                    val historyItem = doc.toObject(FSPHistoryRecord::class.java)
                    if (shouldAddHistoryItem(historyItem, from, to)) {
                        if(!pidsAssigned.contains(historyItem!!.pointId))
                            pidsAssigned.add(historyItem.pointId)
                    }
                }

                val pointsAssigned = ArrayList<RPinpoint>()
                var fetchedPinpointCount = 0

                // if no pinpoints have ever been assigned to the team, return the empty list.
                if(pidsAssigned.size == 0) {
                    c.pinpointsDidDownload(pointsAssigned)
                    return@addOnSuccessListener
                }

                // if pinpoints were assigned to the team, get the pinpoints into a list and return them.
                pidsAssigned.forEach { pid ->
                    val storedPoint = db?.pinpointDao()?.loadPinpointById(pid)
                    if (storedPoint != null) {
                        pointsAssigned.add(storedPoint)
                        fetchedPinpointCount++
                        if (fetchedPinpointCount == pidsAssigned.size)
                            c.pinpointsDidDownload(pointsAssigned.sortedWith(compareBy { it.pinpointTitle }))
                    } else {
                        fetchPinpoint(pid) { pinpoint, ex ->
                            if (ex != null) {
                                Log.e(this.toString(), "An error occurred fetching a pinpoint by id: ${ex.localizedMessage}")
                            } else if (pinpoint != null) {
                                pointsAssigned.add(pinpoint)
                            }

                            fetchedPinpointCount++
                            if (fetchedPinpointCount == pidsAssigned.size)
                                c.pinpointsDidDownload(pointsAssigned.sortedWith(compareBy { it.pinpointTitle }))
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                c.pinpointDownloadFailed(exception)
            }
    }

    fun queryPinpointRecords(t: RTeam, p: RPinpoint, from: Date, to: Date, callback: PinpointQueryCallback) {
        fire.collection("Businesses").document(t.businessId).collection("PinpointRecords")
            .whereEqualTo("pid", p.pinpointId)
            .whereEqualTo("tid", t.teamId)
            .whereGreaterThanOrEqualTo("pinpointDate", from)
            .whereLessThanOrEqualTo("pinpointDate", to)
            .orderBy("pinpointDate", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val records = ArrayList<FSPinpointRecord>()
                if(!querySnapshot.isEmpty) {
                    querySnapshot.forEach { doc ->
                        val rec = doc.toObject(FSPinpointRecord::class.java)
                        rec.recordId = doc.id
                        records.add(rec)
                    }
                }
                callback.pinpointRecordsDidDownload(records)
            }
            .addOnFailureListener { exception ->
                callback.pinpointRecordsDownloadFailed(exception)
            }
    }
}