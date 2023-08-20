package com.astutusdesigns.habitood.sectionrvadapter

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R

class UserViewHolder(view: View, private val itemTappedListener: OnItemTappedListener): RecyclerView.ViewHolder(view), CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    var card = view.findViewById<CardView>(R.id.userDetailCard)
    var nameTV = view.findViewById<TextView>(R.id.user_details_list_item_name)
    var emailTV = view.findViewById<TextView>(R.id.user_details_list_item_email)
    var selectedCB = view.findViewById<CheckBox>(R.id.userSelectedCheckBox)
    var mItemCheckedListener: OnItemCheckedListener? = null
    var acceptCheckChangeInput = true

    init {
        selectedCB.setOnCheckedChangeListener(this)
        card.setOnClickListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(acceptCheckChangeInput)
            when(isChecked) {
                true -> mItemCheckedListener?.onChecked(adapterPosition)
                false -> mItemCheckedListener?.onUnchecked(adapterPosition)
            }
    }

    override fun onClick(v: View?) {
        itemTappedListener.onItemTapped(adapterPosition)
    }
}