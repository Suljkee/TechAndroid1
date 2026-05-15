package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.ResultScreen
import com.example.myapplication.ui.screens.TestScreen
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.viewmodels.SharedTestViewModel
import com.example.myapplication.ui.viewmodels.TestUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: SharedTestViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsState()

                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        when (val state = uiState) {
                            is TestUiState.Idle, is TestUiState.Generating -> {
                                HomeScreen(
                                    isGenerating = state is TestUiState.Generating,
                                    progressText = (state as? TestUiState.Generating)?.progressText,
                                    onSubjectSelected = { subject -> viewModel.startTest(subject) },
                                    onCancelGeneration = { viewModel.cancelGeneration() }
                                )
                            }
                            is TestUiState.Playing -> {
                                TestScreen(
                                    question = state.currentQuestion,
                                    currentIndex = state.currentIndex,
                                    totalCount = state.questions.size,
                                    userAnswer = state.userAnswer,
                                    onAnswerChanged = { answer -> viewModel.saveAnswer(state.currentIndex, answer) },
                                    onNext = { viewModel.nextQuestion() }
                                )
                            }
                            is TestUiState.Evaluating -> {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator()
                                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                                        androidx.compose.material3.Text(state.progressText)
                                    }
                                }
                            }
                            is TestUiState.Result -> {
                                ResultScreen(
                                    correctCount = state.correctCount,
                                    totalCount = state.totalCount,
                                    answeredQuestions = state.answeredQuestions,
                                    onRetry = {
                                        viewModel.resetToHome()
                                    },
                                    onHome = { viewModel.resetToHome() }
                                )
                            }
                            is TestUiState.Error -> {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.material3.Text("Ошибка: ${state.message}", color = androidx.compose.ui.graphics.Color.Red)
                                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                                        androidx.compose.material3.Button(onClick = { viewModel.resetToHome() }) {
                                            androidx.compose.material3.Text("На главную")
                                        }
                                    }
                                }
                            }
                            is TestUiState.Loading -> {
                            }
                        }
                    }
                }
            }
        }
    }
}