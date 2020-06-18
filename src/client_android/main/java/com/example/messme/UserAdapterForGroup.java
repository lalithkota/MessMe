package com.example.messme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class UserAdapterForGroup extends ArrayAdapter<User> {

    String[] already_group;
    UserAdapterForGroup(Context context, ArrayList<User> users, String[] group_list) {
        super(context, 0, users);
        already_group = group_list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        User user = getItem(position);

        if(user==null){
            Log.e("My Error MessMe","Some Error in Group-Add-ArrayAdapter indexing");
            return null;
        }

        //ViewHolder viewHolder;

        if (convertView == null) {

            convertView = new CheckBox(getContext());
            convertView.setPadding(22,22,22,22);
            ((CheckBox)convertView).setTextSize(22);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // first checkbox is getting checked when clicked .. then onclick being called .. so if checked then it was just checked by the user
                    if(((CheckBox)v).isChecked()){
                        User u = (User)v.getTag();
                        ChatHandler.sendStringToServer(ChatHandler.sdo,"-ga "+u.uName+"\0");
                    }
                    ((CheckBox)v).setChecked(true);
                }
            });
        }

        ((CheckBox)convertView).setText(user.name + " (" + user.uName + ")");
        convertView.setTag(user);
        ((CheckBox)convertView).setChecked(false);

        for(String g : already_group){
            if(g.equals(user.uName)){
                ((CheckBox)convertView).setChecked(true);
                break;
            }
        }

        if(!user.is_active){
            convertView.setVisibility(View.GONE);
        }

        return convertView;
    }
}
