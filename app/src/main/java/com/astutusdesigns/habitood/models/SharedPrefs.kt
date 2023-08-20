package com.astutusdesigns.habitood.models

import android.content.Context

/**
 * Created by TMiller on 1/8/2018.
 */
class SharedPrefs {
    companion object {
        fun setSharedPreferenceString(context: Context, key: String, value: String) {
            val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putString(key, value)
            editor.apply()
        }

        fun getSharedPreferenceString(context: Context, key: String): String? {
            val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            return prefs.getString(key, null)
        }

        fun setSharedPreferenceLong(context: Context, key: String, value: Long) {
            val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putLong(key, value)
            editor.apply()
        }

        fun getSharedPreferenceLong(context: Context, key: String): Long {
            val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            return prefs.getLong(key, -1000)
        }
    }
}