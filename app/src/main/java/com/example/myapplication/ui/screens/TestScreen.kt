package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Question
import com.example.myapplication.data.model.QuestionType
import com.example.myapplication.data.model.UserAnswer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TestScreen(
    question: Question,
    currentIndex: Int,
    totalCount: Int,
    userAnswer: UserAnswer?,
    onAnswerChanged: (UserAnswer) -> Unit,
    onNext: () -> Unit
) {
    val isLastQuestion = currentIndex == totalCount - 1
    
    val canProceed = when (question.type) {
        QuestionType.CLOSED -> !userAnswer?.selectedOption.isNullOrBlank()
        QuestionType.OPEN -> !userAnswer?.textAnswer.isNullOrBlank()
        QuestionType.MATCHING -> {
            val answeredCount = userAnswer?.matchingAnswers?.size ?: 0
            val totalPairs = question.pairs?.size ?: 0
            answeredCount == totalPairs && totalPairs > 0
        }
    }

    val scrollState = rememberScrollState()
    val progress by animateFloatAsState(
        targetValue = (currentIndex + 1) / totalCount.toFloat(),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Вопрос ${currentIndex + 1}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "из $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Question Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            AnimatedContent(
                targetState = question,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { width -> width / 2 }).togetherWith(
                        fadeOut() + slideOutHorizontally { width -> -width / 2 }
                    )
                },
                label = "Question Content"
            ) { targetQuestion ->
                when (targetQuestion.type) {
                    QuestionType.CLOSED -> {
                        ClosedQuestionContent(
                            options = targetQuestion.options ?: emptyList(),
                            selectedOption = userAnswer?.selectedOption,
                            onOptionSelected = {
                                onAnswerChanged(UserAnswer(targetQuestion, selectedOption = it))
                            }
                        )
                    }
                    QuestionType.OPEN -> {
                        OpenQuestionContent(
                            text = userAnswer?.textAnswer ?: "",
                            onTextChanged = {
                                onAnswerChanged(UserAnswer(targetQuestion, textAnswer = it))
                            }
                        )
                    }
                    QuestionType.MATCHING -> {
                        MatchingQuestionContent(
                            concepts = targetQuestion.pairs?.map { it.concept } ?: emptyList(),
                            definitions = targetQuestion.pairs?.map { it.definition }?.shuffled() ?: emptyList(),
                            selectedAnswers = userAnswer?.matchingAnswers ?: emptyMap(),
                            onPairSelected = { concept, definition ->
                                val currentMap = userAnswer?.matchingAnswers?.toMutableMap() ?: mutableMapOf()
                                currentMap[concept] = definition
                                onAnswerChanged(UserAnswer(targetQuestion, matchingAnswers = currentMap))
                            }
                        )
                    }
                }
            }
        }

        // Bottom Button Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = onNext,
                    enabled = canProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (isLastQuestion) "Завершить тест" else "Следующий вопрос",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun ClosedQuestionContent(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            
            val containerColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                label = "containerColor"
            )
            val borderColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "borderColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option) }
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun OpenQuestionContent(
    text: String,
    onTextChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChanged,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp),
        label = { Text("Введите ваш развернутый ответ") },
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingQuestionContent(
    concepts: List<String>,
    definitions: List<String>,
    selectedAnswers: Map<String, String>,
    onPairSelected: (String, String) -> Unit
) {
    var showDialogForConcept by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Выберите правильное определение для каждого термина:",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        concepts.forEach { concept ->
            val selectedDef = selectedAnswers[concept]
            val isAnswered = selectedDef != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = concept,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDialogForConcept = concept },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAnswered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (!isAnswered) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedDef ?: "Нажмите, чтобы выбрать...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAnswered) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isAnswered) Icons.Default.CheckCircle else Icons.Default.ArrowDropDown,
                                contentDescription = "Select",
                                tint = if (isAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialogForConcept != null) {
        AlertDialog(
            onDismissRequest = { showDialogForConcept = null },
            title = {
                Text(
                    text = "Выберите определение",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = showDialogForConcept ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Dialog Scrollable List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        definitions.forEach { def ->
                            val isSelectedInOther = selectedAnswers.values.contains(def) && selectedAnswers[showDialogForConcept] != def
                            val isSelectedHere = selectedAnswers[showDialogForConcept] == def
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isSelectedInOther) {
                                        onPairSelected(showDialogForConcept!!, def)
                                        showDialogForConcept = null
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isSelectedHere -> MaterialTheme.colorScheme.primaryContainer
                                    isSelectedInOther -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(1.dp, if (isSelectedHere) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = def,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isSelectedHere -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isSelectedInOther -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isSelectedHere) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialogForConcept = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}
