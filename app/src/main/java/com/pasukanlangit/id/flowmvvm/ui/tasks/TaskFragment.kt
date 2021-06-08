package com.pasukanlangit.id.flowmvvm.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pasukanlangit.id.flowmvvm.R
import com.pasukanlangit.id.flowmvvm.data.SortOrder
import com.pasukanlangit.id.flowmvvm.data.Task
import com.pasukanlangit.id.flowmvvm.databinding.FragmentTaskBinding
import com.pasukanlangit.id.flowmvvm.utils.exhaustive
import com.pasukanlangit.id.flowmvvm.utils.onQueryTextChange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task), TaskAdapter.OnItemClickListener {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //because layout is already inflated in constructor, just use bind method to the view
        val binding = FragmentTaskBinding.bind(view)
        val taskAdapter = TaskAdapter(this)

        binding.apply {
            rvViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                  return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(rvViewTasks)

            fabAddTasks.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when(event){
                    is TaskViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO"){
                                viewModel.onUndoDeleteClick(event.task)
                            }
                            .show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(title = "New Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(event.task, "Edit Task")
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToDeleteAllCompletedTask -> {
                        val action = TaskFragmentDirections.actionGlobalDeleteAllCompletedFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)

        setFragmentResultListener("add_edit_request"){_,bundle ->
            val result = bundle.getInt("add_edit_request")
            viewModel.onAddEditResult(result)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment,menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if(pendingQuery != null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }
        searchView.onQueryTextChange {
            viewModel.searchQuery.value = it
        }

        //setting setting to default
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed).isChecked = viewModel.preferencesFlow.first().hideCompleted
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
       viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
       viewModel.onTaskCheckedChange(task,isChecked)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }
}


