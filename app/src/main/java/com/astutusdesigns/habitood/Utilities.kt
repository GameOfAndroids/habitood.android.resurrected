package com.astutusdesigns.habitood

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*


/**
 * Utilities class for methods which might need to be made available to the entire project.
 * Created by timothy on 1/10/18.
 */
class Utilities {
    companion object {

        val yearMoDayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val yearMoDayTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun disableTouch(activity: Activity) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        fun enableTouch(activity: Activity) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        fun overrideFont(context: Context, defaultFontNameToOverride: String, customFontFileNameInAssets: String) {
            try {
                val customFontTypeface = Typeface.createFromAsset(context.assets, customFontFileNameInAssets)

                val defaultFontTypefaceField = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
                defaultFontTypefaceField.isAccessible = true
                defaultFontTypefaceField.set(null, customFontTypeface)
            } catch (e: Exception) {
                Log.e(this.toString(), "Can not set custom font $customFontFileNameInAssets instead of $defaultFontNameToOverride")
            }

        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * This method will be given a DateRangeSelection object and will return an array of 2 date objects.
         * The date object in position 0 will be the "from" date in the date range.
         * The date object in position 1 will be the "to" date in the date range.
         * If the user has made no selection, then null is returned.
         */
        fun getFromAndToDatesFromRange(range: DateRangeSelection): Array<Date>? {
            val cal = Calendar.getInstance()
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
            val to = cal.time

            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            var dateWasSelected = true
            when(range) {
                DateRangeSelection.NoSelection -> dateWasSelected = false
                DateRangeSelection.Past7Days   -> cal.add(Calendar.DAY_OF_MONTH, -7)
                DateRangeSelection.Past14Days  -> cal.add(Calendar.DAY_OF_MONTH, -14)
                DateRangeSelection.Past21Days  -> cal.add(Calendar.DAY_OF_MONTH, -21)
                DateRangeSelection.PastMonth   -> cal.add(Calendar.MONTH, -1)
            }

            if(dateWasSelected)
                return arrayOf<Date>(cal.time, to)

            return null
        }
    }
}