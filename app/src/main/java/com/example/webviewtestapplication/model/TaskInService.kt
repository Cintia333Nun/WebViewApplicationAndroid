package com.example.webviewtestapplication.model

/**
 * Interface to communicate the JS interface with the view layer of the mobile application.
 * */
interface TaskInService {
    /**
     * "Send a successful response to update the mobile status.
     * */
    fun onSuccessService(vararg objects: Any)
    /**
     * Send a fail response to update the mobile status or start UI action.
     * */
    fun onFailService(vararg objects: Any)
}