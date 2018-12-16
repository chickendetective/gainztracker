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

//class WorkoutAdapter : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>, ItemTouchHelperAdapter {
//    override fun onDismiss(position: Int) {
//        removeWorkout(position)
//    }
//
//    override fun onItemMove(fromPosition: Int, toPosition: Int) {
//        Collections.swap(workoutsList, fromPosition, toPosition)
//        Collections.swap(workoutKeys, fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
//    }
//
//    private var workoutsList = mutableListOf<Workout>()
//    private var workoutKeys = mutableListOf<String>()
//    private var nameToKey : MutableMap<String, String> = HashMap()
//    private var curUser = FirebaseAuth.getInstance().currentUser
//    private var curDate = DateActivity().getDate().toString()
//
//    val context : Context
//
//    private var lastPosition = -1
//
//    constructor(context: Context, itemList: List<Workout>) : super() {
//        this.context = context
//        this.workoutsList.addAll(itemList)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(
//                R.layout.workout_card, parent, false
//        )
//
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return workoutsList.size
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
//        val workout = workoutsList[holder.adapterPosition]
//
//        holder.tvName.text = workout.name
//        holder.tvType.text = workout.type
//
//        when {
//            workout.type == R.string.mobility.toString() -> holder.ivTypeIcon.setImageResource(R.drawable.mobility_workout_icon)
//            workout.type == R.string.strength.toString() -> holder.ivTypeIcon.setImageResource(R.drawable.strength_workout_icon)
//            workout.type == R.string.endurance.toString() -> holder.ivTypeIcon.setImageResource(R.drawable.endurance_workout_icon)
//        }
//
//        holder.btnEdit.setOnClickListener {
//            (context as DateActivity).showEditWorkoutDialog(workout,holder.adapterPosition)
//        }
//        holder.btnDelete.setOnClickListener {
//            removeWorkout(holder.adapterPosition)
//        }
//
//        holder.itemView.setOnClickListener{
//            //            Toast.makeText(context, "BUY THIS!", Toast.LENGTH_LONG).show()
//            Log.d("Workout clicked", holder.itemView.tvName.text.toString())
//            val intentStart = Intent(context, WorkoutActivity::class.java)
//            val workoutName = holder.itemView.tvName.text.toString()
//            val workoutType = holder.itemView.tvType.text.toString()
//            intentStart.putExtra(DateActivity.WORKOUT_ID, nameToKey[workoutName])
//            intentStart.putExtra(DateActivity.WORKOUT_NAME, workoutName)
//            intentStart.putExtra(DateActivity.WORKOUT_TYPE, workoutType)
//            context.startActivity(intentStart)
//        }
//    }
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvName: TextView = itemView.tvName
//        val tvType: TextView = itemView.tvType
//        val btnDelete: Button = itemView.btnDelete
//        val btnEdit: Button = itemView.btnEditExercise
//        val ivTypeIcon: ImageView = itemView.ivTypeIcon
//    }
//
//    fun addWorkout(workout: Workout, key: String) {
//        workoutsList.add(0, workout)
//        Log.d("ADDED", workoutsList.toString())
//        Log.d("Current Date", curDate)
//        workoutKeys.add(key)
//        nameToKey[workout.name] =  key
//        Thread {
//            (context as DateActivity).runOnUiThread {
//                notifyDataSetChanged()
//            }
//        }.start()
//        notifyItemInserted(0)
//        //notifyDataSetChanged()
//    }
//
//    private fun removeWorkout(index: Int) {
//        FirebaseFirestore.getInstance().collection("users").document(curUser!!.uid).
//            collection("DayData").document(curDate).collection("workouts").document(
//                workoutKeys[index]
//        ).delete()
//        val name = workoutsList[index].name
//        workoutsList.removeAt(index)
//        workoutKeys.removeAt(index)
//        nameToKey.remove(name)
//        notifyItemRemoved(index)
//    }
//    fun removeWorkoutByKey(key: String) {
//        val index = workoutKeys.indexOf(key)
//        val workout = workoutsList[index]
//        if (index != -1) {
//            FirebaseFirestore.getInstance().collection("users").document(curUser!!.uid).
//                collection("DayData").document(curDate).collection("workouts").document(
//                key
//            ).delete()
//            workoutsList.removeAt(index)
//            workoutKeys.removeAt(index)
//            nameToKey.remove(workout.name)
//            notifyItemRemoved(index)
//        }
//    }
//
//    fun editWorkout(workout: Workout, key: String) {
//        val index = workoutKeys.indexOf(key)
//        val oldWorkout = workoutsList[index]
//        //val id = nameToKey[oldWorkout.name]
//        nameToKey.remove(oldWorkout.name)
//        if (index != -1){
//            workoutsList[index].name = workout.name
//            workoutsList[index].type = workout.type
//            nameToKey.put(workout.name, key)
//            notifyItemChanged(index)
//        }
//    }
//
//    fun findWorkoutName(workoutID : String): String{
//        for (entry in nameToKey){
//            if (entry.value == workoutID) return entry.key
//        }
//
//        return ""
//    }
//
//    fun findWorkout(workoutID: String): Workout?{
//        val workoutName = findWorkoutName(workoutID)
//        for (workout in workoutsList){
//            if (workout.name == workoutName) return workout
//        }
//        return null
//    }
//}

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
            //            Toast.makeText(context, "BUY THIS!", Toast.LENGTH_LONG).show()
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
        //Collections.swap(exerciseKeys, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun removeWorkout(index: Int) {
        //val exName = exerciseList[index].name
        fb.document(workoutList[index].id)
            .delete()
        workoutList.removeAt(index)
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

    fun updateWorkout(workout: Workout, ind: Int){
        workoutList[ind] = workout
        notifyItemChanged(ind)
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