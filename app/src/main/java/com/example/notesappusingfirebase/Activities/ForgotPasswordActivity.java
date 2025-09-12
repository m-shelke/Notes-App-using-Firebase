package com.example.notesappusingfirebase.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    //ViewBinding class
    ActivityForgotPasswordBinding binding;

    //TAG for logs in logcat
    private static final String TAG="FORGOT_PASS_TAG";

    //Firebase Auth for auth related task
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while sending password recovery instruction
 //   private ProgressDialog progressDialog;

    //AlertDialog for opening the Default Mail of user
    AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating Layout
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        //Attaching layout root
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //setting slide-in animation while onCreate method get called
        binding.emailTil.startAnimation(AnimationUtils.loadAnimation(ForgotPasswordActivity.this,R.anim.tag_anim));

        //ProgressDialog to show while sending password recovery instruction
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Please Wait..");
//        progressDialog.setCanceledOnTouchOutside(false);

        //attributes of AlertDialog Box
        alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this);
        alertDialog.setTitle("Mail Delivered..");
        alertDialog.setIcon(R.drawable.mail);
        alertDialog.setCancelable(true);


        //get instance of FirebaseAuth to Auth related task
        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth.getCurrentUser() != null){

            FirebaseUser user = firebaseAuth.getCurrentUser();
            String emailStr = user.getEmail();

            binding.emailEt.setText(emailStr);
            binding.emailEt.setEnabled(false);
        }

        //Handle submitBtn click, validate data to start password recovery
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //calling  validateData(); method
                validateData();
            }
        });
    }

    private String email;

    private void validateData() {

        Log.d(TAG, "validateData: ");

        //input data
        email = binding.emailEt.getText().toString().trim();

        // Apply bold style to the whole text
        SpannableString spannable = new SpannableString(email);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, email.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Log.d(TAG, "validateData: Email: "+email);

        //Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email patterns, show error in emailEt
            binding.emailEt.setError("Invalid Email Patterns..");
            binding.emailEt.requestFocus();
        }else {
            email = String.valueOf(spannable);

            //Email patterns is valid, send password recovery instruction
            sendPasswordRecoveryInstructions();
        }

    }

    private void sendPasswordRecoveryInstructions() {

        Log.d(TAG, "sendPasswordRecoveryInstructions: ");

        //show progressDialog
//        progressDialog.setMessage("Sending Password Recovery Mail On "+email);
//        progressDialog.show();


        alertDialog.setMessage("Instruction Sent on "+email);



        alertDialog.setPositiveButton("Open Mail", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ForgotPasswordActivity.this, "Mail Box will be open", Toast.LENGTH_SHORT).show();
                    }
                });












        //send password recovery instruction, pass the input email as parameter
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //instruction sent, check email. Sometimes it goes in the spam folder, so if not in inbox check the your spam folder
                  //      progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Password Reset Mail Sent To: "+email, Toast.LENGTH_SHORT).show();

                        alertDialog.show();

                        //changing start icon of TextInputLayout, when password recovery email sent successfully
                        binding.emailTil.setStartIconDrawable(R.drawable.baseline_check_circle_24);

                        //click event on TextInputLayout's starting icon
                        binding.emailTil.setStartIconOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //showing toast message, when start icon get clicked
                                Toast.makeText(ForgotPasswordActivity.this, "Email Sent, Check it-out", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //now visible TextInputLayout Icon
                        binding.emailTil.setStartIconVisible(true);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to sent instruction
                        Log.e(TAG, "onFailure: ",e);
//                        progressDialog.dismiss();

                        Toast.makeText(ForgotPasswordActivity.this, "Failed To Sent Password Reset Email: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}