package com.example.instagramapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.instagramapp.R
import com.example.instagramapp.app.Config
import com.example.instagramapp.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        init()
    }

    private fun init() {
        button_register.setOnClickListener(this)
        text_view_registered_user.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_register -> handleButtonLogin()
            R.id.text_view_registered_user -> handleRegisteredUser()
        }
    }

    private fun handleButtonLogin(){
        var email = edit_text_username.text.toString()
        var password = edit_text_password.text.toString()

        var auth = FirebaseAuth.getInstance()

        /*  Sign the user in by saving the email and password in the database.
            And save the session by using shared preference (locally)
         */
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, object : OnCompleteListener<AuthResult>{
                override fun onComplete(task : Task<AuthResult>){
                    if(task.isSuccessful){
                        Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT).show()
                        var intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(applicationContext, "failed", Toast.LENGTH_SHORT).show()
                    }

                }
            })
    }

    private fun handleRegisteredUser(){
        startActivity(Intent(this, LoginActivity::class.java))
    }

}
