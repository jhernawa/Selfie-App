package com.example.instagramapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.instagramapp.R

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        init()

    }

    private fun init() {

        //get full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        /*//animation
        var topAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        var bottomAnim : Animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        //hooks
        image_view_logo.setAnimation(topAnim)*/

        //delay screen
        var thread = Thread{

            kotlin.run{
                Thread.sleep(3000)
            }

            finish() // to kill current activity (android default back button is also used to kill the current activity)
            startActivity(Intent(this, MainActivity::class.java))
        }

        thread.start()

    }
}
