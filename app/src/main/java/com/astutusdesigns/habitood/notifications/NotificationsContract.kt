package com.astutusdesigns.habitood.notifications

import com.astutusdesigns.habitood.datamodels.FSNotification

/**
 * Created by TMiller on 1/31/2018.
 */
interface NotificationsContract {
    interface Model {
        fun registerNotificationObserver()
        fun removeNotificationObserver()
        fun onNotificationSwiped(n: FSNotification)
    }
    interface Presenter {
        fun onStart()
        fun onStop()
        fun onNotificationsReceived(n: ArrayList<FSNotification>)
        fun onNotificationSwiped(n: FSNotification)
    }
    interface View {
        fun showNotifications(n: ArrayList<FSNotification>)
        fun showErrorMessage()
    }
}