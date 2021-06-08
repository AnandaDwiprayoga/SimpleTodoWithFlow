package com.pasukanlangit.id.flowmvvm.ui.tasks

import androidx.lifecycle.*
import com.pasukanlangit.id.flowmvvm.data.PreferencesManager
import com.pasukanlangit.id.flowmvvm.data.SortOrder
import com.pasukanlangit.id.flowmvvm.data.Task
import com.pasukanlangit.id.flowmvvm.data.TaskDao
import com.pasukanlangit.id.flowmvvm.ui.addedit.ADD_TASK_RESULT_OK
import com.pasukanlangit.id.flowmvvm.ui.addedit.EDIT_TASK_RESULT_OK
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel(){
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent get() = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ){ query, preferences ->
        Pair(query, preferences)
    }.flatMapLatest { (query, preferences) ->
        taskDao.getAllTask(query, preferences.sortOrder, preferences.hideCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChange(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.updateTask(task.copy(completed = checked))
    }

    fun onTaskSwiped(task: Task?) = viewModelScope.launch {
        task?.let { taskNotNull ->
            taskDao.delete(taskNotNull)
            taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(taskNotNull))
        }
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insertTask(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(message))
    }

    fun onDeleteAllCompletedTask() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedTask)
    }

    sealed class TaskEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String): TaskEvent()
        object NavigateToAddTaskScreen : TaskEvent()
        object NavigateToDeleteAllCompletedTask : TaskEvent()
    }
}


