package com.astutusdesigns.habitood.models

import android.util.Log
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RUser
import com.astutusdesigns.habitood.pagination.Paginator
import com.astutusdesigns.habitood.pagination.PaginatorCallback
import com.astutusdesigns.habitood.pagination.pagination_adapters.UserPaginationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson

/**
 * Model class designed to manage all changes made to FSUser objects.
 * Created by TMiller on 1/5/2018.
 */
class FSUserModel private constructor() {

    interface UserProfileCallback {
        fun onProfileDownloaded(user: FSUser?)
        fun onDownloadFailed(exception: Exception?)
    }

    interface RealtimeUserProfileCallback {
        fun onUserProfileUpdateReceived(user: FSUser)
    }

    interface UserProfileOperationCallback {
        fun onSuccess()
        fun onFailure()
    }

    interface UsersDownloadCallback {
        fun usersDidDownload(users: List<FSUser>)
        fun usersDownloadFailed(exception: Exception?)
    }

    companion object {
        val instance: FSUserModel by lazy { Holder.Instance }
    }

    private object Holder { val Instance = FSUserModel() }
    private val fire = FirebaseFirestore.getInstance()
    private val JSON_FSUSER = "FSUser"
    private val userProfileObservers: MutableList<RealtimeUserProfileCallback> = ArrayList()
    private val tag = "FSUserModel"
    private var userProfileListener: ListenerRegistration? = null

    fun startRealtimeUserProfileSync() {
        val fAuthUser = FirebaseAuth.getInstance().currentUser
        userProfileListener = fire.collection("Users").document(fAuthUser!!.uid)
                .addSnapshotListener { documentSnapshot, _ ->
                    if (documentSnapshot?.exists() == true) {
                        val userProfile = documentSnapshot.toObject(FSUser::class.java)!!
                        persistEndUserProfile(userProfile)
                        notifyObserversUserProfileUpdateReceived(userProfile)
                    }
                }
    }

    fun stopRealtimeUserProfileSync() {
        userProfileListener?.remove()
    }

    fun registerUserProfileObserver(callback: RealtimeUserProfileCallback) {
        if(!userProfileObservers.contains(callback))
            userProfileObservers.add(callback)
        val profile = getPersistedUserProfile()
        if(profile != null)
            callback.onUserProfileUpdateReceived(profile)
    }

    fun removeUserProfileObserver(callback: RealtimeUserProfileCallback) {
        userProfileObservers.remove(callback)
    }

    private fun notifyObserversUserProfileUpdateReceived(userProfile: FSUser) {
        userProfileObservers.forEach { observer ->
            observer.onUserProfileUpdateReceived(userProfile)
        }
    }

    fun searchForUnclaimedUserByEmail(email: String, callback: (FSUser?, Exception?) -> Unit) {
        fire.collection("Users")
                .whereEqualTo("email", email)
                .whereEqualTo("businessId", null)
                .get()
                .addOnSuccessListener { docSnap ->
                    if(docSnap?.isEmpty == false) {
                        val user = docSnap.documents[0].toObject(FSUser::class.java)
                        callback(user, null)
                    } else {
                        callback(null, null)
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(this.toString(), "An error occurred searching for user by email: ${error.localizedMessage}")
                    callback(null, error)
                }
    }

    fun searchForBusinessUserByEmail(email: String, callback: (FSUser?, Exception?) -> Unit) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        business ?: callback(null, Exception("No Business Found"))
        business ?: return

        fire.collection("Users")
                .whereEqualTo("businessId", business.businessId)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if(querySnapshot.isEmpty)
                        callback(null, null)
                    else {
                        val user = querySnapshot.documents[0].toObject(FSUser::class.java)
                        callback(user, null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(this.toString(), "An error occurred querying for user by email: ${exception.localizedMessage}")
                    callback(null, exception)
                }
    }

    fun getActiveUsersPaginator(callback: PaginatorCallback<FSUser>): Paginator<FSUser>? {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        business ?: return null

        val query = fire.collection("Users")
                .whereEqualTo("businessId", business.businessId)
                .whereEqualTo("active", true)
                .orderBy("fname", Query.Direction.ASCENDING)
                .orderBy("lname", Query.Direction.ASCENDING)

        return Paginator<FSUser>(UserPaginationAdapter(), query, callback)
    }

    fun getActiveFlpPaginator(callback: PaginatorCallback<FSUser>): Paginator<FSUser>? {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        business ?: return null

        val query = fire.collection("Users")
                .whereEqualTo("businessId", business.businessId)
                .whereEqualTo("rank", FSUser.Rank.FrontlinePersonnel.toString())
                .whereEqualTo("active", true)
                .orderBy("fname", Query.Direction.ASCENDING)
                .orderBy("lname", Query.Direction.ASCENDING)

        return Paginator<FSUser>(UserPaginationAdapter(), query, callback)
    }

    fun getActiveCtlPaginator(onlyForLeadingTeam: Boolean, callback: PaginatorCallback<FSUser>): Paginator<FSUser>? {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        business ?: return null

        var query = fire.collection("Users")
                .whereEqualTo("businessId", business.businessId)
                .whereEqualTo("rank", FSUser.Rank.CoreTeamLeader.toString())
                .whereEqualTo("active", true)
                .orderBy("fname", Query.Direction.ASCENDING)
                .orderBy("lname", Query.Direction.ASCENDING)

        if(onlyForLeadingTeam)
            query = fire.collection("Users")
                    .whereEqualTo("businessId", business.businessId)
                    .whereEqualTo("rank", FSUser.Rank.CoreTeamLeader.toString())
                    .whereEqualTo("ctlTeamLeading", null)
                    .whereEqualTo("active", true)
                    .orderBy("fname", Query.Direction.ASCENDING)
                    .orderBy("lname", Query.Direction.ASCENDING)

        return Paginator<FSUser>(UserPaginationAdapter(), query, callback, 50)
    }

    fun downloadAllBusinessUsers(callback: (ArrayList<FSUser>, Exception?) -> Unit) {
        val userList = ArrayList<FSUser>()
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        if(business == null)
            callback(userList, Exception("business was null"))

        fire.collection("Users")
                .whereEqualTo("businessId", business!!.businessId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if(querySnapshot.isEmpty) {
                        callback(userList, null)
                        return@addOnSuccessListener
                    } else {
                        querySnapshot.documents.forEach { doc ->
                            val u = doc.toObject(FSUser::class.java)
                            if(u != null)
                                userList.add(u)
                        }

                        callback(userList, null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "An error occurred downloading all business users: ${exception.localizedMessage}")
                    callback(userList, exception)
                }
    }

    fun downloadAvailableCoreTeamLeaders(callback: UsersDownloadCallback) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        fire.collection("Users")
                .whereEqualTo("businessId", business!!.businessId)
                .whereEqualTo("rank", "CoreTeamLeader")
                .whereEqualTo("ctlTeamLeading", null)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var users: List<FSUser> = ArrayList<FSUser>()
                    if(!querySnapshot.isEmpty)
                        users = querySnapshot.toObjects(FSUser::class.java)

                    users = users.sortedWith(compareBy{ it.fname })
                    callback.usersDidDownload(users)
                }
                .addOnFailureListener { exception ->
                    callback.usersDownloadFailed(exception)
                }
    }

