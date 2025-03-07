package com.ncs.marioapp.UI.StartScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.ncs.marioapp.Domain.Api.ProfileApiService
import com.ncs.marioapp.Domain.HelperClasses.PrefManager
import com.ncs.marioapp.Domain.Models.ProfileData.UserImpDetails
import com.ncs.marioapp.Domain.Models.ServerResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject


@HiltViewModel
class StartScreenViewModel @Inject constructor(
    val profileApiService: ProfileApiService
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _progressState = MutableLiveData<Boolean>(false)
    val progressState: LiveData<Boolean> get() = _progressState

    private val _userDetailsResult = MutableLiveData<Boolean>()
    val userDetailsResult: LiveData<Boolean> get() = _userDetailsResult

    private val _userImpDetails = MutableLiveData<UserImpDetails>(null)
    val userImpDetails: LiveData<UserImpDetails> get() = _userImpDetails

    private val _kycStatus = MutableLiveData<String>(null)
    val kycStatus: LiveData<String> get() = _kycStatus

    private val _banStatus = MutableLiveData<Boolean>(false)
    val banStatus: LiveData<Boolean> get() = _banStatus

    suspend fun fetchUserKYCHeaderToken(): String? {
        return try {
            val response = profileApiService.getKYCHeader()
            if (response.isSuccessful) {
                response.body()?.get("approvalToken")?.let { PrefManager.setKYCHeaderToken(it.asString) }
                response.body()?.get("is_banned")?.asBoolean.also {
                    _banStatus.value = it
                }
                response.body()?.get("is_approved")?.asString.also {
                    _kycStatus.value = it
                }

            } else {
                handleError(response.errorBody()?.string())
                null
            }
        } catch (e: SocketTimeoutException) {
            _errorMessage.value = "Network timeout. Please try again."
            null
        } catch (e: IOException) {
            _errorMessage.value = "Network error. Please check your connection."
            null
        } catch (e: Exception) {
            _errorMessage.value = "Something went wrong. Please try again."
            null
        }
    }

    suspend fun getUserImpDetails(): UserImpDetails? {
        return try {
            val response = profileApiService.getImportantDetails()
            if (response.isSuccessful) {
                response.body()?.let {
                    val user = Gson().fromJson(it.toString(), UserImpDetails::class.java)
                    _userImpDetails.value = user!!
                    _userDetailsResult.value = true
                    user
                }
            } else {
                handleError(response.errorBody()?.string())
                null
            }
        } catch (e: SocketTimeoutException) {
            _errorMessage.value = "Network timeout. Please try again."
            null
        } catch (e: IOException) {
            _errorMessage.value = "Network error. Please check your connection."
            null
        } catch (e: Exception) {
            _errorMessage.value = "Something went wrong. Please try again."
            null
        }
    }


    private fun handleError(errorResponse: String?) {
        val loginResponse = Gson().fromJson(errorResponse, ServerResponse::class.java)
        _userImpDetails.value = UserImpDetails(message = "", photo = "", role = -1,success = false)
        _userDetailsResult.value = false
        _errorMessage.value = loginResponse.message
    }



}