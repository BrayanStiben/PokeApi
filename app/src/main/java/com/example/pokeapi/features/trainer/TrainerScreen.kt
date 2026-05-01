package com.example.pokeapi.features.trainer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pokeapi.R
import com.example.pokeapi.data.model.UserEntity
import com.example.pokeapi.domain.model.Pokemon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerScreen(
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TrainerViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val capturedCount by viewModel.favoriteCount.collectAsState()
    val firstFavorite by viewModel.firstFavorite.collectAsState()
    val isFlipping by viewModel.isFlipping.collectAsState()
    val coinResult by viewModel.coinResult.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.minigameEvent.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    // Animación de la moneda
    val coinRotation by animateFloatAsState(
        targetValue = if (isFlipping) 1080f else if (coinResult == false) 180f else 0f,
        animationSpec = if (isFlipping) {
            infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Restart)
        } else {
            tween(1000, easing = FastOutSlowInEasing)
        }
    )

    val pokeRed = Color(0xFFE3350D)
    val pokeYellow = Color(0xFFFFCB05)

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("ENTRENADOR", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = pokeRed),
                actions = {
                    IconButton(onClick = { 
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
                .drawBehind {
                    drawCircle(
                        color = pokeRed.copy(alpha = 0.05f),
                        radius = size.maxDimension / 1.5f,
                        center = center.copy(y = size.height * 0.1f)
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Carnet 3D
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(240.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 15f * density
                        }
                        .clickable { rotated = !rotated }
                ) {
                    if (rotation <= 90f) {
                        TrainerCardFront(user, capturedCount, firstFavorite, pokeRed, pokeYellow)
                    } else {
                        TrainerCardBack(user, pokeRed, modifier = Modifier.graphicsLayer { rotationY = 180f })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Giro
                Surface(
                    color = pokeRed.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = pokeRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Toca la tarjeta para ver los datos de seguridad",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // SECCIÓN MINIJUEGO
                Text(
                    "MINIJUEGO: CARA O SELLO",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // La Moneda
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            rotationY = coinRotation
                            cameraDistance = 12f * density
                        }
                        .drawBehind {
                            drawCircle(
                                color = pokeYellow,
                                radius = size.maxDimension / 2
                            )
                        }
                        .border(4.dp, Color.White, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (coinRotation % 360f <= 90f || coinRotation % 360f >= 270f) {
                        // CARA: Pokemon o Pokeball
                        val currentPoke = firstFavorite // Capturamos el valor para evitar el error de delegado
                        if (currentPoke != null) {
                            AsyncImage(
                                model = currentPoke.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            PokeballIcon(pokeRed, size = 70.dp)
                        }
                    } else {
                        // SELLO: Fuego
                        Icon(
                            Icons.Default.Whatshot,
                            null,
                            modifier = Modifier.size(70.dp).graphicsLayer { rotationY = 180f },
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { showDialog = true },
                    enabled = !isFlipping,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp).fillMaxWidth(0.6f)
                ) {
                    Text("JUGAR MINIJUEGO", fontWeight = FontWeight.Bold)
                }
                
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("¿Cara o Sello?", fontWeight = FontWeight.Black) },
                        text = { Text("Si ganas, capturarás un Pokémon aleatorio. Si pierdes, uno de tus favoritos escapará.") },
                        confirmButton = {
                            TextButton(onClick = { 
                                viewModel.playMinigame(true)
                                showDialog = false 
                            }) { Text("CARA", fontWeight = FontWeight.Bold, color = pokeRed) }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                viewModel.playMinigame(false)
                                showDialog = false 
                            }) { Text("SELLO", fontWeight = FontWeight.Bold, color = pokeRed) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrainerCardFront(
    user: UserEntity?, 
    captured: Int, 
    firstFavorite: Pokemon?,
    pokeRed: Color,
    pokeYellow: Color
) {
    val cardGradient = Brush.linearGradient(colors = listOf(pokeRed, Color(0xFFB91C1C)))
    val region = firstFavorite?.let { getRegionFromId(it.id) } ?: "---"

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(cardGradient)) {
            Icon(
                Icons.Default.CatchingPokemon,
                null,
                modifier = Modifier.size(220.dp).align(Alignment.BottomEnd).offset(60.dp, 60.dp).graphicsLayer { alpha = 0.15f },
                tint = Color.White
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("POKÉMON TRAINER", color = pokeYellow, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("LICENCIA OFICIAL", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Verified, null, tint = pokeYellow, modifier = Modifier.size(24.dp))
                }

                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(95.dp).border(3.dp, Color.White, CircleShape).padding(4.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val currentPoke = firstFavorite
                        if (currentPoke != null) {
                            AsyncImage(
                                model = currentPoke.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(75.dp).clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            PokeballIcon(pokeRed, size = 65.dp)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text("NOMBRE", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(user?.trainerName?.uppercase() ?: "---", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            Column {
                                Text("REGIÓN", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(region, color = pokeYellow, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Column {
                                Text("CAPTURA", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$captured POKÉMON", color = pokeYellow, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(45.dp).background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text("VALIDEZ INTERREGIONAL: ACTIVA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
fun TrainerCardBack(user: UserEntity?, pokeRed: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().border(2.dp, pokeRed, RoundedCornerShape(20.dp))) {
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).background(pokeRed))
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(id = R.drawable.qr), contentDescription = null, modifier = Modifier.size(80.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TRAINER ID", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(user?.trainerId ?: "#######", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SECURITY CODE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("PIN: ${user?.trainerId?.takeLast(4) ?: "0000"}", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Esta tarjeta es personal e intransferible. El portador está autorizado para participar en torneos oficiales y acceder a los Centros Pokémon.",
                    color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 14.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PokeballIcon(pokeRed: Color, size: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(pokeRed))
            Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Black))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White))
        }
        Box(modifier = Modifier.size(size / 4).background(Color.White, CircleShape).border(1.dp, Color.Black, CircleShape))
    }
}

fun getRegionFromId(id: Int): String {
    return when (id) {
        in 1..151 -> "KANTO"
        in 152..251 -> "JOHTO"
        in 252..386 -> "HOENN"
        in 387..493 -> "SINNOH"
        in 494..649 -> "UNOVA"
        in 650..721 -> "KALOS"
        in 722..809 -> "ALOLA"
        in 810..905 -> "GALAR"
        in 906..1025 -> "PALDEA"
        else -> "WORLD"
    }
}
