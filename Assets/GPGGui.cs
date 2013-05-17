using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;
	
public class GPGGui : MonoBehaviour {
	
	private enum GPLoginState {loggedout, loggedin};
	private GPLoginState m_loginState = GPLoginState.loggedout;
	bool needFullSignin = false;
	private string dataToSave = "Hello World";
	
	private byte[] key0CloudData = new byte[1024]; // 128k is max length. set it to what you think is you need
	// you can have upto 4 keys range 0-3
	private string clientId = "CLIENTID.apps.googleusercontent.com";
	
	private string testLeaderBoard = "LEADERBOARD_ID";
	private string testAchievement = "ACHIEVEMENT_ID";
	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
	}
	
	void OnGUI() {
		GUILayout.Label("Unity GPG Plugin Demo");
		GUILayout.Space(20);
		GUILayout.BeginVertical();
		GUILayout.BeginHorizontal();
		
		if(GUILayout.Button("Init GPG", GUILayout.Height(60))) {
			Debug.Log("Init called");
			NerdGPG.GPG_SetGameObjectName(name);
			NerdGPG.GPG_Init(clientId);
		}
		if(GUILayout.Button("Silent SignIn", GUILayout.Height(60))) {
			Debug.Log("Silent Signin called");
			needFullSignin = !NerdGPG.GPG_TrySilentSignIn();
		}
		if(needFullSignin) {
			if(GUILayout.Button("SignIn", GUILayout.Height(60))) {
				Debug.Log("Signin called");
				NerdGPG.GPG_SignIn();
			}
		}
		if(m_loginState == GPLoginState.loggedin) {
			if(GUILayout.Button("SignOut", GUILayout.Height(60))) {
				NerdGPG.GPG_SignOut();
			}
		}
		GUILayout.EndHorizontal();
		
		GUILayout.BeginHorizontal();
		if(m_loginState==GPLoginState.loggedin) {
			// we are logged in. we can do leaderboard and achievement and cloud stuff
			if(GUILayout.Button("GPG_ShowAllLeaderBoards", GUILayout.Height(60))) {
				NerdGPG.GPG_ShowAllLeaderBoards();
			}
			if(GUILayout.Button("GPG_ShowLeaderBoards", GUILayout.Height(60))) {
				NerdGPG.GPG_ShowLeaderBoards(testLeaderBoard);
			}
			
			if(GUILayout.Button("GPG_ShowAchievements", GUILayout.Height(60))) {
				NerdGPG.GPG_ShowAchievements();
			}
			
			if(GUILayout.Button("GPG_SubmitScore", GUILayout.Height(60))) {
				NerdGPG.GPG_SubmitScore(testLeaderBoard,80);
			}
			if(GUILayout.Button("GPG_UnlocAchievement", GUILayout.Height(60))) {
				NerdGPG.GPG_UnlocAchievement(testAchievement);
			}
		GUILayout.EndHorizontal();
		GUILayout.BeginHorizontal();	
			dataToSave = GUILayout.TextField(dataToSave,100);
			if(GUILayout.Button("GPG_SaveToCloud", GUILayout.Height(60))) {
				byte[] bytes = new byte[dataToSave.Length * sizeof(char)];
    			System.Buffer.BlockCopy(dataToSave.ToCharArray(), 0, bytes, 0, bytes.Length);
				NerdGPG.GPG_SaveToCloud(0,bytes,bytes.Length);
			}
			if(GUILayout.Button("GPG_LoadFromCloud", GUILayout.Height(60))) {
				
//				NerdGPG.GPG_LoadFromCloud(0,bytes,bytes.Length);
				GCHandle handle = GCHandle.Alloc(key0CloudData,GCHandleType.Pinned);
				NerdGPG.GPG_LoadFromCloud(0,handle.AddrOfPinnedObject(),key0CloudData.Length);
				handle.Free();
			}
		}
		GUILayout.EndHorizontal();
		if(GUILayout.Button("GPG_HasAuthoriser", GUILayout.Height(60))) {
			Debug.Log("HasAuthoriser result "+NerdGPG.GPG_HasAuthoriser());
		}
		GUILayout.EndVertical();
	}
	
	public void GPGAuthResult(string result)
	{
		
		// success/failed
		if(result == "success") {
			m_loginState = GPLoginState.loggedin;
			needFullSignin = false;
		} else 
			m_loginState = GPLoginState.loggedout;
	}
	
	public void OnGPGCloudLoadResult(string result)
	{
		// result is in the format result;keyNum;length
		// where result is either success/conflict/error
		// keyNum is the key for which this result is 0-3 range as per GPG
		// length is the length of data received from GPG Cloud. Important for binary data handling
		// NOTE: In this code we are only saving/loading STRING data. but it should be fine to use it for any binary data
		Debug.Log("OnGPGCloudLoadResult "+result);
		string[] resArr = result.Split(';');
		if(resArr.Length<3)
		{
			Debug.LogError("Length of array after split is less than 3");	
			return; // weird stuff
		}
		
		if(resArr[0]=="success") {
			// lets see what our data holds.
			string str = System.Text.Encoding.Unicode.GetString(key0CloudData);
			Debug.Log("Data read for key "+ resArr[1] + " is " + str + " with len "+ resArr[2] + " and converted string length is "+ str.Length);
			dataToSave = str;
		}
	}
	
	public void OnGPGCloudSaveResult(string result)
	{
		// result is in the format result;keyNum
		// where result is either success/conflict/error
		// keyNum is the key for which this result is 0-3 range as per GPG
		
		Debug.Log("GPG CloudSaveResult "+result);
		string[] resArr = result.Split(';');
		if(resArr.Length<3)
		{
			Debug.LogError("Length of array after split is less than 3");	
			return; // weird stuff
		}
	}
	
	public void OnGPGUnlockAchievementResult(string result)
	{
		Debug.Log("OnGPGUnlockAchievementResult "+result);
	}
	
	public void OnGPGSubmitScoreResult(string result)
	{
		Debug.Log("OnGPGSubmitScoreResult "+result);
	}
}
