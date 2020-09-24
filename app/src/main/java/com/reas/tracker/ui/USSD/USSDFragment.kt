package com.reas.tracker.ui.USSD

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.tracker.MainActivity
import com.reas.tracker.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class USSDFragment : Fragment() {
    lateinit var USSDRecyclerViewAdapter: USSDRecyclerViewAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var data: HashMap<String, String>
    lateinit var jsonFile: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        jsonFile = File(requireContext().getExternalFilesDir(null).toString() + "/USSDResponse.json")

        val root = inflater.inflate(R.layout.fragment_ussd, container, false)
        data = loadJson()


        // Initialize the toolbar
        val toolbar = root.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarUSSD)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.ussdRecyclerView)
        USSDRecyclerViewAdapter = USSDRecyclerViewAdapter(requireActivity(), data)
        recyclerView.adapter = USSDRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_ussd, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadUSSD -> {
                // Updates the data with the newest file
                data.clear()
//                data.addAll(loadJson())
                updateAdapter()
                return true
            }
            R.id.deleteAllUSSD -> {
                jsonFile.delete()
                jsonFile.createNewFile()
                data.clear()
                updateAdapter()
                Toast.makeText(requireContext(), "USSD Logs deleted", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun loadJson(): HashMap<String, String> {
        var temp = HashMap<String, String>()

        // Loads JSON File to ArrayList<Array<String>>
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
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            temp = Gson().fromJson<HashMap<String, String>>(response, type)
        }
        return temp
    }

    fun updateAdapter() {
        USSDRecyclerViewAdapter.notifyDataSetChanged()
    }
}