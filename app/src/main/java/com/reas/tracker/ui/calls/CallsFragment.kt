package com.reas.tracker.ui.calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.reas.tracker.MainActivity
import com.reas.tracker.R

class CallsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_calls, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)


        // Initialize the toolbar
        val toolbar = root.findViewById<Toolbar>(R.id.toolbarCalls)
        setHasOptionsMenu(true)

        // Adds the navigationview button (three lines)
        with (activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            setupActionBarWithNavController(
                (requireContext() as MainActivity).navController,
                (requireContext() as MainActivity).appBarConfiguration
            )
        }

        return root
    }
}