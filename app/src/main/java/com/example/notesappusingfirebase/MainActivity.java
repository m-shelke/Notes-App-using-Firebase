package com.example.notesappusingfirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    //  initiating Firebase Authentication
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //        getting instance of the Firebase here
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is logged-in
        if (firebaseAuth.getCurrentUser() == null){
//            calling goToNextActivity method
            goToNextActivity();
        }
    }

//    method for Intent of LoginActivity to MainActivity
    public void goToNextActivity(){
//        Go LoginActivity to MainActivity via Intent class
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
//        finish stack of the Activity here
        finish();
    }
}