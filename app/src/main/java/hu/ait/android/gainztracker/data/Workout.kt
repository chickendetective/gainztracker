package hu.ait.android.gainztracker.data

data class Workout(var name: String = "",
                var type: String = "",
                   var exercises: ArrayList<Exercise>)