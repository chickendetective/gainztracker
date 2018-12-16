package hu.ait.android.gainztracker

import android.content.Intent
import java.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.HashMap


class MainActivity : AppCompatActivity() {
    private var curUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var date: String
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

//        calendar.setOnDateChangeListener{
//
//            @Override
//            public fun onSelectedDayChange(CalendarView view, int year, int month,
//                    int dayOfMonth) {
//                Intent k = new Intent(GlavnaAktivnost.this, DatumDetalji.class);
//                k.putExtra("godina", year);
//                k.putExtra("mesec", month);
//                k.putExtra("dan", dayOfMonth);
//                startActivity(k);
//
//            }
        val ss = SimpleDateFormat("dd-MM-yyyy")
        date = ss.format(calendar.date)
        calendar.setOnDateChangeListener(CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
            date = dayOfMonth.toString() + "-" + (month+1).toString() + "-" + year.toString()
        }


        )

            btnSelect.setOnClickListener {
                Log.d("DATE SELECTED", date)
                val dateIntent = Intent(this@MainActivity, DateActivity::class.java)
                dateIntent.putExtra(KEY_DATE, date)
                startActivity(dateIntent)
            }
        }
}
