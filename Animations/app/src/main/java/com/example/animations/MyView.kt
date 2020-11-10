package com.example.animations

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val paint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
        textSize = 40.0F
        textAlign = Paint.Align.CENTER
    }
    private var lineAngle = 0.0F
    private val catImage = AppCompatResources.getDrawable(context, R.drawable.cat_in_the_jar)
    private var speed = 1f

    init {
        val a: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.MyView, defStyleAttr, defStyleRes
        )
        try {
            speed = a.getFloat(R.styleable.MyView_speed, 1f)
        } finally {
            a.recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        lineAngle += 0.01F * speed
        if (lineAngle >= 2 * PI) {
            lineAngle = 0.0F
        }

        val x = width.toFloat()
        val y = height.toFloat()
        val radius = 200.0F + 50 * (-(lineAngle / PI - 1) * (lineAngle / PI - 1) + 1)
        val lineLength = 200.0F + 100 * (sin(lineAngle * 5))
        val catRadius = 400.0F

        paint.color = Color.WHITE
        canvas?.drawPaint(paint)

        paint.color = Color.RED
        canvas?.drawCircle(x / 2, y / 2, radius.toFloat(), paint)
        paint.color = Color.BLUE

        canvas?.drawLine(
            x / 2,
            y / 2,
            x / 2 + lineLength * sin(lineAngle),
            y / 2 + lineLength * cos(lineAngle),
            paint
        )

        paint.color = Color.BLACK

        canvas?.drawText(String.format("%.4f", lineAngle), x / 2, y / 2, paint)

        canvas!!.save()
        canvas.rotate((lineAngle * 2 / (2 * PI) * 360).toFloat(), x / 2, y / 2)

        catImage!!.setBounds(
            (x / 2 + catRadius * sin(lineAngle) - catImage.intrinsicWidth / 6).toInt(),
            (y / 2 + catRadius * cos(lineAngle) - catImage.intrinsicHeight / 6).toInt(),
            (x / 2 + catRadius * sin(lineAngle) + catImage.intrinsicWidth / 6).toInt(),
            (y / 2 + catRadius * cos(lineAngle) + catImage.intrinsicHeight / 6).toInt()
        )

        catImage.draw(canvas)
        canvas.restore()

        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return State(super.onSaveInstanceState(), lineAngle)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        lineAngle = (state as State).lineAngle
    }

    class State(source: Parcelable?, var lineAngle: Float) : BaseSavedState(source) {

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out!!.writeFloat(lineAngle)
        }
    }
}