package com.example.fragments.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fragments.R
import com.example.fragments.extensions.navigate
import kotlinx.android.synthetic.main.fragment_button.*
import kotlin.concurrent.fixedRateTimer

private const val ARG_NUMBER = "number"

class ButtonFragment : Fragment() {
    private var number: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            number = it.getInt(ARG_NUMBER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = number.toString()
        button.setOnClickListener{
            navigate(ButtonFragmentDirections.actionButtonFragmentSelf(number + 1))
        }
    }
}