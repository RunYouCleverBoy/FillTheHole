package com.rycbar.testapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.set
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rycbar.holefiller.api.ConnectivityMode
import com.rycbar.holefiller.api.HoleFiller
import com.rycbar.holefiller.api.Image
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CompletableDeferred

class MainActivity : AppCompatActivity() {
    private val testPad: SketchPadManager by lazy { SketchPadManager(testPadView) }
    private val loadImageLiveData = MutableLiveData<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clearHolesButton.setOnClickListener {
            loadImageLiveData.postValue(loadImageLiveData.value ?: "IMG_7833.JPG")
        }
        fillHolesButton.setOnClickListener {
            val deferred = CompletableDeferred<Image>()
            val healer = HoleFiller(HoleFiller.Configuration(0.01, 2f, ConnectivityMode.Connected8))
            val bitmap = testPad.getBitmap() ?: return@setOnClickListener
            healer.heal(bitmap.width, bitmap.height) { x, y ->
                val px = bitmap.getPixel(x, y)
                if (px != testPad.damagedPixelColour) (px.red + px.green + px.blue) / (3f * 255f) else -1f
            }.then { deferred.complete(it) }

            launchUI {
                val image = deferred.await()
                val bmp = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)
                withBlocking {
                    for (y in 0 until bmp.height) {
                        for (x in 0 until bmp.width) {
                            val shade = (image[x, y] * 256f).coerceAtMost(255f).toInt()
                            bmp[x, y] = Color.argb(255, shade, shade, shade)
                        }
                    }
                }
                testPad.reload(bmp)
            }
        }

        loadImageLiveData.observe(this, Observer { name ->
            if (name != null) {
                launchUI {
                    val bitmap = loadBitmap(name)
                    testPad.reload(bitmap)
                }
            }
        })

        loadImageLiveData.postValue("IMG_7833.JPG")

        brushSizeSetting.progress = 10
        brushSizeText.text = getString(R.string.brush_size_text, brushSizeSetting.progress)
        brushSizeSetting.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushSizeText.text = getString(R.string.brush_size_text, brushSizeSetting.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: return
                testPad.setBrushSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, progress.toFloat(), resources.displayMetrics))
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
}
