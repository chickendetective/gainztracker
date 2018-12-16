package hu.ait.android.gainztracker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.gainztracker.DateActivity
import hu.ait.android.gainztracker.R
import hu.ait.android.gainztracker.WorkoutActivity
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.data.Workout
import hu.ait.android.gainztracker.touch.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.exercise_card.view.*
import java.util.*

class ExerciseAdapter: RecyclerView.Adapter<ExerciseAdapter.ExerciseHolder>, ItemTouchHelperAdapter {

    override fun onDismiss(position: Int) {
        removeExercise(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(exerciseList, fromPosition, toPosition)
        Collections.swap(exerciseKeys, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
    private val context: Context
    private var exerciseList = mutableListOf<Exercise>()
    private var exerciseKeys = mutableListOf<String>()
    private var nameToKey : MutableMap<String, String> = HashMap()

    private val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private var lastPosition = -1

    private var workoutID = ""

    constructor(context: Context, itemList: List<Exercise>) : super() {
        this.context = context
        this.exerciseList.addAll(itemList)
        Log.d("CREATED_ADAPTER", exerciseList.toString())
    }

    fun addFromDatabse(myList: List<Exercise>){
        //this.exerciseList.removeAll()
        this.exerciseList.addAll(myList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ExerciseHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.exercise_card, parent, false
        )
        return ExerciseHolder(view)
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    override fun onBindViewHolder(holder: ExerciseHolder, index: Int) {
        val exercise = exerciseList[holder.adapterPosition]

        holder.tvExerciseName.text = exercise.name
        holder.tvMuscleGroup.text = exercise.muscleGroup
        holder.tvSet.text = context.getString(R.string.sets_left) + exercise.set.toString()
        holder.tvRep.text = context.getString(R.string.cur_reps) + exercise.rep.toString()
        holder.tvWeight.text = context.getString(R.string.cur_weight) + exercise.weight.toString()

        when {
            exercise.muscleGroup == R.string.upper_body.toString() -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.upperbody_exercise_icon)
            exercise.muscleGroup == R.string.lower_body.toString() -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.lowerbody_workout_icon)
            exercise.muscleGroup == R.string.core.toString() -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.core_workout_icon)
        }
        holder.btnDeleteExercise.setOnClickListener {
            removeExercise(holder.adapterPosition)
        }
        holder.btnEditExercise.setOnClickListener {
            (context as WorkoutActivity).showEditExerciseDialog(exercise,holder.adapterPosition)
        }
        holder.btnAddSet.setOnClickListener {
            addSet(exercise)
        }
        holder.btnLogSet.setOnClickListener {
            logSet(exercise)
        }
    }

    private fun logSet(exercise: Exercise) {
        val exerciseRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(DateActivity().curDate)
                .collection("workout").document(workoutID)
                .collection("exercise").document(nameToKey[exercise.name]!!)

        val index = exerciseList.indexOf(exercise)
        exerciseRef
                .update("set", exercise.set)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                    exerciseList[index].set = exerciseList[index].set - 1
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    private fun addSet(exercise: Exercise) {
        val exerciseRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(DateActivity().curDate)
                .collection("workout").document(workoutID)
                .collection("exercise").document(nameToKey[exercise.name]!!)

        val index = exerciseList.indexOf(exercise)
        exerciseRef
                .update("sets.setLeft", exercise.set)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                    exerciseList[index].set = exerciseList[index].set + 1
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    class ExerciseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExerciseName: TextView = itemView.tvExerciseName
        val tvMuscleGroup: TextView = itemView.tvMuscleGroup
        val tvSet : TextView = itemView.tvSet
        val tvRep : TextView = itemView.tvRep
        val tvWeight: TextView = itemView.tvWeight
        val ivMuscleGroupIcon: ImageView = itemView.ivMuscleGroupIcon
        val btnDeleteExercise: Button = itemView.btnDeleteExercise
        val btnEditExercise: Button = itemView.btnEditExercise
        val btnLogSet: Button = itemView.btnLogSet
        val btnAddSet: Button = itemView.btnAddSet
    }

    fun addExercise(exercise : Exercise, key: String) {
        exerciseList.add(exercise)
        exerciseKeys.add(key)
        nameToKey.put(exercise.name, key)
        notifyDataSetChanged()
    }

    private fun removeExercise(index: Int) {
        val exName = exerciseList[index].name
        db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(DateActivity().curDate)
                .collection("workout").document(workoutID)
                .collection("exercise").document(exerciseKeys[index])
                .delete()
        exerciseList.removeAt(index)
        exerciseKeys.removeAt(index)
        nameToKey.remove(exName)
        notifyItemRemoved(index)
    }

    fun removeExerciseByKey(id: String){
        val index = exerciseKeys.indexOf(id)
        val exercise = exerciseList[index]
        if (index != -1) {
            db.collection("users").document(curUser!!.uid)
                    .collection("DayData").document(DateActivity().curDate)
                    .collection("workout").document(workoutID)
                    .collection("exercise").document(id)
                    .delete()
            exerciseList.removeAt(index)
            exerciseKeys.removeAt(index)
            nameToKey.remove(exercise.name)
            notifyItemRemoved(index)
        }
    }

    fun editExercise(exercise: Exercise, id: String){
        val index = exerciseKeys.indexOf(id)
        Log.d("CHANGED_EX", exercise.toString() + " " + index.toString())
        val oldWorkout = exerciseList[index]
//        val id = nameToKey[oldWorkout.name]
        nameToKey.remove(oldWorkout.name)
        if (index != -1){
//            exerciseList[index].name = exercise.name
//            exerciseList[index].muscleGroup = exercise.muscleGroup
//            exerciseList[index].set = exercise.set
//            exerciseList[index].rep = exercise.rep
//            exerciseList[index].weight = exercise.weight
            exerciseList[index] = exercise
            nameToKey.put(exercise.name, id)
            notifyItemChanged(index)
        }
    }

    fun setWorkoutID(id: String){
        workoutID = id
    }
}