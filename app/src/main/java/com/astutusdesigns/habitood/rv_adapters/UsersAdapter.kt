package com.astutusdesigns.habitood.rv_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.datamodels.FSUser

/**
 * This is a RecyclerView adapter and view holder. It is designed to be used anywhere in the project
 * where User details need to be displayed.
 * Created by TMiller on 1/12/2018.
 */
class UsersAdapter : RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private var mSearchActive = false
    private var mSearchCriteria = ""
    private var mSearchedUsers = ArrayList<FSUser>()

    private var mProfileImageTargets = ArrayList<Target>()
    private var mRemovedUser: FSUser? = null
    private var mRemovedUserIndex = -1
    private var mItemRemovedUndoCalled = false
    private var mContext: Context
    private var mItemClickedListener: RVItemClicked<FSUser>?
    private var mUsers = ArrayList<FSUser>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    constructor(items: List<FSUser>, itemClickedListener: RVItemClicked<FSUser>? = null) {
        mItemClickedListener = itemClickedListener
        mUsers = sortUsersList(items)
        mContext = HabitoodApp.instance.applicationContext
        notifyDataSetChanged()
    }

    constructor(itemClickedListener: RVItemClicked<FSUser>? = null) {
        mItemClickedListener = itemClickedListener
        mContext = HabitoodApp.instance.applicationContext
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_user_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(mSearchActive) {
            holder.mUserName?.text = String.format(mContext.getString(R.string.name_concat), mSearchedUsers[position].fname, mSearchedUsers[position].lname)
            holder.mEmailView?.text = mSearchedUsers[position].email
        } else {
            holder.mUserName?.text = String.format(mContext.getString(R.string.name_concat), mUsers[position].fname, mUsers[position].lname)
            holder.mEmailView?.text = mUsers[position].email
        }

        holder.mProfileImageView?.setImageResource(R.mipmap.ic_account_circle_white_24dp)

        /*
        val width = holder.mProfileImageView.drawable.intrinsicWidth
        val height = holder.mProfileImageView.drawable.intrinsicHeight

        // if the profile photo url is not empty, then the User has uploaded a profile photo previously.
        if (!mUsers!![position].getProfilePhotoUrl().equals("")) {
            val profileImage = ControllerImages.getCachedProfilePhoto(mContext, mUsers!![position])

            if (profileImage != null) {
                holder.mProfileImageView.setImageDrawable(profileImage)
                return
            }

            // if the photo has not been cached, we'll need to download it. After download, cache it.
            mProfileImageTargets!!.add(ControllerImages.getTarget(mContext, mUsers!![position], holder.mProfileImageView))

            if (profileImage == null) {
                Log.d(tag, "User profile image being resized to: $width by $height")
                Picasso.with(mContext)
                        .load(mUsers!![position].getProfilePhotoUrl())
                        .resize(width, height)
                        .centerInside()
                        .into(mProfileImageTargets!![mProfileImageTargets!!.size - 1])
            }
        }
        */
    }

    override fun getItemCount(): Int {
        return if (mSearchActive) mSearchedUsers.size else mUsers.size

    }

    fun searchUsers(criteria: String?) {
        mSearchCriteria = criteria ?: ""
        when(criteria) {
            null, "" -> deactivateSearch()
            else -> {
                mSearchActive = true
                mSearchedUsers.clear()
                mUsers.forEach { user ->
                    if(user.fname.toLowerCase().contains(criteria.toLowerCase())
                            || user.lname.toLowerCase().contains(criteria.toLowerCase())
                            || user.email.toLowerCase().contains(criteria.toLowerCase())) {
                        mSearchedUsers.add(user)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun deactivateSearch() {
        mSearchActive = false
        mSearchedUsers.clear()
    }

    fun newDataSet(userList: List<FSUser>) {
        mUsers = userList as? ArrayList<FSUser> ?: ArrayList(userList)
        deactivateSearch()
        notifyDataSetChanged()
    }

    fun getUser(position: Int): FSUser {
        return if(mSearchActive) mSearchedUsers[position] else mUsers[position]
    }

    fun addUser(user: FSUser) {
        var userNotAdded = true
        mUsers.forEach { u ->
            if(u.userId == user.userId) {
                userNotAdded = false
            }
        }

        if(userNotAdded) {
            mUsers.add(user)
            mUsers = sortUsersList(mUsers)
        }

        if(mSearchActive) {
            if(user.fname.toLowerCase().contains(mSearchCriteria.toLowerCase()) ||
                    user.lname.toLowerCase().contains(mSearchCriteria.toLowerCase()) ||
                    user.email.toLowerCase().contains(mSearchCriteria.toLowerCase()))
            mSearchedUsers.add(user)
            mSearchedUsers = sortUsersList(mSearchedUsers)
        }

        notifyDataSetChanged()
    }

    private fun sortUsersList(userList: List<FSUser>): ArrayList<FSUser> {
        val ul: List<FSUser> = userList.sortedWith(compareBy{ it.fname })
        val ual: ArrayList<FSUser> = ArrayList()
        ual.addAll(ul)
        return ual
    }

    fun removeUser(position: Int) {
        if(mSearchActive) {
            mRemovedUser = mSearchedUsers[position]
            mSearchedUsers.removeAt(position)

            var userIndex = -1
            for(i in mUsers.indices)
                if(mRemovedUser?.userId == mUsers[i].userId)
                    userIndex = i

            if(userIndex > -1)
                mUsers.removeAt(userIndex)
        } else {
            mRemovedUser = mUsers[position]
            mUsers.removeAt(position)
        }

        mRemovedUserIndex = position
        notifyItemRemoved(position)
        mItemRemovedUndoCalled = false
    }

    fun removeUser(user: FSUser) {
        if(mSearchActive) {
            val index = searchForUserIndex(mSearchedUsers, user)
            if(index > -1)
                removeUser(index)
        } else {
            val index = searchForUserIndex(mUsers, user)
            if(index > -1)
                removeUser(index)
        }
    }

    private fun searchForUserIndex(userList: List<FSUser>, user: FSUser): Int {
        var index = -1
        for(i in 0 until userList.size) {
            if(userList[i].userId == user.userId) {
                index = i
                break
            }
        }
        return index
    }

    fun undoRemoveItem() {
        if (mRemovedUserIndex != -1 && mRemovedUser != null) {
            if(mSearchActive) mSearchedUsers.add(mRemovedUserIndex, mRemovedUser!!) else mUsers.add(mRemovedUserIndex, mRemovedUser!!)
            notifyItemInserted(mRemovedUserIndex)

            mRemovedUserIndex = -1
            mRemovedUser = null
            mItemRemovedUndoCalled = true
        }
    }

    fun wasUndoCalled(): Boolean {
        return mItemRemovedUndoCalled
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val mProfileImageView: ImageView? = view.findViewById(R.id.user_details_list_item_profile_image)
        val mUserName: TextView? = view.findViewById(R.id.user_details_list_item_name)
        val mEmailView: TextView? = view.findViewById(R.id.user_details_list_item_email)

        init {
            view.findViewById<CardView>(R.id.userDetailCard).setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val user = if(mSearchActive) mSearchedUsers[adapterPosition] else mUsers[adapterPosition]
            mItemClickedListener?.onItemClicked(user)
        }
    }
}
