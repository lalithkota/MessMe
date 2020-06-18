package com.example.messme;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VideoCall2Activity extends AppCompatActivity {

    MediaRecorder mediaRecorder;
    DatagramSocket datagramSocket;
    ParcelFileDescriptor parcelFileDescriptor;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);


        try{
            datagramSocket = new DatagramSocket(ChatHandler.port+2, InetAddress.getByName("192.168.0.103"));
            parcelFileDescriptor = ParcelFileDescriptor.fromDatagramSocket(datagramSocket);
        }
        catch (IOException e){
            Log.e("My Error MessMe","Cant open udp socket recorder", e);
            finish();
        }

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //mediaRecorder.setCamera(Camera.open(1));

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setOutputFile(parcelFileDescriptor.getFileDescriptor());
        mediaRecorder.setVideoSize(ChatHandler.VIDEO_WIDTH,ChatHandler.VIDEO_HEIGHT);
        mediaRecorder.setVideoFrameRate(ChatHandler.FRAME_RATE);
    }

    @Override
    public void onStart(){
        super.onStart();
        int parentHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int parentWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        surfaceView = new SurfaceView(this);
        //printf("lalli is a bad boy; lalli what are yo.ju doing?????????? bathroom going ;juttu peeking; ballblast playing; dichikdichik dancing");
        ((FrameLayout)findViewById(R.id.sender_surface_frame_layout_id)).addView(surfaceView);
        ViewGroup.LayoutParams llparams = surfaceView.getLayoutParams();
        llparams.width = parentWidth - (parentHeight*4/3);
        llparams.height = llparams.width*3/4;
        surfaceView.setLayoutParams(llparams);
        surfaceHolder = surfaceView.getHolder();

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
        }
        catch(IOException e){
            Log.e("My Error MessMe", "Problem during mediaRecorder prepare", e);
            finish();
        }
        try{
            mediaRecorder.start();
        }
        catch(Exception e){
            Log.e("My Error MessMe","Can't start mediarecording",e);
            finish();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        try{ parcelFileDescriptor.close(); } catch(IOException e){ Log.e("My Error MessMe","Cant close parcelFileDescriptor",e); }
        datagramSocket.close();

        finish();
    }
}
