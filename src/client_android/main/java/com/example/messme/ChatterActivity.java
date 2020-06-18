package com.example.messme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.io.File;

public class ChatterActivity extends AppCompatActivity {

    boolean is_in_alive;
    int user_index;
    boolean scrollLock;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatter);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        is_in_alive=true;

        Intent myIntent = getIntent();
        user_index = myIntent.getIntExtra("UserIndex",-1);

        mySetTitle(ChatHandler.allUsersList.get(user_index).name);

        lv = findViewById(R.id.chatter_layout_id);

        ChatHandler.populateOldMessages(this, lv, ChatHandler.allUsersList.get(user_index));
        String str;
        if(ChatHandler.allUsersList.get(user_index).uName.charAt(0)==':'){
            str = "-g " + ChatHandler.allUsersList.get(user_index).uName.substring(1)+'\0';
        }
        else{
            str = "-su " + ChatHandler.allUsersList.get(user_index).uName+'\0';
        }
        ChatHandler.sendStringToServer(ChatHandler.sdo,str);
        Log.e("My Error MessMe","String Log "+ str);

        lv.post(new Runnable() {
            @Override
            public void run() {
                lv.setSelection(lv.getAdapter().getCount()-1);
            }
        });
        scrollToTheBottom();
        lv.setOnScrollListener(new ListView.OnScrollListener(){
            private int lastFirstVisibleItem;
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(lastFirstVisibleItem<firstVisibleItem) {
                    //scrolling down
                }
                if(lastFirstVisibleItem>firstVisibleItem) {
                    //scrolling up;
                    scrollLock = false;
                    view.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                }
                lastFirstVisibleItem=firstVisibleItem;

                if(firstVisibleItem + visibleItemCount == totalItemCount){
                    scrollToTheBottom();
                }
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });
    }

    void scrollToTheBottom(){

        scrollLock = true;
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        ChatHandler.allUsersList.get(user_index).no_of_unread = 0;
        ChatHandler.main_adapter.notifyDataSetChanged();
        mySetTitle(ChatHandler.allUsersList.get(user_index).name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_chat_act, menu);
        if(getTitle().charAt(0)!=':'){
            menu.findItem(R.id.add_members_id).setVisible(false);
            menu.findItem(R.id.change_group_name_id).setVisible(false);
            menu.findItem(R.id.leave_group_id).setVisible(false);
            menu.findItem(R.id.call_in_chat_id).setVisible(true);
            menu.findItem(R.id.video_call_in_chat_id).setVisible(true);
        }
        else{
            menu.findItem(R.id.add_members_id).setVisible(true);
            menu.findItem(R.id.change_group_name_id).setVisible(true);
            menu.findItem(R.id.leave_group_id).setVisible(true);
            menu.findItem(R.id.call_in_chat_id).setVisible(false);
            menu.findItem(R.id.video_call_in_chat_id).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.call_in_chat_id){
            callInitiate();
            return true;
        }
        else if(menuItem.getItemId() == R.id.video_call_in_chat_id){
            videoCallInitiate();
            return true;
        }
        else if(menuItem.getItemId() == R.id.add_members_id) {
            addGroupMembersFunc();
            return true;
        }
        else if(menuItem.getItemId() == R.id.change_group_name_id){
            changeGroupNameFunc();
            return true;
        }
        else if(menuItem.getItemId() == R.id.leave_group_id){
            leaveGroupFunc();
            return true;
        }
        else{
            return super.onOptionsItemSelected(menuItem);
        }

    }
    void callInitiate(){
        ChatHandler.sendStringToServer(ChatHandler.sdo,"-v\0");

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("is_caller",1);
        intent.putExtra("user_index",user_index);
        startActivity(intent);
    }
    void videoCallInitiate(){
        ChatHandler.sendStringToServer(ChatHandler.sdo,"-vi\0");

        Intent intent = new Intent(this, VideoCallActivity.class);
        //Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("is_caller",1);
        intent.putExtra("user_index",user_index);
        startActivity(intent);
    }

    void addGroupMembersFunc(){

        //create new Group-Add-Dialog .. and set adapter and all
        Thread thr = new Thread(){
            @Override
            public void run(){
                ChatHandler.sendStringToServer(ChatHandler.sdo,"-gl\0");
                while(!ChatHandler.return_list_ready){
                    try{Thread.sleep(100);}catch(Exception e){}
                }
                final String[] kidList = ChatHandler.return_list.split("\n");
                ChatHandler.return_list_ready = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout ll = new LinearLayout(ChatterActivity.this);
                        ListView lv= new ListView(ChatterActivity.this);
                        ll.addView(lv);
                        ((LinearLayout.LayoutParams)lv.getLayoutParams()).setMargins(24,24,24,24);

                        UserAdapterForGroup udgp = new UserAdapterForGroup(ChatterActivity.this,ChatHandler.allUsersList,kidList);
                        lv.setAdapter(udgp);

                        MyDialogFragment md = new MyDialogFragment("Members in the group (excluding you)",ll, new MyDialogFragment.MRunnable1(){
                            @Override
                            public void run(){
                                dFrag.dismiss();
                            }
                        });
                        //md.myView = ll;

                        md.show(getSupportFragmentManager(),"MyGroupD");
                    }
                });

            }
        };
        thr.start();
    }

    void changeGroupNameFunc(){

        LayoutInflater lf = getLayoutInflater();
        View view = lf.inflate(R.layout.dialog_ip,null);
        ((EditText)view.findViewById(R.id.ip_id_new1)).setHint("New Group Name");

        MyDialogFragment md = new MyDialogFragment("Changing Group Name:", view, new MyDialogFragment.MRunnable1() {
            @Override
            public void run() {
                String new_name = ((EditText)dFrag.getDialog().findViewById(R.id.ip_id_new1)).getText().toString();
                ChatHandler.sendStringToServer(ChatHandler.sdo,"-gc "+ new_name+ "\0");
                dFrag.getActivity().setTitle(new_name);
                dFrag.dismiss();
            }
        });
        //ip_dialog.type = 1;
        md.show(getSupportFragmentManager(), "MyDialogFragment");

    }

    void leaveGroupFunc(){
        //View view = new View(this);
        MyDialogFragment md = new MyDialogFragment("Leave Group?", null, new MyDialogFragment.MRunnable1() {
            @Override
            public void run() {
                ChatHandler.sendStringToServer(ChatHandler.sdo,"-gr\0");
                ChatHandler.allUsersList.get(user_index).is_active = false;
                ChatHandler.main_adapter.notifyDataSetChanged();
                dFrag.getActivity().finish();
            }
        });
        //ip_dialog.type = 1;
        md.show(getSupportFragmentManager(), "MyDialogFragment");

    }

    public void sendMessageChat(View v){
        scrollToTheBottom();

        TextView tv = findViewById(R.id.send_text_id);
        String actual_message = tv.getText().toString();
        if(actual_message.charAt(0)=='-'){
            actual_message = "/" + actual_message;
        }
        ChatHandler.send_msg_to_user(ChatHandler.allUsersList.get(user_index), actual_message, getFilesDir() );
        tv.setText("");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        is_in_alive=false;
    }


    void mySetTitle(String str){
        getSupportActionBar().setTitle(str);

        // for refreshing the title ui
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }
}
