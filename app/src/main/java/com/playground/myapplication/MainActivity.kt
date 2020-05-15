package com.playground.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {
    private var testPad: SketchPadManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPicturePad("IMG_6893.JPG")
    }

    @Suppress("SameParameterValue")
    private fun initPicturePad(pictureAsset: String) {
        launchUI {
            val bitmap = withBlocking {
                val inputStream = assets.open(pictureAsset)
                val immutable = BitmapFactory.decodeStream(inputStream)
                // We don't scale down because the main goal of the test is to be applied on enormous bitmaps
                immutable.copy(Bitmap.Config.ARGB_8888, true)
            }

            testPad = SketchPadManager(sketchPad, bitmap)
        }
    }

    private fun Bitmap.toGreyScaleArray(): Array<Array<Float>> {
        return arrayOf()
    }
}
