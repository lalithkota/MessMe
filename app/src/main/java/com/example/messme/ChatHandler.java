package com.example.messme;

import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;

class ReceiverThread extends Thread{

    MainActivity mRef;
    ChatterActivity cRef;

    ReceiverThread(MainActivity m,ChatterActivity c){
        super();
        mRef=m;
        cRef=c;
    }

    @Override
    public void run(){
        char[] buff = new char[1000];
        try {
            ChatHandler.sd = new Socket(ChatHandler.ip, 5555);
            ChatHandler.sdi= new DataInputStream(ChatHandler.sd.getInputStream());
            ChatHandler.sdo= new DataOutputStream(ChatHandler.sd.getOutputStream());

        }
        catch(Exception e){
            Log.e("My Error MessMe","Connect error",e);

            mRef.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mRef, "Try with correct ip.", Toast.LENGTH_LONG).show();
                }
            });


            mRef.myFinish1();
            return;
        }

        try{
            ChatHandler.sendStringToServer(ChatHandler.sdo,ChatHandler.uName+"\0");
            ChatHandler.sendStringToServer(ChatHandler.sdo,ChatHandler.pass+"\0");

            ChatHandler.read_line(ChatHandler.sdi,buff); //read Ack

            if(buff[0]=='N'){
                mRef.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mRef,"Try Another Username",Toast.LENGTH_SHORT).show();
                    }
                });

                ChatHandler.sd.close();
                ChatHandler.sdi.close();
                ChatHandler.sdo.close();

                mRef.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRef.myFinish2();
                    }
                });
                return;
            }
            else if(buff[0]=='P'){
                mRef.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mRef,"Invalid Password",Toast.LENGTH_SHORT).show();
                    }
                });

                ChatHandler.sd.close();
                ChatHandler.sdi.close();
                ChatHandler.sdo.close();

                mRef.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRef.myFinish3();
                    }
                });
                return;
            }
            else{
                ChatHandler.sendStringToServer(ChatHandler.sdo,ChatHandler.name+"\0");
            }
        }
        catch(Exception e){

        }

        mRef.myFinish4();

        int error_code = ChatHandler.reader_func(mRef, cRef);
    }
}

class ChatHandler{

    static ReceiverThread runningThread;

    static String name;
    static String uName;
    static String ip;
    static String pass;

    static Socket sd;
    static DataInputStream sdi;
    static DataOutputStream sdo;

    static ArrayList<User> allUsersList;
    public static UserAdapter main_adapter;

    static volatile boolean return_list_ready;
    static String return_list;

    //private int reader_quit_flag=0;
    //private int writer_quit_flag=0;

    //private boolean in_call=false;

    /*
    ChatHandler(String un, String n, String i, MainActivity m) {

        uName = un;
        name = n;
        ip = i;

        mRef = m;
        cRef = null;
    }*/

    static void start(MainActivity mRef, ChatterActivity cRef, String u, String p, String n, String i){
        uName=u;
        name=n;
        ip=i;
        pass=p;
        return_list_ready = false;

        allUsersList = new ArrayList<>();
        main_adapter = new UserAdapter(mRef, allUsersList);

        ((ListView)mRef.findViewById(R.id.MainActLinLayId)).setAdapter(main_adapter);

        runningThread = new ReceiverThread(mRef, cRef);
        runningThread.start();
    }

    static void send_msg_to_user(User rec, String msg, File filesDir){
        sendStringToServer(ChatHandler.sdo,msg.replace('\n','\0')+"\0");
        write_to_file(new File(filesDir, "messages/" + rec.uName),true, "m:"+msg.replace("\n","\nm:")+"\n");
    }

    static int sendStringToServer(final DataOutputStream sdo, final String s){
        class AdvThread extends Thread{

            private int success;

            private AdvThread(){
                super();
                success=0;
            }

            @Override
            public void run(){
                try{
                    sdo.write(s.getBytes());
                    success=1;
                }
                catch(Exception e){
                    Log.e("My Error MessMe","Unable to Send. "+s,e);
                    success=2;
                }
            }
        }
        AdvThread tTemp = new AdvThread();
        tTemp.start();
        while(tTemp.success==0);
        return tTemp.success;
    }

