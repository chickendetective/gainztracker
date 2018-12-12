package hu.ait.android.gainztracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import hu.ait.android.gainztracker.adapter.ExerciseAdapter
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.activity_date.*
import kotlinx.android.synthetic.main.activity_workout.*
import java.util.*

class WorkoutActivity : AppCompatActivity(), ExerciseDialog.ExerciseHandler {

    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var exerciseListener: ListenerRegistration

    val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private var curDate = DateActivity().getDate()

    private var workoutID = ""

    //private var curWorkout = null

    companion object {

        val KEY_EXERCISE_TO_EDIT = "KEY_EXERCISE_TO_EDIT"
    }
    private var editIndex: Int = 0

    class SetData(setNo: Int, reps: Int, weight: Double)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        if (intent.hasExtra(DateActivity.WORKOUT_ID)) {
            workoutID = intent.getStringExtra(DateActivity.WORKOUT_ID)
            //curWorkout = workoutAdapter.findWorkout(workoutID)
            tvWorkout.text = workoutAdapter.findWorkout(workoutID)!!.name
        }

        initExerciseRecyclerView()
        fabAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }

    }
    //STILL NEEDS WORK
    private fun initExerciseRecyclerView() {
        //create a document for current date if hasnt existed
        val data = HashMap<String, Any>()
        data.put("lastLogin", Calendar.getInstance().time)
//        db.collection("users").document(curUser!!.uid)
//            .collection("DayData").document(curDate.toString())
//            .collection("workout").document()
        val exercisesCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document(workoutID).collection("exercise")

        exerciseListener = exercisesCollection.addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(querySnapshot: QuerySnapshot?, p1: FirebaseFirestoreException?) {

                if(p1 != null){
                    Toast.makeText(this@WorkoutActivity,"Error: ${p1.message}",
                            Toast.LENGTH_LONG).show()
                    return
                }

                for (docChange in querySnapshot!!.documentChanges) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            exerciseAdapter.addExercise(workout, docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            exerciseAdapter.editExercise(workout, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            exerciseAdapter.removeExerciseByKey(docChange.document.id)
                        }
                    }
                }

            }
        })
    }

    private fun showAddExerciseDialog() {
        ExerciseDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    fun showEditExerciseDialog(exerciseToEdit: Exercise, idx: Int) {
        editIndex = idx
        val editItemDialog = ExerciseDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_EXERCISE_TO_EDIT, exerciseToEdit)
        editItemDialog.arguments = bundle

        editItemDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun exerciseCreated(exercise: Exercise) {
        //add exercise to firebase
        val sets : SetData = SetData(exercise.set, exercise.rep, exercise.weight)
        val data = HashMap<String, Any>()
        data.put("name", exercise.name)
        data.put("muscleGroup", exercise.muscleGroup)
        data.put("sets", exercise.set)
        data.put("reps", exercise.rep)
        data.put("weight", exercise.weight)
        db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document()
                //FIX PATH^^^
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
        val exerciseRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document(exercise.id!!)

        exerciseRef
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