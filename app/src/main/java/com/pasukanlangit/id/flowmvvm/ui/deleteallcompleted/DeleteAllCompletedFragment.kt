package com.pasukanlangit.id.flowmvvm.ui.deleteallcompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedFragment: DialogFragment() {

    private val viewModel: DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you really want to delete all completed task?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") {_,_ ->
                viewModel.onConfirmClick()
            }
            .create()
}