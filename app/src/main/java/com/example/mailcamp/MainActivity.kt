package com.example.mailcamp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mailcamp.ui.theme.MailCampTheme
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.common.SignInButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.AsyncImage
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//import com.google.firebase.storage.FirebaseStorage


class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        setContent {
            MailCampTheme {

                val navController = rememberNavController()
                this.navController = navController // 👈 assign navController to the activity's field

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        val context = LocalContext.current
                        LoginScreen(
                             onLoginClick = {
                                 // Firebase sign-in logic here
                                 val firebaseAuth = FirebaseAuth.getInstance()
                                 firebaseAuth.signInWithEmailAndPassword(email, password)
                                     .addOnCompleteListener { task ->
                                         if (task.isSuccessful) {
                                             Toast.makeText(
                                                 context,
                                                 "Login successful",
                                                 Toast.LENGTH_SHORT
                                             ).show()
                                             navController.navigate("home") {
                                                 popUpTo("login") { inclusive = true }
                                             }
                                         } else {
                                             Toast.makeText(
                                                 context,
                                                 "Login failed: ${task.exception?.message}",
                                                 Toast.LENGTH_SHORT
                                             ).show()
                                         }
                                     }
                             },
                             onGoogleSignInClick = {
                                 googleSignIn(navController)
                             },
                             onCreateAccountClick = {
                                 navController.navigate("signup")
                             },
                             navController = navController
                        )
                    }

                    composable("signup") {
                        var username by remember { mutableStateOf("") }
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }

                        SignUpScreen(
                            navController = navController,
                            onBackClick = { navController.popBackStack() },
                            onSignUpClick = { user, mail, pass ->
                                // Handle Firebase user creation here
                            },
                            onGoogleAccountCreated = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoogleAccountExists = {
                                Toast.makeText(
                                    applicationContext,
                                    "Google account already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            username = username,
                            email = email,
                            password = password,
                            onUsernameChange = { username = it },
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it }
                        )
                    }

                    composable("home") {
                        val viewModel: MainViewModel = viewModel() // Obtain ViewModel here

                        MainScreen(
                            navController = navController, viewModel = viewModel
                        )
                    }
                    composable("profile") { ProfileScreen(navController) }
                }
            }
        }
    }


    private fun googleSignIn(navController: NavController) {
        this.navController = navController
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private lateinit var navController: NavController

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleGoogleSignIn(account, navController)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

    private fun handleGoogleSignIn(account: GoogleSignInAccount?,navController: NavController) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        val isNewUser = result?.additionalUserInfo?.isNewUser ?: false

                        if (isNewUser) {
                            Toast.makeText(
                                this,
                                "Google account not found. Please sign up.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("signup")
                        } else {
                            // If the user already exists, fetch the user data from Firestore
                            val user = firebaseAuth.currentUser
                            user?.let {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users")
                                    .document(it.uid) // Assuming the user document ID is the UID
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val username = documentSnapshot.getString("username") ?: "Unknown"
                                        val subscriberCount = documentSnapshot.getLong("subscribersCount")?.toInt() ?: 0

                                        // Show success message and navigate to home screen with user data
                                        Toast.makeText(
                                            this,
                                            "Login successful",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                            }
                        }
                    }  else {
                        Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}


@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    AndroidView(
        factory = { context ->
            SignInButton(context).apply {
                setSize(SignInButton.SIZE_WIDE)
                setColorScheme(SignInButton.COLOR_AUTO)
                setOnClickListener { onClick() }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}



@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    navController: NavController // Added navController as a parameter to handle navigation
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") } // To show error message
    var showGoogleButton by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var subscriberCount by remember { mutableStateOf(0) }
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6A82FB), // Soft Blue
            Color(0xFFFC5C7D)  // Light Pink
        )
    )
    val context = LocalContext.current // For showing Toast messages
    val firebaseAuth = FirebaseAuth.getInstance()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient) // Apply gradient background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Section
                Text(
                    text = "Welcome to MailChamp!!",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(25.dp))

                // Subtitle Section
                Text(
                    text = "Please Login",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF9D9D9D), // Light Gray
                    fontWeight = FontWeight.Normal,

                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("janice.rodriguez@email.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF3B19E6),
                        unfocusedIndicatorColor = Color(0xFF3B19E6),
                        focusedContainerColor = Color(0xFFF9F8FC),
                        unfocusedContainerColor = Color(0xFFF9F8FC),
                        focusedPlaceholderColor = Color(0xFF3B19E6),
                        unfocusedPlaceholderColor = Color(0xFF3B19E6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF3B19E6),
                        unfocusedIndicatorColor = Color(0xFF3B19E6),
                        focusedContainerColor = Color(0xFFF9F8FC),
                        unfocusedContainerColor = Color(0xFFF9F8FC),
                        focusedPlaceholderColor = Color(0xFF3B19E6),
                        unfocusedPlaceholderColor = Color(0xFF3B19E6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Display error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button with rounded corners and elevated shadow
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both email and password"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            // Perform email/password login
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT)
                                            .show()
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        val exception = task.exception
                                        errorMessage = when (exception) {
                                            is FirebaseAuthInvalidUserException -> "User not found. Please sign up."
                                            is FirebaseAuthInvalidCredentialsException -> "Incorrect credentials. Please try again."
                                            else -> "Login failed: ${exception?.localizedMessage}"
                                        }
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B19E6)),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp) // Add shadow effect
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-in Button
                GoogleSignInButton(onClick = { onGoogleSignInClick() })

                Spacer(modifier = Modifier.height(16.dp))

                // Create Account Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color(0xFF9D9D9D) // Light gray
                    )
                    Text(
                        text = "Create one",
                        color = Color(0xFF3B19E6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onCreateAccountClick)
                    )
                }
            }
        }
    }
}

