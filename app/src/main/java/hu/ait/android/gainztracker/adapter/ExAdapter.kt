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
            exercise.muscleGroup == context.getString(R.string.upper_body) -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.upperbody_exercise_icon)
            exercise.muscleGroup == context.getString(R.string.core) -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.core_workout_icon)
            exercise.muscleGroup == context.getString(R.string.lower_body) -> holder.ivMuscleGroupIcon.setImageResource(R.drawable.lowerbody_workout_icon)
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
        if(exercise.set >= 1) {
            exerciseRef
                    .update("set", exercise.set - 1)
                    .addOnSuccessListener {
                        Log.d("TAG", "DocumentSnapshot successfully updated!")
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
        }
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
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun removeExercise(index: Int) {
        fb.document(exerciseList[index].id)
            .delete()
        exerciseList.removeAt(index)
        notifyItemRemoved(index)
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