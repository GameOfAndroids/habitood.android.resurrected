package com.astutusdesigns.habitood.rv_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.RvSwipeTools
import com.astutusdesigns.habitood.datamodels.FSNotification
import java.util.*

/**
 * Created by TMiller on 1/31/2018.
 */
class NotificationAdapter(private val context: Context,
                          private val callback: NotificationCallback) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(), RvSwipeTools.OnSwipeCallback {

    interface NotificationCallback {
        fun onNotificationSwiped(n: FSNotification)
    }

    private var notifications = ArrayList<FSNotification>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val tools = RvSwipeTools(context, this)
        tools.generateRightSwipeCallback(recyclerView, R.mipmap.ic_check_white_24dp)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.layout_notification_item, parent, false)
        return NotificationViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun onLeftSwipe(adapterPosition: Int) { /* Not used here */ }
    override fun onRightSwipe(adapterPosition: Int) {
        val notification = notifications[adapterPosition]
        callback.onNotificationSwiped(notification)
        notifications.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
    }

    fun newDataSet(notifications: ArrayList<FSNotification>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val icon: ImageView = v.findViewById<ImageView>(R.id.notification_card_icon_view)
        private val notificationText = v.findViewById<TextView>(R.id.notification_card_notif_msg)
        private val notificationDate = v.findViewById<TextView>(R.id.notification_card_notif_date)

        fun bind(notification: FSNotification) {

            when(notification.type) {
                FSNotification.MessageType.AddedToTeam -> {
                    icon.setImageResource(R.mipmap.ic_group_white_24dp)
                    icon.setColorFilter(ContextCompat.getColor(HabitoodApp.instance.applicationContext, R.color.colorPrimary))
                }
                FSNotification.MessageType.PinpointAdded -> {
                    icon.setImageResource(R.mipmap.ic_pinpoint)
                    icon.setColorFilter(ContextCompat.getColor(HabitoodApp.instance.applicationContext, android.R.color.holo_red_light))
                }
                FSNotification.MessageType.PinpointComplete -> {
                    icon.setImageResource(R.mipmap.ic_check_white_24dp)
                    icon.setColorFilter(ContextCompat.getColor(HabitoodApp.instance.applicationContext, android.R.color.holo_green_light))
                }
            }

            notificationText.text = notification.message
            notificationDate.text = notification.simpleDate()
        }
    }
}