package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.AnsweredQuestion
import com.example.myapplication.data.model.QuestionType

@Composable
fun ResultScreen(
    correctCount: Int,
    totalCount: Int,
    answeredQuestions: List<AnsweredQuestion>,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ваш результат",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )
        
        Text(
            text = "$correctCount из $totalCount",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = if (correctCount > totalCount / 2) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(answeredQuestions) { item ->
                ResultItem(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onHome) {
                Text("На главную")
            }
            Button(onClick = onRetry) {
                Text("Пройти ещё раз")
            }
        }
    }
}

@Composable
fun ResultItem(answeredQuestion: AnsweredQuestion) {
    var expanded by remember { mutableStateOf(false) }
    val isCorrect = answeredQuestion.evaluation.isCorrect

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = answeredQuestion.question.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val userAnswerText = when (answeredQuestion.question.type) {
                    QuestionType.CLOSED -> answeredQuestion.userAnswer.selectedOption ?: "Нет ответа"
                    QuestionType.OPEN -> answeredQuestion.userAnswer.textAnswer ?: "Нет ответа"
                    QuestionType.MATCHING -> answeredQuestion.userAnswer.matchingAnswers?.entries?.joinToString("\n") { "${it.key} - ${it.value}" } ?: "Нет ответа"
                }

                val correctAnswerText = when (answeredQuestion.question.type) {
                    QuestionType.CLOSED -> answeredQuestion.question.correctAnswer ?: ""
                    QuestionType.OPEN -> answeredQuestion.question.correctAnswer ?: ""
                    QuestionType.MATCHING -> answeredQuestion.question.pairs?.joinToString("\n") { "${it.concept} - ${it.definition}" } ?: ""
                }

                Text("Ваш ответ:", fontWeight = FontWeight.Bold)
                Text(userAnswerText, modifier = Modifier.padding(bottom = 8.dp))

                Text("Правильный ответ/Эталон:", fontWeight = FontWeight.Bold)
                Text(correctAnswerText, modifier = Modifier.padding(bottom = 8.dp))

                Text("Объяснение LLM:", fontWeight = FontWeight.Bold)
                Text(answeredQuestion.evaluation.feedback ?: answeredQuestion.question.explanation, modifier = Modifier.padding(bottom = 8.dp))
                
                if (answeredQuestion.question.type != QuestionType.OPEN) {
                    Text("Общее объяснение:", fontWeight = FontWeight.Bold)
                    Text(answeredQuestion.question.explanation)
                }
            }
        }
    }
}