    static void write_to_file(File fNew, boolean append, String sNew){
        FileOutputStream fiO;
        try {
            fiO= new FileOutputStream(fNew, append);
        }
        catch(FileNotFoundException e){
            Log.e("My Error MessMe","receiving normal msg "+"Cannot open file",e);
            return;
        }
        try{
            fiO.write(sNew.getBytes());
        }
        catch(Exception e){}
    }


    static void read_line(DataInputStream sdi, char[] myLine) throws IOException{
        int buff=1;
        int i=0;
        while(buff!='\0'){
            buff=sdi.read();

            if(buff<0)throw new IOException("Error in Read_line");

            myLine[i]=(char)buff;
            i++;
        }
    }

    /*static void read_line_from_file(FileInputStream sdi, char[] myLine) throws IOException{
        int buff=1;
        int i=0;
        while(buff!='\n'){
            buff = sdi.read();

            if(buff<0) throw new EOFException("Error in Read_line_from_file");

            if(buff!='\n') {
                myLine[i] = (char)buff;
                i++;
            }
        }
        myLine[i] = '\0';
    }*/
    static void read_line_from_file(RandomAccessFile sdi, char[] myLine) throws IOException{
        int buff=1;
        int i=0;
        while(buff!='\n'){
            buff = sdi.read();

            if(buff<0) throw new EOFException("Error in Read_line_from_file");

            if(buff!='\n') {
                myLine[i] = (char)buff;
                i++;
            }
        }
        myLine[i] = '\0';
    }

    static void read_line_from_file_reverse(RandomAccessFile raf, char[] myLine) throws IOException{
        if(raf.getFilePointer()==0)throw new EOFException("Error in read_line_from_file_reverse");

        int buff = 1;
        int i=0;
        long temp_fp;
        while(buff!='\n'){
            temp_fp = raf.getFilePointer();
            buff = raf.read();

            //if(buff<0) throw new EOFException("Error in read_line_from_file_reverse");

            if(buff!='\n' && buff>0) {
                myLine[i] = (char)buff;
                i++;
            }

            if(temp_fp==0) {
                i++;
                raf.seek(temp_fp);
                break;
            }
            raf.seek(temp_fp-1);
        }
        myLine[i]=0;
    }

