package com.astutusdesigns.habitood.pagination

interface PaginatorCallback<T> {
    fun onPageReceived(items: List<T>)
    fun onEmptyPageReceived()
    fun onFailure(ex: Exception)
}