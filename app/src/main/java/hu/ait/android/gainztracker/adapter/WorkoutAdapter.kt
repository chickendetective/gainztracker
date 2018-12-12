package hu.ait.android.gainztracker.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
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
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.workout_card.view.*

class WorkoutAdapter(var context: Context, var uid: String) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    private var workoutsList = mutableListOf<Workout>()
    private var workoutKeys = mutableListOf<String>()
    private var nameToKey : MutableMap<String, String> = HashMap()
    private var curUser = FirebaseAuth.getInstance().currentUser
    private var curDate = DateActivity().getDate().toString()

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
            workout.type == "Mobility" -> holder.ivTypeIcon.setImageResource(R.drawable.mobility_workout_icon)
            workout.type == "Strength" -> holder.ivTypeIcon.setImageResource(R.drawable.strength_workout_icon)
            workout.type == "Endurance" -> holder.ivTypeIcon.setImageResource(R.drawable.endurance_workout_icon)
        }

        holder.btnEdit.setOnClickListener {
            (context as DateActivity).showEditWorkoutDialog(workout,holder.adapterPosition)
        }
        holder.btnDelete.setOnClickListener {
            removeWorkout(holder.adapterPosition)
        }

        holder.itemView.setOnClickListener{
            //            Toast.makeText(context, "BUY THIS!", Toast.LENGTH_LONG).show()
            val intentStart = Intent(context, WorkoutActivity::class.java)
            val workoutName = holder.itemView.tvName.toString()
            intentStart.putExtra("WORKOUTID", nameToKey[workoutName]) //TODO: change to workout ID
            context.startActivity(intentStart)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.tvName
        val tvType: TextView = itemView.tvType
        val btnDelete: Button = itemView.btnDelete
        val btnEdit: Button = itemView.btnEdit
        val ivTypeIcon: ImageView = itemView.ivTypeIcon
    }

    fun addWorkout(workout: Workout, key: String) {
        workoutsList.add(workout)
        workoutKeys.add(key)
        nameToKey[workout.name] =  key
        notifyDataSetChanged()
    }

    private fun removeWorkout(index: Int) {
        FirebaseFirestore.getInstance().collection("users").document(curUser!!.uid).
            collection("DayData").document(curDate).collection("workouts").document(
                workoutKeys[index]
        ).delete()
        val name = workoutsList[index].name
        workoutsList.removeAt(index)
        workoutKeys.removeAt(index)
        nameToKey.remove(name)
        notifyItemRemoved(index)
    }
    fun removeWorkoutByKey(key: String) {
        val index = workoutKeys.indexOf(key)
        val workout = workoutsList[index]
        if (index != -1) {
            FirebaseFirestore.getInstance().collection("users").document(curUser!!.uid).
                collection("DayData").document(curDate).collection("workouts").document(
                key
            ).delete()
            workoutsList.removeAt(index)
            workoutKeys.removeAt(index)
            nameToKey.remove(workout.name)
            notifyItemRemoved(index)
        }
    }

    fun editWorkout(workout: Workout, key: String) {
        val index = workoutKeys.indexOf(key)
        val oldWorkout = workoutsList[index]
        val id = nameToKey[oldWorkout.name]
        nameToKey.remove(oldWorkout.name)
        if (index != -1){
            workoutsList[index].name = workout.name
            workoutsList[index].type = workout.type
            nameToKey.put(workout.name, id!!)
            notifyItemChanged(index)
        }
    }
}