fun createUserProfileIfNotExists(userId: String, username: String, email: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = firestore.collection("users").document(userId)

    userRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            val userData = hashMapOf(
                "username" to username,
                "email" to email,
                "subscriberCount" to 0
            )
            userRef.set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "User profile created successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to create user profile", e)
                }
        }
    }
}



@Composable
fun SignUpScreen(
    navController: NavHostController, // ✅ Added navController

    onBackClick: () -> Unit,
    onSignUpClick: (String, String, String) -> Unit,
    onGoogleAccountCreated: () -> Unit,
    onGoogleAccountExists: () -> Unit,
    username: String,
    email: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    var username by remember { mutableStateOf("") }
    var showGoogleButton by remember { mutableStateOf(false) }
    // Google Sign-In Client
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Google Sign-In Launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                        val isNewUser = authTask.result?.additionalUserInfo?.isNewUser
                        if (isNewUser == true) {
                            createUserProfileIfNotExists(userId, username, email)

                            Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                            onGoogleAccountCreated()
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Account already exists, please login", Toast.LENGTH_SHORT).show()
                            onGoogleAccountExists()
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Google sign-in failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFEEF2F7) // Soft background color
    ) {

        // UI
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .background(Color(0xFFF9F8FC))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Create account",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF4F83CC), // A refreshing blue color
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(30.dp))


            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (it.isBlank()) {
                        showGoogleButton = false // Auto-hide when field is cleared
                    }
                },
                label = { Text("Username") },
                placeholder = { Text("Enter your username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4F83CC),
                    unfocusedIndicatorColor = Color(0xFFD4D0E7),
                    focusedContainerColor = Color(0xFFF0F4F8),
                    unfocusedContainerColor = Color(0xFFF9F8FC),
                    focusedPlaceholderColor = Color(0xFF5A4E97),
                    unfocusedPlaceholderColor = Color(0xFF5A4E97)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4F83CC),
                    unfocusedIndicatorColor = Color(0xFFD4D0E7),
                    focusedContainerColor = Color(0xFFF0F4F8),
                    unfocusedContainerColor = Color(0xFFF9F8FC),
                    focusedPlaceholderColor = Color(0xFF5A4E97),
                    unfocusedPlaceholderColor = Color(0xFF5A4E97)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Atleast 6 charecters") },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp,horizontal = 10.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4F83CC),
                    unfocusedIndicatorColor = Color(0xFFD4D0E7),
                    focusedContainerColor = Color(0xFFF0F4F8),
                    unfocusedContainerColor = Color(0xFFF9F8FC),
                    focusedPlaceholderColor = Color(0xFF5A4E97),
                    unfocusedPlaceholderColor = Color(0xFF5A4E97)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (password.length < 6) {
                        Toast.makeText(
                            context,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val signInMethods = task.result?.signInMethods
                                    if (!signInMethods.isNullOrEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Account already exists, please login",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // ✅ Create account in FirebaseAuth
                                        FirebaseAuth.getInstance()
                                            .createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { signUpTask ->
                                                if (signUpTask.isSuccessful) {
                                                    val userId =
                                                        FirebaseAuth.getInstance().currentUser?.uid
                                                            ?: ""
                                                    createUserProfileIfNotExists(
                                                        userId,
                                                        username,
                                                        email
                                                    )

                                                    Toast.makeText(
                                                        context,
                                                        "Account created successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.navigate("login") {
                                                        popUpTo("signup") { inclusive = true }
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Signup failed: ${signUpTask.exception?.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error checking email: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F83CC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Up")
            }


            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Or sign up with Google",
                modifier = Modifier
                    .clickable {
                        if (username.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please enter a username before signing up with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showGoogleButton = true
                        }
                    }
                    .padding(8.dp),
                color = Color(0xFF5A4E97),
                textAlign = TextAlign.Center
            )
            if (showGoogleButton) {
                Spacer(modifier = Modifier.height(12.dp))

                GoogleSignInButton(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    }
                )
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController,viewModel: MainViewModel) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) } // Track the refresh state

    val posts by viewModel.posts.collectAsState() // Collect StateFlow into Compose state


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // FAB Icon animation toggle
    val fabIcon = if (showBottomSheet) Icons.Default.Close else Icons.Default.Add

    LaunchedEffect(Unit) {
        isRefreshing = true
        viewModel.fetchPostsFromFirestore {
            isRefreshing = false // Stop refreshing once data is fetched
        }
    }
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            viewModel.fetchPostsFromFirestore {
                // Once data is fetched, stop refreshing
                isRefreshing = false
            }
        }
    ) {
        // Custom loading animation while refreshing
        AnimatedVisibility(visible = isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Create a custom animation (could be a progress bar or custom icon)
                CustomLoadingIndicator()
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                containerColor = Color.White
            ) {

                CreatePostForm(
                    onSubmit = {
                        // Handle actions after post creation (e.g., update UI, refresh content)
                        showBottomSheet = false
                    },
                    onPostCreated = {

                        // Handle any additional actions after the post is created
                    }
                )

            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFFF59D)  // Soft Yellow
            // Light Coral
            // Light blue
// Soft background color
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.fillMaxSize()) {

                    // 🔝 Fixed Row for Profile Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // ← Pushes items apart

                    ) {
                        IconButton(
                            onClick = { navController.navigate("profile") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Change Username") },
                                    onClick = {
                                        expanded = false
                                        // TODO: Handle username change navigation or dialog
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Password") },
                                    onClick = {
                                        expanded = false
                                        // TODO: Handle password change navigation or dialog
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = {
                                        expanded = false
                                        // TODO: Handle sign out logic
                                    }
                                )
                            }
                        }
                    }

                        // 🧾 Scrolling List starts below the profile button
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            items(posts) { post: Post ->
                                PostCard(post = post)
                            }
                        }

                    }
                    // Animate rotation angle (0° when closed, 45° when open)


                    FloatingActionButton(
                        onClick = {
                            showBottomSheet = !showBottomSheet
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add, // Always use Add icon
                            contentDescription = if (showBottomSheet) "Close" else "Create Post",
                        )
                    }

                }
            }
        }

}
@Composable
fun CustomLoadingIndicator() {
    // You can use any animation or custom design here
    // Simple example using a bouncing ball animation for custom effect

    val infiniteTransition = rememberInfiniteTransition()
    val bounceAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0 with LinearEasing
                1f at 500 with LinearEasing
                0f at 1000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.size(30.dp)) {
        val radius = size.minDimension / 2
        drawCircle(
            color = Color(0xFF6200EE), // Custom color (you can change this to any color you'd like)
            radius = radius,
            center = Offset(size.width / 2, (size.height / 2) * (1 + bounceAnim))
        )
    }
}

