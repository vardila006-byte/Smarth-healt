package com.example.myapplication

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// Nombre del proyecto (cámbialo aquí si lo necesitas)
const val APP_NAME = "SMARTH HEALT"

// ─────────────────────────────────────────────
//  NAVEGACIÓN
// ─────────────────────────────────────────────
enum class AppScreen { SPLASH, LOGIN, ROL, CONFIG, MAIN }

enum class UserType(val label: String, val icon: ImageVector) {
    CUIDADOR("Cuidador", Icons.Default.Favorite),
    PACIENTE("Paciente", Icons.Default.Person)
}

enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("Aura", Icons.Default.Home),
    FAMILIA("Mi familia", Icons.Default.Groups),
    GPS("GPS", Icons.Default.LocationOn),
    MENU("Menú", Icons.Default.Menu)
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

/** Flujo: splash → login → elegir rol → mensaje configuración → interfaz principal */
@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }
    var userType by rememberSaveable { mutableStateOf<UserType?>(null) }
    var userEmail by rememberSaveable { mutableStateOf("") }

    when (screen) {
        AppScreen.SPLASH -> {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2000)
                screen = AppScreen.LOGIN
            }
        }

        AppScreen.LOGIN -> LoginScreen(
            onLoginSuccess = { email ->
                userEmail = email
                screen = AppScreen.ROL
            }
        )

        AppScreen.ROL -> RoleScreen(
            onContinue = { tipo ->
                userType = tipo
                screen = AppScreen.CONFIG
            },
            onBack = { screen = AppScreen.LOGIN }
        )

        AppScreen.CONFIG -> ConfigScreen(
            onContinue = { screen = AppScreen.MAIN }
        )

        AppScreen.MAIN -> MainScreen(
            userType = userType ?: UserType.PACIENTE,
            userEmail = userEmail,
            onLogout = {
                userType = null
                userEmail = ""
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
//  LOGIN
// ─────────────────────────────────────────────
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
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
            onClick = { if (validate()) onLoginSuccess(email.trim()) },
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
//  MENSAJE DE CONFIGURACIÓN (después del rol)
// ─────────────────────────────────────────────
@Composable
fun ConfigScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            "Configuración inicial",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Por favor completa la información de la persona con Alzheimer:",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ConfigItem(
            icon = Icons.Default.PhotoLibrary,
            title = "Imágenes de la familia",
            description = "Fotos que ayuden a reconocer a sus seres queridos"
        )
        ConfigItem(
            icon = Icons.Default.Mic,
            title = "Recordatorios de voz",
            description = "Mensajes de audio para medicinas, citas y rutinas"
        )
        ConfigItem(
            icon = Icons.Default.LocationOn,
            title = "Ubicación actual",
            description = "Permite conocer dónde se encuentra en todo momento"
        )
        ConfigItem(
            icon = Icons.Default.Notifications,
            title = "Eventos y recordatorios",
            description = "Alertas diarias importantes"
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
        ) {
            Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun ConfigItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PurpleSoft)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PurpleMain),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(description, fontSize = 13.sp, color = TextGray)
        }
    }
}

