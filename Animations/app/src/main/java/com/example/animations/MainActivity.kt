package com.example.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val scaleAnimator = ObjectAnimator.ofFloat(textView, "scaleX", 2f).apply {
            repeatCount = Animation.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val rotateAnimator = ObjectAnimator.ofFloat(textView, "rotation", 360f).apply {
            repeatCount = Animation.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        AnimatorSet().apply {
            playTogether(scaleAnimator, rotateAnimator)
            duration = 3000
            start()
        }
    }
}