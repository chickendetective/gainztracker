package hu.ait.android.gainztracker

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gainz_viewer.*
import java.net.URL

class GainzViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gainz_viewer)
        lateinit var imgUrl: URL
        if (intent.hasExtra(DateActivity.KEY_VIEW_GAINZ)) {
            imgUrl = URL(intent.getStringExtra(DateActivity.KEY_VIEW_GAINZ))
        }
        val bitmap = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream())
        ivViewGainz.setImageBitmap(bitmap)
    }
}