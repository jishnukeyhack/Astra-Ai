package com.example.aisecretary.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aisecretary.R
import com.example.aisecretary.settings.SettingsManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var settingsManager: SettingsManager
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: MaterialButton
    private lateinit var micButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var gestureDetector: GestureDetectorCompat
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (viewModel.isBackgroundListeningActive()) {
                viewModel.stopBackgroundListening()
                startVoiceInput()
            } else {
                startVoiceInput()
            }
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
        settingsManager = SettingsManager(requireContext())

        // Initialize UI components
        rootLayout = view as ConstraintLayout
        messageRecyclerView = view.findViewById(R.id.recyclerViewMessages)
        inputEditText = view.findViewById(R.id.editTextMessage)
        sendButton = view.findViewById(R.id.buttonSend)
        micButton = view.findViewById(R.id.buttonMic)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        setupRecyclerView()
        setupUIListeners()
        observeViewModel()
        observeSettings()
        setupToolbarMenu()
        setupTripleTapDetection()
        
        // Start background listening automatically when fragment is created if enabled
        if (settingsManager.isWakeWordEnabled()) {
            checkMicrophonePermission { granted ->
                if (granted) {
                    startBackgroundListening()
                }
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsManager.wakeWordEnabled.collectLatest { enabled ->
                if (enabled) {
                    if (!viewModel.isBackgroundListeningActive() && !viewModel.isSpeakingOrProcessing()) {
                        startBackgroundListening()
                    }
                } else {
                    viewModel.stopBackgroundListening()
                }
            }
        }
    }

    private fun setupTripleTapDetection() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return true
            }
            
            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                if (e.actionMasked == MotionEvent.ACTION_UP) {
                    // This is actually the third tap (after a double tap)
                    viewModel.stopSpeaking()
                    Toast.makeText(requireContext(), getString(R.string.speech_stopped), Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })
        
        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Allow the event to be processed by other listeners
        }
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
            viewModel.stopBackgroundListening()
            viewModel.sendMessage()
        }

        micButton.setOnClickListener {
            // When mic button is clicked, switch from background to active listening
            viewModel.stopBackgroundListening()
            checkMicrophonePermissionAndListen()
        }

        inputEditText.setOnEditorActionListener { _, _, _ ->
            viewModel.stopBackgroundListening()
            viewModel.sendMessage()
            true
        }

        inputEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onInputChanged(s?.toString() ?: "")
                // Stop background listening when user starts typing
                if (s?.isNotEmpty() == true && viewModel.isBackgroundListeningActive()) {
                    viewModel.stopBackgroundListening()
                }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.speechEvents.collectLatest { event ->
                handleSpeechEvent(event)
            }
        }
    }

    private fun handleSpeechEvent(event: SpeechEvent) {
        when (event) {
            is SpeechEvent.SpeechEnded -> {
                // Auto-send when speech input ends
                if (inputEditText.text.isNotEmpty()) {
                    viewModel.sendMessage()
                } else {
                    // If nothing was said, go back to background listening if enabled
                    if (settingsManager.isWakeWordEnabled()) {
                        startBackgroundListening()
                    }
                }
            }
            is SpeechEvent.NewMessageReceived -> {
                // Automatically read new messages
                viewModel.readMessage(event.message)
            }
            is SpeechEvent.InitialRequestStarted -> {
                // Show progress for initial long request
                progressIndicator.visibility = View.VISIBLE
                Toast.makeText(
                    requireContext(),
                    getString(R.string.initial_request_message),
                    Toast.LENGTH_LONG
                ).show()
            }
            is SpeechEvent.InitialRequestCompleted -> {
                progressIndicator.visibility = View.GONE
                // Go back to background listening after initial request if enabled
                if (settingsManager.isWakeWordEnabled()) {
                    startBackgroundListening()
                }
            }
            is SpeechEvent.SpeakingCompleted -> {
                // Auto-activate mic after speaking completes
                if (settingsManager.isAutoActivateMicEnabled()) {
                    // Start active voice input instead of just going back to background listening
                    checkMicrophonePermissionAndListen()
                }
            }
            is SpeechEvent.WakeWordDetected -> {
                // Start active listening when wake word is detected
                Toast.makeText(requireContext(), getString(R.string.wake_word_detected), Toast.LENGTH_SHORT).show()
                // Switch from background to active listening
                viewModel.stopBackgroundListening()
                checkMicrophonePermissionAndListen()
            }
        }
    }

    private fun updateUIBasedOnState(uiState: UiState) {
        when (uiState) {
            is UiState.Listening -> {
                micButton.setIconTintResource(R.color.error)
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.voice_input_prompt)
                progressIndicator.visibility = View.GONE
            }
            is UiState.Processing -> {
                micButton.setIconTintResource(R.color.primaryColor)
                micButton.isEnabled = false
                inputEditText.hint = getString(R.string.loading_message)
                if (uiState.isInitialRequest) {
                    progressIndicator.visibility = View.VISIBLE
                }
            }
            is UiState.Error -> {
                micButton.setIconTintResource(R.color.primaryColor)
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.input_hint)
                progressIndicator.visibility = View.GONE
                Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                // Go back to background listening after error if enabled
                if (settingsManager.isWakeWordEnabled()) {
                    startBackgroundListening()
                }
            }
            is UiState.Ready -> {
                micButton.setIconTintResource(R.color.primaryColor)
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.input_hint)
                progressIndicator.visibility = View.GONE
            }
            is UiState.Speaking -> {
                micButton.setIconTintResource(R.color.primaryColor)
                micButton.isEnabled = false
                inputEditText.hint = getString(R.string.speaking)
            }
            is UiState.BackgroundListening -> {
                micButton.setIconTintResource(R.color.secondaryColor)
                micButton.isEnabled = true
                inputEditText.hint = getString(R.string.waiting_for_wake_word)
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
    
    private fun startBackgroundListening() {
        viewModel.startBackgroundListening()
    }

    private fun setupToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_chat, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_clear_chat -> {
                        clearChat()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

    private fun checkMicrophonePermission(onResult: (Boolean) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onResult(true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    requireContext(),
                    "Voice input requires microphone permission",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                onResult(false)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                onResult(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume background listening when the fragment becomes visible again if enabled
        if (settingsManager.isWakeWordEnabled() && 
            !viewModel.isBackgroundListeningActive() && 
            !viewModel.isSpeakingOrProcessing()) {
            startBackgroundListening()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopVoiceInput()
        viewModel.stopSpeaking()
        viewModel.stopBackgroundListening()
        
        // Check if the app is finishing (truly exiting)
        if (activity?.isFinishing == true) {
            // If the app is exiting, unload the model
            viewModel.unloadModel()
        }
    }
}

// Simple TextWatcher interface implementation to reduce boilerplate
abstract class SimpleTextWatcher : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: android.text.Editable?) {}
}