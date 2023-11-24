package com.example.webviewtestapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.webviewtestapplication.model.ModelVideoAgreement

class VideoAgreementViewModel: ViewModel() {
    /**
     * Base URL without credit data
     * */
    companion object {
        private var BASE_URL = "ADD_BASE_URL"
    }

    /**
     * Interface JS name for call in web page
     * */
    val nameInterface = "Android"

    private val _url = MutableLiveData<String>()
    /**
     * URL for view model
     * */
    val url: LiveData<String> = _url
    /**
     * Add user data model for URL Video agreement.
     * */
    fun loadUrlVideoAgreement(model: ModelVideoAgreement) = _url.postValue(initUrlData(model))
    /**
     * Add user data in URL Video agreement.
     * */
    private fun initUrlData(model: ModelVideoAgreement): String {
        return BASE_URL + "?V_Nombre=${model.name}" +
                "&V_Entidad=${model.state}" +
                "&V_Monto=${model.amount}" +
                "&V_Frecuencia=${model.frequency}" +
                "&V_Retencion=${model.retention}" +
                "&V_ID=${model.id}"
    }

    private val _networkState = MutableLiveData<Boolean>()
    /**
     * State connection app
     * */
    val networkState: LiveData<Boolean> = _networkState
    /**
     * Change state connection app
     * */
    fun setNetworkState(state: Boolean) = _networkState.postValue(state)
}