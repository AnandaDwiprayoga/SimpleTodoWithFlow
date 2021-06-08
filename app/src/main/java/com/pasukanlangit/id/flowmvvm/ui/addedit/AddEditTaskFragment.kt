package com.pasukanlangit.id.flowmvvm.ui.addedit

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pasukanlangit.id.flowmvvm.R
import com.pasukanlangit.id.flowmvvm.databinding.FragmentAddEditTaskBinding
import com.pasukanlangit.id.flowmvvm.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task){

    private val viewModel : AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            edtName.setText(viewModel.taskName)
            cbImportant.isChecked = viewModel.taskImportant
            cbImportant.jumpDrawablesToCurrentState()
            tvDateCreated.isVisible = viewModel.task != null
            tvDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"

            edtName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            cbImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportant = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when(event){
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.edtName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}