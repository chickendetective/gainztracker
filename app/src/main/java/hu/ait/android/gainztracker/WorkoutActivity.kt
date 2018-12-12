package hu.ait.android.gainztracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import hu.ait.android.gainztracker.adapter.ExerciseAdapter
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.activity_date.*
import kotlinx.android.synthetic.main.activity_workout.*
import java.util.*

class WorkoutActivity : AppCompatActivity(), ExerciseDialog.ExerciseHandler {

    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseListener: ListenerRegistration

    val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private var curDate = Calendar.getInstance().time

    companion object {

        val KEY_EXERCISE_TO_EDIT = "KEY_EXERCISE_TO_EDIT"
    }
    private var editIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        if (intent.hasExtra(MainActivity.KEY_DATE)) {
            tvDay.text = intent.getStringExtra(MainActivity.KEY_DATE)
            curDate = Date(intent.getStringExtra(MainActivity.KEY_DATE))
        }

        initExerciseRecyclerView()
        fabAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }

    }

    private fun initExerciseRecyclerView() {
        //create a document for current date if hasnt existed
        val data = HashMap<String, Any>()
        data.put("lastLogin", Calendar.getInstance().time)
        db.collection("users").document(curUser!!.uid).collection("DayData")
                .document(curDate.toString()).set(data, SetOptions.merge())
        val workoutsCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout") //create a subcollection for all the workouts

        exerciseListener = workoutsCollection.addSnapshotListener(object: EventListener<QuerySnapshot> {
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
                            exerciseAdapter.addWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            exerciseAdapter.editWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            exerciseAdapter.removeWorkoutByKey(docChange.document.id)
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
        bundle.putSerializable(KEY_EXERCISE_TO_EDIT, workoutToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun exerciseCreated(exercise: Exercise) {
        //add workout to firebase
        val data = HashMap<String, Any>()
        data.put("name", exercise.name)
        data.put("workoutType", exercise.type)
        data.put("numExercise", 0)
        db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document()
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot written with ID: " + documentReference.id)
                    exercise.id = documentReference.id
                    Thread {
                        runOnUiThread {
                            exerciseAdapter.addWorkout(exercise, documentReference.id)
                        }
                    }.start()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
    }

    override fun exerciseUpdated(exercise: Exercise) {
        //update workout in firebase - either using id or index through the keylist in adapter to find it
        val workoutRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document(exercise.id!!)

        workoutRef
                .update("name", exercise.name, "type", exercise.type, "numExercise", exercise.exercises.size)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                    Thread {
                        runOnUiThread {
                            exerciseAdapter.editWorkout(exercise, exercise.id!!)
                        }
                    }.start()
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    fun getDate(): Any {
        return curDate
    }
}