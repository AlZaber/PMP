package com.solvetech.pmp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WordListActivity extends AppCompatActivity {
    private String category;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private static final int REQUEST_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermission();
        Intent i = getIntent();
        category = i.getStringExtra("category");
        ref = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        listView = findViewById(R.id.listView);
        // now create an adapter class

        final MyAdapter adapter = new MyAdapter(this, words, meanings, images);
        listView.setAdapter(adapter);

        ref.child("words").child(category).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String word = dataSnapshot.getKey();
                String mean = dataSnapshot.child("meaning").getValue(String.class);
                String audio = dataSnapshot.child("audio").getValue(String.class);

                download(word);
                words.add(word);
                meanings.add(mean);
                audios.add(audio);
//                adapter.add(value);
//                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // now set item click on list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaPlayer player = new MediaPlayer();
                try {
                    player.setDataSource(audios.get(position));
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    player.prepare();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        // so item click is done now check list view
    }

    private void download(final String file_name){
        String file = WordListActivity.this.getFilesDir()+ File.separator +"Download" + File.separator + file_name + ".mp3";

        File f = new File(file);
        Toast.makeText(WordListActivity.this, f.getPath() +" : " + f.exists(), Toast.LENGTH_SHORT).show();

        if(f.exists()) {
            Toast.makeText(WordListActivity.this, file_name + " already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference sref = storageRef.child(file_name+".mp3");
        sref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadfile(WordListActivity.this, file_name, ".mp3",
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

    private void downloadfile(Context context, String filename, String fileext, String dst, String url){
        DownloadManager dmanager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, dst, filename + fileext);

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
        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    ListView listView;
    ArrayList<String>  words = new ArrayList<>();
    ArrayList<String>  meanings = new ArrayList<>();
    ArrayList<String>  audios = new ArrayList<>();
    int images = R.drawable.fui_ic_phone_white_24dp;
    // so our images and other things are set in array

    // now paste some images in drawable

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> words;
        ArrayList<String>  meanings;
        int rImg;

        MyAdapter (Context c, ArrayList<String>  words, ArrayList<String>  meanings, int img) {
            super(c, R.layout.row, R.id.textView1, words);
            this.context = c;
            this.words = words;
            this.meanings = meanings;
            this.rImg = img;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView myTitle = row.findViewById(R.id.textView1);
            TextView myDescription = row.findViewById(R.id.textView2);

            // now set our resources on views
            images.setImageResource(rImg);
            myTitle.setText(words.get(position));
            myDescription.setText(meanings.get(position));

            return row;
        }
    }
}
