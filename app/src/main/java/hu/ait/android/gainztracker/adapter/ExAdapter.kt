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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.gainztracker.DateActivity
import hu.ait.android.gainztracker.R
import hu.ait.android.gainztracker.WorkoutActivity
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.touch.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.exercise_card.view.*
import java.util.*

class ExAdapter(options: FirestoreRecyclerOptions<Exercise>, context: Context, fb: CollectionReference) :
    FirestoreRecyclerAdapter<Exercise, ExAdapter.ExerciseHolder>(options), ItemTouchHelperAdapter{

    private val context: Context = context
    private val fb: CollectionReference = fb
    private var exerciseList = mutableListOf<Exercise>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ExerciseHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.exercise_card, parent, false
        )
        return ExAdapter.ExerciseHolder(view)
    }

    override fun onBindViewHolder(holder: ExAdapter.ExerciseHolder, position: Int, exercise: Exercise) {
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
        val exerciseRef = fb.document(exercise.id)

        exerciseRef
            .update("set", exercise.set-1)
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully updated!")
                notifyDataSetChanged()
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    private fun addSet(exercise: Exercise) {
        val exerciseRef = fb.document(exercise.id)

        val index = exerciseList.indexOf(exercise)
        exerciseRef
            .update("set", exercise.set + 1)
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully updated!")
                exerciseList[index].set = exerciseList[index].set + 1
                notifyDataSetChanged()
            }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }


    override fun onDismiss(position: Int) {
        removeExercise(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(exerciseList, fromPosition, toPosition)
        //Collections.swap(exerciseKeys, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun removeExercise(index: Int) {
        //val exName = exerciseList[index].name
        fb.document(exerciseList[index].id)
            .delete()
        exerciseList.removeAt(index)
//        exerciseKeys.removeAt(index)
//        nameToKey.remove(exName)
        notifyItemRemoved(index)
    }

//    fun removeExerciseByKey(id: String){
//        val index = exerciseKeys.indexOf(id)
//        val exercise = exerciseList[index]
//        if (index != -1) {
//            db.collection("users").document(curUser!!.uid)
//                .collection("DayData").document(DateActivity().getDate().toString())
//                .collection("workout").document(workoutID)
//                .collection("exercise").document(id)
//                .delete()
//            exerciseList.removeAt(index)
//            exerciseKeys.removeAt(index)
//            nameToKey.remove(exercise.name)
//            notifyItemRemoved(index)
//        }
//    }

//    fun editExercise(exercise: Exercise, id: String){
//        val index = exerciseKeys.indexOf(id)
//        Log.d("CHANGED_EX", exercise.toString() + " " + index.toString())
//        val oldWorkout = exerciseList[index]
////        val id = nameToKey[oldWorkout.name]
////        nameToKey.remove(oldWorkout.name)
//        if (index != -1){
////            exerciseList[index].name = exercise.name
////            exerciseList[index].muscleGroup = exercise.muscleGroup
////            exerciseList[index].set = exercise.set
////            exerciseList[index].rep = exercise.rep
////            exerciseList[index].weight = exercise.weight
//            exerciseList[index] = exercise
//            nameToKey.put(exercise.name, id)
//            notifyItemChanged(index)
//        }
//    }

    fun updateExercise(exercise: Exercise, ind: Int){
        exerciseList[ind] = exercise
        notifyItemChanged(ind)
    }

    fun getExeList(): MutableList<Exercise>{
        return exerciseList
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

}