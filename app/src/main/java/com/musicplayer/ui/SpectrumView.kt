package com.musicplayer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.musicplayer.R
import kotlin.random.Random

class SpectrumView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.color_active)
        style = Paint.Style.FILL
    }

    private val barCount = 3
    private val barHeights = FloatArray(barCount) { 0.2f }
    private val animators = mutableListOf<ValueAnimator>()

    private var isAnimating = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val barWidth = width.toFloat() / (barCount * 2 - 1)
        
        for (i in 0 until barCount) {
            val left = i * barWidth * 2
            val top = height * (1 - barHeights[i])
            val right = left + barWidth
            val bottom = height.toFloat()
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    fun startAnimation() {
        if (isAnimating) return
        isAnimating = true
        
        animators.clear()
        for (i in 0 until barCount) {
            val animator = ValueAnimator.ofFloat(0.2f, 0.8f).apply {
                duration = 300L + Random.nextLong(200L)
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    barHeights[i] = it.animatedValue as Float
                    invalidate()
                }
            }
            animator.start()
            animators.add(animator)
        }
    }

    fun stopAnimation() {
        isAnimating = false
        animators.forEach { it.cancel() }
        animators.clear()
        for (i in 0 until barCount) {
            barHeights[i] = 0.2f
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
