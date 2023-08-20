package com.astutusdesigns.habitood.notifications


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.ProgressBarInterface
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.datamodels.FSNotification
import com.astutusdesigns.habitood.rv_adapters.NotificationAdapter
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment(), NotificationsContract.View, NotificationAdapter.NotificationCallback {

    private val presenter: NotificationsContract.Presenter = NotificationsPresenter(this)
    private var recycler: RecyclerView? = null
    private var adapter: NotificationAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_notifications, container, false)

        adapter = NotificationAdapter(inflater.context, this)

        recycler = v.findViewById<RecyclerView>(R.id.notification_recycler_view)
        recycler?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recycler?.itemAnimator = DefaultItemAnimator()
        recycler?.adapter = adapter

        return v
    }

    override fun onStart() {
        super.onStart()
        (activity as? ProgressBarInterface)?.showProgressBar()
        presenter.onStart()
    }

    override fun onNotificationSwiped(n: FSNotification) {
        presenter.onNotificationSwiped(n)
    }

    override fun showNotifications(n: ArrayList<FSNotification>) {
        adapter?.newDataSet(n)
        (activity as? ProgressBarInterface)?.hideProgressBar()
    }

    override fun showErrorMessage() {
        (activity as? ProgressBarInterface)?.hideProgressBar()
        if(view != null)
            Snackbar.make(requireView(), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }
}