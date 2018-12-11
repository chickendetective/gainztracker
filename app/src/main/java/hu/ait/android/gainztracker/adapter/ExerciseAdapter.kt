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

        holder.tvName.text = exercise.name
        holder.tvType.text = exercise.muscle

        when {
            exercise.muscle == "Upper body" -> holder.ivTypeIcon.setImageResource(R.drawable.upperbody_exercise_icon)
            exercise.muscle == "Lower body" -> holder.ivTypeIcon.setImageResource(R.drawable.lowerbody_workout_icon)
            exercise.muscle == "Core Workout" -> holder.ivTypeIcon.setImageResource(R.drawable.core_workout_icon)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.tvExerciseName
        val tvType: TextView = itemView.tvMuscleGroup
        val tvSet : TextView = itemView.tvSet
        val tvWeight: TextView = itemView.tvWeight
        val btnDelete: Button = itemView.btnDeleteExercise
        val ivTypeIcon: ImageView = itemView.ivTypeIcon
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
}