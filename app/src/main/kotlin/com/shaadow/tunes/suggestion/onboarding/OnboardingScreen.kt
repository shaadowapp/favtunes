package com.shaadow.tunes.suggestion.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding

/**
 * Smart onboarding screen for the suggestion system
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val onboardingManager = remember { OnboardingManager(context) }
    
    var currentStep by remember { mutableIntStateOf(0) }
    var preferences by remember { mutableStateOf(OnboardingPreferences()) }
    
    val steps = listOf("Welcome", "Genres", "Habits", "Discovery")
    val totalSteps = steps.size
    
    // Handle back button
    BackHandler(enabled = currentStep > 0) {
        currentStep = maxOf(0, currentStep - 1)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )
        
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = currentStep == 0,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                WelcomeStep(
                    onNext = { currentStep = 1 },
                    onSkip = onSkip
                )
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = currentStep == 1,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                GenreSelectionStep(
                    genres = onboardingManager.getAvailableGenres(),
                    selectedGenres = preferences.selectedGenres,
                    onGenresChanged = { genres ->
                        preferences = preferences.copy(selectedGenres = genres)
                    },
                    onNext = { currentStep = 2 },
                    onBack = { currentStep = 0 }
                )
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = currentStep == 2,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                ListeningHabitsStep(
                    habits = onboardingManager.getListeningHabits(),
                    selectedHabit = preferences.listeningHabit,
                    onHabitSelected = { habit ->
                        preferences = preferences.copy(listeningHabit = habit)
                    },
                    onNext = { currentStep = 3 },
                    onBack = { currentStep = 1 }
                )
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = currentStep == 3,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                DiscoveryPreferencesStep(
                    discoveryPreferences = onboardingManager.getDiscoveryPreferences(),
                    selectedPreference = preferences.discoveryPreference,
                    onPreferenceSelected = { preference ->
                        preferences = preferences.copy(discoveryPreference = preference)
                    },
                    onComplete = {
                        onboardingManager.completeOnboarding(preferences)
                        onComplete()
                    },
                    onBack = { currentStep = 2 }
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
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎵",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome to FavTunes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Let's personalize your music experience! We'll learn your preferences to suggest music you'll love.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onSkip) {
            Text("Skip for now")
        }
    }
}

/**
 * Genre selection step
 */
@Composable
private fun GenreSelectionStep(
    genres: List<Genre>,
    selectedGenres: Set<String>,
    onGenresChanged: (Set<String>) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Text(
            text = "What music do you enjoy?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select your favorite genres (choose at least 3)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(genres) { genre ->
                GenreCard(
                    genre = genre,
                    isSelected = selectedGenres.contains(genre.name),
                    onToggle = {
                        val newSelection = if (selectedGenres.contains(genre.name)) {
                            selectedGenres - genre.name
                        } else {
                            selectedGenres + genre.name
                        }
                        onGenresChanged(newSelection)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                enabled = selectedGenres.size >= 3
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * Genre selection card
 */
@Composable
private fun GenreCard(
    genre: Genre,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF4CAF50) // Green for selected
            } else {
                Color(0xFF2C2C2C) // Dark gray for unselected
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp
            )
        } else {
            CardDefaults.outlinedCardBorder()
        },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = genre.icon,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = genre.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

/**
 * Listening habits step
 */
@Composable
private fun ListeningHabitsStep(
    habits: List<ListeningHabit>,
    selectedHabit: ListeningHabit?,
    onHabitSelected: (ListeningHabit) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Text(
            text = "How do you listen to music?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This helps us understand your listening style",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(habits) { habit ->
                HabitCard(
                    habit = habit,
                    isSelected = selectedHabit == habit,
                    onSelect = { onHabitSelected(habit) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                enabled = selectedHabit != null
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * Habit selection card
 */
@Composable
private fun HabitCard(
    habit: ListeningHabit,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF4CAF50) // Green for selected
            } else {
                Color(0xFF2C2C2C) // Dark gray for unselected
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
                text = habit.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null
                )
            }
        }
    }
}

/**
 * Discovery preferences step
 */
@Composable
private fun DiscoveryPreferencesStep(
    discoveryPreferences: List<DiscoveryPreference>,
    selectedPreference: DiscoveryPreference?,
    onPreferenceSelected: (DiscoveryPreference) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Text(
            text = "How adventurous are you?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose how much you want to explore new music",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            discoveryPreferences.forEach { preference ->
                DiscoveryCard(
                    preference = preference,
                    isSelected = selectedPreference == preference,
                    onSelect = { onPreferenceSelected(preference) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            
            Button(
                onClick = onComplete,
                enabled = selectedPreference != null
            ) {
                Text("Complete Setup")
            }
        }
    }
}

/**
 * Discovery preference card
 */
@Composable
private fun DiscoveryCard(
    preference: DiscoveryPreference,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF4CAF50) // Green for selected
            } else {
                Color(0xFF2C2C2C) // Dark gray for unselected
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preference.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = preference.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { preference.explorationLevel },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}