package com.example.hustlefix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hustlefix.SessionHelper
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val color: Color = Color.Unspecified,
    val isCategorySelection: Boolean = false
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val pages = listOf(
        OnboardingPage(
            "Welcome to HustleFix",
            "The easiest way to find local experts for any task. From plumbing to painting, we've got you covered.",
            Icons.Default.Build,
            MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            "Secure Payments",
            "Pay safely through our platform using PayFast. Your money is only released when you're happy with the work.",
            Icons.Default.Payment,
            MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            "What interests you?",
            "Select categories you're interested in to personalize your experience.",
            isCategorySelection = true
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val selectedCategories = remember { mutableStateListOf<String>() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = pagerState.currentPage < pages.size - 1 // Allow swipe unless on last page if needed, but let's allow swipe.
            ) { pageIndex ->
                if (pages[pageIndex].isCategorySelection) {
                    CategorySelectionContent(
                        selectedCategories = selectedCategories,
                        onToggle = { cat ->
                            if (selectedCategories.contains(cat)) selectedCategories.remove(cat)
                            else selectedCategories.add(cat)
                        }
                    )
                } else {
                    OnboardingPageContent(pages[pageIndex])
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row {
                    repeat(pages.size) { index ->
                        val color = if (pagerState.currentPage == index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(12.dp),
                            shape = CircleShape,
                            color = color
                        ) {}
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Save interests
                            val prefs = SessionHelper.prefs(context)
                            prefs.edit().putStringSet("user_interests", selectedCategories.toSet()).apply()
                            onFinished()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp).width(140.dp)
                ) {
                    Text(
                        if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "NEXT",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionContent(
    selectedCategories: List<String>,
    onToggle: (String) -> Unit
) {
    val categories = listOf("Plumbing", "Electrical", "Cleaning", "Painting", "Carpentry", "Gardening", "Moving", "Beauty")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("Personalize your Feed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Select services you might need help with", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategories.contains(cat)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(cat) },
                    label = { Text(cat, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(40.dp),
            color = page.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (page.icon != null) {
                    Icon(
                        page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = page.color
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}
