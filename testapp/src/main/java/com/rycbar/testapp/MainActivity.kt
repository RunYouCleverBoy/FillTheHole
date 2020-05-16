package com.rycbar.testapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.set
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rycbar.holefiller.ConnectivityMode
import com.rycbar.holefiller.HoleFiller
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val testPad: SketchPadManager by lazy { SketchPadManager(testPadView) }
    private val loadImageLiveData = MutableLiveData<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clearHolesButton.setOnClickListener {
            loadImageLiveData.postValue(
                loadImageLiveData.value ?: "IMG_6893.JPG"
            )
        }
        fillHolesButton.setOnClickListener {
            val healer = HoleFiller(HoleFiller.Configuration(0.2, 2f, ConnectivityMode.Connected8))
            val bitmap = testPad.getBitmap()?:return@setOnClickListener
            val image = healer.heal(bitmap.width, bitmap.height) {x, y ->
                val px = bitmap.getPixel(x, y)
                if (px != testPad.damagedPixelColour) (px.red + px.green + px.blue)/(3f * 256f) else -1f
            }

            testPad.reload(Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565).also { bmp ->
                for (y in 0 until bmp.height) {
                    for (x in 0 until bmp.width) {
                        val shade = (image[x, y] * 256f).coerceAtMost(255f).toInt()
                        bitmap[x, y] = Color.argb(255, shade, shade, shade)
                    }
                }
            })
        }

        loadImageLiveData.observe(this, Observer { name ->
            if (name != null) {
                launchUI {
                    val bitmap = loadBitmap(name)
                    testPad.reload(bitmap)
                }
            }
        })

        loadImageLiveData.postValue("IMG_6893.JPG")

        brushSizeSetting.progress = 10
        brushSizeText.text = getString(R.string.brush_size_text, brushSizeSetting.progress)
        brushSizeSetting.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushSizeText.text = getString(R.string.brush_size_text, brushSizeSetting.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: return
                testPad.setBrushSize(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        progress.toFloat(),
                        resources.displayMetrics
                    )
                )
            }
        })
    }

    @Suppress("SameParameterValue")
    private suspend fun loadBitmap(pictureAsset: String): Bitmap {
        return withBlocking {
            val inputStream = assets.open(pictureAsset)
            val immutable = BitmapFactory.decodeStream(inputStream)
            // We don't scale down because the main goal of the test is to be applied on enormous bitmaps
            immutable.copy(Bitmap.Config.ARGB_8888, true)
        }
    }

    private fun Bitmap.toGreyScaleArray(): Array<Array<Float>> {
        return arrayOf()
    }
}
