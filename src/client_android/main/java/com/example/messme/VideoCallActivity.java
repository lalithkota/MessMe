package com.example.messme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;

public class VideoCallActivity extends AppCompatActivity {

    int user_index;
    Socket call_socket;
    DataInputStream dis_call_socket;
    DataOutputStream dos_call_socket;
    int is_caller;
    Thread call_receive_thread;
    Thread call_send_thread;

    //AudioRecord audioRecord = null;
    //AudioTrack audioTrack = null;

    //int initial_images_count;
    //Camera mCamera;
    //SurfaceView surfaceView=null;
    CameraPreview mCameraPreview;
    ImageView mImageView=null;
    Bitmap mCurrentReceiverImage=null;

    byte[] call_receive_buffer;
    byte[] call_send_buffer;
    int call_ack;

    static class VideoAdvThread extends Thread {
        boolean done = false;
        boolean crash_signal = false;
        byte[] data;

        VideoAdvThread(byte[] data) {
            super();
            this.data = data;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new File(getFilesDir(),"media/").mkdir();
        //baos = new ByteArrayOutputStream();
        Intent intent = getIntent();
        is_caller = intent.getIntExtra("is_caller",0);
        user_index = intent.getIntExtra("user_index", -1);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        try{
            mCameraPreview = new CameraPreview(this, Camera.open(1));
            ((FrameLayout)findViewById(R.id.sender_surface_frame_layout_id)).addView(mCameraPreview);
        }
        catch(Exception e){
            Log.e("My Error MessMe","cant create a CameraPreview", e);
            finish();
            return;
        }

        call_send_thread = new Thread(){
            @Override
            public void run(){
                int count=0;
                while (true){
                    try {
                        call_socket = new Socket(ChatHandler.ip, ChatHandler.port+2);
                        dis_call_socket = new DataInputStream(call_socket.getInputStream());
                        dos_call_socket = new DataOutputStream(call_socket.getOutputStream());

                        //write acknowledgement
                        if(is_caller==1) dos_call_socket.write(1);

                        Log.e("My Error MessMe","call_server connect success");
                        break;
                    }
                    catch (IOException e) {
                        Log.e("My Error MessMe", "Unable to Connect to call_server ");
                    }

                    try{wait(200);}catch(Exception e){/**/}
                    count++;
                    if(count>10){
                        finish();
                        Log.e("My Error MessMe","Server Socket didnt start. Quitting.");
                        return;
                    }
                }

                try{call_ack = dis_call_socket.read();} catch(Exception e){Log.e("My Error MessMe","cant receive ack");}

                if(call_ack == 2){
                    finish();
                    return;
                }
                else if(call_ack == 0){
                    finish();
                    // print some error.
                    // Log.e("My Error MessMe","cannot connect to user ");
                    return;
                }

                Log.e("My Error MessMe","Video Call - Player Started 1 ");

                //initial_images_count = 0;
                mCameraPreview.dos_call_socket = dos_call_socket;
                while(mCameraPreview.mCamera==null);
                mCameraPreview.startCameraPreview("while sender thread");

                call_receive_thread.start();

                //Log.e("My Error MessMe","preview started success");
                int image_writer_counter=0;
                while(true){
                    try{
                        synchronized (mCameraPreview.pendingBuffers){
                            //if(image_writer_counter<10) new FileOutputStream(new File(getFilesDir(),"media/fromPendingBuffs"+(image_writer_counter)+".jpeg")).write(mCameraPreview.pendingBuffers.element());
                            //send_in_1k_terms(dos_call_socket,mCameraPreview.pendingBuffers.remove());
                            dos_call_socket.write(mCameraPreview.pendingBuffers.remove());
                            //image_writer_counter++;
                            sleep(50);
                        }
                        sleep(30);
                    }
                    catch(NoSuchElementException e){
                        //list empty do nothing
                    }
                    catch(IOException e){
                        Log.e("My Error MessMe","Cant send buffer: ",e);
                        finish();
                        return;
                    }
                    catch(Exception e){
                        Log.e("My Error MessMe", "Unknown exception ",e);
                    }
                    if(mCameraPreview.mCamera==null){
                        break;
                    }
                }
            }
        };
        call_receive_thread = new Thread(){
            @Override
            public void run(){
                while(true){
                    try{
                        synchronized (dis_call_socket){
                            byte[] buff = new byte[40960];
                            dis_call_socket.read(buff);
                            mCurrentReceiverImage = BitmapFactory.decodeByteArray(buff,0,buff.length);
                            Log.e("My Error MessMe","Bitmap created success");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageBitmap(mCurrentReceiverImage);
                                    Log.e("My Error MessMe","Updating image success");
                                }
                            });
                            try{sleep(80);}catch (Exception e){/**/}
                        }
                    }
                    catch(IOException e){
                        Log.e("My Error MessMe","Cant read from server quitting",e);
                        break;
                    }
                }
            }
        };
        call_send_thread.start();
        //new Thread(){
        //    @Override
        //    public void run(){
        //        while(mImageView==null);
        //        try{
        //            mImageView.post(new Runnable(){
        //                @Override
        //                public void run() {
        //                    if(mCurrentReceiverImage!=null){
        //                        mImageView.setImageBitmap(mCurrentReceiverImage);
        //                        Log.e("My Error MessMe","Updating image success");
        //                    }
        //                    mImageView.postDelayed(this,100);
        //                }
        //            });
        //        }
        //        catch(Exception e){
        //            Log.e("My Error MessMe","Cant update receiving ui: quitting: ", e);
        //            return;
        //        }
        //    }
        //}.start();
    }
    @Override
    public void onStart(){
        super.onStart();

        //initialise ImageView
        int parentHeight = getResources().getDisplayMetrics().heightPixels;
        mImageView = findViewById(R.id.receiver_image_view_id);
        ViewGroup.LayoutParams llparams = mImageView.getLayoutParams();
        llparams.width = parentHeight*4/3;
        llparams.height = parentHeight;
        mImageView.setLayoutParams(llparams);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{ dis_call_socket.close(); dos_call_socket.close(); call_socket.close();}catch(Exception e){/*Ignore exception*/}
        mCameraPreview.stopPreviewAndFreeCamera();
        //if(audioRecord!= null){
        //    audioRecord.stop();
        //    audioRecord.release();
        //}
        //if(audioTrack!=null){
        //    audioTrack.stop();
        //    audioTrack.flush();
        //    audioTrack.release();
        //}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //void send_in_1k_terms(DataOutputStream dos, byte[] data) throws IOException{
    //    for(int i=0;i<data.length/1024;i++){
    //        dos.write(data,i*1024,1024);
    //    }
    //}


}
