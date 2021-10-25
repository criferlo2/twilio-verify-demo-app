package com.hashitoapps.twiliotestcriferlo.ui.login.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashitoapps.twiliotestcriferlo.BuildConfig
import com.hashitoapps.twiliotestcriferlo.R
import com.hashitoapps.twiliotestcriferlo.data.extra.ResultWrapper
import com.hashitoapps.twiliotestcriferlo.data.repository.LoginRepository
import com.hashitoapps.twiliotestcriferlo.ui.login.LoggedInUserView
import com.hashitoapps.twiliotestcriferlo.ui.login.LoginFormState
import com.hashitoapps.twiliotestcriferlo.ui.login.LoginResult
import com.twilio.verify.TwilioVerify
import com.twilio.verify.logger.LogLevel
import com.twilio.verify.models.*
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val context = application.applicationContext
    private val twilioVerify = TwilioVerify.Builder(application.applicationContext).apply {
        if (BuildConfig.DEBUG) {
            enableDefaultLoggingService(LogLevel.Debug)
        }
    }.build()

    private val _loginForm = MutableLiveData<LoginFormState>()
    private val pendingLiveData = MutableLiveData<PendingResult<Challenge>>()
    private val updateChallengeLiveData = MutableLiveData<PendingResult<String>>()

    val pendingLiveDataNM: LiveData<PendingResult<Challenge>> = pendingLiveData
    val updateChallengeLiveDataNM: LiveData<PendingResult<String>> = updateChallengeLiveData

    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = loginRepository.login(username, password)

            if (result is ResultWrapper.Success) {
                tokenDevice(result.value.id)
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    private fun tokenDevice(id: String) {
        viewModelScope.launch {
            val result = loginRepository.tokenDevice(id)

            if (result is ResultWrapper.Success) {
                with(result.value) {
                    pushFactor(token, serviceSid, identity, factorType)
                }

            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    private fun pushFactor(token: String, serviceId: String, identity: String, factor: String) {

        var tokenFCM = context.getSharedPreferences("_", AppCompatActivity.MODE_PRIVATE)
            .getString("token", "notoken")

        val factorPayload =
            PushFactorPayload(factor, serviceId, identity, tokenFCM!!, token)

        twilioVerify.createFactor(factorPayload, { factor ->
            val verifyFactorPayload = VerifyPushFactorPayload(factor.sid)
            twilioVerify.verifyFactor(verifyFactorPayload, { factor ->
                registerDevice(identity, factor.sid)
            }, {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            })
        }, {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        })
    }

    fun getPending(factorSid: String, challengeId: String) {
        twilioVerify.getChallenge(challengeId, factorSid,
            { challenge ->
                pendingLiveData.value = PendingResult.PendingSuccess(challenge)
            },
            {
                pendingLiveData.value = PendingResult.PendingError<Nothing>("Error")
            }
        )
    }

    fun clearStorage(factorSid: String) {
        twilioVerify.deleteFactor(factorSid, {
            twilioVerify.clearLocalStorage { }
        }, {
            Log.d("error", it.message.toString())
        })

    }

    fun updateChallenge(factorSid: String, challengeSid: String, newStatus: ChallengeStatus) {
        val updateChallengePayload = UpdatePushChallengePayload(factorSid, challengeSid, newStatus)
        twilioVerify.updateChallenge(updateChallengePayload, {
            updateChallengeLiveData.value =
                PendingResult.PendingSuccess(updateChallengePayload.status.value)
        }, {
            updateChallengeLiveData.value = PendingResult.PendingError<Nothing>("error")
        })
    }

    private fun registerDevice(id: String, sid: String) {
        viewModelScope.launch {
            val result = loginRepository.registerDevice(id, sid)
            if (result is ResultWrapper.Success) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(displayName = result.value.done.toString()))
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 2
    }
}

sealed class PendingResult<out T> {
    data class PendingSuccess<out T>(val value: T) : PendingResult<T>()
    data class PendingError<out T>(val error: String) : PendingResult<Nothing>()
}