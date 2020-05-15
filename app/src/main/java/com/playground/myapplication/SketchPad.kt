package com.playground.myapplication

import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import android.widget.ImageView


class SketchPadManager(private val imageView: ImageView) {
    private lateinit var canvas: Canvas
    private val dirtPaint: Paint = Paint()
    private var penPoint: PointF? = null

    init {
        dirtPaint.color = Color.RED
        dirtPaint.strokeCap = Paint.Cap.ROUND
        dirtPaint.strokeWidth = imageView.resources.getDimension(R.dimen.sketchPadBrushSize)
        bindClickListener(imageView)
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
        val sourcePoint = penPoint ?: return false
        val targetPoint = PointF(sourcePoint.x + x, sourcePoint.y + y).also { penPoint = it }
        canvas.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y, dirtPaint)
        imageView.postInvalidate()
        return true
    }

    private fun handleClick(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                penPoint = PointF(event.x, event.y).apply { canvas.drawPoint(x, y, dirtPaint) }
                true
            }
            MotionEvent.ACTION_MOVE -> strokeToPoint(event.x, event.y)
            MotionEvent.ACTION_UP -> strokeToPoint(event.x, event.y)

            else -> false
        }
    }
    private fun setupClick(imageView: ImageView) = imageView
}