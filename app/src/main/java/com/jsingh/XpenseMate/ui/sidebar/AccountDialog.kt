package com.jsingh.XpenseMate.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SimCardDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountDialog(
    userEmail: String,
    transactionCount: Int,
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Smart Name Extractor
    val cleanName = userEmail.substringBefore("@").filter { it.isLetter() }
    val derivedDisplayName = if (cleanName.isNotEmpty()) cleanName.replaceFirstChar { it.uppercase() } else "User"
    val derivedInitials = derivedDisplayName.take(2).uppercase()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            // --- SECTION 1: IDENTITY ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = derivedInitials,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = derivedDisplayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = userEmail,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- SECTION 2: LIVE DATA VAULT ---
                Text("SECURITY & SYNC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Database Connected", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("$transactionCount Records Encrypted", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SECTION 3: QUICK ACTIONS (Visual UI Polish) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Rounded.SimCardDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            // --- SECTION 4: THE DANGER ZONE ---
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}