package com.astutusdesigns.habitood.sectionrvadapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R

class SectionHeaderHolder(view: View): RecyclerView.ViewHolder(view) {
    var sectionTitle = view.findViewById<TextView>(R.id.sectionTitle)
}