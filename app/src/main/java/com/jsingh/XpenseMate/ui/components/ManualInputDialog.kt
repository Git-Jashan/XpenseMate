package com.jsingh.XpenseMate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.absoluteValue

data class AppContact(val name: String, val vpa: String)

fun getAvatarColor(name: String): Color {
    val colors = listOf(Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF4FC3F7), Color(0xFFFFB74D), Color(0xFF81C784))
    return colors[name.hashCode().absoluteValue % colors.size]
}

@Composable
fun ManualUpiInputDialog(
    recentContacts: List<AppContact>,   // 👈 The UI just accepts the finished list
    frequentContacts: List<AppContact>, // 👈 The UI just accepts the finished list
    onDismiss: () -> Unit,
    onContinue: (String) -> Unit
) {
    var searchInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Tab State: 0 = Recents, 1 = Frequents
    var selectedTab by remember { mutableStateOf(0) }

    // Handle Selector State
    var selectedHandle by remember { mutableStateOf("@paytm") }
    val commonHandles = listOf("@paytm", "@ybl", "@okhdfcbank", "@okicici", "@sbi", "@upi")

    // Smart Validation
    val isPhoneNumber = searchInput.trim().matches(Regex("^[0-9]{10}$"))
    val isUpiId = searchInput.contains("@") && searchInput.length > 5
    val formattedVpa = if (isPhoneNumber) "${searchInput.trim()}$selectedHandle" else searchInput.trim()
    val canProceed = isPhoneNumber || isUpiId

    // 👇 THE UI NOW JUST SWITCHES BETWEEN THE TWO LISTS WE GAVE IT
    val activeList = if (selectedTab == 0) recentContacts else frequentContacts
    val filteredContacts = activeList.filter {
        it.name.contains(searchInput, ignoreCase = true) || it.vpa.contains(searchInput, ignoreCase = true)
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0A0A0A))
                    .clickable(enabled = false) {}
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp)
                    .imePadding()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pay Anyone", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.background(Color(0xFF1C1C1E), CircleShape).size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search Box
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Name, phone number, or UPI ID", color = Color.DarkGray) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2C2C2E), unfocusedBorderColor = Color(0xFF1C1C1E),
                        focusedContainerColor = Color(0xFF1C1C1E), unfocusedContainerColor = Color(0xFF1C1C1E),
                        cursorColor = Color(0xFF0A84FF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (canProceed && filteredContacts.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF121212))
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                            .clickable { onContinue(formattedVpa) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFF0A84FF).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(if (isPhoneNumber) "#" else "@", color = Color(0xFF0A84FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isPhoneNumber) "Pay +91 $searchInput" else "Pay UPI ID", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(formattedVpa, color = Color.Gray, fontSize = 13.sp)
                        }
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color(0xFF0A84FF))
                    }

                   if (isPhoneNumber) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select app handle:", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(commonHandles) { handle ->
                                val isSelected = selectedHandle == handle
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFF0A84FF) else Color(0xFF1C1C1E))
                                        .clickable { selectedHandle = handle }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(handle, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // THE PILL TOGGLE (Recents | Frequents)
                if (searchInput.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(4.dp)
                    ) {
                        // Recents Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedTab == 0) Color(0xFF2C2C2E) else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Recents", color = if (selectedTab == 0) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        // Frequents Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedTab == 1) Color(0xFF2C2C2E) else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Frequents", color = if (selectedTab == 1) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (filteredContacts.isEmpty() && searchInput.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No matching history", color = Color.DarkGray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(filteredContacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchInput = contact.vpa
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatarColor = getAvatarColor(contact.name)
                                Box(
                                    modifier = Modifier.size(48.dp).background(avatarColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.name.take(1).uppercase(), color = avatarColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(contact.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text(if (selectedTab == 1) "Frequent payee" else "Recent payee", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}