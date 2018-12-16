package hu.ait.android.gainztracker.adapter

import android.content.Context
import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.android.gainztracker.DateActivity
import hu.ait.android.gainztracker.R
import hu.ait.android.gainztracker.WorkoutActivity
import hu.ait.android.gainztracker.data.Exercise
import hu.ait.android.gainztracker.data.Workout
import hu.ait.android.gainztracker.touch.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.exercise_card.view.*
import kotlinx.android.synthetic.main.workout_card.view.*
import java.util.*

class WorkoutAdapter(options: FirestoreRecyclerOptions<Workout>, context: Context, fb: CollectionReference) :
    FirestoreRecyclerAdapter<Workout, WorkoutAdapter.WorkoutHolder>(options), ItemTouchHelperAdapter{

    private val context: Context = context
    private val fb: CollectionReference = fb
    private var workoutList = mutableListOf<Workout>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): WorkoutHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.workout_card, parent, false
        )
        return WorkoutAdapter.WorkoutHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutAdapter.WorkoutHolder, position: Int, workout: Workout) {
        holder.tvName.text = workout.name
        holder.tvType.text = workout.type

        Log.d("xml string test", context.getString(R.string.mobility))

        when {
            workout.type == context.getString(R.string.mobility) -> holder.ivTypeIcon.setImageResource(R.drawable.mobility_workout_icon)
            workout.type == context.getString(R.string.strength) -> holder.ivTypeIcon.setImageResource(R.drawable.strength_workout_icon)
            workout.type == context.getString(R.string.endurance) -> holder.ivTypeIcon.setImageResource(R.drawable.endurance_workout_icon)
        }

        holder.btnEdit.setOnClickListener {
            (context as DateActivity).showEditWorkoutDialog(workout,holder.adapterPosition)
        }
        holder.btnDelete.setOnClickListener {
            removeWorkout(holder.adapterPosition)
        }

        holder.itemView.setOnClickListener{
            Log.d("Workout clicked", holder.itemView.tvName.text.toString())
            val intentStart = Intent(context, WorkoutActivity::class.java)
            val workoutName = holder.itemView.tvName.text.toString()
            val workoutType = holder.itemView.tvType.text.toString()
            intentStart.putExtra(DateActivity.WORKOUT_ID, workout.id)
            intentStart.putExtra(DateActivity.WORKOUT_NAME, workoutName)
            intentStart.putExtra(DateActivity.WORKOUT_TYPE, workoutType)
            intentStart.putExtra(DateActivity.WORKOUT_DATE, (context as DateActivity).curDate)
            context.startActivity(intentStart)
        }
    }

    override fun onDismiss(position: Int) {
        removeWorkout(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(workoutList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun removeWorkout(index: Int) {
        fb.document(workoutList[index].id)
            .delete()
        workoutList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun getWorkoutList(): MutableList<Workout>{
        return workoutList
    }

    class WorkoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.tvName
        val tvType: TextView = itemView.tvType
        val btnDelete: Button = itemView.btnDelete
        val btnEdit: Button = itemView.btnEditWorkout
        val ivTypeIcon: ImageView = itemView.ivTypeIcon
    }

}