package hu.ait.android.gainztracker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.dialog_workout.*
import kotlinx.android.synthetic.main.dialog_workout.view.*
import java.lang.RuntimeException

class WorkoutDialog: DialogFragment(), AdapterView.OnItemSelectedListener{

    var categories = mutableListOf<String>()

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    interface WorkoutHandler {
        fun workoutCreated(workout: Workout)
        fun workoutUpdated(workout: Workout)
    }

    private lateinit var etWorkoutName : EditText
    private lateinit var ssCategory: Spinner

    private lateinit var workoutHandler: WorkoutHandler

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        //val categoryList = mutableListOf(1, 2, 3)
        categories = mutableListOf(getString(R.string.mobility), getString(R.string.strength), getString(R.string.endurance))

        if (context is WorkoutHandler){
            workoutHandler = context
        } else{
            throw RuntimeException(
                getString(R.string.handler_error))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("New Workout")

        val rootView = rootViewSetter(builder)
        val adapter: ArrayAdapter<String> = adapterMaker(rootView)

        val arguments = this.arguments
        if (arguments != null && arguments.containsKey(
                DateActivity.KEY_WORKOUT_TO_EDIT)) {
            setItem(arguments, adapter)

            builder.setTitle("Edit Workout") }

        builder.setPositiveButton("OK") {
                dialog, witch -> // empty
        }

        builder.setNegativeButton("Cancel"){
                dialog, witch ->
        }
        return builder.create()
    }

    private fun setItem(arguments: Bundle, adapter: ArrayAdapter<String>) {
        val item = arguments.getSerializable(
            DateActivity.KEY_WORKOUT_TO_EDIT
        )  as Workout
        etWorkoutName.setText(item.name)
        ssCategory.setSelection(adapter.getPosition(item.type))
    }

    private fun rootViewSetter(builder: AlertDialog.Builder): View {
        val rootView = requireActivity().layoutInflater.inflate(
            R.layout.dialog_workout, null
        )
        etWorkoutName = rootView.etWorkoutName

        builder.setView(rootView)
        return rootView
    }

    private fun adapterMaker(rootView: View): ArrayAdapter<String> {
        ssCategory = rootView.spCategory
        ssCategory.onItemSelectedListener = this
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ssCategory.adapter = adapter

        return adapter
    }

    override fun onResume() {
        super.onResume()

        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        val negativeButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_NEGATIVE)
        positiveButton.setOnClickListener {
            posButtonHandler()
        }

        negativeButton.setOnClickListener{
            dialog.dismiss()
        }

    }

    private fun posButtonHandler() {
        if (etWorkoutName.text.isNotEmpty()) {
            val arguments = this.arguments
            if (arguments != null && arguments.containsKey(DateActivity.KEY_WORKOUT_TO_EDIT)) {
                handleItemEdit()
            } else {
                handleItemCreate()
            }

            dialog.dismiss()
        } else {
            if (etWorkoutName.text.isEmpty()) etWorkoutName.error = getString(R.string.empty_error)
        }
    }

    private fun handleItemCreate() {
        //val list : ArrayList<Exercise> = arrayListOf<Exercise>()
        workoutHandler.workoutCreated(
            Workout("",
                etWorkoutName.text.toString(),
                ssCategory.selectedItem.toString(),
                ArrayList(0)
            )
        )
    }

    private fun handleItemEdit() {
        val workoutToEdit = arguments?.getSerializable(
            DateActivity.KEY_WORKOUT_TO_EDIT
        ) as Workout
        workoutToEdit.name = etWorkoutName.text.toString()
        workoutToEdit.type = ssCategory.selectedItem.toString()

        workoutHandler.workoutUpdated(workoutToEdit)
    }
}