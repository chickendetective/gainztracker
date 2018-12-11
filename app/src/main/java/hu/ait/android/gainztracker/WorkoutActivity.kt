package hu.ait.android.gainztracker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.helper.ItemTouchHelper
import hu.ait.android.gainztracker.adapter.ExerciseAdapter
import hu.ait.android.gainztracker.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_workout.*

class WorkoutActivity : AppCompatActivity() {
    private lateinit var itemAdapter: ExerciseAdapter

    companion object {
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }
    private var editIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        fab.setOnClickListener { view ->
            showAddItemDialog()
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        Thread {
            val itemList = AppDatabase.getInstance(
                this@ScrollingActivity
            ).shoppingDao().findAllItems()

            itemAdapter = ShoppingAdapter(
                this@ScrollingActivity,
                itemList
            )

            runOnUiThread {
                recyclerItem.adapter = itemAdapter

                val callback = ItemTouchHelperCallback(itemAdapter)
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(recyclerItem)
            }
        }.start()
    }
}
