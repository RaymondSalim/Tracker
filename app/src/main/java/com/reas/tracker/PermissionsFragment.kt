package com.reas.tracker

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.reas.tracker.service.USSD.USSDService
import kotlinx.android.synthetic.main.fragment_permissions.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
val permissions = arrayOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.MANAGE_OWN_CALLS
)

/**
 * A simple [Fragment] subclass.
 * Use the [PermissionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PermissionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()

        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_permissions, container, false)


        with (view) {
            // Updates the checklist drawable for current permission settings
            checkPerms(this)

            requestPermissions.setOnClickListener {
                (context as MainActivity).requestPerms(permissions)
                checkPerms(view)
            }

            openSettings.setOnClickListener {
                val i = with (Intent()) {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
                context.startActivity(i)
            }

            requestRoles.setOnClickListener { (context as MainActivity).requestRole() }
            openAccessibility.setOnClickListener {
                val i = with (Intent()) {
                    action = Settings.ACTION_ACCESSIBILITY_SETTINGS
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(i)
            }


            permission1.text = permissions[0]
            permission2.text = permissions[1]
            permission3.text = permissions[2]
            permission4.text = permissions[3]
            permission5.text = permissions[4]
            permission6.text = permissions[5]
            permission7.text = permissions[6]
            permission8.text = permissions[7]
            permission9.text = permissions[8]

            if (isAccessibilityServiceEnabled(requireContext(), USSDService::class.java )) {
                permission10.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

        }


        // Initialize the toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbarPerms)
        setHasOptionsMenu(true)

        // Adds the navigationview button (three lines)
        with (context as AppCompatActivity) {
            setSupportActionBar(toolbar)
            setupActionBarWithNavController(
                (requireContext() as MainActivity).navController,
                (requireContext() as MainActivity).appBarConfiguration
            )
        }

        return view
    }

    fun checkPerms(view: View) {
        with (view) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[0]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                view.permission1.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission2.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[2]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission3.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[3]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission4.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[4]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission5.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[5]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission6.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[6]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission7.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[7]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission8.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[8]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permission9.setCheckMarkDrawable(R.drawable.ic_baseline_check_24)
            }
        }
    }


    fun isAccessibilityServiceEnabled(
        context: Context,
        accessibilityService: Class<*>?
    ): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityService!!)
        val enabledServicesSetting: String = Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
            ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService =
                ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) return true
        }
        return false
    }
}