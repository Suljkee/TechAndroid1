package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuestionType {
    CLOSED, OPEN, MATCHING
}

@Serializable
data class MatchingPair(
    val concept: String,
    val definition: String
)

@Serializable
data class Question(
    val type: QuestionType,
    val text: String,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val pairs: List<MatchingPair>? = null,
    val explanation: String
)

@Serializable
data class TestGenerationResponse(
    val questions: List<Question>
)

@Serializable
data class OpenQuestionEvaluation(
    val isCorrect: Boolean,
    val feedback: String
)


data class UserAnswer(
    val question: Question,
    val selectedOption: String? = null,
    val textAnswer: String? = null,
    val matchingAnswers: Map<String, String>? = null
)

data class EvaluationResult(
    val isCorrect: Boolean,
    val feedback: String? = null
)

data class AnsweredQuestion(
    val question: Question,
    val userAnswer: UserAnswer,
    val evaluation: EvaluationResult
)
