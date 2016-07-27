package com.mycompany.myfirstapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.mycompany.myfirstapp.R;

public class SettingsActivity extends PreferenceActivity {
	 @SuppressWarnings("deprecation")
	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       
	       addPreferencesFromResource(R.xml.preference);
	 
	    }
}
