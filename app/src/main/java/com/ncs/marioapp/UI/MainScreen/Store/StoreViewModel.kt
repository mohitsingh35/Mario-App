package com.ncs.marioapp.UI.MainScreen.Store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ncs.marioapp.Domain.Api.MerchApi
import com.ncs.marioapp.Domain.Models.CommonResponse
import com.ncs.marioapp.Domain.Models.MerchPurchase
import com.ncs.marioapp.Domain.Models.MerchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(val merchApi: MerchApi): ViewModel() {
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _progressState = MutableLiveData<Boolean>(false)
    val progressState: LiveData<Boolean> get() = _progressState

    private val purchaseResult = MutableLiveData<CommonResponse?>()
    val purchaseResultLiveData: LiveData<CommonResponse?> get() = purchaseResult

    private val _getMerch = MutableLiveData<MerchResponse>()
    val getMerch: LiveData<MerchResponse> get() = _getMerch

    private val _purchaseMerch = MutableLiveData<String?>()
    val purchaseMerch: LiveData<String?> get() = _purchaseMerch

    fun getNCSMerch() {
        viewModelScope.launch {
            try {
            val response = merchApi.getMerch()
                if (response.isSuccessful) {
                    val res= response.body().toString()
                    val Merch =Gson().fromJson(res,MerchResponse::class.java)
                    _getMerch.value = Merch
                } else {
                    val error = response.errorBody()?.string()
                    _errorMessage.value = error
                    _getMerch.value = MerchResponse(false,"Something went wrong!!", null)
                }
            }
            catch (e: SocketTimeoutException) {
                Log.e("merchResult", "Request timed out: ${e.message}")
                _errorMessage.value = "Network timeout. Please try again."
            } catch (e: IOException) {
                Log.e("merchResult", "Network error: ${e.message}")
                _errorMessage.value = "Network error. Please check your connection."
            } catch (e: Exception) {
                Log.e("merchResult", "Error: ${e.message}")
                _errorMessage.value = "Something went wrong. Please try again."
            }
        }
    }

    fun purchaseMerch(id: String){
        Log.d("merchResult", "Merch $id")
        viewModelScope.launch {
            try {
                val response = merchApi.buyMerch(payload =  MerchPurchase(id))
                if (response.isSuccessful) {
                    val res= response.body().toString()
                    val comRes =Gson().fromJson(res,CommonResponse::class.java)
                    if(comRes.success){
                        purchaseResult.value = comRes
                        Log.d("merchResult", "Error $comRes")
                    }
                    else{
                        _purchaseMerch.value = comRes.message
                        Log.d("merchResult", "Error ${comRes.message}")
                    }
                }
                else {
                    val error = response.errorBody()?.string()
                    val comRes =Gson().fromJson(error,CommonResponse::class.java)
                    _purchaseMerch.value = comRes.message
                    Log.d("merchResult", "Error $error")
                }
                }
            catch (e: SocketTimeoutException) {
                Log.e("merchResult", "Request timed out: ${e.message}")
                _purchaseMerch.value = "Network timeout. Please try again."
            } catch (e: IOException) {
                Log.e("merchResult", "Network error: ${e.message}")
                _purchaseMerch.value = "Network error. Please check your connection."
            } catch (e: Exception) {
                Log.e("merchResult", "Error: ${e.message}")
                _purchaseMerch.value = "Something went wrong. Please try again."
            }
        }
    }
    fun clearPurchaseResult() {
        purchaseResult.value = null
    }

    fun clearPurchaseMessage() {
        _purchaseMerch.value = null
    }
}