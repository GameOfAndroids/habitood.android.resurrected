package com.astutusdesigns.habitood.pagination.pagination_adapters

import com.astutusdesigns.habitood.datamodels.FSTeam
import com.astutusdesigns.habitood.pagination.IPaginationAdapter
import com.google.firebase.firestore.DocumentSnapshot

class TeamPaginationAdapter: IPaginationAdapter<FSTeam> {
    override fun convertToObjects(items: List<DocumentSnapshot>): List<FSTeam> {
        val teams = ArrayList<FSTeam>()
        if(items.isEmpty())
            return teams

        items.forEach { doc ->
            val t = doc.toObject(FSTeam::class.java)
            if(t != null)
                teams.add(t)
        }

        return teams
    }
}