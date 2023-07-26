package com.martin.canvasview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Permanently stores and shows traces of only one pen.
 */
class CanvasView : View, ICanvasView {

    companion object {
        private var TAG = CanvasView::class.java.simpleName
    }

    private val context: Context
    private val paint: Paint
    private var path: Path

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        this.paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5F
        }
        this.path = Path()
        super.setBackgroundColor(Color.WHITE)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currX = event.x
        val currY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                // set the start point of this.path.
                path.moveTo(currX, currY)
            MotionEvent.ACTION_MOVE ->
                // add points into this.path.
                path.lineTo(currX, currY)
            MotionEvent.ACTION_UP -> {}
        }
        // request the view tree to redraw the view, in which onDraw() will be triggered. If the size of the view changed at this time, layout() will also be called.
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        // draw the view.
        canvas.drawPath(path, paint)
    }

    override fun clear() {
        path = Path()
        invalidate()
    }
}