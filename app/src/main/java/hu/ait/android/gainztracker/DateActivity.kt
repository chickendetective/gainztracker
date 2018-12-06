package hu.ait.android.gainztracker

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.AnimationUtils

class DateActivity : AppCompatActivity(), ShoppingItemDialog.ShoppingItemHandler {
    private lateinit var shoppingItemAdapter: ShoppingItemAdapter

    companion object {
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }
    private var editIndex: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        val anim = AnimationUtils.loadAnimation(this@ScrollingActivity, R.anim.fab_anim)

        fabAddShoppingItem.setOnClickListener { view ->
            showAddShoppingItemDialog()
        }
        fabRemoveAllShoppingItems.setOnClickListener {
            fabRemoveAllShoppingItems.startAnimation(anim)
            shoppingItemAdapter.deleteAll()
        }
        fabRemoveBoughtShoppingItems.setOnClickListener {
            fabRemoveBoughtShoppingItems.startAnimation(anim)
            shoppingItemAdapter.deleteBought()
        }
        initRecyclerView()
        tutorial()

    }

    private fun tutorial() {
        if (isFirstStart()) {
            MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.fabAddShoppingItem)
                    .setPrimaryText(getString(R.string.add))
                    .setSecondaryText(getString(R.string.tut_add_long))
                    .show()
            MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.fabRemoveAllShoppingItems)
                    .setPrimaryText(getString(R.string.clear))
                    .setSecondaryText(getString(R.string.tut_clear_long))
                    .show()
            MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.fabAddShoppingItem)
                    .setPrimaryText(getString(R.string.bought_cap))
                    .setSecondaryText(getString(R.string.tut_bought_long))
                    .show()
            saveStart()
        }
    }

    private val KEY_FIRST = "KEY_FIRST"

    fun isFirstStart() : Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        return sp.getBoolean(KEY_FIRST, true)
    }

    fun saveStart() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putBoolean(KEY_FIRST, false)
        editor.apply()
    }



    private fun initRecyclerView() {
        Thread {
            val shoppingItemList = AppDatabase.getInstance(
                    this@ScrollingActivity
            ).shoppingItemDao().findAllShoppingItems()

            shoppingItemAdapter = ShoppingItemAdapter(
                    this@ScrollingActivity,
                    shoppingItemList
            )

            runOnUiThread {
                recyclerShoppingItem.adapter = shoppingItemAdapter

                val callback = ShoppingItemTouchHelperCallback(shoppingItemAdapter)
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(recyclerShoppingItem)
            }
        }.start()
    }

    private fun showAddShoppingItemDialog() {
        ShoppingItemDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    public fun showEditShoppingItemDialog(shoppingItemToEdit: ShoppingItem, idx: Int) {
        editIndex = idx
        val editItemDialog = ShoppingItemDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, shoppingItemToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun shoppingItemCreated(item: ShoppingItem) {
        Thread {
            val shoppingItemId = AppDatabase.getInstance(
                    this@ScrollingActivity).shoppingItemDao().insertShoppingItem(item)

            item.shoppingItemId = shoppingItemId

            runOnUiThread {
                shoppingItemAdapter.addShoppingItem(item)
            }
        }.start()
    }

    override fun shoppingItemUpdated(item: ShoppingItem) {
        Thread {
            AppDatabase.getInstance(
                    this@ScrollingActivity).shoppingItemDao().updateShoppingItem(item)

            runOnUiThread{
                shoppingItemAdapter.updateShoppingItem(item, editIndex)
            }
        }.start()
    }
}