package com.solvetech.pmp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class WordListActivity extends AppCompatActivity {
    private String category;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private static final int REQUEST_PERMISSION = 101;

    ListView listView;
    ArrayList<String>  words;
    ArrayList<String>  meanings;
    private ArrayList<String> audios;
    private ArrayList<Integer> ids;
    private SoundPool sounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        words = new ArrayList<>();
        ids = new ArrayList<>();
        meanings = new ArrayList<>();

        Intent i = getIntent();
        category = i.getStringExtra("category");
        ref = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        getSupportActionBar().setTitle(category);

        listView = findViewById(R.id.listView2);
        // now create an adapter class

        final MyAdapter adapter = new MyAdapter(this, words, meanings, images);
        listView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WordListActivity.this, TestActivity.class);
                intent.putExtra("category", category);
                intent.putStringArrayListExtra("words", words);
                intent.putStringArrayListExtra("means", meanings);
                startActivity(intent);
            }
        });

        ref.child("words").child(category).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String word = dataSnapshot.getKey();
                String mean = dataSnapshot.child("mean").getValue(String.class);

                String path = Environment.getExternalStorageDirectory().toString()+"/PMP-Audio/";
                ids.add(sounds.load(path + word.toLowerCase() +".mp3", 1));

                words.add(word);
                meanings.add(mean);
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
                sounds.play(ids.get(position),1, 1, 1,0,1);
            }
        });
        // so item click is done now check list view
    }

    private void download(final String file_name){
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
//        request.setDestinationInExternalFilesDir(context, "", filename + fileext);
        request.setDestinationInExternalPublicDir("/PMP-Audio", filename + fileext);
        dmanager.enqueue(request);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(WordListActivity.this, CategoryActivity.class);
        startActivity(i);
        finish();
    }

    int images = R.drawable.ic_play_grey;
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
