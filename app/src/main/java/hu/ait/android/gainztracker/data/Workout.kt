package hu.ait.android.gainztracker.data

import java.io.Serializable

data class Workout(var id: String?,
                   var name: String = "",
                var type: String = "",
                   var exercises: ArrayList<Exercise>) : Serializable