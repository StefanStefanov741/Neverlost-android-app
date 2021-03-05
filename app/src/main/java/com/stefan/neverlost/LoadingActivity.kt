package com.stefan.neverlost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        val buttonTimer = Timer()
        buttonTimer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {LoadMain()}
            }
        }, 1500)
    }

    //opens the main activity after 1500ms
    fun LoadMain(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}