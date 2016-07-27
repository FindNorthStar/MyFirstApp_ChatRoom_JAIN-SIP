package com.mycompany.myfirstapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lenovo on 2016/7/4.
 */
public class MyUser {
    private String userName;
    private int imageID;
    public int[] imageGroup_on={R.drawable.online1,R.drawable.online2,R.drawable.online3};
    public int[] imageGroup_off={R.drawable.offline1,R.drawable.offline2,R.drawable.offline3};
    private List<String> recentMessage= new ArrayList<>();
    private int messageSeq;
    private int isOnline;
    public boolean isExist;
    public int imageSatus[]={R.drawable.online_status,R.drawable.not_disturb_status,R.drawable.busy_status_small,R.drawable.groupchat};
    public int currentStatus;
    public MyUser(String userName, int imageID) {
        this.userName = userName;
        this.imageID=imageID;
        messageSeq=0;
        isOnline=1;
        currentStatus=0;
    }

    public MyUser(String userName) {
        this.userName = userName;
        imageID=new Random().nextInt(3);
        messageSeq=0;
        isOnline=1;
        currentStatus=0;
    }

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public MyUser(String recentMsg, String userName, int imageID) {
        this.userName = userName;
        this.imageID = imageID;
        messageSeq=0;
        isOnline=1;
        recentMessage.add(recentMsg);
    }

    public void addRecentMsg(String message){
        recentMessage.add(message);
    }

    public List<String> getRecentMessage() {
        return recentMessage;
    }

    public String getRecentMsg(){
        if(recentMessage.size()==0)
            return "";
        else
            return recentMessage.get(recentMessage.size()-1);
    }

    public int getMessageSeq(){
        return recentMessage.size()-1;
    }
    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public void setMessageSeq(int messageSeq) {
        this.messageSeq = messageSeq;
    }

    public int getImageID() {
        return imageID;
    }

    public String getUserName() {
        return userName;
    }
}
