package com.example.messme;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    SurfaceHolder mHolder;
    Camera mCamera=null;
    DataOutputStream dos_call_socket;
    Queue<byte[]> pendingBuffers;

    CameraPreview(Context context){
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        pendingBuffers = new LinkedList<>();
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    CameraPreview(Context context, Camera camera){
        this(context);
        mCamera = camera;
    }

    public void startCameraPreview(String error_display){
        try{
            mCamera.startPreview();
        }
        catch(Exception e){
            Log.e("My Error MessMe", "Cant start preview " + error_display, e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        int parentHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int parentWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams llparams = getLayoutParams();
        llparams.width = parentWidth - (parentHeight*4/3);
        llparams.height = llparams.width*3/4;
        setLayoutParams(llparams);

        try{
            Camera.Parameters camPars = mCamera.getParameters();
            camPars.setPictureSize(ChatHandler.VIDEO_WIDTH,ChatHandler.VIDEO_HEIGHT);
            camPars.setPreviewSize(ChatHandler.VIDEO_WIDTH,ChatHandler.VIDEO_HEIGHT);
            //camPars.setPreviewFormat(ImageFormat.YV12);

            mCamera.setParameters(camPars);
            mCamera.setDisplayOrientation(0);
            Log.e("My Error MessMe","parameters set success");
        }
        catch(Exception e){
            Log.e("My Error MessMe","cant open cam, this exep ", e);
            ((AppCompatActivity)getContext()).finish();
            return;
        }

        try{
            //mCamera.addCallbackBuffer(new byte[50000]);
            mCamera.setPreviewCallback(this);
        }
        catch(Exception e){
            Log.e("My Error MessMe","Cant set preview callback",e);
        }

        try{
            mCamera.stopFaceDetection();
            mCamera.cancelAutoFocus();
        }
        catch(Exception e){
            Log.e("My Error MessMe","cant change autofocus settings",e);
        }

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d("My Error MessMe", "Error setting preview display while surfaceCreated: ", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        //try{ mCamera.stopPreview();} catch(Exception e){Log.e("My Error MessMe","Cant stop preview while surface changed");}

        // do changes here
        // no changes currently

        //startCameraPreview("while surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreviewAndFreeCamera();
    }

    public void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        byte[] fixed = new byte[40960];
        ByteArrayOutputStream baos;
        baos = new ByteArrayOutputStream();
        //try{baos.flush();}catch(IOException e){Log.e("My Error MessMe","cant flush the baos ",e);}
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, ChatHandler.VIDEO_WIDTH, ChatHandler.VIDEO_HEIGHT, null);
        yuvImage.compressToJpeg(new Rect(0, 0, ChatHandler.VIDEO_WIDTH, ChatHandler.VIDEO_HEIGHT), 50, baos);
        byte[] encoded_data = baos.toByteArray();
        Log.e("My Error MessMe", "data length. " + data.length + " Image format " + ((camera.getParameters().getPreviewFormat() == ImageFormat.NV21) ? "NV21" : "something else") + " final jpeg arr size: " + encoded_data.length);
        System.arraycopy(encoded_data,0,fixed,0,encoded_data.length);

        synchronized (pendingBuffers){
            pendingBuffers.add(fixed);
        }
        try{Thread.sleep(500);}catch(Exception e){Log.e("My Error MessMe","Cant sleep properly");}
        //mCamera.addCallbackBuffer(data);
        //VideoCallActivity.VideoAdvThread vat = new VideoCallActivity.VideoAdvThread(fixed){
        //    @Override
        //    public void run(){
        //        try{
        //            //int[] data_size= new int[4];
        //            //int length = data.length;
        //            //for(int i=0;i<4;i++) data_size[i] = (length>>(i*8))&(0xFF);
        //
        //            done=false;
        //            crash_signal=false;
        //            //for(int i=0;i<4;i++) dos_call_socket.write(data_size[i]);
        //            synchronized (dos_call_socket){
        //                dos_call_socket.write(data);
        //            }
        //            done=true;
        //        }
        //        catch(IOException e){
        //            Log.e("My Error MessMe","Cannot send buffer to server", e);
        //            crash_signal=true;
        //        }
        //    }
        //};
        //vat.start();
        //try{
        //    vat.join();
        //}
        //catch(Exception e){
        //    Log.e("My Error MessMe", "Cant join threads: ", e);
        //}
        //while(!vat.done){
        //    //try{Thread.sleep(200);}catch(Exception e){Log.e("My Error MessMe","Cant sleep for 200 millis",e);}
        //    if(vat.crash_signal){
        //        ((AppCompatActivity)getContext()).finish();
        //        return;
        //    }
        //}
    }
}
