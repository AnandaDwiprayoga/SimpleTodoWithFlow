package com.pasukanlangit.id.flowmvvm.ui.addedit

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pasukanlangit.id.flowmvvm.data.Task
import com.pasukanlangit.id.flowmvvm.data.TaskDao
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel(){

    val task = state.get<Task>("task")
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value){
            field = value
            state.set("taskName", value)
        }

    var taskImportant = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value){
            field = value
            state.set("taskName", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()



    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if(task != null){
            val updatedTask = task.copy(name = taskName, important = taskImportant)
            updateTask(updatedTask)
        }else{
            val newTask = Task(name = taskName, important = taskImportant)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.updateTask(updatedTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insertTask(newTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()

    }
}

const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1