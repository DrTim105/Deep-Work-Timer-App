package com.salihutimothy.deepworktimer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment

private const val TAG = "SettingsDialog"

class SettingsDialog: AppCompatDialogFragment() {
    private lateinit var okButton : Button
    private lateinit var cancelButton : Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.settings_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")

        okButton = view.findViewById(R.id.okButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        super.onViewCreated(view, savedInstanceState)
        okButton.setOnClickListener {
            // saveValues()
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss()}
    }
}