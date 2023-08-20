package com.astutusdesigns.habitood.pagination

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class Paginator<T>(private val adapter    : IPaginationAdapter<T>,
                   private val databaseRef: Query,
                   private val callback   : PaginatorCallback<T>,
                   private val pageSize   : Long = 20.toLong()): OnSuccessListener<QuerySnapshot>, OnFailureListener {

    private var lastFetched: DocumentSnapshot? = null
    var loading = false
    var canFetchMore = true

    fun fetchNextPage() {
        if(canFetchMore && !loading) {
            loading = true
            if (lastFetched == null)
                databaseRef.limit(pageSize)
                        .get()
                        .addOnSuccessListener(this)
                        .addOnFailureListener(this)
            else
                databaseRef.limit(pageSize)
                        .startAfter(lastFetched!!)
                        .get()
                        .addOnSuccessListener(this)
                        .addOnFailureListener(this)
        }
    }

    override fun onSuccess(snapshot: QuerySnapshot?) {
        if(snapshot?.isEmpty == true) {
            canFetchMore = false
            callback.onEmptyPageReceived()
            return
        } else if(snapshot?.count() ?: 0 < pageSize) {
            canFetchMore = false
        }

        val docs = snapshot!!.documents
        callback.onPageReceived(adapter.convertToObjects(docs))
        if(snapshot.documents.isNotEmpty())
            lastFetched = snapshot.documents[snapshot.size() - 1]

        loading = false
    }

    override fun onFailure(p0: Exception) {
        canFetchMore = false
        callback.onFailure(p0)
        loading = false
    }
}