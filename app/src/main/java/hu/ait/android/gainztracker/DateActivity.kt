package hu.ait.android.gainztracker

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.firebase.firestore.*
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.activity_date.*

class DateActivity : AppCompatActivity(), WorkoutDialog.ItemHandler {

    private lateinit var workoutsAdapter: WorkoutAdapter
    private lateinit var workoutsListener: ListenerRegistration

    companion object {
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }
    private var editIndex: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        if (intent.hasExtra(MainActivity.KEY_DATE)) {
            tvDay.text = intent.getStringExtra(MainActivity.KEY_DATE)
        }

        initWorkoutRecyclerView()
        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

    }


    private fun initWorkoutRecyclerView() {
        val db = FirebaseFirestore.getInstance()
        val workoutsCollection = db.collection("workouts")

        workoutsListener = workoutsCollection.addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(querySnapshot: QuerySnapshot?, p1: FirebaseFirestoreException?) {

                if(p1 != null){
                    Toast.makeText(this@DateActivity,"Error: ${p1.message}",
                            Toast.LENGTH_LONG).show()
                    return
                }

                for (docChange in querySnapshot!!.documentChanges) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            workoutsAdapter.addWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            workoutsAdapter.editWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            workoutsAdapter.removeWorkoutByKey(docChange.document.id)
                        }
                    }
                }

            }
        })
    }

    private fun showAddWorkoutDialog() {
        WorkoutDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    fun showEditWorkoutDialog(workoutToEdit: Workout, idx: Int) {
        editIndex = idx
        val editItemDialog = WorkoutDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, workoutToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun workoutCreated(item: Workout) {
        //add workout to firebase
    }

    override fun workoutUpdated(item: Workout) {
        //update workout in firebase
    }
}