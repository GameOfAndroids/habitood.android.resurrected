package com.astutusdesigns.habitood

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReachedTopBottomRecyclerView: RecyclerView {

    interface TopAndBottomListener {
        fun onBottomReached(onBottom: Boolean)
        fun onTopReached(onTop: Boolean)
    }

    constructor(c: Context):this(c, null)
    constructor(c:Context, attr:AttributeSet?):super(c, attr, 0)
    constructor(c:Context, attr: AttributeSet?, defStyle:Int):super(c, attr, defStyle)


    private var linearLayoutManager: LinearLayoutManager? = null
    private var topAndBottomListener: TopAndBottomListener? = null
    private var onBottomNow = false
    private var onTopNow = false
    private var onBottomTopScrollListener:RecyclerView.OnScrollListener? = null


    fun setTopAndBottomListener(l: TopAndBottomListener?){
        if (l != null){
            checkLayoutManager()

            val scrollListener = createBottomAndTopScrollListener()
            onBottomTopScrollListener = scrollListener
            addOnScrollListener(scrollListener)
            topAndBottomListener = l
        } else {
            onBottomTopScrollListener?.let {
                removeOnScrollListener(it)
            }
            topAndBottomListener = null
        }
    }

    private fun createBottomAndTopScrollListener() = object :RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            checkOnTop()
            checkOnBottom()
        }
    }

    private fun checkOnTop(){
        val firstVisible = linearLayoutManager!!.findFirstCompletelyVisibleItemPosition()
        if(firstVisible == 0 || firstVisible == -1 && !canScrollToTop()){
            if (!onTopNow) {
                onTopNow = true
                topAndBottomListener?.onTopReached(true)
            }
        } else if (onTopNow){
            onTopNow = false
            topAndBottomListener?.onTopReached(false)
        }
    }

    private fun checkOnBottom(){
        val lastVisible = linearLayoutManager!!.findLastCompletelyVisibleItemPosition()
        val size = linearLayoutManager!!.itemCount - 1
        if(lastVisible == size || lastVisible == -1 && !canScrollToBottom()){
            if (!onBottomNow){
                onBottomNow = true
                topAndBottomListener?.onBottomReached(true)
            }
        } else if(onBottomNow){
            onBottomNow = false
            topAndBottomListener?.onBottomReached(false)
        }
    }


    private fun checkLayoutManager(){
        if (layoutManager is LinearLayoutManager)
            linearLayoutManager = layoutManager as LinearLayoutManager
        else
            throw Exception("for using this listener, please set LinearLayoutManager")
    }

    private fun canScrollToTop():Boolean = canScrollVertically(-1)
    private fun canScrollToBottom():Boolean = canScrollVertically(1)
}