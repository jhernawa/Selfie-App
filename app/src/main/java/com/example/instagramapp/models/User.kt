package com.example.instagramapp.models

import java.io.Serializable

data class User(
    var email : String,
    var password : String
) : Serializable