data class Post(
    val username: String = "",
    val content: String = "",
    val imageBase64: String = "",
    val timestamp: String = ""
)
@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor =    Color(0xFFFFCCBC)  // Pale Peach

        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,  // To avoid overflow if the username is too long
                        modifier = Modifier.weight(1f) // Ensures the username takes up available space
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        val formattedTimestamp = post.timestamp.split(", ")
                        val time = formattedTimestamp.getOrElse(0) { "Unknown time" }
                        val date = formattedTimestamp.getOrElse(1) { "Unknown date" }

                        // Display Time
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF607D8B),
                        )
                        // Display Date on a new line
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF607D8B),
                        )
                    }
                }


                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF455A64)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    base64Image(
                        base64Str = post.imageBase64, // ← Not post.imageRes anymore
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Button(
                    onClick = { /* Like */ },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF6F61), Color(0xFFFF8A65))
                                ),
                                shape = RectangleShape
                            )
                    ) {
                        Text(
                            text = "❤️ Like",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = { /* Subscribe */ },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                                ),
                                shape = RectangleShape
                            )
                    ) {
                        Text(
                            text = "🔔 Subscribe",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun base64Image(base64Str: String, modifier: Modifier = Modifier) {
    val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    val painter = rememberAsyncImagePainter(model = bitmap)

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}


class MainViewModel : ViewModel() {

    // Create a MutableStateFlow to store posts
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts // Expose as StateFlow

    fun fetchPostsFromFirestore(onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        viewModelScope.launch {
            // Adding delay to simulate loading time
            delay(1000) // 2 seconds delay

            db.collection("posts")
                .get()
                .addOnSuccessListener { result ->
                    val posts = result.documents.map { document ->
                        val timestamp = document.getTimestamp("timestamp")
                        val formattedTimestamp = timestamp?.toDate()?.let {
                            val sdf = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
                            sdf.format(it)
                        } ?: "Unknown time"

                        Post(
                            username = document.getString("username") ?: "",
                            content = document.getString("content") ?: "",
                            imageBase64 = document.getString("imageBase64") ?: "",
                            timestamp = formattedTimestamp
                        )
                    }

                    // Sort posts by timestamp in descending order (latest first)
                    val sortedPosts = posts.sortedByDescending {
                        val timestamp = it.timestamp
                        // Convert formatted timestamp back to Date for comparison
                        val date =
                            SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault()).parse(
                                timestamp
                            )
                        date?.time ?: 0L // If parsing fails, default to 0L
                    }

                    // Update the posts state
                    _posts.value = sortedPosts
                    onComplete() // Call onComplete after data is fetched
                }
                .addOnFailureListener {
                    // Handle failure (you can stop refreshing here if desired)
                    onComplete() // Call onComplete after data is fetched
                }
        }
    }

}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val email = currentUser?.email ?: "No Email"
    var username by remember { mutableStateOf("") }
    var subscriberCount by remember { mutableStateOf(0) }
    var posts by remember { mutableStateOf<List<PostUser>>(emptyList()) } // Define the posts state

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(true) {
        currentUser?.let {
            // Fetch user data
            db.collection("users")
                .document(it.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    username = documentSnapshot.getString("username") ?: "Unknown"
                    subscriberCount = documentSnapshot.getLong("subscriberCount")?.toInt() ?: 0
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }

            // Fetch user posts
            db.collection("users")
                .document(it.uid)
                .collection("my_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // 👈 Order posts by timestamp descending

                .get()
                .addOnSuccessListener { snapshot ->
                    posts = snapshot.documents.map { document ->
                        PostUser(
                            id = document.id,
                            content = document.getString("content") ?: "",
                            imageBase64 = document.getString("imageBase64"), // 👈 fetch imageBase64
                            timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAF4FF) // Soft background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Animated Welcome
            AnimatedWelcomeText(username)

            Spacer(modifier = Modifier.height(30.dp))

            // User Data Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)) // Adding shadow for elevation
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF8B9E92) // Soft green card color
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subscribers Text
                    Text(
                        text = "Subscribers: $subscriberCount",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Notification Email Section
                    Text(
                        text = "You will be notified to this email for new posts:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAE25C) // Light Yellow to highlight the email
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))

            // Posts Section
            Text(
                text = "${username}'s Posts",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display the posts in a list
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    PostItem(post = post)
                }
            }
        }
    }
}

@Composable
fun PostItem(post: PostUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timestamp at the top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formatTimestamp(post.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image (if available)
            post.imageBase64?.let { base64 ->
                val bitmap = remember(base64) { decodeBase64ToBitmap(base64) }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = Color.Black
            )
        }
    }
}

