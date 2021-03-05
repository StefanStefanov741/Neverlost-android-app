package com.stefan.neverlost

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment


class ErrorPopup(val ttl:String,val txt:String) : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setTitle(ttl)
                .setMessage(txt)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i -> })
        return builder.create()
    }
}
