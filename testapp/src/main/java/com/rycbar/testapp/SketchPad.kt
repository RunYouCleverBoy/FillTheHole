package com.rycbar.testapp

import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import android.widget.ImageView


/**
 * Sketch pad manager connects an imageView with a bitmap, converts it to monochrome (greyScale)
 * and allows sketching to emulate defected images.
 */
class SketchPadManager(private val imageView: ImageView) {
    private lateinit var canvas: Canvas
    private val dirtPaint: Paint = Paint()
    private var penPoint: PointF? = null
    private var transform = { x: Float, y: Float -> PointF(x, y) }

    init {
        dirtPaint.color = Color.RED
        dirtPaint.strokeCap = Paint.Cap.ROUND
        dirtPaint.strokeWidth = imageView.resources.getDimension(R.dimen.sketchPadBrushSize)
        bindClickListener(imageView)
        imageView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            transform = calculateScaleFactor()
        }
    }

    fun setBrushSize(widthDp: Float) {
        dirtPaint.strokeWidth = widthDp.coerceAtLeast(1f)
    }

    /**
     * Reload an image as GreyScale to the imageView
     *
     * **Note that this one is blocky** - Use it for test only
     *
     * @param sourceBitmap Source bitmap, can be in colour. It will be converted to greyscale.
     *
     */
    // TODO: Split to main and async part.
    fun reload(sourceBitmap: Bitmap) {
        val bitmap =
            Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.RGB_565)
        canvas = Canvas(bitmap)
        drawBitmapAsGreyScale(sourceBitmap)
        imageView.setImageBitmap(bitmap)
    }

    private fun drawBitmapAsGreyScale(sourceBitmap: Bitmap) {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
    }

    private fun calculateScaleFactor(): (x: Float, y: Float) -> PointF {
        val matrix = imageView.imageMatrix
        val inverse = Matrix()
        matrix.invert(inverse)
        val src = FloatArray(2)
        val dst = FloatArray(2)
        return { x: Float, y: Float ->
            src[0] = x
            src[1] = y
            inverse.mapPoints(dst, 0, src, 0, 1)
            PointF(dst[0], dst[1])
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindClickListener(imageView: ImageView) {
        setupClick(imageView).setOnTouchListener { _, event -> handleClick(event) }
    }

    /**
     * Stroke to point
     *
     * @param x X Coordinate of target point
     * @param y Y Coordinate of target point
     *
     * @return true iff successful
     */
    private fun strokeToPoint(x: Float, y: Float) : Boolean {
        val sourcePoint = penPoint?.let { PointF(it.x, it.y) } ?: return false
        val targetPoint = PointF(x, y).also { penPoint = it }
        canvas.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y, dirtPaint)
        imageView.postInvalidate()
        return true
    }

    private fun handleClick(event: MotionEvent): Boolean {
        val transformed = transform.invoke(event.x, event.y)
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                penPoint =
                    PointF(transformed.x, transformed.y).apply { canvas.drawPoint(x, y, dirtPaint) }
                true
            }
            MotionEvent.ACTION_MOVE -> strokeToPoint(transformed.x, transformed.y)
            MotionEvent.ACTION_UP -> strokeToPoint(transformed.x, transformed.y)

            else -> false
        }
    }
    private fun setupClick(imageView: ImageView) = imageView
}