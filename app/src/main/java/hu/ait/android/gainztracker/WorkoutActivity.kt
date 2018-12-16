package hu.ait.android.gainztracker

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import hu.ait.android.gainztracker.adapter.ExAdapter
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.data.Workout
import hu.ait.android.gainztracker.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_workout.*
import java.util.*


class WorkoutActivity : AppCompatActivity(), ExerciseDialog.ExerciseHandler {

    private lateinit var exerciseAdapter: ExAdapter
    private var context: Context = this@WorkoutActivity
    private lateinit var exerciseListener: ListenerRegistration

    private val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private lateinit var curDate: String

    private var workoutID = ""

    private lateinit var curWorkout: Workout

    companion object {

        val KEY_EXERCISE_TO_EDIT = "KEY_EXERCISE_TO_EDIT"
    }
    private var editIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        Log.d("STARTING", "Starting Workout Activity")

        if (intent.hasExtra(DateActivity.WORKOUT_ID) && intent.hasExtra(DateActivity.WORKOUT_TYPE)
                && intent.hasExtra(DateActivity.WORKOUT_NAME) && intent.hasExtra(DateActivity.WORKOUT_DATE)) {
            workoutID = intent.getStringExtra(DateActivity.WORKOUT_ID)
            val workoutType = intent.getStringExtra(DateActivity.WORKOUT_TYPE)
            val workoutName = intent.getStringExtra(DateActivity.WORKOUT_NAME)
            curDate = intent.getStringExtra(DateActivity.WORKOUT_DATE)

            curWorkout = Workout(workoutID, workoutName, workoutType)
            initExerciseRecyclerView()
            tvWorkout.text = workoutName
        }

        fabAddExercise.setOnClickListener {
            showAddExerciseDialog()
        }

    }
    private fun initExerciseRecyclerView() {
        val data = HashMap<String, Any>()
        data.put("type", curWorkout.type)
        db.collection("users").document(curUser!!.uid)
            .collection("DayData").document(curDate)
            .collection("workout").document(workoutID).set(data, SetOptions.merge())

        val exercisesCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout").document(workoutID).collection("exercise")

        val options = FirestoreRecyclerOptions.Builder<Exercise>()
            .setQuery(exercisesCollection, Exercise::class.java)
            .build()

        exerciseAdapter = ExAdapter(options, context, exercisesCollection)
        val exerciseList = exerciseAdapter.getExeList()

        recyclerExercise.adapter = exerciseAdapter


        Log.d("EXERCISELIST", exerciseList.toString())
        exerciseAdapter.notifyDataSetChanged()

        exerciseListener = exercisesCollection.addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e("TAG", "Listen failed!", e)
                return@EventListener
            }

            for (doc in documentSnapshots!!){
                val exercise = doc.toObject(Exercise::class.java)
                exerciseList.add(exercise)
            }

            exerciseAdapter.notifyDataSetChanged()
            recyclerExercise.adapter = exerciseAdapter
            }
        )

        exerciseAdapter.startListening()

        runOnUiThread {

            val callback = ItemTouchHelperCallback(exerciseAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerExercise)
        }
    }

    override fun onResume(){
        super.onResume()
        initExerciseRecyclerView()
    }

    public override fun onStart() {
        super.onStart()

        exerciseAdapter.startListening()
    }

    public override fun onStop() {
        super.onStop()

        exerciseAdapter.stopListening()
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

        val exCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout").document(workoutID)
                .collection("exercise")

        val newEntry = exCollection.document()

        exercise.id = newEntry.id
        val data = HashMap<String, Any>()
        data.put("id", exercise.id)
        data.put("name", exercise.name)
        data.put("muscleGroup", exercise.muscleGroup)
        data.put("set", exercise.set)
        data.put("rep", exercise.rep)
        data.put("weight", exercise.weight)

        newEntry.set(data)
                .addOnSuccessListener {
                    Log.d("SUCCESS", "Updated Successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
    }

    override fun exerciseUpdated(exercise: Exercise) {
        Log.d("LOGEX", exercise.toString())
        val exerciseRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout").document(workoutID)
                .collection("exercise").document(exercise.id)

        exerciseRef
                .update("name", exercise.name, "muscleGroup", exercise.muscleGroup,
                        "set", exercise.set, "rep", exercise.rep, "weight", exercise.weight)
                .addOnSuccessListener {
                    Log.d("UPDATED", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

}