package com.astutusdesigns.habitood.notifications

import com.astutusdesigns.habitood.datamodels.FSNotification
import com.astutusdesigns.habitood.models.FSNotificationModel

/**
 * Created by TMiller on 1/31/2018.
 */
class NotificationsModel(private val presenter: NotificationsContract.Presenter) : NotificationsContract.Model, FSNotificationModel.RealtimeNotifsCallback {

    override fun registerNotificationObserver() {
        FSNotificationModel.instance.registerRealtimeObserver(this)
    }

    override fun onNotificationsReceived(n: ArrayList<FSNotification>) {
        presenter.onNotificationsReceived(n)
    }

    override fun removeNotificationObserver() {
        FSNotificationModel.instance.removeRealtimeObserver(this)
    }

    override fun onNotificationSwiped(n: FSNotification) {
        FSNotificationModel.instance.onNotificationViewed(n)
    }

}