package com.nerdiacs.nerdgpgplugin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.Session;
import com.unity3d.player.UnityPlayerActivity;

public class NerdUnityPlayerActivity extends UnityPlayerActivity
{

    public NerdUnityPlayerActivity()
    {
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("NerdUnityPlayerActivity", "!! NerdUnityPlayerActivity:: onCreate called!");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		Log.d(TAG, "RequestCode: " + requestCode);
		
        super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode != 9002 || requestCode != 9001)
		{
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
    }

    static final String TAG = "NerdUnityPlayerActivity";
}
