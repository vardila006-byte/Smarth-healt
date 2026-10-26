package com.example.myapplication

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
//  COLORES
// ─────────────────────────────────────────────
val PurpleLight = Color(0xFF9D4EDD)
val PurpleMain = Color(0xFF7B2CBF)
val PurpleDark = Color(0xFF5A189A)
val PurpleSoft = Color(0xFFF3E8FF)
val TextGray = Color(0xFF8A8A8A)
val TextDark = Color(0xFF2D2D2D)
val BorderGray = Color(0xFFE0E0E0)

// ─────────────────────────────────────────────
//  NAVEGACIÓN
// ─────────────────────────────────────────────
enum class AppScreen { SPLASH, LOGIN, ROL, INICIO }

enum class UserType(val label: String, val icon: ImageVector) {
    CUIDADOR("Cuidador", Icons.Default.Favorite),
    PACIENTE("Paciente", Icons.Default.Person)
}

// ─────────────────────────────────────────────
//  ACTIVITY
// ─────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRoot()
            }
        }
    }
}

/** Flujo: splash → login → elegir rol → pantalla temporal de inicio */
@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }
    var userType by rememberSaveable { mutableStateOf<UserType?>(null) }

    when (screen) {
        AppScreen.SPLASH -> {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2000)
                screen = AppScreen.LOGIN
            }
        }

        AppScreen.LOGIN -> LoginScreen(
            onLoginSuccess = { screen = AppScreen.ROL }
        )

        AppScreen.ROL -> RoleScreen(
            onContinue = { tipo ->
                userType = tipo
                screen = AppScreen.INICIO
            },
            onBack = { screen = AppScreen.LOGIN }
        )

        AppScreen.INICIO -> HomeTempScreen(
            userType = userType ?: UserType.PACIENTE,
            onLogout = {
                userType = null
                screen = AppScreen.LOGIN
            }
        )
    }
}

// ─────────────────────────────────────────────
//  SPLASH
// ─────────────────────────────────────────────
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleLight, PurpleMain, PurpleDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Mi compañía\nsegura",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cuidando tu memoria,\nacompañando tu vida.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  LOGIN (funcional: valida y avanza)
// ─────────────────────────────────────────────
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val soon = { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() }

    fun validate(): Boolean {
        emailError = when {
            email.isBlank() -> "Ingresa tu correo"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido"
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> "Ingresa tu contraseña"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        return emailError == null && passwordError == null
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PurpleMain,
        focusedLabelColor = PurpleMain,
        cursorColor = PurpleMain
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text("Bienvenido", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Inicia sesión para continuar", fontSize = 14.sp, color = TextGray)

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            isError = emailError != null,
            supportingText = if (emailError != null) {
                { Text(emailError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = passwordError != null,
            supportingText = if (passwordError != null) {
                { Text(passwordError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { soon() }) {
                Text("¿Olvidaste tu contraseña?", color = PurpleMain, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { if (validate()) onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
        ) {
            Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5D4F5))
            Text(
                " o continuar con ",
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5D4F5))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { soon() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Google", color = TextDark) }

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedButton(
                onClick = { soon() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Facebook", color = TextDark) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("¿No tienes cuenta? ", color = TextGray, fontSize = 14.sp)
            Text(
                "Regístrate",
                color = PurpleMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { soon() }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  ¿CUIDADOR O PACIENTE?
// ─────────────────────────────────────────────
@Composable
fun RoleScreen(onContinue: (UserType) -> Unit, onBack: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<UserType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text("¿Quién eres?", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Selecciona cómo usarás la app", fontSize = 14.sp, color = TextGray)

        Spacer(modifier = Modifier.height(40.dp))

        RoleCard(
            label = UserType.CUIDADOR.label,
            description = "Cuido o acompaño a una persona",
            icon = UserType.CUIDADOR.icon,
            selected = selected == UserType.CUIDADOR,
            modifier = Modifier.fillMaxWidth()
        ) { selected = UserType.CUIDADOR }

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            label = UserType.PACIENTE.label,
            description = "Yo soy la persona a cuidar",
            icon = UserType.PACIENTE.icon,
            selected = selected == UserType.PACIENTE,
            modifier = Modifier.fillMaxWidth()
        ) { selected = UserType.PACIENTE }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { selected?.let(onContinue) },
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleMain,
                disabledContainerColor = PurpleMain.copy(alpha = 0.4f)
            )
        ) {
            Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBack) {
            Text("← Volver al login", color = PurpleMain, fontSize = 14.sp)
        }
    }
}

@Composable
fun RoleCard(
    label: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) PurpleSoft else Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) PurpleMain else BorderGray,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) PurpleMain else PurpleSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else PurpleMain,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) PurpleMain else TextDark
            )
            Text(text = description, fontSize = 13.sp, color = TextGray)
        }
    }
}

// ─────────────────────────────────────────────
//  PANTALLA TEMPORAL (el asistente se hará después)
// ─────────────────────────────────────────────
@Composable
fun HomeTempScreen(userType: UserType, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SMARTH HEALT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleMain,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(PurpleSoft),
            contentAlignment = Alignment.Center
        ) {
            Text("💜", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sesión iniciada como ${userType.label}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "El asistente estará disponible próximamente",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEWS
// ─────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    MyApplicationTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RolePreview() {
    MyApplicationTheme {
        RoleScreen(onContinue = {}, onBack = {})
    }
}