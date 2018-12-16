package hu.ait.android.gainztracker.data

import java.io.Serializable


data class Exercise(var id: String = "",
                    var name: String = "",
                   var muscleGroup: String = "",
                    var set: Int = 0,
                    var rep: Int = 0,
                    var weight: Double = 0.0): Serializable

