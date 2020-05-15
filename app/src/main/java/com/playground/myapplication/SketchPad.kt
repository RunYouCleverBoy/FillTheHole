package com.playground.myapplication

import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import android.widget.ImageView


class SketchPadManager(private val imageView: ImageView, sourceBitmap: Bitmap) {
    private val bitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.RGB_565)
    private val canvas: Canvas = Canvas(bitmap)
    private var penPoint: PointF? = null
    private val dirtPaint:Paint = Paint()

    init {
        dirtPaint.color = Color.RED
        dirtPaint.strokeCap = Paint.Cap.ROUND
        dirtPaint.strokeWidth = imageView.resources.getDimension(R.dimen.sketchPadBrushSize)
        bindClickListener(imageView)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
        imageView.setImageBitmap(bitmap)
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
        val pen = penPoint?:return false
        canvas.drawLine(pen.x, pen.y, x, y, dirtPaint)
        penPoint = PointF(x, y)
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