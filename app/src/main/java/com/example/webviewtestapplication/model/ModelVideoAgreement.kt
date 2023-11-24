package com.example.webviewtestapplication.model

/**
 * Model for the video agreement submission. The video agreement may include the following elements:
 * @param id Credit aplication ID.
 * @param name Client's name.
 * @param state Entity where the request is made, for example, Credifiel.
 * @param amount Agreed amount.
 * @param frequency Number of payments, space encoding (%20), and payment period. Using the following format: 50%20Quincenales
 * @param retention Previously selected retention in app.
 * */
data class ModelVideoAgreement(
    val id: String,
    val name: String,
    val state: String,
    val amount: String,
    val frequency: String,
    val retention: String,
)

/**
 * Model for receiving information sent through the JS interface to the mobile application.
 * @param status Describe the status of video submission/storage for mobile status changes.
 * @param message Display to the user an informative message about a possible error that
 * */
data class ModelVideoAgreementResponseJSInterface(
    val state: String,
    val message: String
)