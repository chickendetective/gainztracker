package hu.ait.android.gainztracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_gainz_viewer.*

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