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

class ExerciseDialog: DialogFragment(), AdapterView.OnItemSelectedListener{

    var categories = mutableListOf<String>()

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    interface ItemHandler {
        fun itemCreated(item: Workout)
        fun itemUpdated(item: Workout)
    }

    private lateinit var etExerciseName : EditText
    private lateinit var ssCategory: Spinner

    private lateinit var itemHandler: ItemHandler

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        //val categoryList = mutableListOf(1, 2, 3)
        categories = mutableListOf("Upper Body", "Lower Body", "Core workout")

        if (context is ItemHandler){
            itemHandler = context
        } else{
            throw RuntimeException(
                getString(R.string.handler_error))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("New Item")

        val rootView = rootViewSetter(builder)
        val adapter: ArrayAdapter<String> = adapterMaker(rootView)

        val arguments = this.arguments
        if (arguments != null && arguments.containsKey(
                DateActivity.KEY_ITEM_TO_EDIT)) { //TODO: change into workout activity later
            setItem(arguments, adapter)

            builder.setTitle("Edit Item") }

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
            DateActivity.KEY_ITEM_TO_EDIT
        )  as Workout
        etExerciseName.setText(item.name)
        ssCategory.setSelection(adapter.getPosition(item.type))
    }

    private fun rootViewSetter(builder: AlertDialog.Builder): View {
        val rootView = requireActivity().layoutInflater.inflate(
            R.layout.dialog_workout, null
        )
        etExerciseName = rootView.etItemName

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
        if (etItemName.text.isNotEmpty()) {
            val arguments = this.arguments
            if (arguments != null && arguments.containsKey(DateActivity.KEY_ITEM_TO_EDIT)) {
                handleItemEdit()
            } else {
                handleItemCreate()
            }

            dialog.dismiss()
        } else {
            if (etItemName.text.isEmpty()) etItemName.error = getString(R.string.empty_error)
        }
    }

    private fun handleItemCreate() {
        //val list : ArrayList<Exercise> = arrayListOf<Exercise>()
        itemHandler.itemCreated(
            Workout(
                etItemName.text.toString(),
                ssCategory.selectedItem.toString(),
                ArrayList(0)
            )
        )
    }

    private fun handleItemEdit() {
        val itemToEdit = arguments?.getSerializable(
            DateActivity.KEY_ITEM_TO_EDIT
        ) as Workout
        itemToEdit.name = etItemName.text.toString()
        itemToEdit.type = ssCategory.selectedItem.toString()

        itemHandler.itemUpdated(itemToEdit)
    }


}