package com.reas.settings

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlin.IllegalStateException

class LoadingDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                val inflater = this.layoutInflater

                builder.setView(inflater.inflate(R.layout.alertdialog, null))

                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
    }
}