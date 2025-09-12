package com.example.notesappusingfirebase.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.notesappusingfirebase.Fragments.AccountFragment;
import com.example.notesappusingfirebase.Fragments.GroupFragment;
import com.example.notesappusingfirebase.Fragments.HomeFragment;
import com.example.notesappusingfirebase.Fragments.SavedFragment;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //Activity ViewBinding
    ActivityMainBinding binding;

    //TAG for logcat
    private static final String TAG = "MAI_ACTIVITY";

    //Y axis translation
    Float translationYaxis = 100f;
    //variable for checking is menu open or not
    boolean isMenuOpen = false;
    //An interpolator where the change flings forward and overshoots the last value then comes back.
    OvershootInterpolator interpolator = new OvershootInterpolator();

    //storing image uri inside this variable
    private Uri imageUri;

    //  initiating Firebase Authentication
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating activity layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //attaching Activity Layout
        setContentView(binding.getRoot());

        //Edge-Edge to screen configuration
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Set a listener that will be notified when a bottom navigation item is selected. This listener will also be notified when the currently selected item is reselected,
        binding.bottomNv.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        //        getting instance of the Firebase here
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            //user is not logged in, move to LoginActivity
            startLoginOption();
        }

        //Return the FragmentManager for interacting with fragments associated with this activity.
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new HomeFragment()).commit();

        //calling showMenu() method
        showMenu();


    }

    //creating method for show/display the menu
    private void showMenu() {

        //Sets the opacity of the view to a value from 0 to 1, where 0 means the view is completely transparent and 1 means the view is completely opaque.
            binding.editFab.setAlpha(0f);
            binding.drawFab.setAlpha(0f);
            binding.imageFab.setAlpha(0f);

            //The vertical position of this view relative to its top position, in pixels.
            binding.editFab.setTranslationY(translationYaxis);
            binding.drawFab.setTranslationY(translationYaxis);
            binding.imageFab.setTranslationY(translationYaxis);

            //Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
            binding.expandFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //logic for opening and collapsing menu
                    if (isMenuOpen){
                        //calling  closeMenu();
                        closeMenu();
                    }else {
                        //calling openMenu();
                        openMenu();
                    }
                }
            });


        //Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
            binding.editFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //start EditFabActivity
                    startActivity(new Intent(MainActivity.this, EditFabActivity.class));
//                    Toast.makeText(MainActivity.this, "Edit Fab Clicked", Toast.LENGTH_SHORT).show();
                    //and closeMenu();
                    closeMenu();
                }
            });

        //Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
            binding.drawFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //start DrawFabActivity
                    startActivity(new Intent(MainActivity.this, DrawFabActivity.class));
//                    Toast.makeText(MainActivity.this, "Draw Fab Clicked", Toast.LENGTH_SHORT).show();
                    //and closeMenu();
                    closeMenu();
                }
            });

        //Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
        binding.imageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //The SDK version of the software currently running on this hardware device. (public static final int TIRAMISU = 33)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery();
                } else {
                    //Requesting WRITE_EXTERNAL_STORAGE permission
                    requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

//                Toast.makeText(MainActivity.this, "Image Fab Clicked", Toast.LENGTH_SHORT).show();
                //and closeMenu();
                closeMenu();
            }
        });


    }

    //creating method for pick image form gallery
    private void pickImageGallery() {

        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
        //getting image result form gallery
        galleryActivityResultLauncher.launch(intent);
    }

    //Method for getting granting storage permission
    private ActivityResultLauncher<String> requestsStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {

                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    //Let's check if permission is granted or not
                    if (isGranted) {
                        //  Storage Permission granted, we can now launch Gallery to pick Image
                        pickImageGallery();
                    } else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    //ActivityResultLauncher for get the result, if the image is picked
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check, if image is picked or not
                    if (result.getResultCode()==Activity.RESULT_OK){
                        //get data
                        Intent data=result.getData();
                        //get Uri of image picked
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked from Gallery "+imageUri);

                        try {

                            String imageUrl = imageUri.toString();
                            Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                            intent.putExtra("imageUrl",imageUrl);
                            startActivity(intent);

//                            Intent intent = new Intent(MainActivity.this, ImageActivity.class);
//                            intent.setData(imageUri);
//                            startActivity(intent);

                        }catch (Exception e){
                            Toast.makeText(MainActivity.this, "No File Selected: "+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        Log.e(TAG, "onActivityResult: "+imageUri );


                    }else {
                        //Canceled
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    //Method for opening the menu
    private void openMenu() {

        //Logic code for close the menu
        isMenuOpen = !isMenuOpen;

        //Sets a drawable as the content of this ImageView.
        binding.expandFab.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);

        //Set the visibility state of this view
        binding.editFab.setVisibility(View.VISIBLE);
        binding.drawFab.setVisibility(View.VISIBLE);
        binding.imageFab.setVisibility(View.VISIBLE);

        //This method returns a ViewPropertyAnimator object, which can be used to animate specific properties on this View
        binding.editFab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.drawFab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.imageFab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    //Method for closing the menu
    private void closeMenu() {

        //Logic code for open the menu
        isMenuOpen = !isMenuOpen;

        //Sets a drawable as the content of this ImageView.
        binding.expandFab.setImageResource(R.drawable.baseline_keyboard_double_arrow_up_24);

        //Set the visibility state of this view
        binding.editFab.setVisibility(View.GONE);
        binding.drawFab.setVisibility(View.GONE);
        binding.imageFab.setVisibility(View.GONE);

        //This method returns a ViewPropertyAnimator object, which can be used to animate specific properties on this View
        binding.editFab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.drawFab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.imageFab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();

    }

    //Represents a standard bottom navigation bar for application.
    private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //Fragment instance
            Fragment selectedFragment = null;

            //Getting Item id
          int itemId = item.getItemId();

          //checking itemId with menu_bottom.xml
          if (itemId == R.id.menu_home){
              selectedFragment = new HomeFragment();

          } else if (itemId == R.id.menu_group) {
              selectedFragment = new GroupFragment();
          } else if (itemId == R.id.menu_my_save) {
              selectedFragment = new SavedFragment();
          }else {
              selectedFragment = new AccountFragment();
          }

//            Return the FragmentManager for interacting with fragments associated with this activity.
            //Start a series of edit operations on the Fragments associated with this FragmentManager
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,selectedFragment).commit();
            return true;
        }
    };

    //Method for launching Login Activity
    private void startLoginOption(){

        //MainActivity to LoginActivity
        startActivity(new Intent(this, LoginActivity.class));
        //finish this activity here
        finish();
    }
}