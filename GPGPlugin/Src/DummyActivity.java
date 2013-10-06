package com.nerdiacs.nerdgpgplugin;

import com.google.android.gms.common.ConnectionResult;
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DummyActivity extends Activity {

	
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
			

	}
	protected void onStart()
	{
		super.onStart();
		try {
			if(NerdGPG.mConnectionResult!=null)
				NerdGPG.mConnectionResult.startResolutionForResult(this, NerdGPG.RC_RESOLVE);	
		} catch (SendIntentException e) {
			Log.e("NERDGPG", "Error in starting connection resolution "+ e.getMessage());
		}
		
	}
	protected void onDestroy ()
	{
		super.onDestroy();
	}
	
	public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if(NerdGPG.mCurrentGPG!=null)
        	NerdGPG.mCurrentGPG.onActivityResult(request, response, data);
        finish();
    }
}
