package hu.ait.android.gainztracker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.gainztracker.R
import hu.ait.android.gainztracker.WorkoutActivity
import hu.ait.android.gainztracker.data.Exercise
import kotlinx.android.synthetic.main.exercise_card.view.*

class ExerciseAdapter(var context: Context, var uid: String) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    private var exerciseList = mutableListOf<Exercise>()
    private var exerciseKeys = mutableListOf<String>()

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.exercise_card, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
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

        }
        holder.btnLogSet.setOnClickListener {

        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        notifyDataSetChanged()
    }

    private fun removeExercise(index: Int) {
        //prob needs to look here if database doesn't work
        FirebaseFirestore.getInstance().collection("exercise").document(
            exerciseKeys[index]
        ).delete()
        exerciseList.removeAt(index)
        exerciseKeys.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeExerciseByKey(id: String){


    }

    fun editExercise(exercise: Exercise, id: String){

        
    }
}