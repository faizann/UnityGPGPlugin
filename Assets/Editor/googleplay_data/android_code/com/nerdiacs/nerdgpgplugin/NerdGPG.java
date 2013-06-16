/**
 * A lot of code is mindlessly copied from GameHelper.java from Android Google Game Play Services sample BaseGameUtils
 * https://github.com/playgameservices/android-samples/blob/master/BaseGameUtils/src/com/google/example/games/basegameutils/GameHelper.java
 */

package com.nerdiacs.nerdgpgplugin;

import java.util.Vector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.util.Base64;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.OnSignOutCompleteListener;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.plus.PlusClient;
import com.unity3d.player.UnityPlayer;


public class NerdGPG implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, OnSignOutCompleteListener, OnStateLoadedListener {

	private static String TAG = "NerdGPG";
	
	public static String gameObjectName  = null;
	public static Activity mParentActivity = null;
	public static Activity mDummyActivity = null;
	public static NerdGPG mCurrentGPG = null;
	public byte[] mKey0Data = null; 
	public byte[] mKey1Data = null; 
	public byte[] mKey2Data = null; 
	public byte[] mKey3Data = null; 
	
	public boolean mDebugLog = true;
	
    GamesClient mGamesClient = null;
    PlusClient mPlusClient = null;
    AppStateClient mAppStateClient = null;
    String[] mScopes = null;
    
 // What clients we manage (OR-able values, can be combined as flags)
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_PLUS = 0x02;
    public final static int CLIENT_APPSTATE = 0x04;
    public final static int CLIENT_PLUSPROFILE = 0x08;
    public final static int CLIENT_ALL = CLIENT_GAMES | CLIENT_PLUS | CLIENT_APPSTATE;

    final static int RC_RESOLVE = 9001;

    // Request code when invoking Activities whose result we don't care about.
    final static int RC_UNUSED = 9002;
    
    // What clients were requested? (bit flags)
    int mRequestedClients = CLIENT_NONE;

    // What clients are currently connected? (bit flags)
    int mConnectedClients = CLIENT_NONE;

    // What client are we currently connecting?
    int mClientCurrentlyConnecting = CLIENT_NONE;

    // Whether to automatically try to sign in on onStart().
    boolean mAutoSignIn = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    public static ConnectionResult mConnectionResult = null;

    // Whether our sign-in attempt resulted in an error. In this case,
    // mConnectionResult
    // indicates what was the error we failed to resolve.
    boolean mSignInError = false;
    // Whether we launched the sign-in dialog flow and therefore are expecting
    // an
    // onActivityResult with the result of that.
    boolean mExpectingActivityResult = false;

    // Are we signed in?
    boolean mSignedIn = false;
    
    // If we got an invitation id when we connected to the games client, it's
    // here.
    // Otherwise, it's null.
    String mInvitationId;

    public NerdGPG() {
    	mCurrentGPG = this;
    	mParentActivity = UnityPlayer.currentActivity;
    	debugLog("ParentActivity name is " + mParentActivity.getClass().getName());
    	
    }

	public boolean init(final Activity activity) {
		if(activity==null) {
			Log.d(TAG, "ParentActivity handle required");
			return false; // 
		}
		
		mParentActivity = activity;	
		debugLog("ParentActivity name is " + mParentActivity.getClass().getName());
        mRequestedClients = CLIENT_ALL; // connect all client types
 		// set scopes
		Vector<String> scopesVector = new Vector<String>();
		if((mRequestedClients & CLIENT_GAMES)!=0)
			scopesVector.add(Scopes.GAMES);
		if((mRequestedClients & CLIENT_PLUS)!=0)
			scopesVector.add(Scopes.PLUS_LOGIN);
		if((mRequestedClients & CLIENT_APPSTATE)!=0)
			scopesVector.add(Scopes.APP_STATE);
//        scopesVector.add(Scopes.PLUS_PROFILE);
        
        mScopes = new String[scopesVector.size()];
        scopesVector.copyInto(mScopes);
        
		mGamesClient = new GamesClient.Builder(mParentActivity, this, this)
        	.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
        	.setScopes(mScopes)
        	.create();
		
		 mPlusClient = new PlusClient.Builder(mParentActivity, this, this)
         	.setScopes(mScopes)
         	.build();
		 
		 mAppStateClient = new AppStateClient.Builder(mParentActivity, this, this)
         	.setScopes(mScopes)
         	.create();
		 
		return false;
	}
    
