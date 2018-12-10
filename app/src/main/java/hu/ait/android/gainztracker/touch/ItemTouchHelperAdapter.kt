package hu.ait.android.gainztracker.touch

interface ItemTouchHelperAdapter{
    fun onDismiss(position: Int)
    fun onItemMove(fromPosition: Int, toPosition: Int)
}