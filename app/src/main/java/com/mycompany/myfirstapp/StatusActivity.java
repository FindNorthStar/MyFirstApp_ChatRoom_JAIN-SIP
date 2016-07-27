package com.mycompany.myfirstapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mycompany.myfirstapp.ua.impl.DeviceImpl;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends Activity{

    private List<String> statusList = new ArrayList<String>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statusList.add("在线");
        statusList.add("勿扰");
        statusList.add("离开");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                StatusActivity.this, android.R.layout.simple_list_item_1, statusList);
        ListView listView = (ListView) findViewById(R.id.candidate_status);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long id){
                String status  = statusList.get(postion);

                if (status.equals("在线")) {
                    DeviceImpl.getInstance().statusCode = 1;
                    DeviceImpl.getInstance().SendAction("sip:server@" +
                            DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                            DeviceImpl.getInstance().getSipProfile().getRemotePort(),"online");
                }
                else if (status.equals("勿扰")) {
                    DeviceImpl.getInstance().statusCode = 2;
                    DeviceImpl.getInstance().SendAction("sip:server@" +
                            DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                            DeviceImpl.getInstance().getSipProfile().getRemotePort(),"busy");

                }
                else if (status.equals("离开")) {
                    DeviceImpl.getInstance().statusCode = 3;
                    DeviceImpl.getInstance().SendAction("sip:server@" +
                            DeviceImpl.getInstance().getSipProfile().getRemoteIp() + ":" +
                            DeviceImpl.getInstance().getSipProfile().getRemotePort(),"afk");
                }

                Toast.makeText(StatusActivity.this, status,
                        Toast.LENGTH_SHORT).show();
                StatusActivity.this.finish();
            }

        });

    }



}
