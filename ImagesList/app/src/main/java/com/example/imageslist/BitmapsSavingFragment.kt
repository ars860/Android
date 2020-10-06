package com.example.imageslist

import android.os.Bundle
import androidx.fragment.app.Fragment

class BitmapsSavingFragment : Fragment() {
    val rowsSaved: MutableList<ImageRow> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun saveToFragment(rows: MutableList<ImageRow>) {
        rowsSaved.clear()
        rowsSaved.addAll(rows)
    }

    fun restoreFromFragment(rows: MutableList<ImageRow>) {
        rows.addAll(rowsSaved)
    }
}