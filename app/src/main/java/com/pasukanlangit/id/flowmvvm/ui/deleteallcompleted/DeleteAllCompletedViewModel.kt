package com.pasukanlangit.id.flowmvvm.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import com.pasukanlangit.id.flowmvvm.data.TaskDao
import com.pasukanlangit.id.flowmvvm.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel(){
    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTask()
    }
}