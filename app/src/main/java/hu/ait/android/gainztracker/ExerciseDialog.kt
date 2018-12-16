package hu.ait.android.gainztracker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import hu.ait.android.gainztracker.data.Exercise
import kotlinx.android.synthetic.main.dialog_exercise.view.*
import java.lang.RuntimeException

class ExerciseDialog: DialogFragment(), AdapterView.OnItemSelectedListener{

    var muscleGroups = mutableListOf<String>()

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    interface ExerciseHandler {
        fun exerciseCreated(exercise: Exercise)
        fun exerciseUpdated(exercise: Exercise)
    }

    private lateinit var etExerciseName : EditText
    private lateinit var ssMuscleGroup: Spinner
    private lateinit var etSet : EditText
    private lateinit var etRep : EditText
    private lateinit var etWeight : EditText


    private lateinit var exerciseHandler: ExerciseHandler

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        muscleGroups = mutableListOf(getString(R.string.upper_body), getString(R.string.lower_body), getString(R.string.core))

        if (context is ExerciseHandler){
            exerciseHandler = context
        } else{
            throw RuntimeException(
                getString(R.string.handler_error))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.new_exercise))

        val rootView = rootViewSetter(builder)
        val adapter: ArrayAdapter<String> = adapterMaker(rootView)

        val arguments = this.arguments
        if (arguments != null && arguments.containsKey(
                WorkoutActivity.KEY_EXERCISE_TO_EDIT)) { //TODO: change into workout activity later
            setItem(arguments, adapter)

            builder.setTitle(getString(R.string.edit_exercise)) }

        builder.setPositiveButton(getString(R.string.ok)) {
                dialog, witch ->
        }

        builder.setNegativeButton(getString(R.string.cancel)){
                dialog, witch ->
        }
        return builder.create()
    }

    private fun setItem(arguments: Bundle, adapter: ArrayAdapter<String>) {
        val item = arguments.getSerializable(
            WorkoutActivity.KEY_EXERCISE_TO_EDIT
        )  as Exercise
        etExerciseName.setText(item.name)
        ssMuscleGroup.setSelection(adapter.getPosition(item.muscleGroup))
        etSet.setText(item.set.toString())
        etRep.setText(item.rep.toString())
        etWeight.setText(item.weight.toString())

    }

    private fun rootViewSetter(builder: AlertDialog.Builder): View {
        val rootView = requireActivity().layoutInflater.inflate(
            R.layout.dialog_exercise, null
        )
        etExerciseName = rootView.etExerciseName
        ssMuscleGroup = rootView.ssMuscleGroup
        etSet = rootView.etSet
        etRep = rootView.etRep
        etWeight = rootView.etWeight


        builder.setView(rootView)
        return rootView
    }

    private fun adapterMaker(rootView: View): ArrayAdapter<String> {
        ssMuscleGroup = rootView.ssMuscleGroup
        ssMuscleGroup.onItemSelectedListener = this
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_item,
            muscleGroups
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ssMuscleGroup.adapter = adapter

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
        if (etExerciseName.text.isNotEmpty()) {
            val arguments = this.arguments
            if (arguments != null && arguments.containsKey(WorkoutActivity.KEY_EXERCISE_TO_EDIT)) {
                handleExerciseEdit()
            } else {
                handleExerciseCreate()
            }

            dialog.dismiss()
        } else {
            if (etExerciseName.text.isEmpty()) etExerciseName.error = getString(R.string.empty_error)
            if (etRep.text.isEmpty()) etRep.error = getString(R.string.empty_error)
            if (etSet.text.isEmpty()) etSet.error = getString(R.string.empty_error)
            if (etWeight.text.isEmpty()) etWeight.error = getString(R.string.empty_error)
        }
    }

    private fun handleExerciseCreate() {
        val exercise = Exercise("",
                etExerciseName.text.toString(),
                ssMuscleGroup.selectedItem.toString(),
                etSet.text.toString().toInt(),
                etRep.text.toString().toInt(),
                etWeight.text.toString().toDouble()
        )
        Log.d("EXERCISE", exercise.toString())
        exerciseHandler.exerciseCreated(
            Exercise("",
                    etExerciseName.text.toString(),
                    ssMuscleGroup.selectedItem.toString(),
                    etSet.text.toString().toInt(),
                    etRep.text.toString().toInt(),
                    etWeight.text.toString().toDouble()
                    )
        )

    }

    private fun handleExerciseEdit() {
        val itemToEdit = arguments?.getSerializable(
            WorkoutActivity.KEY_EXERCISE_TO_EDIT
        ) as Exercise
        itemToEdit.name = etExerciseName.text.toString()
        itemToEdit.muscleGroup = ssMuscleGroup.selectedItem.toString()
        itemToEdit.set =etSet.text.toString().toInt()
        itemToEdit.rep = etRep.text.toString().toInt()
        itemToEdit.weight = etWeight.text.toString().toDouble()

        Log.d("HANDLE_EDIT", itemToEdit.toString())
        exerciseHandler.exerciseUpdated(itemToEdit)
    }


}