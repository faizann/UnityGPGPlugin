package com.nerdiacs.nerdgpgplugin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.Session;
import com.unity3d.player.UnityPlayerNativeActivity;

public class NerdUnityPlayerNativeActivity extends UnityPlayerNativeActivity
{

    public NerdUnityPlayerNativeActivity()
    {
    }

    protected void onCreate(Bundle arg0)
    {
        super.onCreate(arg0);
        Log.d(TAG, "!! onCreate called");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, (new StringBuilder()).append("onActivityResult: requestCode: ").append(requestCode).append(", resultCode:").append(resultCode).append(" intent data: ").append(data.toString()).toString());
        super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != 9002 || requestCode != 9001)
		{
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
    }

    static final String TAG = "NerdNative";

}
