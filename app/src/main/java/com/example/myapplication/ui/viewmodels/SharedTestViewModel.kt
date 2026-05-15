package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AnsweredQuestion
import com.example.myapplication.data.model.EvaluationResult
import com.example.myapplication.data.model.Question
import com.example.myapplication.data.model.QuestionType
import com.example.myapplication.data.model.UserAnswer
import com.example.myapplication.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TestUiState {
    data object Idle : TestUiState
    data object Loading : TestUiState
    data class Generating(val progressText: String) : TestUiState
    data class Playing(
        val questions: List<Question>,
        val currentIndex: Int,
        val currentQuestion: Question,
        val userAnswer: UserAnswer?
    ) : TestUiState
    data class Evaluating(val progressText: String) : TestUiState
    data class Result(
        val answeredQuestions: List<AnsweredQuestion>,
        val correctCount: Int,
        val totalCount: Int
    ) : TestUiState
    data class Error(val message: String) : TestUiState
}

class SharedTestViewModel(
    private val repository: TestRepository = TestRepository() // Manual DI for simplicity
) : ViewModel() {

    private val _uiState = MutableStateFlow<TestUiState>(TestUiState.Idle)
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private val userAnswers = mutableMapOf<Int, UserAnswer>()
    private var testQuestions = emptyList<Question>()

    fun startTest(subject: String) {
        viewModelScope.launch {
            _uiState.value = TestUiState.Generating("Генерация заданий по предмету: $subject...")
            try {
                val response = repository.generateTest(subject)
                testQuestions = response.questions
                userAnswers.clear()
                
                if (testQuestions.isEmpty()) {
                    _uiState.value = TestUiState.Error("Не удалось сгенерировать задания")
                    return@launch
                }
                
                showQuestion(0)
            } catch (e: Exception) {
                _uiState.value = TestUiState.Error("Ошибка генерации: ${e.message}")
            }
        }
    }

    private fun showQuestion(index: Int) {
        if (index < testQuestions.size) {
            _uiState.value = TestUiState.Playing(
                questions = testQuestions,
                currentIndex = index,
                currentQuestion = testQuestions[index],
                userAnswer = userAnswers[index]
            )
        } else {
            evaluateTest()
        }
    }

    fun saveAnswer(index: Int, answer: UserAnswer) {
        userAnswers[index] = answer
        val currentState = _uiState.value
        if (currentState is TestUiState.Playing && currentState.currentIndex == index) {
            _uiState.value = currentState.copy(userAnswer = answer)
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState is TestUiState.Playing) {
            showQuestion(currentState.currentIndex + 1)
        }
    }

    fun cancelGeneration() {
        _uiState.value = TestUiState.Idle
    }
    
    fun resetToHome() {
        _uiState.value = TestUiState.Idle
    }

    private fun evaluateTest() {
        viewModelScope.launch {
            _uiState.value = TestUiState.Evaluating("Проверка ответов...")
            val results = mutableListOf<AnsweredQuestion>()
            var correctCount = 0

            for (i in testQuestions.indices) {
                val question = testQuestions[i]
                val answer = userAnswers[i] ?: UserAnswer(question)

                val eval = try {
                    when (question.type) {
                        QuestionType.CLOSED -> {
                            val isCorrect = answer.selectedOption == question.correctAnswer
                            EvaluationResult(
                                isCorrect = isCorrect,
                                feedback = if (isCorrect) "Верно!" else "Неверно. Правильный ответ: ${question.correctAnswer}"
                            )
                        }
                        QuestionType.MATCHING -> {
                            val pairsMap = question.pairs?.associate { it.concept to it.definition } ?: emptyMap()
                            val userMap = answer.matchingAnswers ?: emptyMap()
                            val isCorrect = pairsMap == userMap
                            EvaluationResult(
                                isCorrect = isCorrect,
                                feedback = if (isCorrect) "Верно!" else "Неверно. Правильные пары: ${pairsMap.entries.joinToString { "${it.key} - ${it.value}" }}"
                            )
                        }
                        QuestionType.OPEN -> {
                            val userText = answer.textAnswer ?: ""
                            val reference = question.correctAnswer ?: ""
                            val llmEval = repository.evaluateOpenQuestion(question.text, reference, userText)
                            EvaluationResult(
                                isCorrect = llmEval.isCorrect,
                                feedback = llmEval.feedback
                            )
                        }
                    }
                } catch (e: Exception) {
                    EvaluationResult(false, "Ошибка проверки: ${e.message}")
                }

                if (eval.isCorrect) correctCount++
                results.add(AnsweredQuestion(question, answer, eval))
            }

            _uiState.value = TestUiState.Result(
                answeredQuestions = results,
                correctCount = correctCount,
                totalCount = testQuestions.size
            )
        }
    }
}
