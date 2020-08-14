package com.reas.tracker.ui.messages

import android.os.Bundle
import android.view.*
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
import com.reas.tracker.service.SMS.SMSBaseObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.HashMap

class MessagesFragment : Fragment() {
    private lateinit var smsRecyclerViewAdapter: com.reas.tracker.ui.messages.SMSRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var data: HashMap<String, ArrayList<SMSBaseObject>>
    private lateinit var jsonFile: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_messsages, container, false)

        jsonFile = File(requireContext().getExternalFilesDir(null).toString() + "/SMS.json")
        data = loadJson()

        // Initialize the toolbar
        val toolbar = root.findViewById<Toolbar>(R.id.toolbarMessages)
        toolbar.title = "Messages"
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

        recyclerView = view.findViewById(R.id.messagesRecyclerView)
        smsRecyclerViewAdapter = SMSRecyclerViewAdapter(requireActivity(), data)
        recyclerView.adapter = smsRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_messages, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadMessages -> {
                // Updates the data with the newest file
                data.clear()
//                data.addAll(loadJson()) TODO
//                updateAdapter()
                return true
            }

            R.id.deleteAllMessages -> {
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

    fun loadJson(): HashMap<String, ArrayList<SMSBaseObject>> {
        // Loads JSON File to ArrayList<SMSObject>
        var temp = HashMap<String, ArrayList<SMSBaseObject>>()

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
            val type = object : TypeToken<HashMap<String, ArrayList<SMSBaseObject>>>() {}.type
            temp = Gson().fromJson<HashMap<String, ArrayList<SMSBaseObject>>>(response, type)
        }
        return temp
    }
}