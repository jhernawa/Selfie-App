package com.example.instagramapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagramapp.R
import com.example.instagramapp.fragments.PhoneNumberRequestFragment

class PhoneNumberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        init()
    }

    private fun init(){
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, PhoneNumberRequestFragment()).commit()
    }

}