	public boolean hasAuthorised() {
		return mSignedIn;
	}
	
	public void showLeaderBoards(String leaderBoardId) {
		if(!mSignedIn)
			return;
		Intent intent = mGamesClient.getLeaderboardIntent(leaderBoardId);
		if(intent!=null)
			mParentActivity.startActivityForResult(intent,RC_UNUSED);
	}
	
	public void showAllLeaderBoards(){
		if(!mSignedIn)
			return;
		Intent intent = mGamesClient.getAllLeaderboardsIntent();
		if(intent!=null)
			mParentActivity.startActivityForResult(intent,RC_UNUSED);
	}
	
	public void showAchievements() {
		if(!mSignedIn)
			return;
		Intent intent = mGamesClient.getAchievementsIntent();
		if(intent!=null)
			mParentActivity.startActivityForResult(intent,RC_UNUSED);
	}
	
	public void submitScore(String leaderboardId, long score) {
		if(!mSignedIn)
			return;
		// might want to use API that gives back result of submission
		mGamesClient.submitScore(leaderboardId, score);
	}
	
	public void unlockAchievement(String achievementId) {
		if(!mSignedIn)
			return;
		mGamesClient.unlockAchievement(achievementId);
	}
	
	public void saveToCloud(int keyNum, String bytes) {
		debugLog("Length of bytes received "+bytes.length());
		//byte[] data = Base64.decode(bytes,)
		byte[] data = Base64.decode(bytes, Base64.DEFAULT);
		mAppStateClient.updateState(keyNum, data); // could also use listener version of this function to get result instantly
	}
	
	public void loadFromCloud(int keyNum) {
		mAppStateClient.loadState(this, keyNum);
	}
	
	public String getLoadedData(int keyNum) {
		String data = null;
		switch(keyNum) {
		case 0:
			data = Base64.encodeToString(mKey0Data, Base64.DEFAULT);
			break;
		case 1:
			data = Base64.encodeToString(mKey1Data, Base64.DEFAULT);
			break;
		case 2:
			data = Base64.encodeToString(mKey2Data, Base64.DEFAULT);
			break;
		case 3:
			data = Base64.encodeToString(mKey3Data, Base64.DEFAULT);
			break;
		}
		return data;
	}
	public void signOut() {
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        mSignInError = false;

        if (mPlusClient != null && mPlusClient.isConnected()) {
            mPlusClient.clearDefaultAccount();
        }
        if (mGamesClient != null && mGamesClient.isConnected()) {
//            showProgressDialog(false);
            mGamesClient.signOut(this);
        }

        // kill connects to all clients but games, which must remain
        // connected til we get onSignOutComplete()
        killConnections(CLIENT_ALL & ~CLIENT_GAMES);		
	}
	
	public boolean silentSignIn() {
		return signIn();
	}
	
	public boolean signIn() {
		if(hasAuthorised())
			return true; // already authorised
			
		mConnectedClients = CLIENT_NONE;
//	    mInvitationId = null;
	    connectNextClient();
		return true;
	}
	
