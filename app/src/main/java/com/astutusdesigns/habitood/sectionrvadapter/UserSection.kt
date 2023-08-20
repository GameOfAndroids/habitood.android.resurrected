package com.astutusdesigns.habitood.sectionrvadapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.rv_adapters.UsersManager

class UserSection(
    val sectionTitle: String?,
    val adapterInterface: SectionedAdapterInterface,
    private val userTappedListener: OnUserTappedListener
): OnItemTappedListener,
    StatelessSection(SectionParameters.builder()
        .itemResourceId(R.layout.layout_user_detail_item)
        .headerResourceId(R.layout.layout_section_header)
        .build()), OnItemCheckedListener {

    var users = UsersManager()
    private var selectable = Selectable.Disabled


    init {
        setHasHeader(true)
    }

    fun addUser(user: FSUser) {
        users.addUser(user)
    }

    fun addUsers(users: List<FSUser>) {
        users.forEach { u ->
            addUser(u)
        }
    }

    fun searchUsers(criteria: String) {
        users.searchUsers(criteria)
    }

    fun setSelectableMode(mode: Selectable) {
        selectable = mode
        users.clearSelectedItems()
    }

    fun getSelectedUsers(): ArrayList<String> {
        return users.getSelectedUsers()
    }

    override fun getContentItemsTotal(): Int {
        return users.getItemCount()
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder? {
        return UserViewHolder(view, this)
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return SectionHeaderHolder(view)
    }

    override fun onItemTapped(index: Int) {
        val userIndex = adapterInterface.getPositionInSection(index)
        val user = users.getUser(userIndex)
        userTappedListener.onUserTapped(user)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val userViewHolder = holder as? UserViewHolder

        val user = users.getUser(position)
        userViewHolder?.nameTV?.text  = user.getDisplayName()
        userViewHolder?.emailTV?.text = user.email
        userViewHolder?.acceptCheckChangeInput = false
        userViewHolder?.mItemCheckedListener = this

        when(selectable) {
            Selectable.Disabled -> {
                userViewHolder?.selectedCB?.visibility = View.GONE
                userViewHolder?.selectedCB?.isChecked = false
            }
            Selectable.Enabled -> {
                userViewHolder?.selectedCB?.visibility = View.VISIBLE
                userViewHolder?.selectedCB?.isChecked = users.isUserSelected(position)
            }
        }

        userViewHolder?.acceptCheckChangeInput = true
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as? SectionHeaderHolder)?.sectionTitle?.text = sectionTitle
    }

    override fun onChecked(index: Int) {
        users.onUserSelected(adapterInterface.getPositionInSection(index))
    }

    override fun onUnchecked(index: Int) {
        users.onUserDeselected(adapterInterface.getPositionInSection(index))
    }
}