    static void newLeftMessage(ChatterActivity cRef, ListView vg, String buff){
        RelativeLayout llTemp= new RelativeLayout(cRef);
        vg.addView(llTemp);
        Log.e("My Error MessMe","Population started 2.5 ");
        ListView.LayoutParams lParams1 = (ListView.LayoutParams)llTemp.getLayoutParams();
        //lParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lParams1.width = ListView.LayoutParams.MATCH_PARENT;
        llTemp.setLayoutParams(lParams1);
        llTemp.setPadding(10,10,100,10);

        TextView tv = new TextView(cRef);
        tv.setText(buff);
        llTemp.addView(tv);
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) tv.getLayoutParams();
        lParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        tv.setLayoutParams(lParams);
        tv.setPadding(16,16,16,20);
        tv.setTextAppearance(cRef,R.style.ChatStyleTheme);
        tv.setBackgroundResource(R.drawable.chat_sender_rectangle);
    }

    static void newRightMessage(ChatterActivity cRef, ListView vg,String buff){
        RelativeLayout llTemp= new RelativeLayout(cRef);
        vg.addView(llTemp);
        ListView.LayoutParams lParams1 = (ListView.LayoutParams)llTemp.getLayoutParams();
        //lParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lParams1.width = ListView.LayoutParams.MATCH_PARENT;
        llTemp.setLayoutParams(lParams1);
        llTemp.setPadding(100,10,10,10);

        TextView tv = new TextView(cRef);
        tv.setText(buff);
        llTemp.addView(tv);
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) tv.getLayoutParams();
        lParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        tv.setLayoutParams(lParams);
        tv.setPadding(16, 16, 16, 20);
        tv.setTextAppearance(cRef, R.style.ChatStyleTheme);
        tv.setBackgroundResource(R.drawable.chat_receiver_rectangle);
    }

    static void populateOldMessages(ChatterActivity cRef, ListView vg, User receiver){
        char[] buff = new char[1000];

        //FileInputStream fis;
        RandomAccessFile fis;
        try{
            fis = new RandomAccessFile(new File(cRef.getFilesDir(),"messages/"+receiver.uName),"r");
            if(fis.length()>1)fis.seek(fis.length()-2);
            Log.e("My Error MessMe","pop start uName "+receiver.uName+ " length of file " + fis.length());
        }
        catch(Exception e){
            Log.e("My Error MessMe","populateOldMsg error "+"Unable to create FileInputStream",e);
            return;
        }
        ArrayList<String> msg_list = new ArrayList<>();
        MyFileAdapter adapter = new MyFileAdapter(cRef, msg_list);
        vg.setAdapter(adapter);

        //String buff_string;
        while(msg_list.size()<100){
            try{ read_line_from_file_reverse(fis, buff);}
            catch(EOFException e){break;}
            catch(Exception e){Log.e("My Error MessMe","file reading error",e);}

            String str = myToStringReverse(buff);
            msg_list.add(0, str);
        }

        adapter.notifyDataSetChanged();

        class AdvThread1 extends Thread{

            private ChatterActivity cRef;
            private RandomAccessFile fis;
            private char[] buff;
            private User receiver;
            private MyFileAdapter adapter;
            private ArrayList<String> msg_list;

            private AdvThread1(ChatterActivity c, RandomAccessFile f, char[] b, User u, MyFileAdapter adapt, ArrayList<String> list){
                cRef = c;
                fis = f;
                buff = b;
                receiver = u;
                adapter = adapt;
                msg_list = list;
            }

            @Override
            public void run(){

                receiver.no_of_unread = 0;
                try{fis.seek(fis.length());}catch(Exception e){}

                Log.e("My Error MessMe","Populate finished"+" Ended");

                while(cRef.is_in_alive){
                    try{ read_line_from_file(fis,buff); }
                    catch(EOFException e){continue;/*Do Nothing*/}
                    catch(Exception e){
                        Log.e("My Error MessMe","ThreadSide File Reading "+"some other error");
                        break;
                    }

                    msg_list.add(myToString(buff));
                    if(cRef.scrollLock) receiver.no_of_unread = 0;      // if scroll locked i.e., if the user is at the bottom of the list then all unread messages are zero

                    cRef.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        }
        AdvThread1 aT1 = new AdvThread1(cRef,fis,buff,receiver,adapter,msg_list);
        aT1.start();
    }

    static int reader_func(MainActivity mRef, ChatterActivity cRef){
        final char[] buff=new char[1000];

        while(true){

            try{
                read_line(sdi,buff);
            }
            catch(Exception e){
                return 1;
            }

            if(buff[0]=='-'){
                class MyRunnable implements Runnable{
                    MainActivity mRef;
                    //int someId;
                    MyRunnable(MainActivity m){
                        super();
                        mRef=m;
                        //someId=id;
                    }
                    public void run(){}
                }
                if(buff[1]=='m'){

                    if(myToString(buff).substring(2,8).equals(" Here ")){
                        return_list_ready = true;
                        return_list = myToString(buff);
                    }
                    else{
                        mRef.runOnUiThread(new MyRunnable(mRef){
                            public void run(){
                                Toast.makeText(mRef,myToString(buff).substring(3),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else if(buff[1]=='q'){
                    for(User u : allUsersList){
                        if(u.uName.equals(myToString(buff).substring(3))){
                            u.is_active=false;
                            break;
                        }
                    }
                    mRef.runOnUiThread(new Runnable(){
                        public void run(){
                            main_adapter.notifyDataSetChanged();
                        }
                    });
                }
                else if(buff[1]=='n'){
                    char[] temp_buff = buff.clone();

                    String uName_newUser = myToString(temp_buff);
                    int ind = uName_newUser.indexOf(" -u ");
                    String name_newUser = uName_newUser.substring(3,ind);
                    uName_newUser = uName_newUser.substring(ind+4);

                    int user_index;// = allUsersList.size();

                    for(user_index = 0; user_index<allUsersList.size();user_index++){
                        if(allUsersList.get(user_index).uName.equals(uName_newUser)){
                            Log.e("My Error MessMe","come on .. -n error uname:" + uName_newUser + " name:" + name_newUser);
                            allUsersList.get(user_index).is_active = true;
                            break;
                        }
                    }

                    if(user_index == allUsersList.size()) allUsersList.add(new User(name_newUser, uName_newUser, user_index, 0, true));

                    mRef.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            main_adapter.notifyDataSetChanged();
                        }
                    });

                    File myFile;
                    try {
                        myFile = new File(mRef.getFilesDir(),"messages/"+uName_newUser);
                        //Log.e("My Error MessMe","File path " + myFile.getAbsolutePath());
                        if(!myFile.createNewFile()){
                            Log.e("My Error MessMe","cannot create file "+"Unable to create file");
                        }

                    }catch(Exception e){
                        Log.e("My Error MessMe","cannot create file "+"some other error",e);
                    }


                    /*mRef.runOnUiThread(new MyRunnable(mRef,user_index){
                        @Override
                        public void run() {
                            TextView tempTextView = new TextView(mRef);
                            tempTextView.setText(allUsersList.get(someId).name);

                            ((LinearLayout) mRef.findViewById(R.id.MainActLinLayId)).addView(tempTextView);

                            ViewGroup.LayoutParams lParams = tempTextView.getLayoutParams();
                            lParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            tempTextView.setLayoutParams(lParams);
                            tempTextView.setPadding(40, 40, 40, 40);
                            tempTextView.setTextAppearance(mRef, R.style.NewUserTextTheme);
                            //tempTextView.setBackgroundResource(R.drawable.my_rectangle);

                            allUsersList.get(someId).textView_id = tempTextView.getId();

                            allUsersList.get(someId).is_active = true;

                            tempTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent myIntent = new Intent(mRef, ChatterActivity.class);
                                    myIntent.putExtra("UserIndex",someId);
                                    mRef.startActivity(myIntent);
                                }
                            });
                            mRef.registerForContextMenu(tempTextView);
                        }
                    });*/

                }
                else if(buff[1]=='v'){
                    Log.e("My Error MessMe","call request received");

                    mRef.runOnUiThread(new MyRunnable(mRef) {
                        @Override
                        public void run() {
                            String buff_string = myToString(buff);
                            User caller = null;
                            for(User u:allUsersList){
                                if(u.uName.equals(buff_string.substring(3))){
                                    caller = u;
                                    break;
                                }
                            }
                            Intent intent = new Intent(mRef, CallActivity.class);
                            intent.putExtra("is_caller",0);
                            intent.putExtra("user_index",caller.indexInList);
                            mRef.startActivity(intent);
                        }
                    });

                }

                else if(buff[1]=='c'){
                    if(buff[2]=='n') {
                        String buffString = myToString(buff);
                        Log.e("My Error MessMe","what the change name" + myToString(buff));
                        for (User u : allUsersList) {
                            if (u.uName.equals(buffString.substring(buffString.indexOf(" -u ") + 4))) {
                                u.name = buffString.substring(4, buffString.indexOf(" -u "));
                                mRef.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChatHandler.main_adapter.notifyDataSetChanged();
                                    }
                                });
                                break;
                            }
                        }
                    }

                }
            }
            else{
                String buffString=myToString(buff);
                int ind = buffString.substring(1).indexOf(':')+1;
                if(buff[0]!=':') {
                    if (!buffString.substring(0, ind).equals(uName) && !buffString.substring(0, ind).equals(name)) {
                        write_to_file(new File(mRef.getFilesDir(), "messages/" + buffString.substring(0, ind)), true, "y:" + buffString.substring(ind + 2) + "\n");
                    }
                }
                else{
                    String temp_if_group_msg = buffString.substring(ind+2);
                    User rec=null;
                    for(User u: allUsersList){
                        if(u.uName.equals(temp_if_group_msg.substring(0,temp_if_group_msg.indexOf(':') ) ) ){
                            rec=u;
                            break;
                        }
                    }
                    if(!temp_if_group_msg.substring(0,temp_if_group_msg.indexOf(':')).equals(uName)){
                        String str = buffString.substring(ind + 2).replace(rec.uName + ": ",rec.name + ": ");
                        write_to_file(new File(mRef.getFilesDir(), "messages/" + buffString.substring(0, ind)), true,"y:" + str + "\n");
                    }
                }

                for(User u: allUsersList){
                    if(u.uName.equals(buffString.substring(0, ind))){
                        u.no_of_unread++;
                        break;
                    }
                }

                mRef.runOnUiThread(new Runnable(){
                    public void run(){
                        main_adapter.notifyDataSetChanged();
                    }
                });
            }

        }
    }

    static String myToString(char[] cArr){
        String a=String.valueOf(cArr[0]);
        for(int i=1;i<cArr.length;i++){
            if(cArr[i]!=0)a+=cArr[i];
            else break;
        }
        return a;
    }
    static String myToStringReverse(char[] cArr){
        return (new StringBuilder(myToString(cArr))).reverse().toString();
    }

}
