package com.example.messme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

    MyDialogFragment ip_dialog = null;
    String temp_name;
    String temp_uName;
    String temp_pass;
    String temp_ip;

    private int MY_PERMISSION_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 23){
            if( ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSION_REQUEST_RECORD_AUDIO);
            }
        }

        /*int files_dir_list;
        try{
            files_dir_list = getFilesDir().list().length;
        }
        catch(NullPointerException e){
            files_dir_list = 0;
        }
        if(files_dir_list */
        if(getFilesDir().list().length== 0){
            setContentView(R.layout.activity_login);
            return;
        }

        File login = new File(getFilesDir(),"login");

        RandomAccessFile fis;
        char[] buff = new char[60];
        try{
            fis = new RandomAccessFile(login,"r");
            ChatHandler.read_line_from_file(fis,buff);
            temp_uName = ChatHandler.myToString(buff).substring(2);
            ChatHandler.read_line_from_file(fis,buff);
            temp_pass = ChatHandler.myToString(buff).substring(2);
            ChatHandler.read_line_from_file(fis,buff);
            temp_name = ChatHandler.myToString(buff).substring(2);

        }
        catch(IOException e){
            Log.e("My Error MessMe","unable to read login details  - 1");
            setContentView(R.layout.activity_login);
            return;
        }

        boolean is_there_old_ip = false;
        try{
            ChatHandler.read_line_from_file(fis,buff);
            temp_ip = ChatHandler.myToString(buff).substring(2);
            is_there_old_ip = true;

        }
        catch(EOFException e){
            askForIP();
            //else Do Nothing
        }
        catch(IOException e){
            Log.e("My Error MessMe","unable to read login details  - 2");
            setContentView(R.layout.activity_login);
            Toolbar myToolbar = findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            return;
        }


        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if(is_there_old_ip) ChatHandler.start(this,null,temp_uName,temp_pass,temp_name,temp_ip);

        FloatingActionButton fab1 = findViewById(R.id.fab);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("My Error MessMe","New Dialog Created");

                LayoutInflater lf = getLayoutInflater();
                View view = lf.inflate(R.layout.dialog_ip,null);
                ((EditText)view.findViewById(R.id.ip_id_new1)).setHint("New Group's Name");

                MyDialogFragment md = new MyDialogFragment("New Group Create :", view, new MyDialogFragment.MRunnable1(){
                    @Override
                    public void run(){
                        String temp_group = ((EditText)dFrag.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
                        ChatHandler.sendStringToServer(ChatHandler.sdo,"-gn " + temp_group + "\0");
                        dFrag.dismiss();
                    }
                });
                md.show(getSupportFragmentManager(),"MyDialogFragment");
            }
        });
    }

    public void askForIP(){

        LayoutInflater lf = getLayoutInflater();
        View view = lf.inflate(R.layout.dialog_ip,null);
        ((EditText)view.findViewById(R.id.ip_id_new1)).setHint("Server IP");
        ((EditText)view.findViewById(R.id.ip_id_new1)).setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        ip_dialog = new MyDialogFragment("Asking IP", view, new MyDialogFragment.MRunnable1() {
            @Override
            public void run() {
                temp_ip = ((EditText)dFrag.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
                ChatHandler.write_to_file(new File(getFilesDir(),"login"),false,"u:"+temp_uName+"\np:"+temp_pass+"\nn:"+temp_name+"\ni:"+temp_ip+"\n");
                ChatHandler.start((MainActivity) ((MyDialogFragment)dFrag).myAppActivity,null,temp_uName,temp_pass,temp_name,temp_ip);
            }
        });
        //ip_dialog.type = 1;
        ip_dialog.show(getSupportFragmentManager(), "MyDialogFragment");
    }

    /*@Override
    public void onPositiveForIp(DialogFragment dialog){
        temp_ip = ((EditText)dialog.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
        ChatHandler.write_to_file(new File(getFilesDir(),"login"),false,"u:"+temp_uName+"\np:"+temp_pass+"\nn:"+temp_name+"\ni:"+temp_ip+"\n");
        ChatHandler.start(this,null,temp_uName,temp_pass,temp_name,temp_ip);
    }
    @Override
    public void onPositiveForGroup(DialogFragment dialog){
        String temp_group = ((EditText)dialog.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
        ChatHandler.sendStringToServer(ChatHandler.sdo,"-gn " + temp_group + "\0");
        dialog.dismiss();
    }
    @Override
    public void onPositiveForChangeName(DialogFragment dialog){
        //
    }*/

    public void myFinish1(){
        //bad/wrong ip
        if(ip_dialog==null){
            askForIP();
        }
    }
    public void myFinish2(){
        //invalid username
        if(ip_dialog != null) ip_dialog.dismiss();
        setContentView(R.layout.activity_login);
        ((TextView)findViewById(R.id.login_empty_text)).setText("Try a different Username");
    }
    public void myFinish3(){
        //invalid password
        if(ip_dialog != null) ip_dialog.dismiss();
        setContentView(R.layout.activity_login);
        ((TextView)findViewById(R.id.login_empty_text)).setText("Invalid Password Try Again");
    }
    public void myFinish4(){
        // ip accepted
        if(ip_dialog != null) ip_dialog.dismiss();
        //ChatHandler.pass = null;
        temp_pass=null;
        temp_name=null;
        temp_uName=null;
        temp_ip=null;
    }

    public void myLogin(View view) {

        EditText nameText = findViewById(R.id.name_id);
        EditText uNameText = findViewById(R.id.uname_id);
        EditText passText = findViewById(R.id.pass_id);

        temp_name = nameText.getText().toString();
        temp_uName = uNameText.getText().toString();
        temp_pass = passText.getText().toString();

        File fTemp1 = new File(getFilesDir(),"messages/");
        File fTemp2 = new File(getFilesDir(),"login");

        if(!fTemp1.mkdir()){
            Log.e("My Error MessMe", "Unable to create directory");
            //return;
            for(String s : fTemp1.list()){
                new File(fTemp1,s).delete();
            }
        }
        if(fTemp2.exists()){
            fTemp2.delete();
        }
        try{
            FileOutputStream fisTemp = new FileOutputStream(fTemp2,true);
            fisTemp.write(("u:"+temp_uName+"\np:"+temp_pass+"\nn:"+temp_name+"\n").getBytes());
        }
        catch(Exception e){
            Log.e("My Error MessMe","Unable to create login file");
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        askForIP();
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        String sNew = ((UserAdapter.ViewHolder)v.getTag()).user.uName;
        /*for(User u: ChatHandler.allUsersList){
            if(){
                sNew=u.uName;
                break;
            }
        }*/
        menu.setHeaderTitle(sNew);
        menu.add(0,0,0,"Call");
        Log.e("My Error MessMe", "received long click and registered context menu");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle()=="Call"){
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.my_toolbar_menu_main_act, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.change_name_menu_item_id) {
            changeMyName();
            return true;
        }
        else{
            return super.onOptionsItemSelected(menuItem);
        }

    }

    void changeMyName(){

        LayoutInflater lf = getLayoutInflater();
        View view = lf.inflate(R.layout.dialog_ip,null);
        ((EditText)view.findViewById(R.id.ip_id_new1)).setHint("Your New Name");

        MyDialogFragment md = new MyDialogFragment("For Name Change", view, new MyDialogFragment.MRunnable1(){
            @Override
            public void run(){
                String newName = ((EditText)dFrag.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
                ChatHandler.name = newName;
                ChatHandler.write_to_file(new File(getFilesDir(),"login"),false,"u:"+ChatHandler.uName+"\np:"+ChatHandler.pass+"\nn:"+ChatHandler.name+"\ni:"+ChatHandler.ip+"\n");
                ChatHandler.sendStringToServer(ChatHandler.sdo,"-c " + newName + "\0");
                dFrag.dismiss();
            }
        });
        md.show(getSupportFragmentManager(),"MyDialogFragment");
    }

}
