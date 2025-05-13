package com.example.aisecretary.ui.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aisecretary.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MemoryFragment : Fragment(), MemoryAdapter.MemoryItemListener {

    private lateinit var viewModel: MemoryViewModel
    private lateinit var memoryAdapter: MemoryAdapter
    
    private lateinit var recyclerViewMemories: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var textViewEmptyState: TextView
    private lateinit var buttonAddMemory: Button
    private lateinit var buttonClearMemories: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[MemoryViewModel::class.java]
        
        // Initialize views
        recyclerViewMemories = view.findViewById(R.id.recyclerViewMemories)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState)
        buttonAddMemory = view.findViewById(R.id.buttonAddMemory)
        buttonClearMemories = view.findViewById(R.id.buttonClearMemories)
        
        // Setup recycler view
        memoryAdapter = MemoryAdapter(this)
        recyclerViewMemories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memoryAdapter
        }
        
        // Setup UI listeners
        setupListeners()
        
        // Observe view model
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Search functionality
        editTextSearch.setOnEditorActionListener { _, _, _ ->
            viewModel.searchMemories(editTextSearch.text.toString())
            true
        }
        
        // Add memory button
        buttonAddMemory.setOnClickListener {
            showAddMemoryDialog()
        }
        
        // Clear all memories button
        buttonClearMemories.setOnClickListener {
            showClearConfirmationDialog()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.memories.collectLatest { memories ->
                memoryAdapter.submitList(memories)
                
                // Show empty state if no memories
                if (memories.isEmpty()) {
                    textViewEmptyState.visibility = View.VISIBLE
                } else {
                    textViewEmptyState.visibility = View.GONE
                }
            }
        }
    }
    
    private fun showAddMemoryDialog(memoryId: Long = 0, key: String = "", value: String = "") {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_memory, null)
        val editTextKey = dialogView.findViewById<EditText>(R.id.editTextKey)
        val editTextValue = dialogView.findViewById<EditText>(R.id.editTextValue)
        
        // Pre-fill if editing existing memory
        if (key.isNotEmpty()) {
            editTextKey.setText(key)
            editTextValue.setText(value)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(if (memoryId == 0L) "Add Memory" else "Edit Memory")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val keyText = editTextKey.text.toString()
                val valueText = editTextValue.text.toString()
                viewModel.addOrUpdateMemory(keyText, valueText, memoryId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Memories")
            .setMessage("Are you sure you want to delete all memories? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearAllMemories()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onEditMemory(id: Long, key: String, value: String) {
        showAddMemoryDialog(id, key, value)
    }
    
    override fun onDeleteMemory(id: Long) {
        viewModel.deleteMemory(id)
    }
}