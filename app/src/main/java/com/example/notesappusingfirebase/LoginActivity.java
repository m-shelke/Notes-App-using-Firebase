package com.example.notesappusingfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

//  store requestCode_SignIn as a 11 in Integer
    private final static int requestCode_SignIn = 100;

//  TAG for logging error in LogCat
    private static final String TAG = "LOGIN_ACTIVITY";

    //ProgressDialog obj
    private ProgressDialog progressDialog;

//  ViewBinding class obj
    ActivityLoginBinding binding;

//  initiating Firebase Authentication
    FirebaseAuth firebaseAuth;

//  initiating Realtime Firebase database
    FirebaseDatabase firebaseDatabase;

//  Enabling GoogleSignInClient for Google ID sign-in
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

//      inflating Layout
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
//      Attaching root
        setContentView(binding.getRoot());

//        This whole code for changing the background color of StatusBar for this activity
       /*  Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(LoginActivity.this,R.color.white));*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//      This line for Application
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//      This line only for Activity
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//        This line for hiding StatusBar for this activity
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        this line is for setting status-bar background color
//        getWindow().setStatusBarColor(getResources().getColor(R.color.blue));


//        getting instance of the Firebase here
        firebaseAuth = FirebaseAuth.getInstance();

        //initiate setup ProgressDialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...!!");
        progressDialog.setCanceledOnTouchOutside(false);

//        getting instance of the RealTime Firebase Database here
        firebaseDatabase = FirebaseDatabase.getInstance();

        //if the user is logged-in
        if (firebaseAuth.getCurrentUser() != null){

//            calling goToNextActivity method
            goToNextActivity();
        }

//        calling showAccounts(); method
        showAccounts();

//        click event on googleLoginBtn linearlayout
        binding.googleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                calling signIn(); method
                signIn();
            }
        });

        //handle loginBtn click, start login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //calling validateData(); method
                validateData();
            }
        });

        //handle noAccountTv click, open RegisterEmailActivity to register user with email and password
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if user clicked noAccount layout tag, jumped him to RegisterActivity and don't finish this activity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //Handle forgotPasswordTv click, open ForgotPasswordActivity to send Password recovery instruction to registered email
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if user clicked forgotPasswordTv layout tag, jumped him to ForgotPasswordActivity and don't finish this activity
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

    }

    //Declaring variable
    private String email,password;

    //creating method for validating email and password
    private void validateData() {

        //input data
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();

        //logged email and password, that user enter
        Log.d(TAG,"ValidateData: email: "+email);
        Log.d(TAG,"ValidateData: Password: "+password);

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            //email pattern is Invalid, show error
            binding.emailEt.setError("Invalid Email..");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {

            //password is not entered, show error
            binding.passwordEt.setError("Enter Password...");
            binding.passwordEt.requestFocus();
        }else {
            //email pattern is valid and password is entered
            loginUser();
        }
    }

    ///creating method for login user, if email and password match
    private void loginUser() {

        //show progress..
        progressDialog.setMessage("Login In..");
        progressDialog.show();

        //start user login
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //user login success
                        Log.d(TAG,"onSuccess: Logged In...");

                        //dismissing progressDialog
                        progressDialog.dismiss();

                        //start MainActivity, if login operation get success
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //user login failed
                        Log.e(TAG,"onFailure: ",e);

                        //Toasting error of login operation
                        Toast.makeText(LoginActivity.this, "Login Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                        //dismissing progressDialog
                        progressDialog.dismiss();
                    }
                });
    }

//    creating separate method for showing all Google Accounts of user
    private void showAccounts() {

//        For accessed of the Google Account, we have provide some Data and Tokens to the Google. It can done vie GoogleSignInOption
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                automatically fetch it form Google_services.json file
                .requestIdToken(getString(R.string.default_web_client_id))
//                request Email account to Google
                .requestEmail()
//                and finally build
                .build();

//        GoogleSignInClient act as client between application and Google server and it get ID of the User form google server
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

//    creating signIn(); method for signing google account
    private void signIn() {

//        getting Google SignIn Intent for this activity
        Intent intent = googleSignInClient.getSignInIntent();
//        and starting activity here
        startActivityForResult(intent, requestCode_SignIn);

    }


//       (when user chose one of Google Account form the list of Google Account list) From onActivityResult, we get Data of User which he/she clicked on Google Account
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        and if the resultCode is not null, checking that
        if (resultCode == RESULT_OK) {

//        if the requestCode from onActivityResult match with startActivityForResult
            if (requestCode == requestCode_SignIn) {
//            then run the following task and getting Google SignedIn Account data From getSignInIntent with onActivityResult
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


                //try-catch block
                try {

//                    getting result of the GoogleSignInAccount and store it to "account" variable
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    //We logged the account id in logcat
                    Log.d(TAG, "onActivityResult: " + account.getId());

//                    calling authWithGoogle method here and passing idToken via account.getIdToken
                    authWithGoogle(account.getIdToken());

                } catch (ApiException e) {

                    //hiding progress bar if error come
                    binding.progressBar.setVisibility(View.GONE);

                    //catch error
                    throw new RuntimeException(e);

                }
            }
        }
    }

//  creating method for google auth credential signup
    public void authWithGoogle(String idToken) {

//        visible progress bar
        binding.progressBar.setVisibility(View.VISIBLE);

//        hiding ui part of activity
        binding.uiPart.setVisibility(View.GONE);
        binding.SkipCv.setVisibility(View.GONE);

//        GoogleAuthProvider will gives Authentication Credential and store inside credential and passed it to method parameter called "idToken"
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

//        By passing credential to Firebase and signIn With Credential
        firebaseAuth.signInWithCredential(credential)
//                addOnCompleteListener for if signIn With Credential completed
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

//                        if the task is completed, then it Successful otherwise it's failed
                        if (task.isSuccessful()) {

                            //again hiding gif
                            binding.progressBar.setVisibility(View.GONE);

//                           getting current user form firebase
                            FirebaseUser user = firebaseAuth.getCurrentUser();

//                            email should not be null
                            assert user != null;
//                             logging user user email for check purpose
                            Log.e(TAG,"Current User Email"+user.getEmail());

//                            Getting user data form firebase and sent it to the UserModel Java class
                            assert user != null;
                            UserModel firebaseUser = new UserModel(user.getUid(), user.getDisplayName(), Objects.requireNonNull(user.getPhotoUrl()).toString(),user.getEmail());

                            firebaseDatabase.getReference()
                                    .child("NotesUserAccounts")
//                                    Specific and Unique id of User
                                    .child(user.getUid())
                                    .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if the task of uploading data to Firebase Realtime database is successful then.....
                                            if (task.isSuccessful()) {

                                                //again hiding loading gif
                                                binding.progressBar.setVisibility(View.GONE);

                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                                               and finished the all back end stack activity
                                                finishAffinity();

                                            } else {
//                                                In case problem occurred in the Firebase Realtime Database for uploading the data for that Toast message
                                                Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

//                            Getting Google Account Profile Photo Url form the Firebase and Google Server
                            Log.e("Profile", Objects.requireNonNull(user.getPhotoUrl()).toString());
                        } else {
//                            In case, error occurred then display in logcat
                            Log.e("Error", Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()));
                        }
                    }
                });
    }

//         method for Intent of LoginActivity to MainActivity
    public void goToNextActivity(){
//        Go LoginActivity to MainActivity via Intent class
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
//        finish stack of the Activity here
        finish();
    }
}