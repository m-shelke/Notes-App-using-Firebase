package com.example.notesappusingfirebase.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    //ViewBinding of Activity
    ActivitySplashScreenBinding binding;

    //Animation duration define here
 //   long animationDuration = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating activity layout
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        //attaching activity layout
        setContentView(binding.getRoot());

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashScreenActivity), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //This subclass of ValueAnimator provides support for animating properties on target objects
//        ObjectAnimator animatorY = ObjectAnimator.ofFloat(binding.progressBar,"y",500);
//        ObjectAnimator animatorX = ObjectAnimator.ofFloat(binding.progressBar,"x",350);

        //setting duration for "" AND "Y" axis
//        animatorY.setDuration(animationDuration);
//        animatorX.setDuration(animationDuration);

        //This class plays a set of Animator objects in the specified order. Animations can be set up to play together, in sequence, or after a specified delay
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animatorY,animatorX);
//        animatorSet.start();


        //      This line for Application
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //allow you to implement timeouts, ticks, and other timing-based behavior.
        Handler handler = new Handler();

        //to be run after the specified amount of time elapses
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Launches the MainActivity.
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
               //Closes the current activity and all activities in the current task (useful for preventing the user from navigating back to the splash screen).
                finish();
            }
        },1500);  // Delay of 1.5 seconds
    }
}