	private void connectNextClient() {
        // do we already have all the clients we need?
        int pendingClients = mRequestedClients & ~mConnectedClients;
        if (pendingClients == 0) {
            debugLog("All clients now connected. Sign-in successful.");
            succeedSignIn();
            return;
        }

        // which client should be the next one to connect?
        if (mGamesClient != null && (0 != (pendingClients & CLIENT_GAMES))) {
            debugLog("Connecting GamesClient.");
            mClientCurrentlyConnecting = CLIENT_GAMES;
        } else if (mPlusClient != null && (0 != (pendingClients & CLIENT_PLUS))) {
            debugLog("Connecting PlusClient.");
            mClientCurrentlyConnecting = CLIENT_PLUS;
        } else if (mAppStateClient != null && (0 != (pendingClients & CLIENT_APPSTATE))) {
            debugLog("Connecting AppStateClient.");
            mClientCurrentlyConnecting = CLIENT_APPSTATE;
        } else {
            throw new AssertionError("Not all clients connected, yet no one is next. R="
                    + mRequestedClients + ", C=" + mConnectedClients);
        }

        connectCurrentClient();
    }

	void connectCurrentClient() {
        switch (mClientCurrentlyConnecting) {
            case CLIENT_GAMES:
            	mParentActivity.runOnUiThread(new Runnable() {

					public void run() {
						mGamesClient.connect();
					}            		
            	});
                
                break;
            case CLIENT_APPSTATE:
            	mParentActivity.runOnUiThread(new Runnable() {
					public void run() {
						mAppStateClient.connect();
					}
            	});
                
                break;
            case CLIENT_PLUS:
            	mParentActivity.runOnUiThread(new Runnable() {
            		public void run() {
            			mPlusClient.connect();
            		}
            	});
                break;
        }
    }
	
	void succeedSignIn() {
        debugLog("All requested clients connected. Sign-in succeeded!");
        mSignedIn = true;
        mSignInError = false;
        mAutoSignIn = true;
        mUserInitiatedSignIn = false;
        UnitySendMessageSafe("GPGAuthResult", "success");
    }


	void killConnections(int whatClients) {
        if ((whatClients & CLIENT_GAMES) != 0 && mGamesClient != null
                && mGamesClient.isConnected()) {
            mConnectedClients &= ~CLIENT_GAMES;
            mGamesClient.disconnect();
        }
        if ((whatClients & CLIENT_PLUS) != 0 && mPlusClient != null
                && mPlusClient.isConnected()) {
            mConnectedClients &= ~CLIENT_PLUS;
            mPlusClient.disconnect();
        }
        if ((whatClients & CLIENT_APPSTATE) != 0 && mAppStateClient != null
                && mAppStateClient.isConnected()) {
            mConnectedClients &= ~CLIENT_APPSTATE;
            mAppStateClient.disconnect();
        }
    }
	
	public void onSignOutComplete() {
		if (mGamesClient.isConnected())
            mGamesClient.disconnect();
		UnitySendMessageSafe("GPGAuthResult", "signedout");
	}

	public void onConnectionFailed(ConnectionResult result) {
		// save connection result for later reference
        mConnectionResult = result;
        debugLog("onConnectionFailed: result " + result.getErrorCode());
        if(result.hasResolution())
        	resolveConnectionResult();
        else
        	UnityPlayer.UnitySendMessage(TAG, "GPGAuthResult", "error");
	}

	public void onConnected(Bundle connectionHint) {
		debugLog("onConnected: connected! client=" + mClientCurrentlyConnecting);

        // Mark the current client as connected
        mConnectedClients |= mClientCurrentlyConnecting;

        // If this was the games client and it came with an invite, store it for
        // later retrieval.
        if (mClientCurrentlyConnecting == CLIENT_GAMES && connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint.getParcelable(GamesClient.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // accept invitation
                debugLog("onConnected: connection hint has a room invite!");
                mInvitationId = inv.getInvitationId();
                debugLog("Invitation ID: " + mInvitationId);
            }
        }

        // connect the next client in line, if any.
        connectNextClient();
		
	}

