package com.example.notesappusingfirebase.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.health.connect.datatypes.ActiveCaloriesBurnedRecord;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    ActivityChangePasswordBinding binding;

    //ProgressDialog to show while sending password recovery instruction
    private ProgressDialog progressDialog;

    private static final String TAG = "CHANGE_PASS_TAG";

    //Firebase Auth for auth related task
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        String text = "If You Don't Know Current Password, then Click Here";
        SpannableString spannableString = new SpannableString(text);

// Define the range of "Click Here"
        int start = text.indexOf("Click Here");
        int end = start + "Click Here".length();

// Make it clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 👇 Action when user clicks "Click Here"
                Intent intent = new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#6750A3")); // highlight color
                ds.setUnderlineText(true); // show underline like a link
                ds.setFakeBoldText(true);
            }
        };

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// Apply to TextView
        binding.knowCurrentPinTv.setText(spannableString);
        binding.knowCurrentPinTv.setMovementMethod(LinkMovementMethod.getInstance());
        binding.knowCurrentPinTv.setHighlightColor(Color.TRANSPARENT);


        //get instance of the firebase auth for Auth related task
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //initiate ProgressDialog to show while changing password
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //readyBtn click, start validate(); method
        binding.changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }


    private void validateData() {

        Log.d(TAG, "validateData: Current Password " + currentPassword);
        Log.d(TAG, "validateData: New Password " + newPassword);
        Log.d(TAG, "validateData: Confirm New Password " + confirmNewPassword);

        //input data
        currentPassword = binding.currentPasswordEt.getText().toString();
        newPassword = binding.newPasswordEt.getText().toString();
        confirmNewPassword = binding.confirmNewPasswordEt.getText().toString();

        //validate data
        if (currentPassword.isEmpty()) {
            //Current Password Filed (currentPasswordEt) is empty, show error in currentPasswordEt
            binding.currentPasswordEt.setError("Please Enter Current Password");
            binding.currentPasswordEt.requestFocus();
        } else if (newPassword.isEmpty()) {
            //New Password Filed (newPasswordEt) is empty, show error in newPasswordEt
            binding.newPasswordEt.setError("Please Enter New Password");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()) {
            //Confirm New Password Filed (confirmNewPasswordEt) is empty, show error in confirmNewPasswordEt
            binding.confirmNewPasswordEt.setError("Please Enter Confirmation Password");
            binding.confirmNewPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)) {
            //password in newPasswordEt and confirmNewPasswordEt does not match, show error in confirmNewPasswordEt
            binding.confirmNewPasswordEt.setError("Password doesn't match");
            binding.confirmNewPasswordEt.requestFocus();
        } else {
            //all data is validated, verify current password is correct first before updating password
            authenticateUserForUpdatePassword();
        }
    }

    private void authenticateUserForUpdatePassword() {

        Log.d(TAG, "authenticateUserForUpdatePassword: ");

        //show progress
        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        //before changing password, re-authenticate the user to check if the has entered correct current password
        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Successful Authentication, being update
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to Authenticate user, may be wrong current password entered
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Failed To Authenticate Due To " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword() {

        Log.d(TAG, "updatePassword: ");

        //show progress
        progressDialog.setMessage("Updating Password ");
        progressDialog.show();

        //being update password, pass the password as parameter
        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //password update success, you may do logout and move to login activity, if you want
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Password Update Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //password update failure,show error message
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Failed To Update Password Due To " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}