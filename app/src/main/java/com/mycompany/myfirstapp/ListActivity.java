package com.mycompany.myfirstapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mycompany.myfirstapp.ua.impl.DeviceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private List<MyUser> userList=new ArrayList<MyUser>();
    private UserAdapter adapter;
    private ListView myListView;
    private int actionSwitch;//用来切换对list的相应事件
    String savedusername;
    private String myName;

    private Handler listHandler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 3:
                    /*Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();*/
                    refreshList(msg.obj.toString());
                    break;
                case 4:
                    TransferMsg transferMsg=(TransferMsg)msg.obj;
                    if(transferMsg.getTo().contains("All")){
                        //群聊信息
                        userList.get(0).addRecentMsg(transferMsg.getFrom()+":"+transferMsg.getContent());
                        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                        Toast.makeText(ListActivity.this, "你收到一条群聊消息\n"+transferMsg.getFrom()+"说："
                                +transferMsg.getContent(),
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        for(MyUser u:userList){
                            if(u.getUserName().equals(transferMsg.getFrom()))
                            {
                                u.addRecentMsg(transferMsg.getContent());
                                adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                                break;
                            }
                        }
                        Toast.makeText(ListActivity.this, "你收到一条来自"+transferMsg.getFrom()+"的私聊消息:"
                                +transferMsg.getContent(),
                                Toast.LENGTH_LONG).show();
                        //私聊信息

                    }
                    break;
                case 5:
                    final String applyName=msg.obj.toString();
                    AlertDialog.Builder dialog = new AlertDialog.Builder
                            (ListActivity.this);
                    dialog.setTitle("好友申请");
                    dialog.setMessage(applyName+"请求加你为好友.");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("同意", new DialogInterface.
                            OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeviceImpl.getInstance().SendAction(applyName,"approve");
                        }
                    });
                    dialog.setNegativeButton("拒绝", new DialogInterface.
                            OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeviceImpl.getInstance().SendAction(applyName,"refuse");
                        }
                    });
                    dialog.show();
                    break;
                case 6://online
                    for(MyUser u:userList){
                        if(u.getUserName().equals(msg.obj.toString()))
                        {
                            u.setCurrentStatus(0);
                           adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                            break;
                        }
                    }

                    break;
                case 7://busy
                    for(MyUser u:userList){
                        if(u.getUserName().equals(msg.obj.toString()))
                        {
                            u.setCurrentStatus(1);
                            adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                            break;
                        }
                    }
                    break;
                case 8://afk
                    for(MyUser u:userList){
                        if(u.getUserName().equals(msg.obj.toString()))
                        {
                            u.setCurrentStatus(2);
                            adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        actionSwitch=1;
        myName="sip:"+DeviceImpl.getInstance().getSipProfile().getSipUserName()+"@"+
                DeviceImpl.getInstance().getSipProfile().getLocalEndpoint();

        DeviceImpl.getInstance().setDeviceHandler(listHandler);
        DeviceImpl.getInstance().SendAskListInviteMessage("sip:server@" +
                DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                DeviceImpl.getInstance().getSipProfile().getRemotePort());
        initUsers();
        //初始化用户列表-群聊列表
        setTitle(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        adapter=new UserAdapter(
                ListActivity.this,R.layout.user_item,userList);
        myListView=(ListView)findViewById(R.id.listView);
        myListView.setAdapter(adapter);
        //增加点击名字的响应函数
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user=userList.get(position);
                if(actionSwitch==1)
                {
                    //jump to chat panel
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    intent.putExtra("recentMsgList", (Serializable) user.getRecentMessage());
                    intent.putExtra("targetURI",user.getUserName());
                    user.getRecentMessage().clear();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
                else if(actionSwitch==2)
                {
                    actionSwitch=1;//复位
                    Toast.makeText(ListActivity.this, "已向"+user.getUserName()+"发送好友申请",
                            Toast.LENGTH_SHORT).show();
                    DeviceImpl.getInstance().SendAction(user.getUserName(),"add");
                }

            }
        });
    }

    private MyUser getMyUserByName(List<MyUser> list, String userName){
        for (MyUser myUser : userList){
            if (myUser.getUserName().equals(userName)){
                return myUser;
            }
        }
        return null;
    }

    private void refreshList(String rawString){

        String names[] = rawString.split(";");

        List<MyUser> newUserList = new ArrayList<MyUser>();

        //reset isExist

        for (MyUser myUser : userList){
            myUser.isExist = false;
        }

        //multichat set isExist to true
        userList.get(0).isExist = true;

        //change the isExist if userList has names
        for (String name : names){
            if(name.equals(myName)) {
                continue;
            }
            MyUser tempUser = this.getMyUserByName(userList,name);
            if (tempUser != null){
                tempUser.isExist = true;
                //isExist = true means it is exist
            }
            else{
                MyUser user1=new MyUser(name);
                userList.add(user1);
                user1.isExist = true;
            }
        }

        //from userList remove element by index, if one element is removed, index subtracts itself
        for (int i=0;i<userList.size();i++){
            if (userList.get(i).isExist == false){
                userList.remove(i);
                i--;
            }
        }

        adapter.notifyDataSetChanged(); // 当有新消息时，刷新ListView中的显示

    }

    private void initUsers(){
        MyUser user1 = new MyUser("多人群聊");
        user1.addRecentMsg("点击进入多人群聊");
        user1.setCurrentStatus(3);
        userList.add(user1);
    }

    @Override
    protected void onResume() {

        super.onResume();

        setContentView(R.layout.activity_list);

        DeviceImpl.getInstance().setDeviceHandler(listHandler);

        //初始化用户列表-群聊列表
        adapter=new UserAdapter(
                ListActivity.this,R.layout.user_item,userList);
        myListView=(ListView)findViewById(R.id.listView);
        myListView.setAdapter(adapter);
        //增加点击名字的响应函数
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user=userList.get(position);
                if(actionSwitch==1)
                {
                    //jump to chat panel
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    intent.putExtra("recentMsgList", (Serializable) user.getRecentMessage());
                    intent.putExtra("targetURI",user.getUserName());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
                else if(actionSwitch==2)
                {
                    actionSwitch=1;//复位
                    Toast.makeText(ListActivity.this, "已向"+user.getUserName()+"发送好友申请",
                            Toast.LENGTH_SHORT).show();
                    DeviceImpl.getInstance().SendAction(user.getUserName(),"add");
                }

            }
        });

        //update userlist if resultcode is OK
        if(!DeviceImpl.getInstance().getReCallMsgList().isEmpty())
        {
            List<String> reCallMsgList=DeviceImpl.getInstance().getReCallMsgList();
            for(String s:reCallMsgList){
                String items[]=s.split("#502750694#");
                for(MyUser user:userList){
                    if(user.getUserName().equals(items[0])){
                        user.addRecentMsg(items[1]);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();
            DeviceImpl.getInstance().getReCallMsgList().clear();
        }


        invalidateOptionsMenu();

    }

    @Override
    protected void onStart(){

        super.onStart();

        setContentView(R.layout.activity_list);

        DeviceImpl.getInstance().setDeviceHandler(listHandler);
        DeviceImpl.getInstance().SendAskListInviteMessage("sip:server@" +
                DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                DeviceImpl.getInstance().getSipProfile().getRemotePort());
        initUsers();
        //初始化用户列表-群聊列表
        setTitle(DeviceImpl.getInstance().getSipProfile().getSipUserName());
        adapter=new UserAdapter(
                ListActivity.this,R.layout.user_item,userList);
        myListView=(ListView)findViewById(R.id.listView);
        myListView.setAdapter(adapter);
        //增加点击名字的响应函数
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user=userList.get(position);
                if(actionSwitch==1)
                {
                    //jump to chat panel
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    intent.putExtra("recentMsgList", (Serializable) user.getRecentMessage());
                    intent.putExtra("targetURI",user.getUserName());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
                else if(actionSwitch==2)
                {
                    actionSwitch=1;//复位
                    Toast.makeText(ListActivity.this, "已向"+user.getUserName()+"发送好友申请",
                            Toast.LENGTH_SHORT).show();
                    DeviceImpl.getInstance().SendAction(user.getUserName(),"add");
                }

            }
        });

        //update userlist if resultcode is OK
        if(!DeviceImpl.getInstance().getReCallMsgList().isEmpty())
        {
            List<String> reCallMsgList=DeviceImpl.getInstance().getReCallMsgList();
            for(String s:reCallMsgList){
                String items[]=s.split("#502750694#");
                for(MyUser user:userList){
                    if(user.getUserName().equals(items[0])){
                        user.addRecentMsg(items[1]);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();
            DeviceImpl.getInstance().getReCallMsgList().clear();
        }


        invalidateOptionsMenu();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onDestroy(){
        //unbindService(conn);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (DeviceImpl.getInstance().statusCode == 1){
            getMenuInflater().inflate(R.menu.menu_list, menu);
        }
        else if (DeviceImpl.getInstance().statusCode == 2){
            getMenuInflater().inflate(R.menu.menu_list_not_disturb, menu);
        }
        else if (DeviceImpl.getInstance().statusCode == 3){
            getMenuInflater().inflate(R.menu.menu_list_busy, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout_button) {
            //清除后退
            DeviceImpl.getInstance().SendMessage("server",
                    DeviceImpl.getInstance().getSipProfile().getRemoteEndpoint(),"logout");
            ListActivity.this.finish();

        }
        else if(id==R.id.add_button)
        {
            Toast.makeText(ListActivity.this, "点击列表中名字添加对方为好友",
                    Toast.LENGTH_LONG).show();
            actionSwitch=2;
        }
        else if(id==R.id.change_status){
            Intent statusIntent = new Intent(this,StatusActivity.class);
            startActivity(statusIntent);
        }

        return super.onOptionsItemSelected(item);
    }

}
