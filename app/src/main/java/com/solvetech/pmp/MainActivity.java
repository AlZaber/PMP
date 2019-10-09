package com.solvetech.pmp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";
    private DatabaseReference ref;
    private StorageReference storageRef;
    private ArrayList<String> audios;
    private static final int REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ref = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        audios = new ArrayList<>();
        checkPermission();

        createSignInIntent();
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }
    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                ref.child("words").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String word = child.getKey();
                            if(!audios.contains(word.toLowerCase()+".mp3")){
                                download(word.toLowerCase());
                                audios.add(word.toLowerCase()+".mp3");
                            }
                        }
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                      adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                ref.child("words").addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        startProfileActivity(CategoryActivity.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

//                ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        boolean isNewUser = false;
//                        String key = checkNewUser(dataSnapshot);
//                        if(key != null){
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) { }
//                });

            } else {
                Log.d(TAG, "logged in failed");
                Toast.makeText(MainActivity.this, "Sign in failed. Please Retry", Toast.LENGTH_SHORT).show();
                createSignInIntent();
            }
        }
    }
    // [END auth_fui_result]

    private void download(final String file_name){
        StorageReference sref = storageRef.child(file_name+".mp3");
        sref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadfile(MainActivity.this, file_name, ".mp3",
                        Environment.DIRECTORY_DOWNLOADS, uri.toString());
//                Toast.makeText(WordListActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(WordListActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadfile(Context context, String filename, String fileext, String dst, String url) {
        DownloadManager dmanager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalFilesDir(context, "", filename + fileext);
        request.setDestinationInExternalPublicDir("/PMP-Audio", filename + fileext);
        dmanager.enqueue(request);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
            ) {
                //permission denied
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION);
            } else {
                //permission already granted
                haveFileList();
            }
        } else {
            //system OS is < Marshmallow
            haveFileList();
        }
    }

    private void haveFileList() {
        String path = Environment.getExternalStorageDirectory().toString()+"/PMP-Audio";
//        String path = WordListActivity.this.getFilesDir().toString() + "/Download";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            audios.add(files[i].getName());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    haveFileList();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void startProfileActivity(Class<?> cls){
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
        finish();
    }

    private String checkNewUser(DataSnapshot dataSnapshot){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String key;
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            String user = postSnapshot.child("Email").getValue(String.class);
            /*Log.d(TAG, "User snap: "+postSnapshot);
            Log.d(TAG, "User dtring: "+user);
            Log.d(TAG, "User Email: "+currentUser.getEmail());*/
            if (user.equalsIgnoreCase(currentUser.getEmail())) {
                key = postSnapshot.getKey();
//                Toast.makeText(MainActivity.this, "user already exists", Toast.LENGTH_SHORT).show();
                return key;
            }
        }
        return null;
    }

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_signout]
    }
}
