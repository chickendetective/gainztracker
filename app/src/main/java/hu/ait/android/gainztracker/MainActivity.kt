package hu.ait.android.gainztracker

import android.content.Intent
import java.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap


class MainActivity : AppCompatActivity() {
    private var curUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    companion object {
        val KEY_DATE = "KEY_DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //var selectedDate : Long = calendar.date
        //add a document for new user or update the existing one
        val userName = curUser?.email
        val data = HashMap<String, Any?>()
        data.put("userName", userName)
        data.put("lastLogin", Calendar.getInstance().time)
        Log.d("LOGGEDIN", curUser.toString())
        db.collection("users").document(curUser!!.uid).set(data, SetOptions.merge())

        btnSelect.setOnClickListener {
            val dateSelected = calendar.date
            Log.d("DATE SELECTED", dateSelected.toString())
            val detailIntent = Intent(this@MainActivity, DateActivity::class.java)
            detailIntent.putExtra(KEY_DATE, dateSelected.toString())
            startActivity(detailIntent)
        }
    }
}
