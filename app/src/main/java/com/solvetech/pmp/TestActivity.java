package com.solvetech.pmp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TestActivity extends AppCompatActivity {

    TextView seriallbl;
    EditText wordtxt;
    EditText meantxt;
    Button playbtn;
    Button submitbtn;

    private String category;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private static final int REQUEST_PERMISSION = 101;

    private ArrayList<Vocab> vocab;
    private ArrayList<String> qWords;
    private ArrayList<String> qMeans;
    private ArrayList<String> wWords;
    private ArrayList<String> wMeans;
    private Vocab currentWord;
    private int pos = 0;
    private SoundPool sounds;
    String path = Environment.getExternalStorageDirectory().toString()+"/PMP-Audio/";
    int images =  R.drawable.ic_play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_test);

        Intent i = getIntent();
        category = i.getStringExtra("category");
        ref = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        vocab = new ArrayList<>();
        wWords = new ArrayList<>();
        wMeans = new ArrayList<>();
        qWords = i.getStringArrayListExtra("words");
        qMeans = i.getStringArrayListExtra("means");
        sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);

        seriallbl = findViewById(R.id.seriallbl);
        wordtxt = findViewById(R.id.wordtxt);
        meantxt = findViewById(R.id.meantxt);
        playbtn = findViewById(R.id.playbtn);
        submitbtn = findViewById(R.id.submitbtn);

        for(int j=0; j<qWords.size(); j++){
            String word = qWords.get(j);
            Log.v("Vocab", word);
            String mean = qMeans.get(j);
            int id = sounds.load(path + word.toLowerCase() +".mp3", 1);
            Vocab v = new Vocab(word, mean, id);
            vocab.add(v);
        }

        Collections.shuffle(vocab, new Random(System.currentTimeMillis()));
        currentWord = vocab.get(0);
        seriallbl.setText((pos+1)+"/"+vocab.size());

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    sounds.play(currentWord.id,1, 1, 1,0,1);
            }
        });

        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = currentWord.word.toLowerCase();
                String mean = currentWord.mean.toLowerCase();
                String ansW = wordtxt.getText().toString().toLowerCase();
                String ansM = meantxt.getText().toString().toLowerCase();
                if(!(word.equals(ansW) && mean.equals(ansM))){
                    wWords.add(currentWord.word);
                    wMeans.add(currentWord.mean);
                }

                wordtxt.setText("");
                wordtxt.setHint("word");
                meantxt.setText("");
                meantxt.setHint("meaning");

                if(pos + 1 < vocab.size()){
                    pos++;
                    currentWord = vocab.get(pos);
                    seriallbl.setText((pos+1)+"/"+vocab.size());
                } else {
                    Intent i = new Intent(TestActivity.this, ResultActivity.class);
                    i.putStringArrayListExtra("words", wWords);
                    i.putStringArrayListExtra("means", wMeans);
                    i.putExtra("total", ""+ vocab.size());
                    i.putExtra("category", category);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(TestActivity.this, WordListActivity.class);
        i.putExtra("category", category);
        startActivity(i);
        finish();
    }

    class Vocab {
        String word;
        String mean;
        int id;

        public Vocab(String word, String mean, int id) {
            this.word = word;
            this.mean = mean;
            this.id = id;
        }
    }
}
