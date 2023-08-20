package com.astutusdesigns.habitood

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.models.FSUserModel
import com.astutusdesigns.habitood.pagination.Paginator
import com.astutusdesigns.habitood.pagination.PaginatorCallback
import com.astutusdesigns.habitood.rv_adapters.UsersAdapter

/**
 * Modal alert dialog builder that allows user to swipe to add
 * users to lists/positions/etc.
 *
 * Created by TMiller on 1/16/2018.
 */
class SelectUserDialog() : RvSwipeTools.OnSwipeCallback, SearchView.OnQueryTextListener, SearchView.OnCloseListener, PaginatorCallback<FSUser>, ReachedTopBottomRecyclerView.TopAndBottomListener {

    interface UserSwipedInterface {
        fun swipedLeftUser(user: FSUser)
        fun swipedRightUser(user: FSUser)
    }

    private var swipeCallback: UserSwipedInterface? = null
    private var adapter: UsersAdapter? = null
    private var dialog: AlertDialog? = null
    private var paginator: Paginator<FSUser>? = null
    private var rv: ReachedTopBottomRecyclerView? = null
    private var progressBar: ProgressBar? = null
    var uidNotAllowedForSelection = FSUserModel.instance.getPersistedUserProfile()?.userId

    fun setOnSwipeCallback(swipeCallback: UserSwipedInterface) {
        this.swipeCallback = swipeCallback
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun setPaginator(userPaginator: Paginator<FSUser>) {
        progressBar?.visibility = View.VISIBLE
        this.paginator = userPaginator
        paginator?.fetchNextPage()
    }

    override fun onPageReceived(items: List<FSUser>) {
        // no matter what the situation is, the end user will not be selecting themselves for ctl leading a team or
        // adding themselves to a team. To prevent users from adding themselves twice as ctl's, the end user's id is
        // used to filter them out of any list allowing selection.
        items.forEach { user ->
            if(user.userId != uidNotAllowedForSelection)
                adapter?.addUser(user)
        }

        progressBar?.visibility = View.INVISIBLE
    }

    override fun onEmptyPageReceived() {
        progressBar?.visibility = View.INVISIBLE
    }

    override fun onFailure(ex: Exception) {
        progressBar?.visibility = View.INVISIBLE
    }

    override fun onTopReached(onTop: Boolean) { /* Not used in this implementation. */ }
    override fun onBottomReached(onBottom: Boolean) {
        if(paginator?.canFetchMore == true)
            paginator?.fetchNextPage()
    }

    fun createUserSelectorDialog(context: Context, userList: List<FSUser>, title: String, swipeToGenerate: SwipeToGenerate): AlertDialog {
        dialog = AlertDialog.Builder(context).create()
        dialog?.setTitle(title)

        adapter = UsersAdapter(userList)

        val view = LayoutInflater.from(context).inflate(R.layout.layout_team_add_users, null)
        rv = view.findViewById(R.id.usersRecyclerView)
        rv?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv?.setTopAndBottomListener(this)
        rv?.adapter = adapter

        val searchView = view.findViewById<SearchView>(R.id.userSearchView)
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)

        progressBar = view.findViewById(R.id.loadingCtlPB)
        if(paginator?.loading == true)
            progressBar?.visibility = View.VISIBLE

        when(swipeToGenerate) {
            SwipeToGenerate.Left -> RvSwipeTools(context, this).generateLeftSwipeCallback(rv!!, R.mipmap.ic_delete_white_24dp)
            SwipeToGenerate.Right -> RvSwipeTools(context, this).generateRightSwipeCallback(rv!!, R.mipmap.ic_add_white_24dp)
            SwipeToGenerate.Bidirectional -> RvSwipeTools(context, this).generateBidirectionalSwipeCallBack(rv!!, R.mipmap.ic_delete_white_24dp, R.mipmap.ic_add_white_24dp)
        }

        dialog?.setView(view)
        dialog?.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ -> dialog?.dismiss() }

        return dialog!!
    }

    override fun onLeftSwipe(adapterPosition: Int) {
        swipeCallback?.swipedLeftUser(getUserFromIndex(adapterPosition))
        adapter?.removeUser(adapterPosition)
    }

    override fun onRightSwipe(adapterPosition: Int) {
        swipeCallback?.swipedRightUser(getUserFromIndex(adapterPosition))
        adapter?.removeUser(adapterPosition)
    }

    private fun getUserFromIndex(position: Int): FSUser {
        return adapter!!.getUser(position)
    }

    override fun onClose(): Boolean {
        adapter?.deactivateSearch()
        return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean { /* Not used */ return false }
    override fun onQueryTextChange(queryText: String?): Boolean {
        adapter?.searchUsers(queryText)
        return true
    }
}