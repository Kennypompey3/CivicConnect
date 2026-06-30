package com.example.civicconnect.ui.onboarding

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civicconnect.R
import com.example.civicconnect.data.UserSessionManager
import com.example.civicconnect.ui.components.LegalContentType
import com.example.civicconnect.ui.components.LegalGlassDialog
import com.google.firebase.auth.FirebaseAuth

enum class AuthMode { SIGN_IN, SIGN_UP }
enum class UserRole { RESIDENT, AGENCY }

@Composable
fun OnboardingLoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val firebaseAuth = remember { try { FirebaseAuth.getInstance() } catch (_: Exception) { null } }

    // --- SCREEN LAYOUT CONTROLLERS ---
    var authMode by remember { mutableStateOf(AuthMode.SIGN_UP) }
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENT) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToPolicies by remember { mutableStateOf(false) }

    var isBackendProcessing by remember { mutableStateOf(false) }
    var activeLegalDialog by remember { mutableStateOf<LegalContentType?>(null) }

    // --- VALIDATION CORE CORES ---
    val isLastNameInvalid = lastName.isNotBlank() && lastName.any { it.isDigit() }
    val isPasswordMismatched = authMode == AuthMode.SIGN_UP && confirmPassword.isNotBlank() && password != confirmPassword

    val isFormInputValid = when (authMode) {
        AuthMode.SIGN_IN -> email.contains("@") && password.isNotBlank() && agreedToPolicies
        AuthMode.SIGN_UP -> firstName.isNotBlank() && lastName.isNotBlank() && !isLastNameInvalid &&
                email.contains("@") && password.length >= 6 && !isPasswordMismatched && agreedToPolicies
    }

    val backgroundBlurAlpha = if (activeLegalDialog != null) 14.dp else 0.dp

    // Dynamic internal card padding allocations to combat excess whitespace in Sign-In mode smoothly
    val dynamicCardInternalPadding = if (authMode == AuthMode.SIGN_IN) 24.dp else 16.dp
    val dynamicInterFieldSpacing = if (authMode == AuthMode.SIGN_IN) 18.dp else 8.dp
    val dynamicLabelGapSpacing = if (authMode == AuthMode.SIGN_IN) 6.dp else 3.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .blur(backgroundBlurAlpha)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // 🚀 FIXED: Anchors structural elements down top-to-bottom uniformly
        ) {
            // Static Baseline Header offsets (Stops position changes when switching modes)
            Spacer(modifier = Modifier.height(44.dp))

            // --- 1. BRAND LOGO HEADER ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chatgpt_image_15_juin_2026__15_53_52),
                    contentDescription = "CivicConnect Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .width(54.dp)
                        .fillMaxHeight()
                )
                Text(
                    text = "CivicConnect",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D1B2A)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. MULTI-ROLE TAB SEGMENT SELECTOR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFEFEF).copy(alpha = 0.7f))
                    .padding(4.dp)
            ) {
                val activeColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77))
                val inactiveColors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)

                Button(
                    onClick = { selectedRole = UserRole.RESIDENT },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (selectedRole == UserRole.RESIDENT) activeColors else inactiveColors,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = if (selectedRole == UserRole.RESIDENT) Color.White else Color(0xFF0D1B2A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Resident", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == UserRole.RESIDENT) Color.White else Color(0xFF0D1B2A))
                }

                Button(
                    onClick = { selectedRole = UserRole.AGENCY },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (selectedRole == UserRole.AGENCY) activeColors else inactiveColors,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = if (selectedRole == UserRole.AGENCY) Color.White else Color(0xFF0D1B2A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Agency", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedRole == UserRole.AGENCY) Color.White else Color(0xFF0D1B2A))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. MAIN COMPONENT AUTHCARD CONTAINER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.03f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.92f),
                                Color.White.copy(alpha = 0.75f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.30f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(dynamicCardInternalPadding), // 🚀 FIXED: Card dynamically expands sizing structure based on authentication states
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = if (authMode == AuthMode.SIGN_UP) "Create an Account" else "Welcome Back", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
                Text(
                    text = if (authMode == AuthMode.SIGN_UP) "Join your community to start reporting issues." else "Sign in to track existing local neighborhood reports.",
                    fontSize = 12.sp, color = Color(0xFF778DA9), modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // --- TEXT BOX FIELDS MAP ---
                if (authMode == AuthMode.SIGN_UP) {
                    Text(text = "First Name", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A)) // 🚀 FIXED: Font upscale
                    Spacer(modifier = Modifier.height(dynamicLabelGapSpacing))
                    CivicCustomTextField(
                        value = firstName, onValueChange = { firstName = it },
                        placeholder = "John",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF415A77), modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(dynamicInterFieldSpacing))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Last Name", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A)) // 🚀 FIXED: Font upscale
                        AnimatedVisibility(visible = isLastNameInvalid, enter = fadeIn(), exit = fadeOut()) {
                            Text(text = "Invalid Characters", color = Color(0xFFE63946), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(dynamicLabelGapSpacing))
                    CivicCustomTextField(
                        value = lastName, onValueChange = { lastName = it },
                        placeholder = "Doe",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF415A77), modifier = Modifier.size(16.dp)) },
                        isError = isLastNameInvalid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(dynamicInterFieldSpacing))
                }

                Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A)) // 🚀 FIXED: Font upscale
                Spacer(modifier = Modifier.height(dynamicLabelGapSpacing))
                CivicCustomTextField(
                    value = email, onValueChange = { email = it },
                    placeholder = "resident@civicconnect.app",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF415A77), modifier = Modifier.size(16.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(dynamicInterFieldSpacing))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A)) // 🚀 FIXED: Font upscale
                    if (authMode == AuthMode.SIGN_IN) {
                        Text(text = "forgot password?", color = Color(0xFF50BB6E), fontSize = 12.sp, modifier = Modifier.clickable { })
                    }
                }
                Spacer(modifier = Modifier.height(dynamicLabelGapSpacing))
                CivicCustomTextField(
                    value = password, onValueChange = { password = it },
                    placeholder = "**********",
                    leadingIcon = { Icon(Icons.Default.Key, null, tint = Color(0xFF415A77), modifier = Modifier.size(16.dp)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                if (authMode == AuthMode.SIGN_UP) {
                    Spacer(modifier = Modifier.height(dynamicInterFieldSpacing))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Confirm Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A)) // 🚀 FIXED: Font upscale
                        AnimatedVisibility(visible = isPasswordMismatched, enter = fadeIn(), exit = fadeOut()) {
                            Text(text = "Password does not match", color = Color(0xFFE63946), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(dynamicLabelGapSpacing))
                    CivicCustomTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        placeholder = "**********",
                        leadingIcon = { Icon(Icons.Default.Key, null, tint = Color(0xFF415A77), modifier = Modifier.size(16.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = isPasswordMismatched,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- 4. LEGAL DISCLOSURE ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToPolicies, onCheckedChange = { agreedToPolicies = it ?: false },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0D1B2A)),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(modifier = Modifier.weight(1f)) {
                        Text(text = "I agree to the ", fontSize = 12.sp, color = Color(0xFF1B263B))
                        Text(
                            text = "Terms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A),
                            modifier = Modifier.clickable { activeLegalDialog = LegalContentType.TERMS }
                        )
                        Text(text = " and ", fontSize = 12.sp, color = Color(0xFF1B263B))
                        Text(
                            text = "Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A),
                            modifier = Modifier.clickable { activeLegalDialog = LegalContentType.PRIVACY }
                        )
                    }
                }

                // --- 5. SUBMIT ACTION BUTTONS & GOOGLE PILL HUB ---
                Row(
                    modifier = Modifier.fillMaxWidth().height(46.dp), // 🚀 FIXED: Slightly compressed button height matrix for defensive single-screen budgeting
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isFormInputValid && !isBackendProcessing) {
                                isBackendProcessing = true
                                if (firebaseAuth != null) {
                                    if (authMode == AuthMode.SIGN_UP) {
                                        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
                                            .addOnSuccessListener {
                                                UserSessionManager.saveSession(firstName.trim())
                                                isBackendProcessing = false
                                                onLoginSuccess()
                                            }
                                            .addOnFailureListener { ex ->
                                                isBackendProcessing = false
                                                Toast.makeText(context, "Error: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                                            .addOnSuccessListener {
                                                UserSessionManager.saveSession("User Profile")
                                                isBackendProcessing = false
                                                onLoginSuccess()
                                            }
                                            .addOnFailureListener { ex ->
                                                isBackendProcessing = false
                                                Toast.makeText(context, "Failed: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                } else {
                                    val resolvedDisplayName = if (authMode == AuthMode.SIGN_UP) firstName.trim() else "John"
                                    UserSessionManager.saveSession(resolvedDisplayName)
                                    isBackendProcessing = false
                                    onLoginSuccess()
                                }
                            }
                        },
                        enabled = isFormInputValid && !isBackendProcessing,
                        modifier = Modifier.weight(1.5f).fillMaxHeight().shadow(4.dp, RoundedCornerShape(999.dp)),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77), disabledContainerColor = Color(0xFFE0E1DD))
                    ) {
                        if (isBackendProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(text = if (authMode == AuthMode.SIGN_UP) "Create Account" else "Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.4f).background(Color(0xFFE0E1DD)))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0E1DD), RoundedCornerShape(999.dp))
                            .clickable { Toast.makeText(context, "Connecting to Google Auth API...", Toast.LENGTH_SHORT).show() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "G", color = Color(0xFFE63946), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Google", color = Color(0xFF0D1B2A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 6. FOOTER SWITCH TOGGLE ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(text = if (authMode == AuthMode.SIGN_UP) "Already have an account? " else "Don't have an account yet? ", fontSize = 12.sp, color = Color(0xFF778DA9))
                    Text(
                        text = if (authMode == AuthMode.SIGN_UP) "Sign In" else "Sign Up", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A),
                        modifier = Modifier.clickable {
                            authMode = if (authMode == AuthMode.SIGN_UP) AuthMode.SIGN_IN else AuthMode.SIGN_UP
                        }
                    )
                }
            }
        }

        if (activeLegalDialog != null) {
            LegalGlassDialog(contentType = activeLegalDialog!!, onDismiss = { activeLegalDialog = null })
        }
    }
}

// --- 🚀 UPGRADED INTERACTIVE INPUT TEXT FIELD SCENE ---
@Composable
fun CivicCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(38.dp) // 🚀 FIXED: Upscaled interaction frame boundary lines for better contrast breathing room
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isError) Color(0xFFE63946) else Color(0xFFE0E1DD),
                shape = RoundedCornerShape(8.dp)
            ),
        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0D1B2A)), // 🚀 FIXED: Font size upscale to 14.sp
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.LightGray, fontSize = 14.sp) // 🚀 FIXED: Placeholder scale parity match
                    }
                    innerTextField()
                }
            }
        }
    )
}