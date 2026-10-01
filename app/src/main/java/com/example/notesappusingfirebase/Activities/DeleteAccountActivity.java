package com.example.notesappusingfirebase.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class DeleteAccountActivity extends AppCompatActivity {

    ActivityDeleteAccountBinding binding;
    String provider = "";
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String text = "If You Don't Know Your Password, Reset Here";
        SpannableString spannableString = new SpannableString(text);

// Define the range of "Click Here"
        int start = text.indexOf("Reset Here");
        int end = start + "Reset Here".length();

// Make it clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 👇 Action when user clicks "Click Here"
                Intent intent = new Intent(DeleteAccountActivity.this, ForgotPasswordActivity.class);
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

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        assert user != null;
        for (UserInfo info : user.getProviderData()) {
            provider = info.getProviderId();
        }

        if (provider.equals("google.com")) {
            binding.passwordLayout.setVisibility(View.GONE);
            binding.knowCurrentPinTv.setVisibility(View.GONE);
        }else {
            binding.passwordLayout.setVisibility(View.VISIBLE);
        }

        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String psw = binding.passwordEt.getText().toString().trim();

                if ( !psw.isEmpty() && provider.equals("password")){
                    // EMAIL-PASSWORD USER → re-auth with password
                    reAuthWithPasswordAndDelete(user,psw);
                }else if (provider.equals("google.com")){
                    // GOOGLE USER → re-auth with google
                   reAuthWithGoogleAndDelete(user);
                }else if (psw.isEmpty()){
                    binding.passwordEt.setError("Password Required");
                    binding.passwordEt.requestFocus();
                }else {
                    Toast.makeText(DeleteAccountActivity.this, "Unknown Method Call", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private void reAuthWithPasswordAndDelete(FirebaseUser user, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> deleteAllUserData(user))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Wrong password!", Toast.LENGTH_SHORT).show());
    }

    private void reAuthWithGoogleAndDelete(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            Toast.makeText(this, "Google sign-in expired, sign in again!", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> deleteAllUserData(user))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Re-auth failed!", Toast.LENGTH_SHORT).show());
    }

    private void deleteAllUserData(FirebaseUser user) {
        String uid = user.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("NotesUserAccounts").child(uid);
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("NotesModel").child(uid);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // 1️⃣ Delete profile image
        storage.getReference("NotesModel/"+"profile"+uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });

        // 2️⃣ Delete all note images
        notesRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot note : snapshot.getChildren()) {
                String imageUrl = "" + note.child("url").getValue();

                if (!imageUrl.equals("null") && !imageUrl.isEmpty()) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
                }
            }

            // 3️⃣ Delete notesModel
            notesRef.removeValue();

            // 4️⃣ Delete user data
            userRef.removeValue();

            // 5️⃣ Delete Auth Account
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete auth account!", Toast.LENGTH_SHORT).show();
                    });

        });
    }


}