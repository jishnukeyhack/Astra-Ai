package com.example.aisecretary.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aisecretary.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var micButton: ImageButton

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceInput()
        } else {
            Toast.makeText(
                requireContext(),
                "Voice input requires microphone permission",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Initialize UI components
        messageRecyclerView = view.findViewById(R.id.recyclerViewMessages)
        inputEditText = view.findViewById(R.id.editTextMessage)
        sendButton = view.findViewById(R.id.buttonSend)
        micButton = view.findViewById(R.id.buttonMic)

        setupRecyclerView()
        setupUIListeners()
        observeViewModel()
        setupToolbarMenu()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        messageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupUIListeners() {
        sendButton.setOnClickListener {
            viewModel.sendMessage()
        }

        micButton.setOnClickListener {
            checkMicrophonePermissionAndListen()
        }

        inputEditText.setOnEditorActionListener { _, _, _ ->
            viewModel.sendMessage()
            true
        }

        inputEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onInputChanged(s?.toString() ?: "")
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                messageAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    messageRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentInput.collectLatest { inputText ->
                if (inputEditText.text.toString() != inputText) {
                    inputEditText.setText(inputText)
                    inputEditText.setSelection(inputText.length)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { uiState ->
                updateUIBasedOnState(uiState)
            }
        }
    }

    private fun updateUIBasedOnState(uiState: UiState) {
        when (uiState) {
            is UiState.Listening -> {
                micButton.isEnabled = false
                inputEditText.hint = getString(R.string.voice_input_prompt)
            }
            is UiState.Processing -> {
                micButton.isEnabled = false
                inputEditText.hint = getString(R.string.loading_message)
            }
            is UiState.Error -> {
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.input_hint)
                Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
            }
            is UiState.Ready -> {
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.input_hint)
            }
        }
    }

    private fun checkMicrophonePermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceInput()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    requireContext(),
                    "Voice input requires microphone permission",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceInput() {
        viewModel.startVoiceInput()
    }

    private fun setupToolbarMenu() {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_chat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chat -> {
                clearChat()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearChat() {
        // Show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Conversation")
            .setMessage("Are you sure you want to clear the entire conversation?")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearConversation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopVoiceInput()
    }
}

// Simple TextWatcher interface implementation to reduce boilerplate
abstract class SimpleTextWatcher : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: android.text.Editable?) {}
}