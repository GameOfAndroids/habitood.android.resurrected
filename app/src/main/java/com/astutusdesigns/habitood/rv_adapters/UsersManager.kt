package com.astutusdesigns.habitood.rv_adapters

import com.astutusdesigns.habitood.datamodels.FSUser

class UsersManager() {

    // the section is only if a section name will be applied to the recycler view.
    var sectionTitle: String? = null
    private var users = ArrayList<FSUser>()
    private var searchedUsers = ArrayList<FSUser>()
    private var searchActive = false
    private var selectedUsers = ArrayList<String>()
    private var searchCriteria = ""

    fun addUser(user: FSUser) {
        if(users.any { u -> u.userId == user.userId })
            return

        users.add(user)
        users = sort(users)

        if(searchActive) {
            if(user.fname.toLowerCase().contains(searchCriteria.toLowerCase()) ||
               user.lname.toLowerCase().contains(searchCriteria.toLowerCase()) ||
               user.email.toLowerCase().contains(searchCriteria.toLowerCase())) {
                searchedUsers.add(user)
                searchedUsers = sort(searchedUsers)
            }
        }
    }

    fun searchUsers(query: String) {
        searchCriteria = query
        if(query.isEmpty()) {
            searchActive = false
            searchedUsers.clear()
        } else {
            searchActive = true
            searchedUsers.clear()
            users.filterTo(searchedUsers) { it.fname.toLowerCase().contains(query.toLowerCase()) ||
                                            it.lname.toLowerCase().contains(query.toLowerCase()) ||
                                            it.email.toLowerCase().contains(query.toLowerCase()) }
        }
    }

    fun clearDataSet() {
        users.clear()
        searchedUsers.clear()
        searchActive = false
    }

    fun onUserSelected(index: Int) {
        val uid = if(searchActive) searchedUsers[index].userId else users[index].userId
        selectedUsers.add(uid)
    }

    fun onUserDeselected(index: Int) {
        val uidIndex = if(searchActive) selectedUsers.indexOf(searchedUsers[index].userId) else selectedUsers.indexOf(users[index].userId)
        if(uidIndex > -1)
            selectedUsers.removeAt(uidIndex)
    }

    fun getSelectedUsers(): ArrayList<String> {
        return selectedUsers
    }

    fun clearSelectedItems() {
        selectedUsers.clear()
    }

    fun getItemCount(): Int {
        return if(searchActive) searchedUsers.count() else users.count()
    }

    fun getUser(index: Int): FSUser {
        return if(searchActive) searchedUsers[index] else users[index]
    }

    fun isUserSelected(index: Int): Boolean {
        val user = getUser(index)
        if(selectedUsers.contains(user.userId))
            return true

        return false
    }

    fun removeUser(index: Int) {
        if(searchActive) {
            val u = searchedUsers.removeAt(index)
            users.filter { it.userId == u.userId }
        } else
            users.removeAt(index)
    }

    fun removeUser(user: FSUser): Int {
        val index = users.indexOfFirst { x -> x.userId == user.userId }
        if(index > -1)
            removeUser(index)

        return index
    }

    fun getRange(): Int {
        return if(sectionTitle == null) getItemCount() else getItemCount() + 1
    }

    private fun sort(userList: ArrayList<FSUser>): ArrayList<FSUser> {
        return ArrayList(userList.sortedWith(compareBy {it.fname} ))
    }
}