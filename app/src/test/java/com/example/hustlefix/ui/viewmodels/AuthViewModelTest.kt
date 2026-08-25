package com.example.hustlefix.ui.viewmodels

import android.content.Context
import com.example.hustlefix.User
import com.example.hustlefix.data.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val userRepository = mockk<UserRepository>()
    private val auth = mockk<FirebaseAuth>()
    private val context = mockk<Context>(relaxed = true)
    private lateinit var viewModel: AuthViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(userRepository, auth)
        
        // Mock static NetworkUtils
        mockkObject(com.example.hustlefix.util.NetworkUtils)
        every { com.example.hustlefix.util.NetworkUtils.isNetworkAvailable(any()) } returns true
        
        // Mock FirebaseMessaging
        mockkStatic(com.google.firebase.messaging.FirebaseMessaging::class)
        val messaging = mockk<com.google.firebase.messaging.FirebaseMessaging>()
        every { com.google.firebase.messaging.FirebaseMessaging.getInstance() } returns messaging
        val tokenTask = mockk<Task<String>>()
        every { messaging.token } returns tokenTask
        every { tokenTask.addOnSuccessListener(any()) } returns tokenTask
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `login rejected when profile missing`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "test-uid"
        
        val task = mockk<Task<AuthResult>>()
        every { task.isSuccessful } returns true
        every { task.exception } returns null
        
        // Handle addOnCompleteListener
        val slot = slot<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            slot.captured.onComplete(task)
            task
        }
        
        every { auth.signInWithEmailAndPassword(any(), any()) } returns task
        every { auth.currentUser } returns firebaseUser
        
        coEvery { userRepository.getUserProfile("test-uid") } returns Result.success(null)
        every { auth.signOut() } just Runs

        viewModel.login("test@example.com", "password", context)

        // Capture state
        val state = viewModel.uiState.value
        assertEquals("User profile not found. Please register.", state.error)
    }

    @Test
    fun `login rejected when user suspended`() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "test-uid"
        
        val task = mockk<Task<AuthResult>>()
        every { task.isSuccessful } returns true
        
        val slot = slot<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>()
        every { task.addOnCompleteListener(capture(slot)) } answers {
            slot.captured.onComplete(task)
            task
        }
        
        every { auth.signInWithEmailAndPassword(any(), any()) } returns task
        every { auth.currentUser } returns firebaseUser
        
        val suspendedUser = User().apply { isSuspended = true }
        coEvery { userRepository.getUserProfile("test-uid") } returns Result.success(suspendedUser)
        every { auth.signOut() } just Runs

        viewModel.login("test@example.com", "password", context)

        val state = viewModel.uiState.value
        assertEquals("Your account has been suspended.", state.error)
    }
}
