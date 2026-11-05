package com.example.mycampuscomp

import android.os.Bundle
import android.util.Log
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
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.repository.AssignmentRepository
import com.example.mycampuscomp.repository.TimetableRepository
import com.example.mycampuscomp.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIStudyAssistantActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView

    private var thinkingMessage: TextView? = null

    private val assignmentRepository by lazy {
        AssignmentRepository(
            AppDatabase.getDatabase(applicationContext).assignmentDao()
        )
    }

    private val timetableRepository by lazy {
        TimetableRepository(
            AppDatabase.getDatabase(applicationContext).timetableDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ai_study_assistant)

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatContainer = findViewById(R.id.chatContainer)
        chatScrollView = findViewById(R.id.chatScrollView)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        showWelcomeMessage()

        sendButton.setOnClickListener {
            sendMessage()
        }

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

    // ============================================================
    // WELCOME MESSAGE
    // ============================================================

    private fun showWelcomeMessage() {
        addTutorMessage(getWelcomeMessageForLanguage())
    }

    private fun getWelcomeMessageForLanguage(): String {
        return when (LanguageManager.currentTag()) {
            "xh" -> "Molo! Mna ndingumncedisi wakho wasekampasi.\n\nNdingakunceda nge:\n• Izabelo\n• Iiklasi\n• Ishedyuli neentsuku ezilindelekileyo\n• Ukucwangcisa izifundo\n\nZama ukundibuza:\n\"Ziziphi izabelo endinazo?\"\n\nokanye\n\n\"Ziziphi iiklasi endinazo namhlanje?\""
            "zu" -> "Sawubona! Ngingumsizi wakho wasekhampasi.\n\nNgingakusiza nge:\n• Izabelo\n• Amakilasi\n• Isikhathi nezinsuku ezinqunyiwe\n• Ukuhlela ukufunda\n\nZama ukungibuza:\n\"Ngineziphi izabelo?\"\n\nnoma\n\n\"Amakilasi amaphi enginawo namhlanje?\""
            "af" -> "Hallo! Ek is jou Kampusassistent.\n\nEk kan jou help met:\n• Opdragte\n• Klasse\n• Rooster en keerdatums\n• Studiebeplanning\n\nProbeer my vra:\n\"Watter opdragte het ek?\"\n\nof\n\n\"Watter klasse het ek vandag?\""
            "st" -> "Lumela! Ke mothusi oa hau oa khampase.\n\nNka u meletsa ka:\n• Ditshebeletso\n• Ditlelase\n• Senako le matsatsi a ho qetela\n• Morero oa ho ithuta\n\nLeka ho botsa:\n\"Ke na le ditshebeletso dife?\"\n\nkapa\n\n\"Ke na le ditlelase dife kajeno?\""
            "tn" -> "Dumela! Ke mothusi wa gago wa khampase.\n\nNka go thusha ka:\n• Ditiro\n• Ditlelase\n• Thulaganyo le matlha a bokhutlo\n• Togamaano ya go ithuta\n\nLeka go mpotsa:\n\"Ke na le ditiro dife?\"\n\nkgotsa\n\n\"Ke na le ditlelase dife gompieno?\""
            "nso" -> "Dumela! Ke mothuši wa gago wa khampase.\n\nNka go thuša ka:\n• Mešomo\n• Ditlelase\n• Tšhupametši le matšatši a mafelelo\n• Leanothuto\n\nLeka go mpotšiša:\n\"Ke na le mešomo efe?\"\n\ngoba\n\n\"Ke na le ditlelase dife hlale?\""
            "ss" -> "Sawubona! Ngimtfusi wakho wasekhamphasini.\n\nNgimfuna kukusiza nge:\n• Tifundvo\n• Tinhlelonto netinsuku tekugcina\n• Kuhlela kufundza\n\nTsample kumbingata:\n\"Ngitiphi tifundvo enginatfo?\"\n\nnoma\n\n\"Ngitiphi tinhlelonto enginatfo namuhla?\""
            "ts" -> "Avuxeni! Hi mina mupfuni wa wena wa khampasi.\n\nNdingaku pfuna hi:\n• Swiavelo\n• Ticlass\n• Xhedyuli na masiku yo hetelela\n• Pulani ya dyondzo\n\nRingeta ku vutisa:\n\"I swiavelo swihi leswi ndzi nga na swona?\"\n\nkumbe\n\n\"I ticlass tihi leswi ndzi nga na swona namuntlha?\""
            "ve" -> "Ndaa! Ndi muthusi wau wa khamphasi.\n\nNdi nga u thusa nga:\n• Mushumo\n• Kilasi\n• Tshededzo na maduvha a u fhedzisela\n• Pulane ya u guda\n\nLinga u mbudzisa:\n\"Ndi na mushumo ufhio?\"\n\nkana\n\n\"Ndi na kilasi dzifhio namusi?\""
            "nr" -> "Lotjhani! Ngingumsizi wakho wasekhamphasini.\n\nNgingakusiza nge:\n• Imisebenzi\n• Izifundo\n• Isikhathi samakilasi\n• Ukuhlela ukufunda\n\nRinga ukumbuza:\n\"Ngineyiphi imisebenzi?\"\n\nnamhana\n\n\"Ngineyiphi izifundo namhla?\""
            else -> "Hello! I'm your Campus Assistant.\n\nI can help you with:\n• Assignments\n• Classes\n• Your timetable\n• Due dates\n• Study planning\n• Your MyCampusComp information\n\nTry asking me:\n\"What assignments do I have?\"\n\nor\n\n\"What classes do I have today?\""
        }
    }

    // ============================================================
    // STUDENT DATA CONTEXT
    // ============================================================

    private suspend fun buildStudentContext(): String {
        val assignmentList: List<Assignment> =
            assignmentRepository.getAssignmentsLocal().firstOrNull() ?: emptyList()

        val timetableList: List<TimetableClass> =
            timetableRepository.getAllClassesLocal().firstOrNull() ?: emptyList()

        val builder = StringBuilder()

        builder.appendLine("ASSIGNMENTS:")
        if (assignmentList.isEmpty()) {
            builder.appendLine("No assignments are currently stored.")
        } else {
            assignmentList.forEach { assignment ->
                builder.appendLine(
                    "- ${assignment.title} | Due: ${assignment.dueDate} | Status: ${assignment.status}"
                )
            }
        }

        builder.appendLine()
        builder.appendLine("CLASSES / TIMETABLE:")
        if (timetableList.isEmpty()) {
            builder.appendLine("No classes are currently stored.")
        } else {
            timetableList.forEach { timetableClass ->
                builder.appendLine(
                    "- ${timetableClass.subject} | ${timetableClass.day} | ${timetableClass.time} | Room: ${timetableClass.room} | Type: ${timetableClass.type}"
                )
            }
        }

        return builder.toString()
    }

    private fun buildPrompt(userQuestion: String, studentContext: String): String {
        val currentTag = LanguageManager.currentTag()
        val languageName = LanguageManager.displayName(currentTag, "English")

        return """
            SYSTEM INSTRUCTION:
            You are the Campus Assistant inside MyCampusComp powered by OpenRouter GPT-4o.
            You are a helpful university student assistant.

            IMPORTANT RULES:
            1. Respond to the student in $languageName ($currentTag).
            2. Use the actual student data provided below when answering questions about assignments, classes, timetable, due dates, or schedules.
            3. Never invent assignments, classes, due dates, rooms, or timetable entries.
            4. If the user asks "view timetable", "what classes do I have today?", or "what assignments do I have?", display their exact timetable or assignments from the data provided.
            5. If the requested information is not present in the data, state clearly that you cannot find it in their MyCampusComp data.
            6. Keep answers friendly, clear, concise, and helpful in $languageName.

            STUDENT DATA:
            $studentContext

            USER QUESTION:
            $userQuestion
        """.trimIndent()
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    private fun sendMessage() {
        val userQuestion = messageInput.text.toString().trim()
        if (userQuestion.isEmpty()) {
            return
        }

        addStudentMessage(userQuestion)
        messageInput.text.clear()

        sendButton.isEnabled = false
        messageInput.isEnabled = false

        showThinkingMessage()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val studentContext = buildStudentContext()
                val fullPrompt = buildPrompt(userQuestion, studentContext)

                val response = RetrofitClient.aiService.askAI(
                    AIRequest(message = fullPrompt)
                )

                withContext(Dispatchers.Main) {
                    removeThinkingMessage()

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val reply = body.response ?: body.error ?: "No response received."

                        addTutorMessage(reply)
                        awardAiPoints()
                    } else {
                        val code = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e("AIStudyAssistant", "HTTP $code: $errorBody")

                        if (code == 500 && errorBody?.contains("OPENROUTER_API_KEY") == true) {
                            addTutorMessage("The AI Assistant key is missing in Azure. Please configure OPENROUTER_API_KEY in Azure App Settings.")
                            showErrorToast("Azure missing OPENROUTER_API_KEY")
                        } else {
                            addTutorMessage("Sorry, I couldn't process your request right now. (HTTP $code)")
                            showErrorToast("Server error: $code")
                        }
                    }

                    enableChat()
                }

            } catch (e: Exception) {
                Log.e("CampusAssistant", "AI error", e)
                withContext(Dispatchers.Main) {
                    removeThinkingMessage()
                    addTutorMessage("I'm having trouble connecting to the Campus Assistant. Please check your connection and try again.")
                    showErrorToast("Connection error: ${e.message}")
                    enableChat()
                }
            }
        }
    }

    // ============================================================
    // GAMIFICATION
    // ============================================================

    private fun awardAiPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                UserRepository(context = applicationContext).addGamificationPoints(15, "badge_ai_study")
            } catch (e: Exception) {
                Log.e("Gamification", "Could not award AI points", e)
            }
        }
    }

    // ============================================================
    // THINKING MESSAGE & UI HELPERS
    // ============================================================

    private fun showThinkingMessage() {
        val textView = TextView(this)
        textView.text = "Thinking..."
        textView.setTextColor(ContextCompat.getColor(this, R.color.secondary_text))
        textView.textSize = resources.getDimension(R.dimen.ai_message_text_size)
        val padding = resources.getDimensionPixelSize(R.dimen.ai_bubble_padding)
        textView.setPadding(padding, padding, padding, padding)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_ai_chat_bubble)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = resources.getDimensionPixelSize(R.dimen.ai_message_spacing)
        textView.layoutParams = params

        chatContainer.addView(textView)
        thinkingMessage = textView
        scrollToBottom()
    }

    private fun removeThinkingMessage() {
        thinkingMessage?.let { chatContainer.removeView(it) }
        thinkingMessage = null
    }

    private fun addStudentMessage(message: String) {
        val textView = TextView(this)
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.student_bubble_text))
        textView.textSize = resources.getDimension(R.dimen.ai_message_text_size)
        val padding = resources.getDimensionPixelSize(R.dimen.ai_bubble_padding)
        textView.setPadding(padding, padding, padding, padding)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_student_chat_bubble)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.END
        params.bottomMargin = resources.getDimensionPixelSize(R.dimen.ai_message_spacing)
        textView.layoutParams = params

        chatContainer.addView(textView)
        scrollToBottom()
    }

    private fun addTutorMessage(message: String) {
        val label = TextView(this)
        label.text = getString(R.string.ai_tutor_label)
        label.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
        label.textSize = resources.getDimension(R.dimen.ai_label_text_size)
        label.setTypeface(null, android.graphics.Typeface.BOLD)

        val labelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        labelParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.ai_message_label_margin)
        label.layoutParams = labelParams

        chatContainer.addView(label)

        val textView = TextView(this)
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
        textView.textSize = resources.getDimension(R.dimen.ai_message_text_size)
        val padding = resources.getDimensionPixelSize(R.dimen.ai_bubble_padding)
        textView.setPadding(padding, padding, padding, padding)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_ai_chat_bubble)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = resources.getDimensionPixelSize(R.dimen.ai_message_spacing)
        textView.layoutParams = params

        chatContainer.addView(textView)
        scrollToBottom()
    }

    private fun enableChat() {
        sendButton.isEnabled = true
        messageInput.isEnabled = true
        messageInput.requestFocus()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun scrollToBottom() {
        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
    }
}
