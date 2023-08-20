package com.astutusdesigns.habitood.models

import android.util.Log
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.FSVendor
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson

/**
 * MVP Model for the business package. Created by TMiller on 1/8/2018.
 */
class FSBusinessModel private constructor() {

    interface FSBusinessModelCallback {
        fun businessProfileWasDownloaded(business: FSBusiness?)
        fun businessProfileDownloadFailed(exception: Exception?)
    }

    interface FSBusinessUserAddedCallback {
        fun userWasAddedToBusiness()
        fun userAddedToBusinessFailed(exception: Exception?)
    }

    interface BusinessCallback {
        fun businessUpdate(business: FSBusiness)
    }

    private val tag = "FSBusinessModel"
    private val GSON_FSBUSINESS = "GsonBusinessObject"
    private val GSON_VENDOR = "GsonVendorObject"
    private val fire = FirebaseFirestore.getInstance()
    private var businessIdReceived = false
    private var businessCallbacks = ArrayList<BusinessCallback>()
    private object Holder { val Instance = FSBusinessModel() }

    companion object {
        val instance: FSBusinessModel by lazy { FSBusinessModel.Holder.Instance }
    }

    fun registerBusinessObserver(callback: BusinessCallback) {
        if(!businessCallbacks.contains(callback))
            businessCallbacks.add(callback)
        val b = getPersistedBusinessProfile()
        if(b != null)
            callback.businessUpdate(b)
    }

    fun removeBusinessObserver(callback: BusinessCallback) {
        businessCallbacks.remove(callback)
    }

    fun refreshBusinessProfile() {
        FSUserModel.instance.registerUserProfileObserver(object : FSUserModel.RealtimeUserProfileCallback {
            override fun onUserProfileUpdateReceived(user: FSUser) {
                when(user.businessId.isNullOrEmpty()) {
                    true -> return
                    false -> {
                        when(businessIdReceived) {
                            true -> return
                            false -> businessIdReceived = true
                        }
                    }
                }

                Log.d(tag, "fetching business profile...")
                fire.collection("Businesses").document(user.businessId!!).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val business = documentSnapshot.toObject(FSBusiness::class.java)
                            business?.businessId = documentSnapshot.id
                            persistBusinessProfile(business)
                            notifyObservers()
                            if(business != null)
                                getVendorProfile(business)

                            Log.d("FSBusinessModel", "Business profile received.")

                            // this must be placed here instead of early in the method. Causes concurrency exception otherwise.
                            FSUserModel.instance.removeUserProfileObserver(this)
                        }
            }
        })
    }

    private fun getVendorProfile(business: FSBusiness) {
        business.vendorId?.let { vid ->
            fire.collection("Vendors").document(vid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val vId = documentSnapshot.id
                        val v = documentSnapshot.toObject(FSVendor::class.java)
                        if(v != null)
                            v.id = vId

                        persistVendorProfile(v)
                    }
                }
        }
    }

    private fun notifyObservers() {
        val b = getPersistedBusinessProfile()
        businessCallbacks.forEach { c ->
            c.businessUpdate(b!!)
        }
    }

//    fun stopRealtimeBusinessUpdates() {
//        if(realtimeListenerRunning) {
//            listenerRegistration?.remove()
//            listenerRegistration = null
//            realtimeListenerRunning = false
//            Log.d("FSBusinessModel", "Realtime listener has been stopped.")
//        }
//    }

    /**
     * This is needed for when users try to input their business id.
     */
    fun downloadBusinessProfile(businessId: String, callback: FSBusinessModelCallback) {
        fire.collection("Businesses").document(businessId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val business = documentSnapshot.toObject(FSBusiness::class.java)
                        business?.businessId = documentSnapshot.id
                        callback.businessProfileWasDownloaded(business)
                    }
                }
                .addOnFailureListener { exception ->
                    callback.businessProfileDownloadFailed(exception)
                }
    }

    fun addBusinessUser(businessId: String, userId: String, callback: FSBusinessUserAddedCallback) {
        val params = HashMap<String,String>()
        params["bid"] = businessId
        params["uid"] = userId

        CloudModel.instance.firebaseCloudFunction("add_user_to_business", params) { respMap, err ->
            if(err != null)
                callback.userAddedToBusinessFailed(err)
            else if(respMap!!["status"] as String == "success")
                callback.userWasAddedToBusiness()
            else
                callback.userAddedToBusinessFailed(Exception(respMap["status"] as String))
        }
    }

    fun setManualAddEnabled(isEnabled: Boolean) {
//        val b = getPersistedBusinessProfile()!!
//        fire.collection("Businesses").document(b.businessId)
//                .update("manualAddEnabled", isEnabled)
        val bid = FSBusinessModel.instance.getPersistedBusinessProfile()!!.businessId

        val params = HashMap<String,Any>()
        params["bid"] = bid
        params["enabled"] = if(isEnabled) 1 else 0

        CloudModel.instance.firebaseCloudFunction("set_business_manual_add", params) { res, err ->
            if(err != null) {
                Log.e(this.toString(), "An error occurred while setting manual add enabled: ${err.localizedMessage}")
            } else if(res != null) {
                val msg = res["status"]
                when(msg) {
                    "undef_params" -> Log.e(this.toString(), "Parameters were undefined.")
                    "access_denied" -> Log.e(this.toString(), "Access was denied.")
                    "invalid_params" -> Log.e(this.toString(), "Parameters were invalid.")
                    "success" -> Log.d(this.toString(), "Business manual add set to $isEnabled")
                }
            }
        }
    }

    fun persistBusinessProfile(business: FSBusiness?) {
        var gsonBusiness = ""
        if(business != null)
            gsonBusiness = Gson().toJson(business)
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_FSBUSINESS, gsonBusiness)
    }

    fun getPersistedBusinessProfile(): FSBusiness? {
        val businessProfile = SharedPrefs.getSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_FSBUSINESS)
        return if(businessProfile.isNullOrEmpty()) null else Gson().fromJson(businessProfile, FSBusiness::class.java)
    }

    private fun persistVendorProfile(vendor: FSVendor?) {
        val v = Gson().toJson(vendor)
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_VENDOR, v)
    }

    fun getPersistedVendorProfile(): FSVendor? {
        val data = SharedPrefs.getSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_VENDOR)
        return if(data.isNullOrEmpty()) null else Gson().fromJson(data, FSVendor::class.java)
    }

    fun wipe() {
        businessIdReceived = false
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_FSBUSINESS, "")
        SharedPrefs.setSharedPreferenceString(HabitoodApp.instance.applicationContext, GSON_VENDOR, "")
    }


}