package com.astutusdesigns.habitood.pagination.pagination_adapters

import com.astutusdesigns.habitood.datamodels.FSTeam
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.pagination.IPaginationAdapter
import com.google.firebase.firestore.DocumentSnapshot

class RTeamPaginationAdapter: IPaginationAdapter<RTeam> {
    override fun convertToObjects(items: List<DocumentSnapshot>): List<RTeam> {
        val teams = ArrayList<RTeam>()
        if(items.isEmpty())
            return teams

        items.forEach { doc ->
            val t = doc.toObject(FSTeam::class.java)
            if(t != null)
                teams.add(RTeam(doc.id, t))
        }

        return teams
    }
}