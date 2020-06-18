package com.example.messme;

class User {
    String name;
    String uName;
    int indexInList;
    int no_of_unread;
    boolean is_active;
    int textView_id;

    User(String name, String uName, int ind, int no_of_unread, boolean active){
        this.name = name;
        this.uName = uName;
        this.indexInList = ind;
        this.no_of_unread = no_of_unread;
        this.is_active = active;
    }
}
