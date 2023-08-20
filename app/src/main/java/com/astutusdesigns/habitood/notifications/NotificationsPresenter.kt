package com.astutusdesigns.habitood.notifications

import com.astutusdesigns.habitood.datamodels.FSNotification

/**
 * Created by TMiller on 1/31/2018.
 */
class NotificationsPresenter(private val view: NotificationsContract.View) : NotificationsContract.Presenter {

    private val tag = "NotificationsPresenter"
    private val model: NotificationsContract.Model = NotificationsModel(this)

    override fun onStart() {
        model.registerNotificationObserver()
    }

    override fun onStop() {
        model.removeNotificationObserver()
    }

    override fun onNotificationsReceived(n: ArrayList<FSNotification>) {
        view.showNotifications(n)
    }

    override fun onNotificationSwiped(n: FSNotification) {
        model.onNotificationSwiped(n)
    }
}