package com.example.myapplication.data.repository

import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.OpenQuestionEvaluation
import com.example.myapplication.data.model.TestGenerationResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.google.ai.client.generativeai.type.FunctionType

class TestRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // Schema for Test Generation
    private val testSchema = Schema(
        name = "test_response",
        description = "Response containing a list of test questions",
        type = FunctionType.OBJECT,
        properties = mapOf(
            "questions" to Schema(
                name = "questions",
                description = "A list of test questions",
                type = FunctionType.ARRAY,
                items = Schema(
                    name = "question",
                    description = "A single question object",
                    type = FunctionType.OBJECT,
                    properties = mapOf(
                        "type" to Schema(
                            name = "type",
                            description = "Can be 'CLOSED', 'OPEN', or 'MATCHING'",
                            type = FunctionType.STRING
                        ),
                        "text" to Schema(
                            name = "text",
                            description = "The question text",
                            type = FunctionType.STRING
                        ),
                        "options" to Schema(
                            name = "options",
                            description = "Only for CLOSED type",
                            type = FunctionType.ARRAY,
                            items = Schema(name = "option", description = "Option text", type = FunctionType.STRING)
                        ),
                        "correctAnswer" to Schema(
                            name = "correctAnswer",
                            description = "Correct option for CLOSED, or reference answer for OPEN",
                            type = FunctionType.STRING
                        ),
                        "pairs" to Schema(
                            name = "pairs",
                            description = "Only for MATCHING type. A list of concept-definition pairs.",
                            type = FunctionType.ARRAY,
                            items = Schema(
                                name = "pair",
                                description = "A concept-definition pair",
                                type = FunctionType.OBJECT,
                                properties = mapOf(
                                    "concept" to Schema(name = "concept", description = "The concept", type = FunctionType.STRING),
                                    "definition" to Schema(name = "definition", description = "The definition", type = FunctionType.STRING)
                                )
                            )
                        ),
                        "explanation" to Schema(
                            name = "explanation",
                            description = "General explanation of the topic/answer",
                            type = FunctionType.STRING
                        )
                    ),
                    required = listOf("type", "text", "explanation")
                )
            )
        ),
        required = listOf("questions")
    )

    // Schema for Evaluation
    private val evalSchema = Schema(
        name = "evaluation",
        description = "Evaluation result",
        type = FunctionType.OBJECT,
        properties = mapOf(
            "isCorrect" to Schema(
                name = "isCorrect",
                description = "True if the answer is correct",
                type = FunctionType.BOOLEAN
            ),
            "feedback" to Schema(
                name = "feedback",
                description = "Explanation of what is right or wrong in the user's answer",
                type = FunctionType.STRING
            )
        ),
        required = listOf("isCorrect", "feedback")
    )

    private val generationModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = testSchema
        }
    )

    private val evalModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = evalSchema
        }
    )

    suspend fun generateTest(subject: String): TestGenerationResponse = withContext(Dispatchers.IO) {
        val prompt = """
            Generate an exam preparation test for the subject: "$subject".
            Return exactly 5 questions.
            Ensure there is AT LEAST ONE question of type CLOSED, ONE of type OPEN, and ONE of type MATCHING.
            For CLOSED: Provide 4 'options', and 1 'correctAnswer' exactly matching one option.
            For OPEN: Provide a 'correctAnswer' (reference answer).
            For MATCHING: Provide 4 'pairs' of 'concept' and 'definition'.
            Provide an 'explanation' for each question.
            Output JSON format matching this structure: { "questions": [ { ... } ] }
        """.trimIndent()

        val response = generationModel.generateContent(prompt)
        val jsonText = response.text ?: throw IllegalStateException("Empty response from LLM")
        json.decodeFromString<TestGenerationResponse>(jsonText)
    }

    suspend fun evaluateOpenQuestion(
        questionText: String,
        referenceAnswer: String,
        userAnswer: String
    ): OpenQuestionEvaluation = withContext(Dispatchers.IO) {
        val prompt = """
            You are an exam evaluator.
            Question: "$questionText"
            Reference correct answer: "$referenceAnswer"
            User's answer: "$userAnswer"
            
            Evaluate if the user's answer is correct or partially correct enough to be considered valid.
            Provide feedback explaining what is correct or incorrect.
        """.trimIndent()

        val response = evalModel.generateContent(prompt)
        val jsonText = response.text ?: throw IllegalStateException("Empty response from LLM")
        json.decodeFromString<OpenQuestionEvaluation>(jsonText)
    }
}
