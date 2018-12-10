package hu.ait.android.gainztracker

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.AnimationUtils
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Workout

class DateActivity : AppCompatActivity(), WorkoutDialog.ItemHandler {

    private lateinit var workoutAdapter: WorkoutAdapter

    companion object {
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }
    private var editIndex: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        initRecyclerView()

    }


    private fun initRecyclerView() {

    }

    private fun showAddWorkoutDialog() {
        WorkoutDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    public fun showEditWorkoutDialog(workoutToEdit: Workout, idx: Int) {
        editIndex = idx
        val editItemDialog = WorkoutDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, workoutToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun workoutCreated(item: Workout) {
        Thread {
            val shoppingItemId = AppDatabase.getInstance(
                    this@ScrollingActivity).shoppingItemDao().insertShoppingItem(item)

            item.shoppingItemId = shoppingItemId

            runOnUiThread {
                shoppingItemAdapter.addShoppingItem(item)
            }
        }.start()
    }

    override fun workoutUpdated(item: Workout) {
        Thread {
            AppDatabase.getInstance(
                    this@ScrollingActivity).shoppingItemDao().updateShoppingItem(item)

            runOnUiThread{
                shoppingItemAdapter.updateShoppingItem(item, editIndex)
            }
        }.start()
    }
}