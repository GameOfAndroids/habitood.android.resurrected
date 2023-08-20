package com.astutusdesigns.habitood.pagination

import com.google.firebase.firestore.DocumentSnapshot

interface IPaginationAdapter<T> {
    fun convertToObjects(items: List<DocumentSnapshot>): List<T>
}