package hu.ait.android.gainztracker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var selectedDate : Long = calendar.date
        btnSelect.setOnClickListener {
            val dateSelected = calendar.date
            val detailIntent = Intent(this@MainActivity, DateActivity::class.java)
            detailIntent.putExtra("DATE", dateSelected)
            startActivity(detailIntent)
        }
    }
}
