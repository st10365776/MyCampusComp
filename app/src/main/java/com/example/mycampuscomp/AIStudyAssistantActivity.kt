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

    companion object {

        private const val GEMINI_API_KEY =
            ""

        private const val GEMINI_MODEL =
            "gemini-3.6-flash"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_ai_study_assistant
        )

        messageInput =
            findViewById(R.id.messageInput)

        sendButton =
            findViewById(R.id.sendButton)

        chatContainer =
            findViewById(R.id.chatContainer)

        chatScrollView =
            findViewById(R.id.chatScrollView)

        showWelcomeMessage()

        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener { _, actionId, event ->

            if (
                actionId == EditorInfo.IME_ACTION_SEND ||
                (
                        event != null &&
                                event.keyCode == KeyEvent.KEYCODE_ENTER &&
                                event.action == KeyEvent.ACTION_DOWN
                        )
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

        addTutorMessage(
            "Hello! I'm your Campus Assistant.\n\n" +
                    "I can help you with:\n" +
                    "• Assignments\n" +
                    "• Classess\n" +
                    "• Your timetable\n" +
                    "• Due dates\n" +
                    "• Study planning\n" +
                    "• Your MyCampusComp information\n\n" +
                    "Try asking me:\n" +
                    "\"What assignments do I have?\"\n\n" +
                    "or\n\n" +
                    "\"What classes do I have today?\""
        )
    }

    // ============================================================
    // AI SYSTEM INSTRUCTION
    // ============================================================

    private fun buildAppSystemInstruction(
        studentContext: String
    ): GeminiContent {

        val prompt = """
            You are the Campus Assistant inside MyCampusComp.

            You are a helpful university student assistant.

            IMPORTANT RULES

            1. USE THE STUDENT'S ACTUAL DATA

            When answering questions about assignments, classes,
            timetable, due dates or student information, use the
            STUDENT DATA provided below.

            NEVER invent assignments, classes, due dates, rooms or
            timetable entries.

            2. ASSIGNMENTS

            If the student asks:

            - What assignments do I have?
            - What assignments are due?
            - What do I need to submit?
            - What's due this week?
            - Show my assignments
            - What homework do I have?

            Use the actual assignment data.

            Include where available:

            - Assignment name
            - Due date
            - Status

            3. CLASSES

            If the student asks:

            - What classes do I have?
            - What class do I have today?
            - What lectures do I have?
            - What is my schedule?
            - What's my timetable?

            Use the actual timetable data.

            Include where available:

            - Subject
            - Day
            - Time
            - Room
            - Type

            4. COMBINED QUESTIONS

            If the student asks:

            "What do I have today?"

            Give BOTH:

            - Classes
            - Relevant assignments/tasks

            If the student asks:

            "What do I have this week?"

            Give:

            - Classes
            - Assignments
            - Important due dates

            5. ADDING ASSIGNMENTS

            If the student asks you to add an assignment,
            use add_assignment.

            After the function succeeds, clearly tell the student
            that the assignment was added.

            6. ADDING CLASSES

            If the student asks you to add a class,
            use add_timetable_class.

            After the function succeeds, clearly tell the student
            that the class was added.

            7. ACTION CONFIRMATION

            If an action has successfully completed, NEVER tell the
            student that it failed simply because a follow-up AI
            response failed.

            The application already knows whether the action
            succeeded.

            8. DO NOT INVENT DATA

            If the student's data does not contain the requested
            information, say that you cannot find it in their
            MyCampusComp data.

            Do not make up information.

            9. GENERAL QUESTIONS

            You can answer normal study-related questions even when
            they are not directly related to the student's database.

            10. STYLE

            Keep responses:

            - Friendly
            - Clear
            - Concise
            - Helpful

            STUDENT DATA

            $studentContext
        """.trimIndent()

        return GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = prompt
                )
            )
        )
    }

    // ============================================================
    // GET ACTUAL STUDENT DATA
    // ============================================================

    private suspend fun buildStudentContext(): String {

        val assignmentList: List<Assignment> =
            assignmentRepository
                .getAssignmentsLocal()
                .firstOrNull()
                ?: emptyList()

        val timetableList: List<TimetableClass> =
            timetableRepository
                .getAllClassesLocal()
                .firstOrNull()
                ?: emptyList()

        val builder = StringBuilder()

        // --------------------------------------------------------
        // ASSIGNMENTS
        // --------------------------------------------------------

        builder.appendLine("ASSIGNMENTS:")

        if (assignmentList.isEmpty()) {

            builder.appendLine(
                "No assignments are currently stored."
            )

        } else {

            assignmentList.forEach { assignment ->

                builder.appendLine(
                    "- ${assignment.title} | " +
                            "Due: ${assignment.dueDate} | " +
                            "Status: ${assignment.status}"
                )
            }
        }

        builder.appendLine()

        // --------------------------------------------------------
        // CLASSES
        // --------------------------------------------------------

        builder.appendLine("CLASSES / TIMETABLE:")

        if (timetableList.isEmpty()) {

            builder.appendLine(
                "No classes are currently stored."
            )

        } else {

            timetableList.forEach { timetableClass ->

                builder.appendLine(
                    "- ${timetableClass.subject} | " +
                            "${timetableClass.day} | " +
                            "${timetableClass.time} | " +
                            "Room: ${timetableClass.room} | " +
                            "Type: ${timetableClass.type}"
                )
            }
        }

        return builder.toString()
    }

    // ============================================================
    // GEMINI TOOLS
    // ============================================================

    private fun buildAppTools(): List<GeminiTool> {

        return listOf(

            GeminiTool(

                functionDeclarations = listOf(

                    // ------------------------------------------------
                    // ADD ASSIGNMENT
                    // ------------------------------------------------

                    GeminiFunctionDeclaration(

                        name = "add_assignment",

                        description =
                            "Adds a new assignment to the student's " +
                                    "actual assignment list.",

                        parameters =
                            GeminiSchema(

                                type = "OBJECT",

                                properties = mapOf(

                                    "title" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "The assignment title"
                                    ),

                                    "dueDate" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "The assignment due date"
                                    ),

                                    "status" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Assignment status",
                                        enum = listOf(
                                            "Pending",
                                            "Urgent",
                                            "Completed"
                                        )
                                    )
                                ),

                                required = listOf(
                                    "title",
                                    "dueDate"
                                )
                            )
                    ),

                    // ------------------------------------------------
                    // ADD CLASS
                    // ------------------------------------------------

                    GeminiFunctionDeclaration(

                        name = "add_timetable_class",

                        description =
                            "Adds a class to the student's " +
                                    "actual timetable.",

                        parameters =
                            GeminiSchema(

                                type = "OBJECT",

                                properties = mapOf(

                                    "subject" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Subject or module name"
                                    ),

                                    "time" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Class time"
                                    ),

                                    "room" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Room or venue"
                                    ),

                                    "day" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Day of the week",
                                        enum = listOf(
                                            "Mon",
                                            "Tue",
                                            "Wed",
                                            "Thu",
                                            "Fri",
                                            "Sat",
                                            "Sun"
                                        )
                                    ),

                                    "type" to GeminiSchema(
                                        type = "STRING",
                                        description =
                                            "Class type",
                                        enum = listOf(
                                            "Lecture",
                                            "Lab",
                                            "Tutorial"
                                        )
                                    )
                                ),

                                required = listOf(
                                    "subject",
                                    "time",
                                    "room",
                                    "day"
                                )
                            )
                    )
                )
            )
        )
    }

    // ============================================================
    // EXECUTE FUNCTION CALL
    // ============================================================

    private suspend fun executeFunctionCall(
        call: GeminiFunctionCall
    ): Map<String, Any> {

        val args =
            call.args ?: emptyMap()

        return when (call.name) {

            // ====================================================
            // ADD ASSIGNMENT
            // ====================================================

            "add_assignment" -> {

                val title =
                    args["title"]
                        ?.toString()
                        .orEmpty()

                val dueDate =
                    args["dueDate"]
                        ?.toString()
                        .orEmpty()

                val status =
                    args["status"]
                        ?.toString()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Pending"

                if (
                    title.isBlank() ||
                    dueDate.isBlank()
                ) {

                    return mapOf(
                        "success" to false,
                        "message" to
                                "Assignment title and due date " +
                                "are required."
                    )
                }

                try {

                    assignmentRepository.saveAssignment(

                        Assignment(
                            title = title,
                            dueDate = dueDate,
                            status = status
                        )
                    )

                    mapOf(
                        "success" to true,
                        "message" to
                                "Successfully added assignment " +
                                "'$title' due $dueDate."
                    )

                } catch (e: Exception) {

                    android.util.Log.e(
                        "AIStudyAssistant",
                        "Failed to save assignment",
                        e
                    )

                    mapOf(
                        "success" to false,
                        "message" to
                                "Could not save the assignment."
                    )
                }
            }

            // ====================================================
            // ADD TIMETABLE CLASS
            // ====================================================

            "add_timetable_class" -> {

                val subject =
                    args["subject"]
                        ?.toString()
                        .orEmpty()

                val time =
                    args["time"]
                        ?.toString()
                        .orEmpty()

                val room =
                    args["room"]
                        ?.toString()
                        .orEmpty()

                val day =
                    args["day"]
                        ?.toString()
                        .orEmpty()

                val type =
                    args["type"]
                        ?.toString()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Lecture"

                if (
                    subject.isBlank() ||
                    time.isBlank() ||
                    room.isBlank() ||
                    day.isBlank()
                ) {

                    return mapOf(
                        "success" to false,
                        "message" to
                                "Subject, time, room and day " +
                                "are required."
                    )
                }

                try {

                    timetableRepository.saveClass(

                        TimetableClass(
                            subject = subject,
                            time = time,
                            room = room,
                            day = day,
                            type = type
                        )
                    )

                    mapOf(
                        "success" to true,
                        "message" to
                                "Successfully added $subject to " +
                                "your $day timetable at $time."
                    )

                } catch (e: Exception) {

                    android.util.Log.e(
                        "AIStudyAssistant",
                        "Failed to save class",
                        e
                    )

                    mapOf(
                        "success" to false,
                        "message" to
                                "Could not save the class."
                    )
                }
            }

            // ====================================================
            // UNKNOWN FUNCTION
            // ====================================================

            else -> {

                mapOf(
                    "success" to false,
                    "message" to
                            "Unknown function: ${call.name}"
                )
            }
        }
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    private fun sendMessage() {

        val message =
            messageInput.text
                .toString()
                .trim()

        if (message.isEmpty()) {
            return
        }

        // Show user's message immediately.
        addStudentMessage(message)

        messageInput.text.clear()

        sendButton.isEnabled = false
        messageInput.isEnabled = false

        showThinkingMessage()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                // =================================================
                // GET CURRENT STUDENT DATA
                // =================================================

                val studentContext =
                    buildStudentContext()

                val systemInstruction =
                    buildAppSystemInstruction(
                        studentContext
                    )

                val tools =
                    buildAppTools()

                // =================================================
                // CURRENT MESSAGE ONLY
                // =================================================

                val conversation =
                    mutableListOf(

                        GeminiContent(

                            role = "user",

                            parts = listOf(

                                GeminiPart(
                                    text = message
                                )
                            )
                        )
                    )

                // =================================================
                // FIRST GEMINI REQUEST
                // =================================================

                var response =
                    RetrofitClient
                        .geminiService
                        .generateContent(

                            model = GEMINI_MODEL,

                            apiKey = GEMINI_API_KEY,

                            request = GeminiRequest(

                                contents = conversation,

                                systemInstruction =
                                    systemInstruction,

                                tools = tools
                            )
                        )

                var modelPart =
                    response.body()
                        ?.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()

                val functionCall =
                    modelPart?.functionCall

                var actionSucceeded =
                    false

                var actionMessage =
                    ""

                // =================================================
                // FUNCTION CALL
                // =================================================

                if (
                    response.isSuccessful &&
                    functionCall != null
                ) {

                    val result =
                        executeFunctionCall(
                            functionCall
                        )

                    actionSucceeded =
                        result["success"] == true

                    actionMessage =
                        result["message"]
                            ?.toString()
                            .orEmpty()

                    // ------------------------------------------------
                    // Add function call to conversation
                    // ------------------------------------------------

                    conversation.add(

                        GeminiContent(

                            role = "model",

                            parts = listOf(

                                GeminiPart(

                                    functionCall =
                                        functionCall
                                )
                            )
                        )
                    )

                    // ------------------------------------------------
                    // Add function result
                    // ------------------------------------------------

                    conversation.add(

                        GeminiContent(

                            role = "user",

                            parts = listOf(

                                GeminiPart(

                                    functionResponse =
                                        GeminiFunctionResponse(

                                            name =
                                                functionCall.name,

                                            response =
                                                result
                                        )
                                )
                            )
                        )
                    )

                    // =================================================
                    // SECOND GEMINI REQUEST
                    // =================================================

                    val followUpResponse =
                        try {

                            RetrofitClient
                                .geminiService
                                .generateContent(

                                    model =
                                        GEMINI_MODEL,

                                    apiKey =
                                        GEMINI_API_KEY,

                                    request =
                                        GeminiRequest(

                                            contents =
                                                conversation,

                                            systemInstruction =
                                                systemInstruction,

                                            tools =
                                                tools
                                        )
                                )

                        } catch (e: Exception) {

                            android.util.Log.e(
                                "GeminiAPI",
                                "Follow-up request failed",
                                e
                            )

                            null
                        }

                    if (
                        followUpResponse != null &&
                        followUpResponse.isSuccessful
                    ) {

                        response =
                            followUpResponse

                        modelPart =
                            response.body()
                                ?.candidates
                                ?.firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                    }
                }

                // =================================================
                // RETURN TO UI
                // =================================================

                withContext(Dispatchers.Main) {

                    removeThinkingMessage()

                    // =================================================
                    // ACTION SUCCESS
                    // =================================================

                    if (actionSucceeded) {

                        val finalMessage =
                            modelPart?.text
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: actionMessage.ifBlank {
                                    "Done — I've completed that for you."
                                }

                        addTutorMessage(
                            finalMessage
                        )

                        awardAiPoints()
                    }

                    // =================================================
                    // NORMAL AI RESPONSE
                    // =================================================

                    else if (
                        response.isSuccessful &&
                        modelPart?.text
                            ?.isNotBlank() == true
                    ) {

                        val finalMessage =
                            modelPart.text

                        addTutorMessage(
                            finalMessage
                        )

                        awardAiPoints()
                    }

                    // =================================================
                    // AI ERROR
                    // =================================================

                    else {

                        val code =
                            response.code()

                        android.util.Log.e(
                            "GeminiAPI",
                            "HTTP $code"
                        )

                        addTutorMessage(
                            "Sorry, I couldn't process that " +
                                    "request right now. Please try again."
                        )

                        showErrorToast(
                            "The Campus Assistant could not respond."
                        )
                    }

                    enableChat()
                }

            } catch (e: Exception) {

                android.util.Log.e(
                    "CampusAssistant",
                    "AI error",
                    e
                )

                withContext(Dispatchers.Main) {

                    removeThinkingMessage()

                    addTutorMessage(
                        "I'm having trouble connecting to " +
                                "the Campus Assistant. Please try again."
                    )

                    showErrorToast(
                        "Connection problem."
                    )

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

                UserRepository(
                    context = applicationContext
                ).addGamificationPoints(
                    15,
                    "badge_ai_study"
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "Gamification",
                    "Could not award AI points",
                    e
                )
            }
        }
    }

    // ============================================================
    // THINKING MESSAGE
    // ============================================================

    private fun showThinkingMessage() {

        val textView =
            TextView(this)

        textView.text =
            "Thinking..."

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.secondary_text
            )
        )

        textView.textSize =
            resources.getDimension(
                R.dimen.ai_message_text_size
            )

        val padding =
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )

        textView.setPadding(
            padding,
            padding,
            padding,
            padding
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_ai_chat_bubble
            )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams =
            params

        chatContainer.addView(
            textView
        )

        thinkingMessage =
            textView

        scrollToBottom()
    }

    private fun removeThinkingMessage() {

        thinkingMessage?.let {

            chatContainer.removeView(it)
        }

        thinkingMessage = null
    }

    // ============================================================
    // STUDENT MESSAGE
    // ============================================================

    private fun addStudentMessage(
        message: String
    ) {

        val textView =
            TextView(this)

        textView.text =
            message

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.student_bubble_text
            )
        )

        textView.textSize =
            resources.getDimension(
                R.dimen.ai_message_text_size
            )

        val padding =
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )

        textView.setPadding(
            padding,
            padding,
            padding,
            padding
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_student_chat_bubble
            )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.gravity =
            Gravity.END

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams =
            params

        chatContainer.addView(
            textView
        )

        scrollToBottom()
    }

    // ============================================================
    // AI MESSAGE
    // ============================================================

    private fun addTutorMessage(
        message: String
    ) {

        val label =
            TextView(this)

        label.text =
            getString(
                R.string.ai_tutor_label
            )

        label.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.primary_text
            )
        )

        label.textSize =
            resources.getDimension(
                R.dimen.ai_label_text_size
            )

        label.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val labelParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        labelParams.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_label_margin
            )

        label.layoutParams =
            labelParams

        chatContainer.addView(
            label
        )

        val textView =
            TextView(this)

        textView.text =
            message

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.primary_text
            )
        )

        textView.textSize =
            resources.getDimension(
                R.dimen.ai_message_text_size
            )

        val padding =
            resources.getDimensionPixelSize(
                R.dimen.ai_bubble_padding
            )

        textView.setPadding(
            padding,
            padding,
            padding,
            padding
        )

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_ai_chat_bubble
            )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.bottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.ai_message_spacing
            )

        textView.layoutParams =
            params

        chatContainer.addView(
            textView
        )

        scrollToBottom()
    }

    // ============================================================
    // ENABLE CHAT
    // ============================================================

    private fun enableChat() {

        sendButton.isEnabled =
            true

        messageInput.isEnabled =
            true

        messageInput.requestFocus()
    }

    // ============================================================
    // ERROR TOAST
    // ============================================================

    private fun showErrorToast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============================================================
    // SCROLL
    // ============================================================

    private fun scrollToBottom() {

        chatScrollView.post {

            chatScrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }
}
