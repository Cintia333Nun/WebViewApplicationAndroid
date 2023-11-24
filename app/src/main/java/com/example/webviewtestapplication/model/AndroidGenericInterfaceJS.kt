package com.example.webviewtestapplication.model

import android.webkit.JavascriptInterface

/**
 * Class to define the JS interface for communication between the web page displayed in the
 * web view and the mobile application.
 * @param interfaceResponseService Java interface to update the view layer with the response from
 * the JS interface used in onReceiveVideoAgreement and the methods defined in the main activity.
 * */
class AndroidGenericInterfaceJS(private val interfaceResponseService: TaskInService) {

    /**
     * This interface with the @JavascriptInterface annotation can be called from the web page
     * to communicate with the mobile application. Use the TaskInService interface to update
     * the view layer.
     *
     * This is a brief example for the usage of the JS interface from the server:
     * <!DOCTYPE html>
     * <html lang="en">
     * <head>
     *     <meta charset="UTF-8">
     *     <meta name="viewport" content="width=device-width, initial-scale=1.0">
     *     <title>JavaScript Interface Example</title>
     * </head>
     * <body>
     *
     * <button onclick="showMessage()">Show Message</button>
     *
     * <script type="text/javascript">
     *     function showMessage() {
     *         Android.onReceiveVideoAgreement("OK", "Mensaje de lo realizado/ocurrido");
     *     }
     * </script>
     *
     * </body>
     * </html>
     *
     * @param status Describe the status of video submission/storage for mobile status changes.
     * @param message Display to the user an informative message about a possible error that
     * occurred while saving the video agreement.
     *
     * */
    @JavascriptInterface
    fun onReceiveVideoAgreement(status: String, message: String) {
        println("AndroidGenericInterfaceJS Paso por AndroidGenericInterfaceJS onReceiveVideoAgreement")
        try {
            interfaceResponseService.onSuccessService(
                ModelVideoAgreementResponseJSInterface(status, message)
            )
        } catch (e: Exception) {
            interfaceResponseService.onFailService(
                ModelVideoAgreementResponseJSInterface(
                    "FAIL", "Se produjo un error interno en la aplicación."
                )
            )
        }
    }

}