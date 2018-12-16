package hu.ait.android.gainztracker

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.Glide
import hu.ait.android.gainztracker.R.drawable.pre_gainz_pic
import kotlinx.android.synthetic.main.activity_gainz_viewer.*
import java.net.URL

class GainzViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gainz_viewer)
        lateinit var imgUrl: String
        if (intent.hasExtra(DateActivity.KEY_VIEW_GAINZ)) {
            imgUrl = intent.getStringExtra(DateActivity.KEY_VIEW_GAINZ)
        }
        Glide.with(applicationContext).load(imgUrl).into(ivViewGainz)
    }
}