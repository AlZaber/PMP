package com.solvetech.pmp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    TextView seriallbl;
    TextView congtxt;
    ListView listView;
    ArrayList<String> words;
    ArrayList<String>  meanings;
    String total = "";
    private String category;
    private ArrayList<Integer> ids;
    private SoundPool sounds;

    int images =  R.drawable.ic_play_grey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent i = getIntent();

        words = i.getStringArrayListExtra("words");
        meanings = i.getStringArrayListExtra("means");
        total = i.getStringExtra("total");
        category = i.getStringExtra("category");
        ids = new ArrayList<>();
        sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);

        seriallbl = findViewById(R.id.seriallbl2);
        listView = findViewById(R.id.listView2);
        congtxt = findViewById(R.id.congtxt);
        // now create an adapter class
        Button retake = findViewById(R.id.retestbtn);

        congtxt.setVisibility(View.GONE);
        retake.setVisibility(View.GONE);
        if(words.size() > 0)
            retake.setVisibility(View.VISIBLE);
        else
            congtxt.setVisibility(View.VISIBLE);
        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, TestActivity.class);
                intent.putExtra("category", category);
                intent.putStringArrayListExtra("words", words);
                intent.putStringArrayListExtra("means", meanings);
                startActivity(intent);
            }
        });

        final ResultAdapter adapter = new ResultAdapter(this, words, meanings, images);
        listView.setAdapter(adapter);

        seriallbl.setText("Score :  " + (Integer.parseInt(total) - words.size()) + "/" + total);

        String path = Environment.getExternalStorageDirectory().toString()+"/PMP-Audio/";
        for (String word : words ) {
            ids.add(sounds.load(path + word +".mp3", 1));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sounds.play(ids.get(position),1, 1, 1,0,1);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(ResultActivity.this, WordListActivity.class);
        i.putExtra("category", category);
        startActivity(i);
        finish();
    }

    class ResultAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> words;
        ArrayList<String>  meanings;
        int rImg;

        ResultAdapter (Context c, ArrayList<String>  words, ArrayList<String>  meanings, int img) {
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
