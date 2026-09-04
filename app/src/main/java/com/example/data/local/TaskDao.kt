package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dateString = :date ORDER BY isCompleted ASC, id ASC")
    fun getTasksForDate(date: String): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isCompleted = 1")
    fun getAllCompletedTasks(): Flow<List<DailyTask>>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE dateString = :date AND isCompleted = 1")
    fun getCompletedTasksCount(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE dateString = :date")
    fun getTotalTasksCount(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Delete
    suspend fun deleteTask(task: DailyTask)

    @Query("UPDATE daily_tasks SET isCompleted = :isCompleted, currentCount = :currentCount WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean, currentCount: Int)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