fun formatTimestamp(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
}



fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Data class to represent a Post
data class PostUser(
    val id: String,
    val content: String,
    val imageBase64: String? = null, // Rename imageUrl -> imageBase64 to match
    val timestamp: Date
)

@Composable
fun AnimatedWelcomeText(username: String) {
    var fadeIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fadeIn = true
    }

    AnimatedVisibility(
        visible = fadeIn,
        enter = fadeInEnter(),
        exit = fadeOutExit()
    ) {
        Text(
            text = "Welcome $username!!",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EA) // Vibrant purple color for the welcome text
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// Fade In and Out Transitions with adjusted durations
fun fadeInEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = 2000, // Increased duration for fade-in
            delayMillis = 300 // Adding delay for smoother start
        )
    ) + slideInVertically(initialOffsetY = { -40 })
}

fun fadeOutExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = 2000, // Increased duration for fade-out
            delayMillis = 300 // Adding delay for smoother exit
        )
    ) + slideOutVertically(targetOffsetY = { 40 })
}


@Composable
fun CreatePostForm(onSubmit: () -> Unit, onPostCreated: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser // <-- Move this to top
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var username by remember { mutableStateOf("") } // <-- Add this
    val db = FirebaseFirestore.getInstance()
    var isLoading by remember { mutableStateOf(false) } // State for loading

    LaunchedEffect(currentUser?.uid) {
        currentUser?.let {
            db.collection("users")
                .document(it.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    username = documentSnapshot.getString("username") ?: "Anonymous"
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Ensure the form is scrollable on smaller screens
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Text
        Text(
            text = "Create a Post",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Post Content Input
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("What's on your mind?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Slightly taller text field
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
            singleLine = false,

            )


        // Image Picker Box with shadow and hover effects
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF7F53AC), Color(0xFF6200EA)) // Purple gradient
                    )
                )
                .border(2.dp, Color(0xFF6200EA), RoundedCornerShape(16.dp)) // Add border with color
                .clickable { imagePickerLauncher.launch("image/*") }
                .shadow(8.dp, RoundedCornerShape(16.dp), clip = true), // Enhanced shadow
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Tap to select image",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Button(
                onClick = {
                    val userId = currentUser?.uid


                    if (userId != null && selectedImageUri != null) {
                        val base64Image = uriToBase64(context, selectedImageUri!!)

                        if (base64Image != null) {
                            val postId = db.collection("posts").document().id
                            val post = hashMapOf(
                                "postId" to postId,
                                "userUid" to userId,
                                "username" to username,
                                "content" to content,
                                "imageBase64" to base64Image,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            isLoading = true // Start loading

                            // Save to public collection
                            db.collection("posts").document(postId).set(post)
                                .addOnSuccessListener {
                                    // Also save to user-specific collection
                                    db.collection("users").document(userId)
                                        .collection("my_posts").document(postId).set(post)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Post created successfully!\nPlease Refresh the page",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            onSubmit()
                                            onPostCreated() // Additional callback to navigate or update the UI

                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Failed to save to user posts.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Failed to create post.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnCompleteListener {
                                    isLoading = false // Stop loading after completion
                                }
                        } else {
                            Toast.makeText(context, "Could not convert image!", Toast.LENGTH_SHORT)
                                .show()
                        }

                    } else {
                        Toast.makeText(context, "Please select an image!", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Send Post",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

        }
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes()
        inputStream?.close()
        if (byteArray != null) Base64.encodeToString(byteArray, Base64.DEFAULT) else null
    } catch (e: Exception) {
        null
    }
}


