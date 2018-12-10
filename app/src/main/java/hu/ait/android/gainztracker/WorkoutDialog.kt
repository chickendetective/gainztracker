package hu.ait.android.gainztracker

import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.AdapterView
import hu.ait.android.gainztracker.data.Workout

class WorkoutDialog: DialogFragment(), AdapterView.OnItemSelectedListener{
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface ItemHandler {
        fun itemCreated(item: Workout)
        fun itemUpdated(item: Workout)
    }
}