package com.example.mycampuscomp

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIStudyAssistantActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView

    private var thinkingMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ai_study_assistant)

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatContainer = findViewById(R.id.chatContainer)
        chatScrollView = findViewById(R.id.chatScrollView)

        // Show welcome message
        showWelcomeMessage()

        // Send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Keyboard support
        messageInput.setOnEditorActionListener { _, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN)
            ) {

                sendMessage()
                true

            } else {
                false
            }
        }
    }

    private fun showWelcomeMessage() {

        addTutorMessage(
            "Hello! I'm your Campus Assistant. " +
                    "How can I help you today?"
        )
    }

    private fun sendMessage() {

        val message = messageInput.text.toString().trim()

        // Do nothing if message is empty
        if (message.isEmpty()) {
            return
        }

        // Add student's message
        addStudentMessage(message)

        // Clear input
        messageInput.text.clear()

        // Disable sending while AI is responding
        sendButton.isEnabled = false
        messageInput.isEnabled = false

        // Show Thinking...
        showThinkingMessage()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val request = AIRequest(message)

                val response =
                    RetrofitClient.aiService.askAI(request)

                withContext(Dispatchers.Main) {

                    removeThinkingMessage()

                    if (response.isSuccessful &&
                        response.body() != null
                    ) {

                        val aiMessage =
                            response.body()!!.response

                        addTutorMessage(aiMessage)

                    } else {

                        addTutorMessage(
                            "Sorry, I couldn't process your request right now. " +
                                    "Please try again."
                        )

                        showErrorToast(
                            "Server error: ${response.code()}"
                        )
                    }

                    enableChat()

                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    removeThinkingMessage()

                    addTutorMessage(
                        "I'm having trouble connecting to the Campus Assistant. " +
                                "Please check your connection and try again."
                    )

                    showErrorToast(
                        "Connection error"
                    )

                    enableChat()
                }
            }
        }
    }

    // --------------------------------
    // THINKING MESSAGE
    // --------------------------------

    private fun showThinkingMessage() {

        val textView = TextView(this)

        textView.text = "Thinking..."
        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.secondary_text
            )
        )

        textView.textSize = resources.getDimension(
            R.dimen.ai_message_text_size
        )

        textView.setPadding(
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_ai_chat_bubble
            )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams = params

        chatContainer.addView(textView)

        thinkingMessage = textView

        scrollToBottom()
    }

    private fun removeThinkingMessage() {

        thinkingMessage?.let {

            chatContainer.removeView(it)

        }

        thinkingMessage = null
    }

    // --------------------------------
    // STUDENT MESSAGE
    // --------------------------------

    private fun addStudentMessage(message: String) {

        val textView = TextView(this)

        textView.text = message

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.student_bubble_text
            )
        )

        textView.textSize = resources.getDimension(
            R.dimen.ai_message_text_size
        )

        textView.setPadding(
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_student_chat_bubble
            )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.gravity = Gravity.END

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams = params

        chatContainer.addView(textView)

        scrollToBottom()
    }

    // --------------------------------
    // AI MESSAGE
    // --------------------------------

    private fun addTutorMessage(message: String) {

        val label = TextView(this)

        label.text = getString(
            R.string.ai_tutor_label
        )

        label.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.primary_text
            )
        )

        label.textSize = resources.getDimension(
            R.dimen.ai_label_text_size
        )

        label.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val labelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        labelParams.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_label_margin
            )

        label.layoutParams = labelParams

        chatContainer.addView(label)

        val textView = TextView(this)

        textView.text = message

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.primary_text
            )
        )

        textView.textSize = resources.getDimension(
            R.dimen.ai_message_text_size
        )

        textView.setPadding(
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            ),
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_ai_chat_bubble
            )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams = params

        chatContainer.addView(textView)

        scrollToBottom()
    }

    // --------------------------------
    // ENABLE CHAT
    // --------------------------------

    private fun enableChat() {

        sendButton.isEnabled = true
        messageInput.isEnabled = true

        messageInput.requestFocus()
    }

    // --------------------------------
    // ERROR MESSAGE
    // --------------------------------

    private fun showErrorToast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // --------------------------------
    // SCROLL TO BOTTOM
    // --------------------------------

    private fun scrollToBottom() {

        chatScrollView.post {

            chatScrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }
}