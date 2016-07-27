package com.mycompany.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mycompany.myfirstapp.ua.SipProfile;
import com.mycompany.myfirstapp.ua.impl.DeviceImpl;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.HashMap;

public class MyActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    private EditText enterRemoteIP;
    private EditText enterRemotePort;
    private Button loginButton;
    private String remoteIP;
    private String remotePort;
    private String localUserName;
    private String localIP;
    private String localPort;

    private Intent sipIntent;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Name = "nameKey";

    //SharedPreferences sharedPreferences;
    SharedPreferences prefs;

    SipProfile sipProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the user interface layout for this Activity
        // The layout file is defined in the project res/layout/main_activity.xml file

        setContentView(R.layout.activity_my);//读进说明,形成界面
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);//工具栏,XML定义
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);//浮动按钮
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        enterRemoteIP = (EditText) findViewById(R.id.enter_IP);
        enterRemotePort = (EditText) findViewById(R.id.enter_Port);

        loginButton = (Button) findViewById(R.id.sign_in_button);

        localIP = getWifiIpAddress(MyActivity.this);
        //init connection information
        DeviceImpl.getInstance().setSipProfile(new SipProfile());
        //sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // register preference change listener
        prefs.registerOnSharedPreferenceChangeListener(this);
        initializeSipFromPreferences();

        Editor editor = prefs.edit();
        editor.putString("pref_local_ip",localIP);
        editor.commit();

        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("customHeader1","customValue1");
        customHeaders.put("customHeader2","customValue2");

        DeviceImpl.getInstance().Initialize(getApplicationContext(), DeviceImpl.getInstance().getSipProfile(),customHeaders);
        DeviceImpl.getInstance().setDeviceHandler(handler);
        setTitle("BUPT-SIP聊天工具");

    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onRestart(){

        super.onRestart();

        setContentView(R.layout.activity_my);//读进说明,形成界面
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);//工具栏,XML定义
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);//浮动按钮
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        enterRemoteIP = (EditText) findViewById(R.id.enter_IP);
        enterRemotePort = (EditText) findViewById(R.id.enter_Port);

        loginButton = (Button) findViewById(R.id.sign_in_button);

        localIP = getWifiIpAddress(MyActivity.this);
        //init connection information
        DeviceImpl.getInstance().setSipProfile(new SipProfile());
        //sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // register preference change listener
        prefs.registerOnSharedPreferenceChangeListener(this);
        initializeSipFromPreferences();

        Editor editor = prefs.edit();
        editor.putString("pref_local_ip",localIP);
        editor.commit();

        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("customHeader1","customValue1");
        customHeaders.put("customHeader2","customValue2");

        //DeviceImpl.getInstance().Initialize(getApplicationContext(), DeviceImpl.getInstance().getSipProfile(),customHeaders);
        DeviceImpl.getInstance().setDeviceHandler(handler);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {

        remoteIP = enterRemoteIP.getText().toString();
        remotePort = enterRemotePort.getText().toString();

        //userName = enterName.getText().toString();

        if (remoteIP.equals("") || remotePort.equals("")) {
            Toast.makeText(getBaseContext(), "连接信息不完整", Toast.LENGTH_SHORT).show();
        } else {

            DeviceImpl.getInstance().getSipProfile().setRemoteIp(remoteIP);
            DeviceImpl.getInstance().getSipProfile().setRemotePort(Integer.parseInt(remotePort));
            DeviceImpl.getInstance().getSipProfile().setLocalIp(prefs.getString("pref_local_ip",localIP));
            DeviceImpl.getInstance().getSipProfile().setLocalPort(Integer.parseInt(prefs.getString("pref_local_port","5080")));
            DeviceImpl.getInstance().getSipProfile().setSipUserName(prefs.getString("pref_sip_user","qwesd"));

            //connectionBinder.sendMsg("sip:zhaoyue@" + ip + ":" + port, userName);

            DeviceImpl.getInstance().SendInviteMessage("sip:server@" + DeviceImpl.getInstance().getSipProfile().getRemoteIp() +
                    ":" + DeviceImpl.getInstance().getSipProfile().getRemotePort());
        }

    }

    public void onLoginSuccess(){

        /*String n = "chatname";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Name,n);
        editor.commit();*/

        //activity to chat

        Intent intent = new Intent(getBaseContext(), ListActivity.class);
       /* intent.putExtra("targetname","qwe");
        intent.putExtra("targetaddress","192.168.137.:5080");
*/


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startActivity(intent);
        Toast.makeText(getBaseContext(), "连接成功", Toast.LENGTH_SHORT).show();
    }

    public void onLoginFail(){
        /*unbindService(conn);
        stopService(sipIntent);*/
        Toast.makeText(getBaseContext(), "连接失败/超时", Toast.LENGTH_SHORT).show();
    }

    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    onLoginSuccess();
                    break;
                case 2:
                    onLoginFail();
                    break;
                default:
                    break;
            }
        }

    };


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_local_ip")) {
            DeviceImpl.getInstance().getSipProfile().setLocalIp(prefs.getString("pref_local_ip", localIP));
        } else if (key.equals("pref_local_port")) {
            DeviceImpl.getInstance().getSipProfile().setLocalPort(Integer.parseInt(prefs.getString("pref_local_port", "5080")));
        }  else if (key.equals("pref_sip_user")) {
            DeviceImpl.getInstance().getSipProfile().setSipUserName(prefs.getString("pref_sip_user", "qwesd"));
        } else if (key.equals("pref_sip_password")) {
            DeviceImpl.getInstance().getSipProfile().setSipPassword(prefs.getString("pref_sip_password", "1234"));
        }
    }

    @SuppressWarnings("static-access")
    private void initializeSipFromPreferences() {
        DeviceImpl.getInstance().getSipProfile().setLocalIp((prefs.getString("pref_local_ip", localIP)));
        DeviceImpl.getInstance().getSipProfile().setLocalPort(Integer.parseInt(prefs.getString("pref_local_port", "5080")));
        DeviceImpl.getInstance().getSipProfile().setSipUserName(prefs.getString("pref_sip_user", "qwesd"));
        DeviceImpl.getInstance().getSipProfile().setSipPassword(prefs.getString("pref_sip_password", "1234"));

    }

    protected String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;

        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            //mUIMsgHandler.sendMessageToUI("WIFIIP Unable to get host address.");
            Toast.makeText(getBaseContext(), "无法获取WiFi地址", Toast.LENGTH_SHORT).show();
            ipAddressString = null;
        }
        return ipAddressString;
    }

}
