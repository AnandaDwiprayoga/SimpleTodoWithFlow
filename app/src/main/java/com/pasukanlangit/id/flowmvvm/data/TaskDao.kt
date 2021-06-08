package com.pasukanlangit.id.flowmvvm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTask()

    fun getAllTask(searchQuery: String, sortOrder: SortOrder, isHideComplete: Boolean) : Flow<List<Task>> =
        when(sortOrder){
            SortOrder.BY_DATE -> getTaskSortedByDateCreated(searchQuery,isHideComplete)
            SortOrder.BY_NAME -> getTaskSortedByName(searchQuery,isHideComplete)
        }

    @Query("SELECT * FROM task_table WHERE (completed != :isHideComplete OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTaskSortedByName(searchQuery: String, isHideComplete: Boolean) : Flow<List<Task>>
    //becuase flow is async you don't need to add suspend fun

    @Query("SELECT * FROM task_table WHERE (completed != :isHideComplete OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTaskSortedByDateCreated(searchQuery: String, isHideComplete: Boolean) : Flow<List<Task>>
}