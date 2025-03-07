package com.ncs.marioapp.Domain.Models

data class CreateProfileBody(
    val FCM_token: String,
    val admission_number: String,
    val branch: String,
    val domain: List<String>,
    val other_domain:String,
    val name: String,
    val socials: Map<String, String>,
    val year: Int,
    val admitted_to:String,
    val photo_token:String,
    val id_card_token:String
)