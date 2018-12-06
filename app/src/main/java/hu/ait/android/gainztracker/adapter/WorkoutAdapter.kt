package hu.ait.android.gainztracker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

class WorkoutAdapter : RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder>, ShoppingItemTouchHelperAdapter {


    var shoppingItems = mutableListOf<ShoppingItem>()

    val context : Context

    constructor(context: Context, shoppingItemList: List<ShoppingItem>) : super() {
        this.context = context
        this.shoppingItems.addAll(shoppingItemList)
    }

    constructor(context: Context) : super() {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.shopping_item, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return shoppingItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingItem = shoppingItems[position]

        holder.tvName.text = shoppingItem.name
        holder.cbBought.isChecked = shoppingItem.bought
        holder.tvDescription.text = shoppingItem.description
        holder.tvPrice.text = "$"+shoppingItem.price

        when {
            shoppingItem.category == "groceries" -> holder.category.setImageResource(food_icon)
            shoppingItem.category == "clothes" -> holder.category.setImageResource(clothes_icon)
            shoppingItem.category == "electronics" -> holder.category.setImageResource(electronics_icon)
        }

        holder.btnDelete.setOnClickListener {
            deleteShoppingItem(holder.adapterPosition)
        }

        holder.btnEdit.setOnClickListener {
            (context as ScrollingActivity).showEditShoppingItemDialog(
                    shoppingItem, holder.adapterPosition
            )
        }
        holder.cbBought.setOnClickListener {
            shoppingItem.bought = shoppingItem.bought.not()
        }
    }

    private fun deleteShoppingItem(adapterPosition: Int) {
        Thread {
            AppDatabase.getInstance(
                    context).shoppingItemDao().deleteShoppingItem(shoppingItems[adapterPosition])

            shoppingItems.removeAt(adapterPosition)

            (context as ScrollingActivity).runOnUiThread {
                notifyItemRemoved(adapterPosition)
            }
        }.start()
    }
    fun deleteAll(){
        shoppingItems.clear()
        notifyDataSetChanged()
    }
    fun deleteBought(){
        for(shoppingItem in shoppingItems){
            if (shoppingItem.bought){
                deleteShoppingItem(shoppingItems.indexOf(shoppingItem))
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val cbBought = itemView.cbBought
        val btnDelete = itemView.btnDelete
        val btnEdit = itemView.btnEdit
        val tvName = itemView.tvName
        val tvPrice = itemView.tvPrice
        val tvDescription = itemView.tvDescription
        val category = itemView.categoryIcon
    }

    fun addShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItems.add(0, shoppingItem)
        //notifyDataSetChanged()
        notifyItemInserted(0)
    }

    override fun onDismissed(position: Int) {
        deleteShoppingItem(position)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(shoppingItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun updateShoppingItem(item: ShoppingItem, editIndex: Int) {
        shoppingItems[editIndex] = item
        notifyItemChanged(editIndex)
    }

}