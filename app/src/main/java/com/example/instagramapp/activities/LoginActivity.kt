package com.example.instagramapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.instagramapp.R
import com.example.instagramapp.fragments.PhoneNumberRequestFragment
import com.example.instagramapp.fragments.PhoneNumberVerifyFragment
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.edit_text_password
import kotlinx.android.synthetic.main.activity_register.edit_text_username
import java.util.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val GOOGLE_SIGN_IN_CODE = 101

    //Google Authentication
    lateinit var auth : FirebaseAuth
    lateinit var googleSignInClient : GoogleSignInClient

    //Facebook Authentication
    lateinit var callbackManager : CallbackManager
    lateinit var loginManagerFB: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //initialize the needed variable
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        /*FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);*/

        //Configure Facebook Sign In
        callbackManager = CallbackManager.Factory.create()
        loginManagerFB = LoginManager.getInstance()




        init()
    }

    private fun init(){
        button_login.setOnClickListener(this)
        text_view_new_user.setOnClickListener(this)
        button_google.setOnClickListener(this)
        button_facebook.setOnClickListener(this)
        button_phone_number.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.button_login -> handleButtonLogin()
            R.id.text_view_new_user -> handleTextViewNewUser()
            R.id.button_google -> signInGoogle()
            R.id.button_facebook -> signInFacebookWithLoginManager()
            R.id.button_phone_number -> signInPhoneNumber()
        }
    }

    private fun handleButtonLogin(){

        var email = edit_text_username.text.toString()
        var password = edit_text_password.text.toString()


        /*  Sign the user in by saving the email and password in the database.
            And save the session by using shared preference (locally)
         */
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
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

    private fun handleTextViewNewUser(){
        startActivity(Intent(this, RegisterActivity::class.java))
    }


    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
    }

    private fun signInFacebookWithLoginManager(){
        loginManagerFB.logInWithReadPermissions(this, Arrays.asList("email"))

        loginManagerFB.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult?) {
                Toast.makeText(applicationContext, "login successfull", Toast.LENGTH_SHORT).show()
                handleFacebookAccessToken(loginResult!!.accessToken)
            }

            override fun onCancel() {
                Log.e("error", "login cancelled")
                Toast.makeText(applicationContext, "login is cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Log.e("error", "login failed on exception")
                Toast.makeText(applicationContext, "login is error", Toast.LENGTH_SHORT).show()
            }

        });

    }

    /*private fun signInFacebookWithLoginButton(){

        // Callback registration
        button_facebook.setPermissions(Arrays.asList("email"))

        button_facebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Toast.makeText(applicationContext, "login successfull", Toast.LENGTH_SHORT).show()
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.e("error", "login cancelled")
                Toast.makeText(applicationContext, "login is cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.e("error", "login failed on exception")
                Toast.makeText(applicationContext, "login is error", Toast.LENGTH_SHORT).show()
            }
        })
    }*/

    private fun handleFacebookAccessToken(token : AccessToken){

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))

                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("error", "authentication failed")
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

            }

    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Sign In with Google Failed On Exception", Toast.LENGTH_SHORT).show()
            }
        }


        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser

                    Toast.makeText(this, "Sign In with Google is Successfull", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Sign In with Google Failed", Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun signInPhoneNumber(){
        startActivity(Intent(this, PhoneNumberActivity::class.java))
    }




}
