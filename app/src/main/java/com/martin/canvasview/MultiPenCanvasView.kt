package com.martin.canvasview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Permanently stores and shows traces of multiple pens.
 */
class MultiPenCanvasView : View, ICanvasView {

    companion object {
        private var TAG = MultiPenCanvasView::class.java.simpleName
    }

    /**
     * A segment of continuous path.
     */
    data class Segment(
        val pointerId: Int,
        val path: Path
    )

    private val context: Context
    private val paint: Paint

    // keeps all the history drawn path. a continuous path is recognized as a segment.
    private val segmentList: MutableList<Segment> = mutableListOf()

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
        super.setBackgroundColor(Color.WHITE)
    }

    /**
     * when a finger leaves screen, in the pointerIndex list, its pointerIndex will be removed. and the other pointerIndexes after it will all move left to fill the blank slot.
     *
     * For example, there are currently 3 fingers touching screen (by time sequence):
     * pointerIds:          0, 1, 2
     *                      |  |  |
     * pointerIndexes:      0, 1, 2
     *
     * AFTER the second finger leaves screen:
     * pointerIds:          0, 1, 2
     *                      |    /
     * pointerIndexes:      0, 1
     * for the pointerId 1, its pointerIndex is invalid (-1).
     *
     * then AFTER a new finger (no matter it is the previously left one or not) touches screen:
     * pointerIds:          0, 1, 2
     *                      |  |  |
     * pointerIndexes:      0, 1, 2
     *
     * By the way, in android, each pointerId represents a touching finger.
     * A touching finger <--> a valid pointerId <--> the pointerIndex of the pointerId is valid (>= 0).
     * In segmentList, there are usually many segments which pointerIds are the same. But only the last one of them should be consumed.
     * The word "consume" means: the segment of the pointerId calls lineTo(x, y) to update its "path" field.
     * The later the segments are added, the more possible the segment's pointerId is valid.
     * Query from end to start, since the recently-added segments are always near the tail, while the earlier-added segments are always near the head.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            // a new finger down, means a start of a new [Segment].
            // no matter how close the distance from a pointerId's current point to the its last point is, the current point will always be a start of new [Segment].
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                val segment = Segment(
                    pointerId = pointerId,
                    path = Path().apply { moveTo(x, y) },
                )
                segmentList.add(segment)
                Log.d(
                    TAG,
                    "add a new segment, pointerId = $pointerId, pointerIndex = $pointerIndex, pointerCount = ${event.pointerCount}"
                )
            }
            // triggered when any finger moves.
            MotionEvent.ACTION_MOVE -> {
                val consumedPointerIds: MutableList<Int> = mutableListOf()
                // find the current drawing segment(s) and add points into its path.
                segmentList.reversed().forEach {
                    val pointerIndex = event.findPointerIndex(it.pointerId)
                    if (pointerIndex >= 0 && it.pointerId !in consumedPointerIds) {
                        val x = event.getX(pointerIndex)
                        val y = event.getY(pointerIndex)
                        it.path.lineTo(x, y)
                        consumedPointerIds.add(it.pointerId)
                    } else { // pointerId has been either invalidated or consumed.
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                Log.d(
                    TAG,
                    "pointer up, pointerId = $pointerId, pointerIndex = $pointerIndex, pointerCount = ${event.pointerCount}"
                )
            }
        }
        // request the view tree to redraw the view, in which onDraw() will be triggered. If the size of the view changed at this time, layout() will also be called.
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        segmentList.forEach {
            canvas.drawPath(it.path, paint)
        }
    }

    override fun clear() {
        segmentList.clear()
        invalidate()
    }
}