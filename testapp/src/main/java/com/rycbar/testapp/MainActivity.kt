package com.rycbar.testapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
            // TODO: Fill the holes engine
            // TODO: testPad?.load()
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
