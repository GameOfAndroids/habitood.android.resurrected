package com.astutusdesigns.habitood.rv_adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.RvSwipeTools
import com.astutusdesigns.habitood.datamodels.RPinpoint
import java.util.*

/**
 * Created by timothy on 1/25/18.
 */
class SwipePinpointsAdapter(private val context: Context,
                            private val rv: RecyclerView,
                            private val cardToUse: SwipeCard,
                            private var activePinpoints: ArrayList<RPinpoint>?,
                            private val callback: RecordPinpointSwipeCallback) : RecyclerView.Adapter<SwipePinpointsAdapter.PinpointEntryViewHolder>(),
                                                                                 RvSwipeTools.OnSwipeCallback {

    enum class SwipeCard { Normal, Mini }

    interface RecordPinpointSwipeCallback {
        fun onLeftSwipe(p: RPinpoint)
        fun onRightSwipe(p: RPinpoint)
    }

    private val tools = RvSwipeTools(context, this)
    init {
        tools.generateBidirectionalSwipeCallBack(rv, R.mipmap.ic_close_white_24dp, R.mipmap.ic_add_white_24dp)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun newDataSet(pp: ArrayList<RPinpoint>) {
        activePinpoints = pp
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return activePinpoints!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinpointEntryViewHolder {
        val cardView =
        when(cardToUse) {
            SwipeCard.Normal -> LayoutInflater.from(context).inflate(R.layout.layout_swipable_pinpoint_card, parent, false) as View
            SwipeCard.Mini -> LayoutInflater.from(context).inflate(R.layout.layout_swipable_pinpoint_card_mini, parent, false)
        }

        return PinpointEntryViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: PinpointEntryViewHolder, position: Int) {
        holder.bind(activePinpoints!![position])
    }

    private fun getItem(position: Int): RPinpoint {
        return activePinpoints!![position]
    }

    private fun onPinpointSwiped(position: Int) {
        // remove the swiped pinpoint.
        val swipedPoint = activePinpoints!![position]
        activePinpoints?.removeAt(position)
        notifyItemRemoved(position)

        // replace the fresh pinpoint to be swiped again.
        activePinpoints?.add(position, swipedPoint)
        notifyItemInserted(position)
    }

    override fun onLeftSwipe(adapterPosition: Int) {
        callback.onLeftSwipe(activePinpoints!![adapterPosition])
        onPinpointSwiped(adapterPosition)
    }

    override fun onRightSwipe(adapterPosition: Int) {
        callback.onRightSwipe(activePinpoints!![adapterPosition])
        onPinpointSwiped(adapterPosition)
    }

    inner class PinpointEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val pinpointTextView = itemView.findViewById<TextView>(R.id.swipable_pinpoint_name)
        private val pinpointDescrTextView = itemView.findViewById<TextView>(R.id.swipable_pinpoint_description)

        fun bind(pinpoint: RPinpoint) {
            pinpointTextView.text = pinpoint.pinpointTitle
            pinpointDescrTextView.text = pinpoint.pinpointDescription
        }
    }
}
