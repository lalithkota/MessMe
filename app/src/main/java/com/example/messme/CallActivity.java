package com.example.messme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class CallActivity extends AppCompatActivity {


    int user_index;
    Socket call_socket;
    DataInputStream dis_call_socket;
    DataOutputStream dos_call_socket;
    int is_caller;
    Thread call_receive_thread;
    Thread call_send_thread;

    AudioRecord audioRecord = null;
    AudioTrack audioTrack = null;

    byte[] call_receive_buffer;
    byte[] call_send_buffer;
    int call_ack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        is_caller = intent.getIntExtra("is_caller",0);
        user_index = intent.getIntExtra("user_index", -1);

        if(is_caller == 1) {
            setContentView(R.layout.activity_call2);
        }
        else if(is_caller == 0){
            setContentView(R.layout.activity_call);
        }

        ((TextView)findViewById(R.id.caller_title_id)).setText(ChatHandler.allUsersList.get(user_index).name);
        ((TextView)findViewById(R.id.caller_subtitle_id)).setText(ChatHandler.allUsersList.get(user_index).uName);

        call_ack=0;

        call_receive_thread = new Thread(){
            @Override
            public void run() {

                int count=0;
                while (true){
                    try {
                        call_socket = new Socket(ChatHandler.ip, 5556);
                        dis_call_socket = new DataInputStream(call_socket.getInputStream());
                        dos_call_socket = new DataOutputStream(call_socket.getOutputStream());


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
                    Log.e("My Error MessMe","cannot connect to user ");
                    return;
                }

                Log.e("My Error MessMe","Call - Player Started 1 ");

                call_send_thread.start();

                audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,44100,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,1024, AudioTrack.MODE_STREAM);
                call_receive_buffer = new byte[1024];

                audioTrack.play();

                Log.e("My Error MessMe","Call - Player Started 2 ");
                while(true){
                    try{
                        dis_call_socket.read(call_receive_buffer);
                        audioTrack.write(call_receive_buffer,0,1024);
                    }
                    catch(Exception e){
                        break;
                    }
                }
                Log.e("My Error MessMe", "call_quit_success");
                finish();
            }
        };
        call_send_thread = new Thread(){
            @Override
            public void run(){
                Log.e("My Error MessMe","Call - Recording Started 1 ");
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,1024);
                call_send_buffer = new byte[1024];

                audioRecord.startRecording();

                Log.e("My Error MessMe","Call - Recording Started 2 ");
                while(true){
                    try{
                        audioRecord.read(call_send_buffer,0,1024);
                        dos_call_socket.write(call_send_buffer);
                    }
                    catch(Exception e){
                        break;
                    }
                }
                Log.e("My Error MessMe","quit initiate success");
                finish();
            }
        };
        call_receive_thread.start();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{ dis_call_socket.close(); dos_call_socket.close(); call_socket.close();}catch(Exception e){/*Ignore exception*/}

        if(audioRecord!= null){
            audioRecord.stop();
            audioRecord.release();
        }

        if(audioTrack!=null){
            audioTrack.stop();
            audioTrack.flush();
            audioTrack.release();

        }
    }

    public void onGreenTouch(View view){
        new Thread(){
            @Override
            public void run(){
                try{ dos_call_socket.write(1);Log.e("My Error MessMe","green touch success"); } catch(IOException e){ Log.e("My Error MessMe","Green Touch Error",e); }
            }
        }.start();

        setContentView(R.layout.activity_call2);

        ((TextView)findViewById(R.id.caller_title_id)).setText(ChatHandler.allUsersList.get(user_index).name);
        ((TextView)findViewById(R.id.caller_subtitle_id)).setText(ChatHandler.allUsersList.get(user_index).uName);
    }

    public void onRedTouch(View view){
        finish();
    }

}
