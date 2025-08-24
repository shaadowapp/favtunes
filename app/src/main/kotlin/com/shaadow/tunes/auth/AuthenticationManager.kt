package com.shaadow.tunes.auth

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * High-level authentication manager providing easy-to-use authentication API
 */
class AuthenticationManager private constructor(
    private val context: Context,
    private val config: DeviceTokenConfig = DeviceTokenConfig()
) : AuthEventListener {
    
    private val authImpl = DeviceTokenAuthImpl(context, config)
    private val crossDeviceSync = CrossDeviceSync(
        authImpl.getTokenStorage(),
        authImpl.getDeviceInfoCollector()
    )
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Authentication state
    private val _authState = MutableStateFlow(AuthenticationState.UNKNOWN)
    val authState: StateFlow<AuthenticationState> = _authState.asStateFlow()
    
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()
    
    private val _errorState = MutableStateFlow<AuthError?>(null)
    val errorState: StateFlow<AuthError?> = _errorState.asStateFlow()
    
    init {
        authImpl.setEventListener(this)
        initializeAuthState()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AuthenticationManager? = null
        
        fun getInstance(context: Context, config: DeviceTokenConfig = DeviceTokenConfig()): AuthenticationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthenticationManager(context.applicationContext, config).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Initialize authentication state on app start
     */
    private fun initializeAuthState() {
        scope.launch {
            try {
                _authState.value = AuthenticationState.CHECKING
                
                val status = authImpl.getAuthStatus()
                if (status.isAuthenticated) {
                    _authState.value = AuthenticationState.AUTHENTICATED
                    _userInfo.value = UserInfo(
                        userId = status.userId!!,
                        deviceId = authImpl.getStoredToken()?.deviceId ?: "",
                        isTokenValid = status.isTokenValid,
                        tokenExpiresAt = status.tokenExpiresAt
                    )
                } else {
                    _authState.value = AuthenticationState.NOT_AUTHENTICATED
                }
            } catch (e: Exception) {
                _authState.value = AuthenticationState.ERROR
                _errorState.value = AuthError(
                    type = AuthErrorType.UNKNOWN_ERROR,
                    message = "Failed to initialize authentication: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Perform seamless login (main entry point for authentication)
     * @return True if login was successful
     */
    suspend fun login(): Boolean {
        return try {
            _authState.value = AuthenticationState.AUTHENTICATING
            _errorState.value = null
            
            val result = authImpl.performSeamlessLogin()
            
            if (result.isSuccess) {
                _authState.value = AuthenticationState.AUTHENTICATED
                _userInfo.value = UserInfo(
                    userId = result.userId!!,
                    deviceId = authImpl.getStoredToken()?.deviceId ?: "",
                    isTokenValid = true,
                    tokenExpiresAt = result.newToken?.expiresAt
                )
                true
            } else {
                _authState.value = AuthenticationState.NOT_AUTHENTICATED
                _errorState.value = AuthError(
                    type = result.errorType ?: AuthErrorType.UNKNOWN_ERROR,
                    message = result.errorMessage ?: "Login failed"
                )
                false
            }
        } catch (e: Exception) {
            _authState.value = AuthenticationState.ERROR
            _errorState.value = AuthError(
                type = AuthErrorType.UNKNOWN_ERROR,
                message = "Login error: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Logout and clear authentication data
     * @return True if logout was successful
     */
    suspend fun logout(): Boolean {
        return try {
            val token = authImpl.getStoredToken()?.token
            val success = if (token != null) {
                authImpl.revokeToken(token)
            } else {
                authImpl.clearStoredToken()
            }
            
            if (success) {
                _authState.value = AuthenticationState.NOT_AUTHENTICATED
                _userInfo.value = null
                _errorState.value = null
            }
            
            success
        } catch (e: Exception) {
            _errorState.value = AuthError(
                type = AuthErrorType.UNKNOWN_ERROR,
                message = "Logout error: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return authImpl.hasValidToken()
    }
    
    /**
     * Get current authentication status
     */
    suspend fun getStatus(): AuthStatus {
        return authImpl.getAuthStatus()
    }
    
    /**
     * Force token refresh
     * @return True if refresh was successful
     */
    suspend fun refreshToken(): Boolean {
        return try {
            val currentToken = authImpl.getStoredToken()?.token
            if (currentToken != null) {
                val newToken = authImpl.refreshToken(currentToken)
                newToken != null
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate sync code for cross-device authentication
     * @return Sync code or null if failed
     */
    suspend fun generateSyncCode(): String? {
        return crossDeviceSync.generateSyncCode(getCurrentAppVersion())
    }
    
    /**
     * Import authentication from sync code
     * @param syncCode Sync code from another device
     * @return True if import was successful
     */
    suspend fun importFromSyncCode(syncCode: String): Boolean {
        return try {
            val syncData = crossDeviceSync.parseSyncCode(syncCode)
            if (syncData != null) {
                // Generate new token for this device
                val deviceInfo = authImpl.getCurrentDeviceInfo()
                val request = TokenGenerationRequest(
                    deviceInfo = deviceInfo,
                    userId = syncData.userId
                )
                val newToken = authImpl.generateDeviceToken(request)
                
                if (newToken != null) {
                    // Update authentication state
                    _authState.value = AuthenticationState.AUTHENTICATED
                    _userInfo.value = UserInfo(
                        userId = syncData.userId,
                        deviceId = newToken.deviceId,
                        isTokenValid = true,
                        tokenExpiresAt = newToken.expiresAt
                    )
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            _errorState.value = AuthError(
                type = AuthErrorType.UNKNOWN_ERROR,
                message = "Sync import error: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _errorState.value = null
    }
    
    /**
     * Start automatic token refresh monitoring
     */
    fun startAutoRefresh() {
        authImpl.getRefreshManager().startAutoRefresh(scope)
    }
    
    /**
     * Stop automatic token refresh monitoring
     */
    fun stopAutoRefresh() {
        authImpl.getRefreshManager().stopAutoRefresh()
    }
    
    // AuthEventListener implementation
    override fun onTokenGenerated(token: DeviceToken) {
        scope.launch {
            val userId = authImpl.getTokenStorage().getStoredUserId()
            if (userId != null) {
                _userInfo.value = UserInfo(
                    userId = userId.toString(),
                    deviceId = token.deviceId,
                    isTokenValid = true,
                    tokenExpiresAt = token.expiresAt
                )
            }
        }
    }
    
    override fun onTokenRefreshed(oldToken: String, newToken: DeviceToken) {
        scope.launch {
            val currentUser = _userInfo.value
            if (currentUser != null) {
                _userInfo.value = currentUser.copy(
                    tokenExpiresAt = newToken.expiresAt,
                    isTokenValid = true
                )
            }
        }
    }
    
    override fun onTokenRevoked(token: String) {
        _authState.value = AuthenticationState.NOT_AUTHENTICATED
        _userInfo.value = null
    }
    
    override fun onAuthenticationSuccess(userId: String) {
        _authState.value = AuthenticationState.AUTHENTICATED
    }
    
    override fun onAuthenticationFailure(error: AuthErrorType, message: String) {
        _authState.value = AuthenticationState.ERROR
        _errorState.value = AuthError(type = error, message = message)
    }
    
    /**
     * Get current app version
     */
    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}

/**
 * Authentication state enum
 */
enum class AuthenticationState {
    UNKNOWN,
    CHECKING,
    AUTHENTICATING,
    AUTHENTICATED,
    NOT_AUTHENTICATED,
    ERROR
}

/**
 * User information data class
 */
data class UserInfo(
    val userId: String,
    val deviceId: String,
    val isTokenValid: Boolean,
    val tokenExpiresAt: Long? = null
) {
    /**
     * Check if token is expiring soon
     */
    fun isTokenExpiringSoon(): Boolean {
        return tokenExpiresAt?.let { 
            (it - System.currentTimeMillis()) < AuthConstants.REFRESH_THRESHOLD_MILLIS 
        } ?: false
    }
}

/**
 * Authentication error data class
 */
data class AuthError(
    val type: AuthErrorType,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Check if error is recoverable
     */
    fun isRecoverable(): Boolean {
        return type in listOf(
            AuthErrorType.NETWORK_ERROR,
            AuthErrorType.TOKEN_EXPIRED
        )
    }
}