package com.astutusdesigns.habitood

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper class used to attach swiping capabilities to recycler views.
 * Created by TMiller on 1/16/2018.
 */
class RvSwipeTools(private val context: Context,
                   private val swipeCallback: OnSwipeCallback) {

    private var paint = Paint()

    interface OnSwipeCallback {
        fun onLeftSwipe(adapterPosition: Int)
        fun onRightSwipe(adapterPosition: Int)
    }

    fun generateLeftSwipeCallback(attachingRecyclerView: RecyclerView, swipeIconResourceId: Int) {
        val swipeItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {
                    swipeCallback.onLeftSwipe(position)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    swipeCallback.onRightSwipe(position)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    paint.color = Color.parseColor("#D32F2F")
                    val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(background, paint)
                    val icon = BitmapFactory.decodeResource(context.resources, swipeIconResourceId)
                    val iconDest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(icon, null, iconDest, paint)

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }

        val swipeLeftTouchHelper = ItemTouchHelper(swipeItemTouchCallback)
        swipeLeftTouchHelper.attachToRecyclerView(attachingRecyclerView)
        attachingRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    /**
     * This method will give the RecyclerView passed to it the ability to Swipe right.
     * @param attachingRecyclerView recyclerview which will be given the swipe right capabilities.
     * @param swipeIconResourceId   the icon resource id which will be underneath the view, visible when the user swipes away the top view.
     */
    fun generateRightSwipeCallback(attachingRecyclerView: RecyclerView, swipeIconResourceId: Int) {
        val swipeItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT)
                    swipeCallback.onLeftSwipe(position)
                else if (direction == ItemTouchHelper.RIGHT)
                    swipeCallback.onRightSwipe(position)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    paint.color = ContextCompat.getColor(context, android.R.color.holo_green_dark)
                    val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(background, paint)
                    val icon = BitmapFactory.decodeResource(context.resources, swipeIconResourceId)
                    val iconDest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(icon, null, iconDest, paint)

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }

        val swipeLeftTouchHelper = ItemTouchHelper(swipeItemTouchCallback)
        swipeLeftTouchHelper.attachToRecyclerView(attachingRecyclerView)
        attachingRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    /**
     * This method will attach to the RecyclerView passed into it an ItemTouchHelper enabling swiping on the RecyclerView.
     * @param attachingRecyclerView     RecyclerView to which we add swiping ability.
     * @param leftSwipeIconResourceId   Icon which will be shown underneath the view as the view is swiped to the left.
     * @param rightSwipeIconResourceId  Icon which will be shown underneath the view as the view is swiped to the right.
     */
    fun generateBidirectionalSwipeCallBack(attachingRecyclerView: RecyclerView, leftSwipeIconResourceId: Int, rightSwipeIconResourceId: Int) {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT)
                    swipeCallback.onLeftSwipe(position)
                else if (direction == ItemTouchHelper.RIGHT)
                    swipeCallback.onRightSwipe(position)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dX < 0) {
                        val itemView = viewHolder.itemView
                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3

                        paint.color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, paint)
                        val icon = BitmapFactory.decodeResource(context.resources, leftSwipeIconResourceId)
                        val iconDest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, iconDest, paint)

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    } else {
                        val itemView = viewHolder.itemView
                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3

                        paint.color = ContextCompat.getColor(context, android.R.color.holo_green_dark)
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, paint)
                        val icon = BitmapFactory.decodeResource(context.resources, rightSwipeIconResourceId)
                        val iconDest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, iconDest, paint)

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    }
                }
            }
        }

        val swipeLeftTouchHelper = ItemTouchHelper(itemTouchCallback)
        swipeLeftTouchHelper.attachToRecyclerView(attachingRecyclerView)
        attachingRecyclerView.itemAnimator = DefaultItemAnimator()
    }
}