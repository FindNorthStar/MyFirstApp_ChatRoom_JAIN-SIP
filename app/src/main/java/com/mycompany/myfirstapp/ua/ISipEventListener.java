package com.mycompany.myfirstapp.ua;

import java.util.EventListener;

import com.mycompany.myfirstapp.ua.impl.SipEvent;

public interface ISipEventListener extends EventListener {

	public void onSipMessage(SipEvent sipEvent);
}
