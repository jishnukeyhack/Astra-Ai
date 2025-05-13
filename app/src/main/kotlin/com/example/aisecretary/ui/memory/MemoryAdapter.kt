package com.example.aisecretary.ui.memory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aisecretary.R
import com.example.aisecretary.data.model.MemoryFact
import java.text.SimpleDateFormat
import java.util.*

class MemoryAdapter(private val listener: MemoryItemListener) : 
    ListAdapter<MemoryFact, MemoryAdapter.MemoryViewHolder>(MemoryDiffCallback()) {
    
    interface MemoryItemListener {
        fun onEditMemory(id: Long, key: String, value: String)
        fun onDeleteMemory(id: Long)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view, listener)
    }
    
    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memoryFact = getItem(position)
        holder.bind(memoryFact)
    }
    
    class MemoryViewHolder(
        itemView: View, 
        private val listener: MemoryItemListener
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val textViewKey: TextView = itemView.findViewById(R.id.textViewKey)
        private val textViewValue: TextView = itemView.findViewById(R.id.textViewValue)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        
        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        
        fun bind(memoryFact: MemoryFact) {
            textViewKey.text = memoryFact.key
            textViewValue.text = memoryFact.value
            textViewDate.text = dateFormat.format(Date(memoryFact.timestamp))
            
            buttonEdit.setOnClickListener {
                listener.onEditMemory(memoryFact.id, memoryFact.key, memoryFact.value)
            }
            
            buttonDelete.setOnClickListener {
                listener.onDeleteMemory(memoryFact.id)
            }
        }
    }
    
    class MemoryDiffCallback : DiffUtil.ItemCallback<MemoryFact>() {
        override fun areItemsTheSame(oldItem: MemoryFact, newItem: MemoryFact): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MemoryFact, newItem: MemoryFact): Boolean {
            return oldItem == newItem
        }
    }
}