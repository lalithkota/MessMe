package com.example.messme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {

    // to create a new dialog use this to instantiate ... the runner from the constructor is run when ok is pressed

    static class MRunnable1 implements Runnable{
        DialogFragment dFrag;

        @Override
        public void run(){
        }
    }
    //int type;
    //private String myHint;
    private String myTitle;
    MRunnable1 myRun;
    Context myAppActivity;
    //boolean simple_dialog;
    View myView;

    /*MyDialogFragment(String title, String hint, boolean type_simple, MRunnable1 runner){
        super();
        myTitle = title;
        myHint = hint;
        myRun = runner;
        myRun.dFrag = this;
        simple_dialog = type_simple;
        myView = null;
    }*/

    MyDialogFragment(String title, View view, MRunnable1 runner){
        super();
        myTitle = title;
        myRun = runner;
        myRun.dFrag = this;
        myView = view;

    }

    /*public interface MyDialogListener{
        void onPositiveForIp(DialogFragment dialog);
        void onPositiveForGroup(DialogFragment dialog);
        void onPositiveForChangeName(DialogFragment dialog);
    }*/

    //MyDialogListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        myAppActivity = context;
    }

    @Override
    public void onCancel(DialogInterface dialog){
        //Do Nothing;
    }

    /*private void dialogDealer(){
        if(type==1){
            listener.onPositiveForIp(this);
        }

        else if(type==2){
            listener.onPositiveForGroup(this);
        }
        else if(type==3){
            listener.onPositiveForChangeName(this);
        }
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*if(simple_dialog) {
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_ip, null));
        }
        else{*/
        if(myView != null) builder.setView(myView);
        //}

        builder.setPositiveButton("OK",null);

        AlertDialog temp_d1 = builder.create();

        temp_d1.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //if(simple_dialog)((EditText)getDialog().findViewById(R.id.ip_id_new1)).setHint(myHint);
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //dialogDealer();
                        myRun.run();
                    }
                });
            }
        });
        if(myTitle != null) temp_d1.setTitle(myTitle);

        return temp_d1;
    }
}
