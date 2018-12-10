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
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.workout_card.view.*

class WorkoutAdapter(var context: Context, var uid: String) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    private var workoutsList = mutableListOf<Workout>()
    private var workoutKeys = mutableListOf<String>()

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.workout_card, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return workoutsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
        val workout = workoutsList[holder.adapterPosition]

        holder.tvName.text = workout.name
        holder.tvType.text = workout.type

        when {
            workout.type == "mobility" -> holder.ivTypeIcon.setImageResource(R.drawable.mobility_workout_icon)
            workout.type == "strength" -> holder.ivTypeIcon.setImageResource(R.drawable.strength_workout_icon)
            workout.type == "endurance" -> holder.ivTypeIcon.setImageResource(R.drawable.endurance_workout_icon)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.tvName
        val tvType: TextView = itemView.tvType
        val btnDelete: Button = itemView.btnDelete
        val ivTypeIcon: ImageView = itemView.ivTypeIcon
    }

    fun addWorkout(workout: Workout, key: String) {
        workoutsList.add(workout)
        workoutKeys.add(key)
        notifyDataSetChanged()
    }

    private fun removeWorkout(index: Int) {
        FirebaseFirestore.getInstance().collection("workouts").document(
                workoutKeys[index]
        ).delete()

        workoutsList.removeAt(index)
        workoutKeys.removeAt(index)
        notifyItemRemoved(index)
    }
}