	public void onDisconnected() {
		debugLog("onDisconnected.");
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        mSignInError = false;
        mInvitationId = null;
        mConnectedClients = CLIENT_NONE;
        UnitySendMessageSafe("GPGAuthResult", "signedout");
	}
	
	/**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_RESOLVE) {
            // We're coming back from an activity that was launched to resolve a
            // connection
            // problem. For example, the sign-in UI.
            mExpectingActivityResult = false;
            debugLog("onActivityResult, req " + requestCode + " response " + responseCode);
            if (responseCode == Activity.RESULT_OK) {
                // Ready to try to connect again.
                debugLog("responseCode == RESULT_OK. So connecting.");
                connectCurrentClient();
            } else {
                // Whatever the problem we were trying to solve, it was not
                // solved.
                // So give up and show an error message.
                debugLog("responseCode != RESULT_OK, so not reconnecting.");
                giveUp();
            }
        }
    }
    
    void resolveConnectionResult() {
        // Try to resolve the problem
        debugLog("resolveConnectionResult: trying to resolve result: " + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingActivityResult = true;
                debugLog("Resolving intent with activity "+mParentActivity);
                // create dummy activity
                /*mDummyActivity = new DummyActivity();
                Intent intent = new Intent();
                mDummyActivity.startActivity(intent);*/
                
                Intent intent = new Intent(mParentActivity, DummyActivity.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    			//mParentActivity.startActivity(intent);
    			mParentActivity.startActivityForResult(intent, RC_UNUSED);
    			
//                mConnectionResult.startResolutionForResult(mParentActivity, RC_RESOLVE);
                //PendingIntent intent = mConnectionResult.getResolution();
//                mParentActivity.startActivityForResult((Intent)intent, RC_RESOLVE);
            } catch (Exception e) {
                // Try connecting again
                debugLog("SendIntentException.");
                connectCurrentClient();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp();
        }
    }

    
    void giveUp() {
        mSignInError = true;
        mAutoSignIn = false;
        debugLog("giveUp: giving up on connection. " +
                ((mConnectionResult == null) ? "(no connection result)" :
                        ("Status code: " + mConnectionResult.getErrorCode())));

        if (mConnectionResult != null) {
            UnitySendMessageSafe("GPGAuthResult", "error,"+mConnectionResult.getErrorCode());
        } else {
            // this is a bug
            Log.e("GameHelper", "giveUp() called with no mConnectionResult");
            UnitySendMessageSafe("GPGAuthResult", "error");
        }
    }
    
	private void debugLog(String msg) {
		if (mDebugLog)
            Log.d(TAG, msg);
	}

	private void UnitySendMessageSafe(String method, String param)
	{
		if(gameObjectName==null || method==null)
			return;
		UnityPlayer.UnitySendMessage(gameObjectName, method, param);
	}
	public void onStateConflict(int stateKey, String resolvedVersion,
			byte[] localData, byte[] serverData) {
		Log.e(TAG,"Conflict arose in data for key "+ stateKey);
		
		// by default we override localdata over remote data
		// very simple but flawed.
		mAppStateClient.resolveState(this, stateKey, resolvedVersion, localData);
	}

	public void onStateLoaded(int statusCode, int stateKey, byte[] localData) {
		//System.arraycopy(localData, 0, mLastLoadedData, 0, localData.length);
		if(statusCode != AppStateClient.STATUS_OK) {
			Log.e(TAG,"Error in loading key data "+stateKey + " " + statusCode);
			UnitySendMessageSafe("OnGPGCloudLoadResult","error;"+stateKey+";0");
			return;
		}
		switch(stateKey) {
		case 0:
			mKey0Data = localData;
			break;
		case 1:
			mKey1Data = localData;
			break;
		case 2:
			mKey2Data = localData;
			break;
		case 3:
			mKey3Data = localData;
			break;
		}
		
		UnitySendMessageSafe("OnGPGCloudLoadResult","success;"+stateKey+";"+localData.length);
	}
}
