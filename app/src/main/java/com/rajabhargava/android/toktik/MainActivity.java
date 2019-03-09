package com.rajabhargava.android.toktik;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int VIDEO_CAPTURE = 101;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    private Cursor videocursor;
    private int video_column_index;
    ListView videolist;
    int count;
    String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //onStart();
        //checkPer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button cam = (Button) findViewById(R.id.camera);


        if(!hasCamera()) {
            cam.setEnabled(false);
            Toast.makeText(this,"No camera found",Toast.LENGTH_SHORT).show();
        }

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //File videoFile = new File(Environment.getDataDirectory().getAbsolutePath()+"/1.mp4");
                checkPer();
                Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                //Uri videoUri = Uri.fromFile(videoFile);
                //i.putExtra(MediaStore.EXTRA_OUTPUT,videoUri);
                startActivityForResult(i,VIDEO_CAPTURE);
            }
        });
        checkPer();
        init_phone_video_grid();
    }

    private void checkPer() {
        int hasReadPer = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if(hasReadPer != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        int hasWritePer = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasWritePer != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        int hasCamPer = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA);
        if(hasCamPer != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA},REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();

                try {

                    File newfile;

                    AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                    FileInputStream in = videoAsset.createInputStream();

                    File filepath = Environment.getExternalStorageDirectory();
                    File dir = new File(filepath.getAbsolutePath() + "/" +"TokTik" + "/");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    newfile = new File(dir, "video_"+System.currentTimeMillis()+".mp4");

                    if (newfile.exists()) newfile.delete();



                    OutputStream out = new FileOutputStream(newfile);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();

                    Log.v("", "Copy file successful.");


//                    videoUri = data.getData();
//                    videoview.setVideoURI(videoUri);
//                    videoview.start();



                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    @SuppressWarnings("deprecation")
    private void init_phone_video_grid() {
        System.gc();
        String[] proj = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE };
        checkPer();
        videocursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                proj, null, null, null);
        count = videocursor.getCount();
        Toast.makeText(this,"Videos = " + count,Toast.LENGTH_SHORT).show();
        videolist = (ListView) findViewById(R.id.video_list);
        videolist.setAdapter(new VideoAdapter(getApplicationContext()));
        videolist.setOnItemClickListener(videogridlistener);
    }

    private AdapterView.OnItemClickListener videogridlistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position,
                                long id) {

            System.gc();
            video_column_index = videocursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            videocursor.moveToPosition(position);
            String filename = videocursor.getString(video_column_index);
            Intent intent = new Intent(MainActivity.this,
                    ViewVideo.class);
            intent.putExtra("videofilename", filename);
            startActivity(intent);
        }
    };

    public class VideoAdapter extends BaseAdapter {
        private Context vContext;

        public VideoAdapter(Context c) {
            vContext = c;
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            System.gc();
            ViewHolder holder;
            String id = null;
            convertView = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(vContext).inflate(
                        R.layout.listitem, parent, false);
                holder = new ViewHolder();
                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.txtTitle);
                holder.txtSize = (TextView) convertView
                        .findViewById(R.id.txtSize);
                holder.thumbImage = (ImageView) convertView
                        .findViewById(R.id.imgIcon);
                holder.share = (TextView) convertView.findViewById(R.id.share);


                video_column_index = videocursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                videocursor.moveToPosition(position);
                id = videocursor.getString(video_column_index);
                video_column_index = videocursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                videocursor.moveToPosition(position);
                // id += " Size(KB):" +
                // videocursor.getString(video_column_index);
                holder.txtTitle.setText(id);
                holder.txtSize.setText(" Size(KB):"
                        + videocursor.getString(video_column_index));

                String[] proj = { MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATA };
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
                        MediaStore.Video.Media.DISPLAY_NAME + "=?",
                        new String[] { id }, null);
                cursor.moveToFirst();
                long ids = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media._ID));

                ContentResolver crThumb = getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(
                        crThumb, ids, MediaStore.Video.Thumbnails.MICRO_KIND,
                        options);
                holder.thumbImage.setImageBitmap(curThumb);
                curThumb = null;

                final String filename = videocursor.getString(video_column_index);


                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        //Toast.makeText(MainActivity.this,"Path is"  ,Toast.LENGTH_SHORT).show();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        //Uri uri = Uri.fromFile(getFileStreamPath(filename));
                        //String uri2 = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, uri);
                        Toast.makeText(MainActivity.this,"Path is " + Uri.parse(filename)  ,Toast.LENGTH_SHORT).show();

                        sendIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(filename));
                        sendIntent.setDataAndType(Uri.parse(filename),"video/mp4");
                        //sendIntent.setType("video/*");
                       // sendIntent.setPackage("com.whatsapp");
                        startActivity(sendIntent);
                    }
                });

            } /*
             * else holder = (ViewHolder) convertView.getTag();
             */
            return convertView;
        }
    }

    static class ViewHolder {

        TextView txtTitle;
        TextView txtSize;
        ImageView thumbImage;
        TextView share;
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    protected void onStart() {
//        super.onStart();
//        checkCamPer();
//        checkReadWritePer();
//    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void checkReadWritePer() {
//        int hasReadPer = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
//        if(hasReadPer != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
//        }
//        int hasWritePer = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if(hasWritePer != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void checkCamPer() {
//        int hasCamPer = checkSelfPermission(Manifest.permission.CAMERA);
//        if(hasCamPer != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[] {Manifest.permission.CAMERA},REQUEST_CODE_ASK_PERMISSIONS);
//        }
//    }
}
