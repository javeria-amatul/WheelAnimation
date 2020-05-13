package com.javeria.wheelanimation

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var progressBarEditText: EditText
    private lateinit var animataButton: Button
    private lateinit var progressBar: CircleSeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBarEditText = findViewById(R.id.percent_of_progress_et)
        animataButton = findViewById(R.id.animate_btn)
        progressBar = findViewById(R.id.picker)
        animataButton.setOnClickListener(this)
        progressBar.setPosition(0)
    }

    override fun onClick(view: View?) {
        try {
            val percentagEntered = Integer.parseInt(progressBarEditText.text.toString())
            if (percentagEntered > 100) {
                Toast.makeText(this, "Enter a value less than 100", Toast.LENGTH_LONG).show()
            } else {
                val percentage = (3.66 * percentagEntered).toInt()
                val animation = ArcAngleAnimation(progressBar, percentage);
                animation.setDuration(1000);
                progressBar.startAnimation(animation);
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Value entered is invalid", Toast.LENGTH_LONG).show()
        }
    }
}
