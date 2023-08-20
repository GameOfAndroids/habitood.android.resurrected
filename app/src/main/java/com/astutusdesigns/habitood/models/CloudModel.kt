package com.astutusdesigns.habitood.models

import com.google.firebase.functions.FirebaseFunctions

/**
 * Created by TMiller on 2/16/2018.
 */
class CloudModel private constructor() {

    private object Holder { val Instance = CloudModel() }

    companion object {
        val instance: CloudModel by lazy { CloudModel.Holder.Instance }
    }

    @Suppress("UNCHECKED_CAST")
    fun firebaseCloudFunction(functionName: String, data: Map<String,Any>, responseHandler: (Map<String, Any>?, Exception?) -> Unit) {
        FirebaseFunctions.getInstance()
                .getHttpsCallable(functionName)
                .call(data)
                .continueWith { res ->
                    res.continueWith { task ->
                        if(task.exception != null)
                            responseHandler(null, task.exception)
                        else {
                            val result = (task.result?.data as? Map<String, Any>) ?: HashMap<String, Any>()
                            responseHandler(result, null)
                        }
                    }
                }
    }
}