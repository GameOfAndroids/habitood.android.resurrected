package com.astutusdesigns.habitood.models

import android.util.Log
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.FSTeam
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RPinpoint
import com.astutusdesigns.habitood.datamodels.RTeam
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * The FSTeamModel class will manage all data regarding a team.
 * Created by TMiller on 1/10/2018.
 */
class FSTeamModel private constructor() : FSUserModel.RealtimeUserProfileCallback {

    interface RealtimeTeamUpdate {
        fun teamUpdateDidDownload(team: RTeam)
    }

    interface ListOfTeamsDownloadCallback {
        fun listOfTeamsDidDownload(teams: List<RTeam>)
        fun listOfTeamsDownloadFailed(exception: Exception?)
    }

    interface TeamDeleteCallback {
        fun teamDeleted()
    }

    companion object {
        val instance: FSTeamModel by lazy { Holder.Instance }
    }

    private object Holder { val Instance = FSTeamModel() }
    private val tag = "FSTeamModel"
    private val activeTeamVar = "activeTeamVar"
    private val fire = FirebaseFirestore.getInstance()
    private var teamMembershipListener: ListenerRegistration? = null
    private val teamEventListeners = HashMap<String, ListenerRegistration>()                  // listeners for teams. team id is the key.
    private val teamObserverCallbacks = HashMap<String, MutableList<RealtimeTeamUpdate>>()    // team observer callbacks. team id is the key. list holds all callbacks for that team.
    private var realtimeSyncRunning = false


    fun startRealtimeTeamSync() {
        if(!realtimeSyncRunning) {
            realtimeSyncRunning = true
            FSUserModel.instance.registerUserProfileObserver(this)
            Log.d(tag, "Realtime sync is running.")
        }
    }

    fun stopRealtimeTeamSync() {
        if(realtimeSyncRunning) {
            realtimeSyncRunning = false
            FSUserModel.instance.removeUserProfileObserver(this)
            teamEventListeners.keys.forEach { key ->
                teamEventListeners[key]?.remove()
            }
            teamEventListeners.clear()
            Log.d(tag, "Realtime sync has been stopped.")
        }
    }

    override fun onUserProfileUpdateReceived(user: FSUser) {
        if(user.businessId.isNullOrEmpty()) {
            return
        }

        // check for user's team memberships.
        fire.collection("Businesses").document(user.businessId!!).collection("TeamMembers")
                .whereEqualTo("uid", user.userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // remove all team entries which will be replaced by the incoming query.
                    HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().deleteAll()
                    teamEventListeners.keys.forEach { key ->
                        teamEventListeners[key]?.remove()
                    }
                    teamEventListeners.clear()

                    // get all the teams from the fresh query.
                    val docs = querySnapshot.documents
                    docs.forEach { docSnapshot ->
                        val teamId = docSnapshot.get("tid") as String
                        setupTeamObserver(user.businessId!!, teamId)
                    }
                }
    }

    private fun setupTeamObserver(businessId: String, teamId: String) {
        // store the teams the user is a part of in the local db.
        teamEventListeners[teamId] = fire.collection("Businesses").document(businessId).collection("Teams").document(teamId)
                .addSnapshotListener { documentSnapshot, exception ->
                    if(exception != null)
                        Log.e(tag, "Error occurred in setting up team observer. See exception: $exception")
                    else {
                        if(documentSnapshot?.exists() == true) {
                            val fst = documentSnapshot.toObject(FSTeam::class.java)
                            if(fst != null) {
                                val rTeam = RTeam(documentSnapshot.id, fst) // Creating a RoomTeam or RTeam automatically saves/replaces team in db

                                rTeam.pinpointKeys.forEach { pid ->
                                    FSPinpointModel.instance.syncPinpoint(pid)
                                }

                                notifyObservers(rTeam)
                            }
                        }
                    }
                }
    }

