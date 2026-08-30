package com.example.hustlefix.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.hustlefix.R

@Composable
fun WelcomeScreen(
    suspensionReason: String? = null,
    onServiceProviderClick: () -> Unit,
    onClientClick: () -> Unit,
    onSupportClick: () -> Unit = {},
    onDismissSuspension: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    var showSuspensionDialog by remember { mutableStateOf(suspensionReason != null) }

    LaunchedEffect(Unit) {
        visible = true
    }

    if (showSuspensionDialog && suspensionReason != null) {
        AlertDialog(
            onDismissRequest = { /* Prevent accidental dismissal */ },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false, 
                dismissOnClickOutside = false
            ),
            title = { Text(stringResource(R.string.account_suspended), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text(suspensionReason) },
            confirmButton = {
                Button(onClick = { 
                    showSuspensionDialog = false
                    onDismissSuspension()
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSuspensionDialog = false
                    onSupportClick()
                }) {
                    Text(stringResource(R.string.chat_with_admin))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Gradient Background Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
                    initialOffsetY = { -100 },
                    animationSpec = tween(1200)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.hustlefix_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.welcome_banner),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.welcome_tagline),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.welcome_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 600)) + slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(1200, delayMillis = 600)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.choose_path),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Button(
                        onClick = onServiceProviderClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.im_provider),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = onClientClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.im_client),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
