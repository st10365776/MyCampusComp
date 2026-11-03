package com.example.mycampuscomp

data class OpenRouterRequest(
    val model: String = "openai/gpt-4o",
    val messages: List<OpenRouterMessage>,
    val tools: List<OpenRouterTool>? = null
)

data class OpenRouterMessage(
    val role: String,
    val content: String? = null,
    val tool_calls: List<OpenRouterToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

data class OpenRouterToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenRouterFunctionCall
)

data class OpenRouterFunctionCall(
    val name: String,
    val arguments: String
)

data class OpenRouterTool(
    val type: String = "function",
    val function: OpenRouterFunctionDeclaration
)

data class OpenRouterFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: OpenRouterSchema
)

data class OpenRouterSchema(
    val type: String = "object",
    val properties: Map<String, OpenRouterProperty>? = null,
    val required: List<String>? = null
)

data class OpenRouterProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>? = null
)

data class OpenRouterChoice(
    val message: OpenRouterMessage? = null
)
