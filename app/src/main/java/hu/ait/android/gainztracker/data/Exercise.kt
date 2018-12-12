package hu.ait.android.gainztracker.data

import java.io.Serializable


data class Exercise(var id: String?,
                    var name: String = "",
                   var muscle: String = "",
                    var rep: Number = 0,
                    var set: Number = 0): Serializable