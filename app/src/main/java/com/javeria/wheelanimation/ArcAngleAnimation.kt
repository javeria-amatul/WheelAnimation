package com.javeria.wheelanimation

import android.view.animation.Animation
import android.view.animation.Transformation


class ArcAngleAnimation(private val circleSeekBar: CircleSeekBar, newAngle: Int) : Animation() {

    private val oldAngle: Float
    private val newAngle: Float

    init {
        this.oldAngle = circleSeekBar.getAngle()
        this.newAngle = newAngle.toFloat()
    }

    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
        val angle = 0 + (newAngle - oldAngle) * interpolatedTime
        circleSeekBar.setPosition(angle.toInt())
        circleSeekBar.requestLayout()
    }
}