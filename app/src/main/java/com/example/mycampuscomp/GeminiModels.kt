package com.example.mycampuscomp

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val tools: List<GeminiTool>? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String? = null,
    val functionCall: GeminiFunctionCall? = null,
    val functionResponse: GeminiFunctionResponse? = null
)

data class GeminiFunctionCall(
    val name: String,
    val args: Map<String, Any>? = null
)

data class GeminiFunctionResponse(
    val name: String,
    val response: Map<String, Any>
)

data class GeminiTool(
    val functionDeclarations:
    List<GeminiFunctionDeclaration>
)

data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiSchema
)

data class GeminiSchema(
    val type: String,
    val description: String? = null,
    val properties:
    Map<String, GeminiSchema>? = null,
    val required:
    List<String>? = null,
    val enum:
    List<String>? = null
)

data class GeminiResponse(
    val candidates:
    List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content:
    GeminiContent?
)