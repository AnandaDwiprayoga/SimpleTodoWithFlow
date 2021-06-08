package com.pasukanlangit.id.flowmvvm.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pasukanlangit.id.flowmvvm.data.Task
import com.pasukanlangit.id.flowmvvm.databinding.ItemTaskBinding

class TaskAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Task, TaskAdapter.TaskViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Task>(){
            override fun areItemsTheSame(oldItem: Task, newItem: Task)
                = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Task, newItem: Task)
                = oldItem == newItem

        }
    }
    
    inner class TaskViewHolder(private val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply { 
                root.setOnClickListener { 
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                
                cbCompleted.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, cbCompleted.isChecked)
                    }
                }
            }
        }
        fun bind(task: Task){
            binding.apply {
                cbCompleted.isChecked = task.completed
                tvName.text = task.name
                tvName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
       val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
       return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
       val currentItem = getItem(position)
       holder.bind(currentItem)
    }
    
    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

}