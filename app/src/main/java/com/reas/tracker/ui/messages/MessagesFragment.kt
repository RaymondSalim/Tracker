package com.reas.tracker.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.MainActivity
import com.reas.tracker.MessagesViewModel
import com.reas.tracker.R
import com.reas.tracker.service.SMS.SMSBaseObject
import java.io.File
import java.util.*

class MessagesFragment : Fragment() {
    private lateinit var smsRecyclerViewAdapter: SMSRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private var data: SortedMap<String, SMSBaseObject>? = null
    private lateinit var jsonFile: File

    private val model: MessagesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        val time = Calendar.getInstance().timeInMillis
        super.onCreate(savedInstanceState)
        data = model.liveSortedData.value
        val timeEnd = Calendar.getInstance().timeInMillis
        Log.d("load", "onCreate: ${timeEnd - time}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val time = Calendar.getInstance().timeInMillis

        val root = inflater.inflate(R.layout.fragment_messsages, container, false)

//        val viewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
//
//        viewModel.liveData.observe(viewLifecycleOwner, Observer {  })


//        model.liveSortedData.observe(
//            viewLifecycleOwner,
//            Observer<SortedMap<String, SMSBaseObject>> {
//                Log.d("TAG", "onCreateView: data changed")
//                data = it
//            })



//        jsonFile = File(requireContext().getExternalFilesDir(null).toString() + "/SMS.json")
//        data = loadJson()

        // Initialize the toolbar
        val toolbar = root.findViewById<Toolbar>(R.id.toolbarMessages)
        toolbar.title = "Messages"
        setHasOptionsMenu(true)

        // Adds the navigationview button (three lines)
        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            setupActionBarWithNavController(
                (requireContext() as MainActivity).navController,
                (requireContext() as MainActivity).appBarConfiguration
            )
        }

        val timeEnd = Calendar.getInstance().timeInMillis
        Log.d("load", "onCreateView: ${timeEnd - time}")
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val time = Calendar.getInstance().timeInMillis
        recyclerView = view.findViewById(R.id.messagesRecyclerView)
        smsRecyclerViewAdapter = SMSRecyclerViewAdapter(requireActivity(), data!!)
        recyclerView.adapter = smsRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val timeEnd = Calendar.getInstance().timeInMillis
        Log.d("load", "onViewCreated: ${timeEnd - time}")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_messages, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadMessages -> {
                // Updates the data with the newest file
                data?.clear()
//                data.addAll(loadJson()) TODO
//                updateAdapter()
                return true
            }

            R.id.deleteAllMessages -> {
                jsonFile.delete()
                jsonFile.createNewFile()
                data?.clear()
//                updateAdapter()
                Toast.makeText(requireContext(), "Messages deleted", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


}
