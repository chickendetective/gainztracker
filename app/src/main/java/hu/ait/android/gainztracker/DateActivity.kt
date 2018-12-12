package hu.ait.android.gainztracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.activity_date.*
import java.util.*

class DateActivity : AppCompatActivity(), WorkoutDialog.WorkoutHandler {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var workoutListener: ListenerRegistration

    val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private var curDate = Calendar.getInstance().time

    companion object {

        val KEY_WORKOUT_TO_EDIT = "KEY_WORKOUT_TO_EDIT"
    }
    private var editIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        if (intent.hasExtra(MainActivity.KEY_DATE)) {
            tvDay.text = intent.getStringExtra(MainActivity.KEY_DATE)
            curDate = Date(intent.getStringExtra(MainActivity.KEY_DATE))
        }

        initWorkoutRecyclerView()
        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

    }

    private fun initWorkoutRecyclerView() {
        //create a document for current date if hasnt existed
        val data = HashMap<String, Any>()
        data.put("lastLogin", Calendar.getInstance().time)
        db.collection("users").document(curUser!!.uid).collection("DayData")
                .document(curDate.toString()).set(data, SetOptions.merge())
        val workoutsCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout") //create a subcollection for all the workouts

        workoutListener = workoutsCollection.addSnapshotListener(object: EventListener<QuerySnapshot> {
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
                            workoutAdapter.addWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            workoutAdapter.editWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            workoutAdapter.removeWorkoutByKey(docChange.document.id)
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
        bundle.putSerializable(KEY_WORKOUT_TO_EDIT, workoutToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun workoutCreated(workout: Workout) {
        //add workout to firebase
        val data = HashMap<String, Any>()
        data.put("name", workout.name)
        data.put("workoutType", workout.type)
        data.put("numExercise", 0)
        db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot written with ID: " + documentReference.id)
                    workout.id = documentReference.id
                    Thread {
                        runOnUiThread {
                            workoutAdapter.addWorkout(workout, documentReference.id)
                        }
                    }.start()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
    }

    override fun workoutUpdated(workout: Workout) {
        //update workout in firebase - either using id or index through the keylist in adapter to find it
        val workoutRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document(workout.id!!)

        workoutRef
                .update("name", workout.name, "type", workout.type, "numExercise", workout.exercises.size)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                    Thread {
                        runOnUiThread {
                            workoutAdapter.editWorkout(workout, workout.id!!)
                        }
                    }.start()
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    fun getDate(): Any {
        return curDate
    }
}