    /**
     * This method should be used to fetch a user profile if the pertinent data is only user details
     * such as name and email address.
     */
    fun fetchUserProfileByUid(uid: String, callback: UserProfileCallback) {
        downloadUserProfileById(uid, callback)
//        val locallySavedUser = HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).userDao().loadUserById(uid)
//        when(locallySavedUser) {
//            null -> downloadUserProfileById(uid, callback)
//            else -> callback.onProfileDownloaded(locallySavedUser.toFSUser())
//        }
    }

    /**
     * This method should be used to fetch a user profile if the latest information available on the
     * user is required.
     */
    fun downloadUserProfileById(userId: String, callback: UserProfileCallback) {
        fire.collection("Users").document(userId)
                .get()
                .addOnFailureListener { exception ->
                    callback.onDownloadFailed(exception)
                }
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(FSUser::class.java)
                        callback.onProfileDownloaded(user)
                        user ?: return@addOnSuccessListener

                        // store user in local db if not null.
                        RUser(user)
                    }
                }
    }

//    fun updateUserProfile(user: FSUser, callback: UserProfileOperationCallback?) {
//        fire.collection("Users").document(user.userId)
//                .set(user, SetOptions.merge())
//                .addOnSuccessListener { _ ->
//                    callback?.onSuccess()
//                }
//                .addOnFailureListener { _ ->
//                    callback?.onFailure()
//                }
//    }

    fun downloadUsersEligibleForTeams(callback: UsersDownloadCallback) {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()!!

        fire.collection("Users")
                .whereEqualTo("active", true)
                .whereEqualTo("businessId", business.businessId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = ArrayList<FSUser>()
                    if(!querySnapshot.isEmpty) {
                        querySnapshot.documents.forEach { docSnap ->
                            val user = docSnap.toObject(FSUser::class.java)
                            if(user != null) {
                                userList.add(user)
                            }
                        }
                    }

                    callback.usersDidDownload(userList)
                }
                .addOnFailureListener { exception ->
                    callback.usersDownloadFailed(exception)
                }
    }

    /**
     * This method will create a user profile.
     */
    fun createUserProfile(userProfile: FSUser, callback: UserProfileOperationCallback) {
        if(userProfile.userId.isEmpty())
            return

        val params = HashMap<String, Any>()
        params["uid"] = userProfile.userId
        params["fname"] = userProfile.fname
        params["lname"] = userProfile.lname
        params["email"] = userProfile.email

        CloudModel.instance.firebaseCloudFunction("create_user_account", params) { res, err ->
            if(err != null) {
                callback.onFailure()
                Log.e(this.toString(), "An error occurred creating user profile: ${err.localizedMessage}")
            } else if(res != null) {
                when (res["status"] as String) {
                    "undef_params" -> { Log.e(this.toString(), "parameters were undefined."); callback.onFailure() }
                    "user_id_exists" -> { Log.e(this.toString(), "user id is not unique and already exists."); callback.onFailure() }
                    "user_email_exists" -> { Log.e(this.toString(), "user email is not unique and already exists."); callback.onFailure() }
                    "success" -> callback.onSuccess()
                }
            }
        }
    }

    /**
     * This should only be called by HabitoodApp. Never call this in any other place.
     */
    fun wipe() {
        persistEndUserProfile(null)
    }

    /**
     * This method will persist the user's profile in shared preferences.
     */
    private fun persistEndUserProfile(user: FSUser?) {
        val userJson = Gson().toJson(user)
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, JSON_FSUSER, userJson)
        Log.d(tag, "FSUser profile saved.")
    }

    /**
     * This method will get the persisted user profile in shared preferences.
     */
    fun getPersistedUserProfile(): FSUser? {
        return Gson().fromJson(SharedPrefs.getSharedPreferenceString(HabitoodApp.instance.applicationContext, JSON_FSUSER), FSUser::class.java)
    }
}