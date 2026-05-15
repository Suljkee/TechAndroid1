package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SubjectItem(
    val name: String,
    val emoji: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    isGenerating: Boolean,
    progressText: String?,
    onSubjectSelected: (String) -> Unit,
    onCancelGeneration: () -> Unit
) {
    val subjects = listOf(
        SubjectItem("Математика", "📐", listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
        SubjectItem("История Казахстана", "🏛️", listOf(Color(0xFFFA709A), Color(0xFFFEE140))),
        SubjectItem("Биология", "🧬", listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
        SubjectItem("Казахский язык", "📚", listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC)))
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = isGenerating,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.95f))
            },
            label = "generation_state"
        ) { generating ->
            if (generating) {
                LoadingScreen(progressText, onCancelGeneration)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
                    ) {
                        Column {
                            Text(
                                text = "Привет! 👋",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Какой предмет будем изучать сегодня?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { subject ->
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn() + scaleIn(initialScale = 0.8f)
                            ) {
                                SubjectCard(subject, onClick = { onSubjectSelected(subject.name) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(subject: SubjectItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = subject.gradientColors.first().copy(alpha = 0.5f))
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(subject.gradientColors))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = subject.emoji,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(progressText: String?, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(72.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = 6.dp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Нейросеть составляет тест...",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = progressText ?: "Анализируем тему и подбираем лучшие вопросы",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(56.dp))
        
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Отмена", style = MaterialTheme.typography.titleMedium)
        }
    }
}
