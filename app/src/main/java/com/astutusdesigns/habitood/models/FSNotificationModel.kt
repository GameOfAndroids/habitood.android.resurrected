package com.astutusdesigns.habitood.models

import android.util.Log
import com.astutusdesigns.habitood.datamodels.FSNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Created by TMiller on 1/31/2018.
 */
class FSNotificationModel {

    interface NotificationSaveCallback {
        fun notificationSaveComplete()
    }

    interface NotificationsDownloadedCallback {
        fun onNotificationsDownloaded(n: ArrayList<FSNotification>)
        fun onNotificationsError(exception: Exception?)
    }

    interface RealtimeNotifsCallback {
        fun onNotificationsReceived(n: ArrayList<FSNotification>)
    }

    private object Holder { val instance = FSNotificationModel() }
    private val tag = "FSNotificationModel"
    private val fire = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private val realtimeCallbacks = ArrayList<RealtimeNotifsCallback>()
    private var downloadedNotifications: ArrayList<FSNotification> = ArrayList()

    companion object {
        val instance: FSNotificationModel by lazy { FSNotificationModel.Holder.instance }
    }

    fun startRealtimeNotificationSync() {
        // start the listener if it has not already been started.
        val fUser = FirebaseAuth.getInstance().currentUser
        if(fUser != null && listenerRegistration == null) {
            val query: Query = fire.collection("Users").document(fUser.uid).collection("Notifications").orderBy("date", Query.Direction.DESCENDING)
            listenerRegistration = query.addSnapshotListener { querySnapshot, _ ->
                downloadedNotifications = ArrayList<FSNotification>()
                if (querySnapshot?.isEmpty == false) {
                    querySnapshot.documents.forEach { doc ->
                        val n = doc.toObject(FSNotification::class.java)
                        n?.nid = doc.id
                        if(n != null)
                            downloadedNotifications.add(n)
                    }
                    Log.d(tag, "Notifications downloaded.")
                }
                notifyObservers()
            }
        }
        Log.d(tag, "Notification sync started.")
    }

    fun stopRealtimeNotificationSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        Log.d(tag, "Notification sync stopped.")
    }

    private fun notifyObservers() {
        realtimeCallbacks.forEach { c ->
            c.onNotificationsReceived(downloadedNotifications)
        }
    }

    fun registerRealtimeObserver(realtimeCallback: RealtimeNotifsCallback) {
        if(!realtimeCallbacks.contains(realtimeCallback))
            realtimeCallbacks.add(realtimeCallback)

        realtimeCallback.onNotificationsReceived(downloadedNotifications)
    }

    fun removeRealtimeObserver(realtimeCallback: RealtimeNotifsCallback) {
        realtimeCallbacks.remove(realtimeCallback)
    }

    fun setNewNotification(n: FSNotification, uid: String, callback: NotificationSaveCallback? = null) {
        /* notification code when re-introducing notifications.
        fire.collection("Users").document(uid).collection("Notifications")
                .add(n)
                .addOnCompleteListener { _ ->
                    callback?.notificationSaveComplete()
                }*/
    }

    fun onNotificationViewed(n: FSNotification) {
        val user = FSUserModel.instance.getPersistedUserProfile()
        fire.collection("Users").document(user!!.userId).collection("Notifications").document(n.nid!!).delete()
    }

    fun wipe() {
        downloadedNotifications = ArrayList()
    }
}