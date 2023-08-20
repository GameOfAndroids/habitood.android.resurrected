package com.astutusdesigns.habitood.pagination.pagination_adapters

import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RUser
import com.astutusdesigns.habitood.pagination.IPaginationAdapter
import com.google.firebase.firestore.DocumentSnapshot

class UserPaginationAdapter: IPaginationAdapter<FSUser> {
    override fun convertToObjects(items: List<DocumentSnapshot>): List<FSUser> {
        val users = ArrayList<FSUser>()
        if(items.isEmpty())
            return users

        items.forEach { doc ->
            val u = doc.toObject(FSUser::class.java)
            if(u != null) {
                // add the user to the list.
                users.add(u)
                // add or update the user in the local db.
                RUser(u)
            }
        }

        return users
    }
}