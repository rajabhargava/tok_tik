package com.rajabhargava.android.toktik;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.MediaController;
import android.widget.VideoView;

public class ViewVideo extends Activity {
    private String filename;
    VideoView vv;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        filename = extras.getString("videofilename");
        // vv = new VideoView(getApplicationContext());
        setContentView(R.layout.activity_view);
        vv = (VideoView) findViewById(R.id.videoView);
        vv.setVideoPath(filename);
        vv.setMediaController(new MediaController(this));
        vv.requestFocus();
        vv.start();
    }
}
