package com.shaadow.tunes.suggestion.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Skip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem

/**
 * Smart onboarding screen that learns user preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suggestionSystem = remember { AdvancedSuggestionSystem(context) }
    
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var selectedListeningHabits by remember { mutableStateOf(setOf<ListeningHabit>()) }
    var isCompleting by remember { mutableStateOf(false) }
    
    val steps = listOf(
        OnboardingStep.Welcome,
        OnboardingStep.GenreSelection,
        OnboardingStep.ListeningHabits,
        OnboardingStep.Complete
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth(),
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Step content
        when (steps[currentStep]) {
            OnboardingStep.Welcome -> {
                WelcomeStep(
                    onNext = { currentStep++ }
                )
            }
            OnboardingStep.GenreSelection -> {
                GenreSelectionStep(
                    selectedGenres = selectedGenres,
                    onGenreToggle = { genre ->
                        selectedGenres = if (genre in selectedGenres) {
                            selectedGenres - genre
                        } else {
                            selectedGenres + genre
                        }
                    },
                    onNext = { currentStep++ },
                    onSkip = { currentStep++ }
                )
            }
            OnboardingStep.ListeningHabits -> {
                ListeningHabitsStep(
                    selectedHabits = selectedListeningHabits,
                    onHabitToggle = { habit ->
                        selectedListeningHabits = if (habit in selectedListeningHabits) {
                            selectedListeningHabits - habit
                        } else {
                            selectedListeningHabits + habit
                        }
                    },
                    onNext = { currentStep++ },
                    onSkip = { currentStep++ }
                )
            }
            OnboardingStep.Complete -> {
                CompleteStep(
                    isCompleting = isCompleting,
                    onFinish = {
                        isCompleting = true
                        // Apply user preferences
                        suggestionSystem.setInitialPreferences(selectedGenres)
                        // Could also apply listening habits here
                        onComplete()
                    }
                )
            }
        }
    }
}

/**
 * Welcome step
 */
@Composable
private fun WelcomeStep(
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome to FavTunes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Let's personalize your music experience.\nWe'll learn your preferences to suggest music you'll love.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Genre selection step
 */
@Composable
private fun GenreSelectionStep(
    selectedGenres: Set<String>,
    onGenreToggle: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val genres = listOf(
        "Pop", "Rock", "Hip-Hop", "Jazz", "Classical", "Electronic",
        "Country", "R&B", "Indie", "Folk", "Metal", "Reggae",
        "Blues", "Funk", "Punk", "Alternative"
    )
    
    Column {
        Text(
            text = "What music do you enjoy?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select your favorite genres (optional)",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(genres) { genre ->
                GenreChip(
                    genre = genre,
                    isSelected = genre in selectedGenres,
                    onToggle = { onGenreToggle(genre) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Skip,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Skip")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = selectedGenres.isNotEmpty()
            ) {
                Text("Continue")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Genre chip component
 */
@Composable
private fun GenreChip(
    genre: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        onClick = onToggle,
        label = { Text(genre) },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Listening habits step
 */
@Composable
private fun ListeningHabitsStep(
    selectedHabits: Set<ListeningHabit>,
    onHabitToggle: (ListeningHabit) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val habits = listOf(
        ListeningHabit.DISCOVERY to "I love discovering new music",
        ListeningHabit.FAMILIAR to "I prefer familiar songs",
        ListeningHabit.LONG_SESSIONS to "I listen for long periods",
        ListeningHabit.SHORT_SESSIONS to "I prefer quick listening sessions",
        ListeningHabit.BACKGROUND to "Music is usually in the background",
        ListeningHabit.FOCUSED to "I actively listen to music"
    )
    
    Column {
        Text(
            text = "How do you listen?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Help us understand your listening style (optional)",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(habits) { (habit, description) ->
                ListeningHabitItem(
                    habit = habit,
                    description = description,
                    isSelected = habit in selectedHabits,
                    onToggle = { onHabitToggle(habit) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Skip,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Skip")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Listening habit item
 */
@Composable
private fun ListeningHabitItem(
    habit: ListeningHabit,
    description: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                onValueChange = { onToggle() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Completion step
 */
@Composable
private fun CompleteStep(
    isCompleting: Boolean,
    onFinish: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We'll start learning your preferences as you listen.\nYour recommendations will get better over time.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCompleting
        ) {
            if (isCompleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Setting up...")
            } else {
                Text("Start Listening")
            }
        }
    }
}

/**
 * Onboarding steps
 */
private enum class OnboardingStep {
    Welcome,
    GenreSelection,
    ListeningHabits,
    Complete
}

/**
 * Listening habits
 */
enum class ListeningHabit {
    DISCOVERY,
    FAMILIAR,
    LONG_SESSIONS,
    SHORT_SESSIONS,
    BACKGROUND,
    FOCUSED
}