// ─────────────────────────────────────────────
//  INTERFAZ PRINCIPAL (encabezado + menú inferior + panel)
// ─────────────────────────────────────────────
@Composable
fun MainScreen(userType: UserType, userEmail: String, onLogout: () -> Unit) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val context = LocalContext.current
    val soon = { Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show() }

    Scaffold(
        topBar = {
            AppHeader(userType = userType, userEmail = userEmail)
        },
        bottomBar = {
            NavigationBar(containerColor = PurpleDark) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == MainTab.HOME) {
                                val selected = currentTab == tab
                                RobotAvatar(
                                    modifier = Modifier.size(30.dp),
                                    headColor = if (selected) PurpleMain else Color.White.copy(alpha = 0.75f),
                                    faceColor = PurpleDark,
                                    eyeColor = Color.White,
                                    bodyColor = if (selected) PurpleMain else Color.White.copy(alpha = 0.75f),
                                    armColor = if (selected) PurpleLight else Color.White.copy(alpha = 0.5f),
                                    accentColor = if (selected) PurpleDark else Color.White.copy(alpha = 0.5f)
                                )
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label, fontSize = 11.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PurpleDark,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MainTab.HOME -> HomeScreen(
                userName = displayName(userEmail),
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.FAMILIA -> SimpleScreen(
                emoji = "👨‍👩‍👧",
                title = "Mi familia",
                message = "Aquí aparecerán las imágenes de la familia",
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.GPS -> SimpleScreen(
                emoji = "📍",
                title = "GPS",
                message = "Aquí se mostrará la ubicación actual",
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.MENU -> MenuPanel(
                onClose = { currentTab = MainTab.HOME },
                onLogout = onLogout,
                onOption = soon,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/** Nombre a mostrar a partir del correo: "juan.perez@x.com" -> "Juan.perez" */
fun displayName(email: String): String =
    email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Usuario" }

/** Encabezado: nombre del proyecto a la izquierda y usuario en la esquina derecha */
@Composable
fun AppHeader(userType: UserType, userEmail: String) {
    val name = displayName(userEmail)
    val initial = name.first().uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = APP_NAME,
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = PurpleMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Datos del usuario que inició sesión
        Row(
            modifier = Modifier
                .widthIn(max = 140.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(PurpleSoft)
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PurpleMain),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = userType.label,
                    fontSize = 11.sp,
                    color = PurpleMain,
                    maxLines = 1
                )
            }
        }
    }
}

/** Pantalla de inicio con el robot asistente (Aura) */
@Composable
fun HomeScreen(userName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(PurpleSoft),
            contentAlignment = Alignment.Center
        ) {
            RobotAvatar(modifier = Modifier.size(110.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Hola, $userName", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

/** Robot dibujado con Canvas, usando la paleta morada de la app */
@Composable
fun RobotAvatar(
    modifier: Modifier = Modifier,
    headColor: Color = PurpleMain,
    faceColor: Color = PurpleDark,
    eyeColor: Color = Color.White,
    bodyColor: Color = PurpleMain,
    armColor: Color = PurpleLight.copy(alpha = 0.55f),
    accentColor: Color = PurpleDark
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Brazo saludando (izquierda)
        val arm = Path().apply {
            moveTo(w * 0.22f, h * 0.80f)
            quadraticBezierTo(w * 0.00f, h * 0.78f, w * 0.06f, h * 0.50f)
        }
        drawPath(
            path = arm,
            color = armColor,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )

        // Antena
        drawLine(
            color = accentColor,
            start = Offset(w * 0.5f, h * 0.07f),
            end = Offset(w * 0.5f, h * 0.17f),
            strokeWidth = w * 0.03f,
            cap = StrokeCap.Round
        )
        drawCircle(color = accentColor, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.06f))

        // Orejas
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(w * 0.08f, h * 0.36f),
            size = Size(w * 0.12f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(w * 0.80f, h * 0.36f),
            size = Size(w * 0.12f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.05f)
        )

        // Cabeza
        drawRoundRect(
            color = headColor,
            topLeft = Offset(w * 0.16f, h * 0.17f),
            size = Size(w * 0.68f, h * 0.45f),
            cornerRadius = CornerRadius(w * 0.13f)
        )

        // Pantalla de la cara
        drawRoundRect(
            color = faceColor,
            topLeft = Offset(w * 0.22f, h * 0.23f),
            size = Size(w * 0.56f, h * 0.33f),
            cornerRadius = CornerRadius(w * 0.10f)
        )

        // Ojos
        drawOval(
            color = eyeColor,
            topLeft = Offset(w * 0.32f, h * 0.32f),
            size = Size(w * 0.13f, h * 0.15f)
        )
        drawOval(
            color = eyeColor,
            topLeft = Offset(w * 0.55f, h * 0.32f),
            size = Size(w * 0.13f, h * 0.15f)
        )

        // Cuerpo (domo)
        drawArc(
            color = bodyColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.26f, h * 0.66f),
            size = Size(w * 0.48f, h * 0.68f)
        )
    }
}

/** Panel del menú: X para cerrar + Eventos, Videos, Gallery */
@Composable
fun MenuPanel(
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onOption: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Botón X para cerrar
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar menú",
                    tint = TextDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Menú",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones en filas de chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuChip("Eventos", Icons.Default.Event, Modifier.weight(1f), onOption)
            MenuChip("Videos", Icons.Default.PlayArrow, Modifier.weight(1f), onOption)
            MenuChip("Gallery", Icons.Default.PhotoLibrary, Modifier.weight(1f), onOption)
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Cerrar sesión", color = Color(0xFFD32F2F), fontSize = 15.sp)
        }
    }
}

@Composable
fun MenuChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(PurpleSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PurpleMain,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

/** Pantalla simple temporal */
@Composable
fun SimpleScreen(emoji: String, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(PurpleSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
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
fun ConfigPreview() {
    MyApplicationTheme {
        ConfigScreen(onContinue = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainPreview() {
    MyApplicationTheme {
        MainScreen(userType = UserType.CUIDADOR, userEmail = "maria.lopez@correo.com", onLogout = {})
    }
}