    fun registerTeamObserver(team: RTeam, callback: RealtimeTeamUpdate) {
        var list = teamObserverCallbacks[team.teamId]
        if(list == null) {
            list = ArrayList()
            list.add(callback)
            teamObserverCallbacks[team.teamId] = list
        } else
            teamObserverCallbacks[team.teamId]?.add(callback)

        setupTeamObserver(team.businessId, team.teamId)
    }

    fun removeTeamObserver(team: RTeam, callback: RealtimeTeamUpdate) {
        teamObserverCallbacks[team.teamId]?.remove(callback)
        if(teamObserverCallbacks[team.teamId]?.count() == 0) {
            teamEventListeners[team.teamId]?.remove()
            teamEventListeners.remove(team.teamId)
        }
    }

    private fun notifyObservers(team: RTeam) {
        val observerList = teamObserverCallbacks[team.teamId]
        observerList?.forEach { o ->
            o.teamUpdateDidDownload(team)
        }
        Log.d(tag, "Observers of the team: ${team.teamName} were notified of an update.")
    }

    /**
     * This method will retrieve a team profile by id.
     */
    fun fetchTeam(tid: String, callback: (RTeam?, Exception?) -> Unit) {
        val businessId = FSBusinessModel.instance.getPersistedBusinessProfile()?.businessId ?: ""
        fire.collection("Businesses").document(businessId).collection("Teams").document(tid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val team = documentSnapshot.toObject(FSTeam::class.java)
                        team ?: callback(null, null)
                        val rTeam = RTeam(documentSnapshot.id, team!!)
                        callback(rTeam, null)
                    }
                }
                .addOnFailureListener { exception ->
                    callback(null, exception)
                }
    }

    /**
     * This method will call the cloud function to create a new team.
     */
    fun createNewTeam(businessId: String, ctl: FSUser, sctl: FSUser, teamName: String, responseHandler: (String?, Exception?) -> Unit) {
        val params = HashMap<String, Any>()
        params["bid"] = businessId
        params["ctln"] = "${ctl.fname} ${ctl.lname}"
        params["cid"] = ctl.userId
        params["sctln"] = "${sctl.fname} ${sctl.lname}"
        params["sid"] = sctl.userId
        params["tname"] = teamName

        CloudModel.instance.firebaseCloudFunction("create_business_team", params) { resp, err ->
            if(err != null)
                responseHandler(null, err)
            else {
                if(resp != null) {
                    val strResponse = resp["status"] as String
                    responseHandler(strResponse, null)
                }
            }
        }
    }

    /**
     * This method will add a user to a team. The callback is an optional callback defaulted to null. This will
     * also send a notification to the user who has been added to the team.
     */
    fun addUserToTeam(team: RTeam, user: FSUser, callback: ((error: Exception?) -> Unit)? = null) {
        val data = HashMap<String, Any>()
        data["uid"] = user.userId
        data["tid"] = team.teamId
        fire.collection("Businesses").document(user.businessId!!).collection("TeamMembers").document().set(data)
                .addOnFailureListener { exception ->
                    callback?.invoke(exception)
                }
                .addOnSuccessListener {
                    callback?.invoke(null)
                }
    }

    /**
     * This method will call the cloud function that will go through the process of marking a team as deleted.
     */
    fun deleteTeam(team: RTeam, callback: (String?, Exception?) -> Unit) {
        val data = HashMap<String, Any>()
        data["bid"] = team.businessId
        data["tid"] = team.teamId

        CloudModel.instance.firebaseCloudFunction("delete_team", data) { resp, err ->
            if(err != null) {
                Log.e(this.toString(), "An error occurred deleting team from cloud function: ${err.localizedMessage}")
                callback(null, err)
            } else {
                if(resp != null)
                    callback(resp.getValue("status") as String, null)
                else {
                    callback(null, null)
                }
            }
        }
    }

    /**
     * This method will return the id's of all users who are a part of a team.
     */
    fun getTeamMemberIds(team: RTeam, callback: (ArrayList<String>?, Exception?) -> Unit) {
        fire.collection("Businesses").document(team.businessId).collection("TeamMembers")
                .whereEqualTo("tid", team.teamId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val members = ArrayList<String>()
                    querySnapshot.documents.forEach { docSnap ->
                        members.add(docSnap.get("uid") as String)
                    }
                    callback(members, null)
                }
                .addOnFailureListener { exception ->
                    callback(null, exception)
                }
    }

    /**
     * This method will return the [RTeam] the user is leading (This applies only to CTL's).
     */
    fun getTeamLeading(): RTeam? {
        val u = FSUserModel.instance.getPersistedUserProfile()
        if(u?.ctlTeamLeading.isNullOrEmpty())
            return null

        return HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().loadTeamById(u?.ctlTeamLeading!!)
    }

    /**
     * This method will remove a user from a team.
     */
    fun removeUserFromTeam(user: FSUser, team: RTeam, callback: ((Exception?) -> Unit)? = null) {
        fire.collection("Businesses").document(user.businessId!!).collection("TeamMembers")
                .whereEqualTo("tid", team.teamId)
                .whereEqualTo("uid", user.userId)
                .get()
                .addOnSuccessListener { docSnap ->
                    if(docSnap.documents.size == 1)
                        fire.collection("Businesses").document(user.businessId!!).collection("TeamMembers").document(docSnap.documents[0].id).delete()
                                .addOnCompleteListener {
                                    callback?.invoke(null)
                                }
                }
    }

    /**
     * DEPRECATED. This function gets all business teams. Warning: this function does not paginate! Avoid using this.
     */
    fun getAllBusinessTeams(businessId: String, showDeletedTeams: Boolean, callback: ListOfTeamsDownloadCallback) {
        val ref =
        when(showDeletedTeams) {
            true -> fire.collection("Businesses").document(businessId).collection("Teams")
            false -> fire.collection("Businesses").document(businessId).collection("Teams")
                        .whereEqualTo("teamDeleted", false)
        }

        ref.orderBy("teamName")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val allTeams: MutableList<RTeam> = ArrayList()
                if(!querySnapshot.isEmpty) {
                    querySnapshot!!.documents.forEach { doc ->
                        val team = doc.toObject(FSTeam::class.java)
                        if(team != null) {
                            val rTeam = RTeam(doc!!.id, team)
                            allTeams.add(rTeam)
                        }
                    }
                }

                callback.listOfTeamsDidDownload(allTeams)
            }
            .addOnFailureListener { exception ->
                callback.listOfTeamsDownloadFailed(exception)
            }
    }

    fun addPinpointToTeam(team: RTeam, pinpoint: RPinpoint, responseHandler: (Map<String, Any>?, Exception?) -> Unit) {
        val data = HashMap<String, Any>()
        data["bid"] = team.businessId
        data["tid"] = team.teamId
        data["pid"] = pinpoint.pinpointId

        CloudModel.instance.firebaseCloudFunction("add_pinpoint_to_team", data, responseHandler)
    }

    fun removePinpointFromTeam(team: RTeam, pinpoint: RPinpoint, responseHandler: (Map<String,Any>?, Exception?) -> Unit) {
        val data = HashMap<String, Any>()
        data["bid"] = team.businessId
        data["tid"] = team.teamId
        data["pid"] = pinpoint.pinpointId

        CloudModel.instance.firebaseCloudFunction("remove_pinpoint_from_team", data, responseHandler)
    }

    fun setActiveTeam(team: RTeam) {
        storeUsersActiveTeam(team.teamId)
    }

    fun getActiveTeam(): RTeam? {
        val teamId = getUsersActiveTeam()
        if(teamId.isNullOrEmpty())
            return null

        return HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().loadTeamById(teamId!!)
    }

    private fun storeUsersActiveTeam(teamId: String?) {
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, activeTeamVar, teamId ?: "")
    }

    private fun getUsersActiveTeam(): String? {
        return SharedPrefs.getSharedPreferenceString(HabitoodApp.instance.applicationContext, activeTeamVar)
    }

    fun wipe() {
        storeUsersActiveTeam(null)
    }
}
