package com.reas.tracker.ui.calls

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.tracker.MainActivity
import com.reas.tracker.R
import com.reas.tracker.service.calls.CallBaseObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.HashMap

class CallsFragment : Fragment() {
    lateinit var callRecyclerViewAdapter: com.reas.tracker.ui.calls.CallRecyclerViewAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var data: HashMap<String, ArrayList<CallBaseObject>>
    lateinit var jsonFile: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_calls, container, false)

        jsonFile = File(requireContext().getExternalFilesDir(null).toString() + "/Call.json")
        data = loadJson()


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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_calls, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadCalls -> {
                // Updates the data with the newest file
                data.clear()
//                data.addAll(loadJson()) TODO
//                updateAdapter()
                return true
            }

            R.id.deleteAllCalls -> {
                jsonFile.delete()
                jsonFile.createNewFile()
                data.clear()
//                updateAdapter()
                Toast.makeText(requireContext(), "Messages deleted", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.callRecyclerView)
        callRecyclerViewAdapter = CallRecyclerViewAdapter(requireActivity(), data)
        recyclerView.adapter = callRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("asd", "onViewCreated: created")
    }

    fun loadJson(): HashMap<String, ArrayList<CallBaseObject>> {
        // Loads JSON File to ArrayList<CallBaseObject>
        var temp = HashMap<String, ArrayList<CallBaseObject>>()

        val fileReader = FileReader(jsonFile)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        val response = stringBuilder.toString()

        if (response != "") {
            val type = object : TypeToken<HashMap<String, ArrayList<CallBaseObject>>>() {}.type
            temp = Gson().fromJson<HashMap<String, ArrayList<CallBaseObject>>>(response, type)
        }
        return temp
    }
}