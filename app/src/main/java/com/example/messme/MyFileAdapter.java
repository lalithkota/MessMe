package com.example.messme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MyFileAdapter extends ArrayAdapter<String> {

    ViewHolder viewHolder;
    boolean popFin;

    class ViewHolder{
        TextView tv;

        void setVisibility(int visibility){
            tv.setVisibility(visibility);
        }
    }

    MyFileAdapter(Context context, ArrayList<String> list){
        super(context,0,list);
        popFin = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        String str = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.new_message_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tv = convertView.findViewById(R.id.new_message_tv_id);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv.setText(str.substring(2));

        if(str.charAt(0)=='y' && str.charAt(1)==':'){
            RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) viewHolder.tv.getLayoutParams();
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
            viewHolder.tv.setLayoutParams(rl);
            viewHolder.tv.setBackgroundResource(R.drawable.chat_receiver_rectangle);
        }
        else if(str.charAt(0)=='m' && str.charAt(1)==':'){
            RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) viewHolder.tv.getLayoutParams();
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
            viewHolder.tv.setLayoutParams(rl);
            viewHolder.tv.setBackgroundResource(R.drawable.chat_sender_rectangle);
        }

        if(position == getCount() - 1){
            popFin = true;
        }

        ChatterActivity cRef = (ChatterActivity) getContext();
        int no_unread = ChatHandler.allUsersList.get(cRef.user_index).no_of_unread;
        if(no_unread > 0)cRef.mySetTitle(ChatHandler.allUsersList.get(cRef.user_index).name + " (" + no_unread + ")");
        else if(no_unread==0) cRef.mySetTitle(ChatHandler.allUsersList.get(cRef.user_index).name);

        return convertView;
    }
}
