package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity() {
    companion object {
        val THEME_KEY = "THEME"
    }

    var isDarkTheme = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDarkTheme = savedInstanceState?.getBoolean(THEME_KEY) ?: false
        updateTheme()

        Log.i("A", if (isDarkTheme) "dark theme" else "light")
        setContentView(R.layout.main_activity)

        updateSwitch()

        themeSwitch.setOnClickListener {
            isDarkTheme = !isDarkTheme
            recreate()
            updateTheme()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(THEME_KEY, isDarkTheme)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isDarkTheme = savedInstanceState.getBoolean(THEME_KEY)
        updateSwitch()
        updateTheme()
    }

    private fun updateSwitch() {
        if (isDarkTheme) {
            themeSwitch.isChecked = isDarkTheme
        }
    }

    private fun updateTheme() {
        if (isDarkTheme) {
            theme.applyStyle(R.style.DarkTheme, true)
//                setTheme(R.style.LightTheme)
        } else {
            theme.applyStyle(R.style.LightTheme, true)
//            setTheme(R.style.DarkTheme)
        }
    }
}