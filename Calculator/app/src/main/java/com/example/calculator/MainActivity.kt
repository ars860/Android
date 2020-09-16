package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.max

enum class ButtonType { BUTTON, CHECKBOX, SWITCH }
enum class KeyboardType { NUMBERS, ACTIONS }

data class ButtonTemplate(
    val text: String,
    val type: ButtonType,
    val keyboardType: KeyboardType = KeyboardType.ACTIONS,
    val customCallback: ((String) -> String)? = null
)

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Calculator"
        const val INPUT_KEY = "INPUT_KEY"
        val buttons: MutableList<ButtonTemplate> = mutableListOf(
            ButtonTemplate("+", ButtonType.BUTTON),
            ButtonTemplate("-", ButtonType.BUTTON),
            ButtonTemplate("*", ButtonType.BUTTON),
            ButtonTemplate("/", ButtonType.BUTTON),
            ButtonTemplate("(", ButtonType.BUTTON),
            ButtonTemplate(")", ButtonType.BUTTON),
            ButtonTemplate(".", ButtonType.BUTTON)
        )

        init {
            for (i in 0..9) {
                buttons.add(
                    ButtonTemplate(
                        i.toString(),
                        ButtonType.BUTTON,
                        KeyboardType.NUMBERS

                    )
                )
            }

            buttons.add(ButtonTemplate("<<", ButtonType.BUTTON) { s: String ->
                if (s.isEmpty())
                    s
                else s.substring(
                    0,
                    s.length - 1
                )
            })
        }

        private const val MAX_INPUT_LENGTH: Int = 50
        private const val MAX_INPUT_TEXT_SIZE_SP: Int = 100
        private const val MIN_INPUT_TEXT_SIZE_SP: Int = 20

        fun updateInputSize(input: TextView) {
            input.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                max(
                    (MAX_INPUT_LENGTH - input.text.length.toFloat()) / MAX_INPUT_LENGTH * MAX_INPUT_TEXT_SIZE_SP,
                    MIN_INPUT_TEXT_SIZE_SP.toFloat()
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setContentView(R.layout.activity_main)

//        if(resources.configuration.orientation == 0) {
//            actionsKeyboard.flexWrap = FlexWrap.NOWRAP
//        }

        for (buttonTemplate in buttons) {
            val button = when (buttonTemplate.type) {
                ButtonType.BUTTON -> {
                    Button(this)
                }
                ButtonType.SWITCH -> {
                    Switch(this)
                }
                ButtonType.CHECKBOX -> {
                    CheckBox(this)
                }
            }

            button.text = buttonTemplate.text
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

            button.layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                flexBasisPercent = if (resources.configuration.orientation == 1) 0.333F else 0.199F
            }

            if (buttonTemplate.customCallback == null) {
                button.setOnClickListener {
                    inputWindow.text = "${inputWindow.text}${button.text}"
                    updateInputSize(inputWindow)
                }
            } else {
                val callback: (String) -> String = buttonTemplate.customCallback
                button.setOnClickListener {
                    inputWindow.text = callback(inputWindow.text.toString())
                    updateInputSize(inputWindow)
                }
            }

            when (buttonTemplate.keyboardType) {
                KeyboardType.NUMBERS -> keyboard.addView(button)
                KeyboardType.ACTIONS -> actionsKeyboard.addView(button)
            }

        }

        runButton.setOnClickListener {
            val result = Expression(inputWindow.text.toString()).calculate()
            inputWindow.text = result.toString()
            updateInputSize(inputWindow)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(TAG, "onSaveInstanceState")

        outState.putString(INPUT_KEY, inputWindow.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.i(TAG, "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
        inputWindow.text = savedInstanceState.getString(INPUT_KEY)
        updateInputSize(inputWindow)
    }
}