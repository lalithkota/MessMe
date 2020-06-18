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
import android.widget.TextView;

import java.util.ArrayList;

class UserAdapter extends ArrayAdapter<User> {

    static class ViewHolder{
        TextView new_user_tv;
        TextView unread_no_view;
        User user;

        void setVisibility(int visibility){
            new_user_tv.setVisibility(visibility);
            unread_no_view.setVisibility(visibility);
        }
    }

    UserAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    /*void updateOneUser(User rec){
        for(User u: ChatHandler.allUsersList){
            if(rec.uName==){

            }
        }
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user = getItem(position);

        if(user==null){
            Log.e("My Error MessMe","Some Error in ArrayAdapter indexing");
            return null;
        }

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.new_user_in_main, parent, false);
            //convertView.setBackgroundResource(R.drawable.my_rectangle);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(getContext(), ChatterActivity.class);
                    myIntent.putExtra("UserIndex",user.indexInList);
                    getContext().startActivity(myIntent);
                }
            });

            user.textView_id = convertView.getId();
            ((Activity)getContext()).registerForContextMenu(convertView);

            viewHolder = new ViewHolder();
            viewHolder.new_user_tv = convertView.findViewById(R.id.new_user_name);
            viewHolder.unread_no_view = convertView.findViewById(R.id.unread_msg_view_id);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.new_user_tv.setText(user.name);
        viewHolder.user=user;

        if(!user.is_active){
            convertView.setVisibility(View.GONE);
            viewHolder.setVisibility(View.GONE);
        }
        else{
            convertView.setVisibility(View.VISIBLE);
            viewHolder.setVisibility(View.VISIBLE);
        }


        if(user.no_of_unread>0){
            viewHolder.unread_no_view.setText(" ("+user.no_of_unread+")");
            viewHolder.new_user_tv.setTypeface(null, Typeface.BOLD);
            viewHolder.unread_no_view.setTypeface(null, Typeface.BOLD);
        }
        else{
            viewHolder.unread_no_view.setText("");
            viewHolder.unread_no_view.setTypeface(null, Typeface.NORMAL);
            viewHolder.new_user_tv.setTypeface(null, Typeface.NORMAL);
        }

        return convertView;
    }
}
