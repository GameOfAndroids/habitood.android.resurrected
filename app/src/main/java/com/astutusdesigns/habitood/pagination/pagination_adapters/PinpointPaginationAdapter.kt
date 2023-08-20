package com.astutusdesigns.habitood.pagination.pagination_adapters

import com.astutusdesigns.habitood.datamodels.FSPinpoint
import com.astutusdesigns.habitood.datamodels.RPinpoint
import com.astutusdesigns.habitood.pagination.IPaginationAdapter
import com.google.firebase.firestore.DocumentSnapshot

class PinpointPaginationAdapter: IPaginationAdapter<RPinpoint> {
    override fun convertToObjects(items: List<DocumentSnapshot>): List<RPinpoint> {
        val pinpoints = ArrayList<RPinpoint>()
        if(items.isEmpty())
            return pinpoints

        items.forEach { doc ->
            val fsp = doc.toObject(FSPinpoint::class.java)
            if(fsp != null)
                pinpoints.add(RPinpoint(doc.id, fsp))
        }

        return pinpoints
    }
}