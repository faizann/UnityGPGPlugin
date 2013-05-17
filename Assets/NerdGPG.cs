using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;
using System.IO;


public class NerdGPG : MonoBehaviour {
	
	//NOTE: You might want to make this class a singleton that doesnt die between scenes
	
	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
	}
	
	// Imports
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
    public static extern void GPG_Init([In, MarshalAs(UnmanagedType.LPStr)]string clientId);
	[DllImport("__Internal")]
    public static extern bool GPG_TrySilentSignIn ();
	[DllImport("__Internal")]
    public static extern void GPG_SignIn();
	[DllImport("__Internal")]
    public static extern void GPG_SignOut();
	
	[DllImport("__Internal")]
    public static extern void GPG_GameSignIn();
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// SetGameObjectName
	/// </summary>
	public static extern void GPG_SetGameObjectName([In, MarshalAs(UnmanagedType.LPStr)]string gameObjectName);
 	
	
	[DllImport("__Internal")]
    public static extern void GPG_ShowAllLeaderBoards();
	
	[DllImport("__Internal")]
    public static extern void GPG_ShowAchievements();
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// GPs the g_ show leader boards.
	/// </summary>
	/// <param name='leaderboardId'>
	/// Leaderboard identifier.
	/// </param>
    public static extern void GPG_ShowLeaderBoards([In, MarshalAs(UnmanagedType.LPStr)]string leaderBoardId);
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// GPs the g_ submit score.
	/// </summary>
	/// <param name='leaderBoardId'>
	/// Leader board identifier.
	/// </param>
	/// <param name='value'>
	/// Score Value.
	/// </param>
    public static extern void GPG_SubmitScore([In, MarshalAs(UnmanagedType.LPStr)]string leaderBoardId, long value);
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// GPs the g_ unloc achievement.
	/// </summary>
	/// <param name='achievementId'>
	/// Achievement identifier.
	/// </param>
    public static extern void GPG_UnlocAchievement([In, MarshalAs(UnmanagedType.LPStr)]string achievementId);
	
	
	// Cloud SAVE/LOAD API
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// GPs the g_ save to cloud.
	/// Note: in case of conflict the localdata always wins. change it in plugin if you want different behaviour
	/// </summary>
	/// <param name='keyNum'>
	/// Key number. 0-3 as per GPG 
	/// </param>
	/// <param name='bytes'>
	/// Bytes.
	/// </param>
	/// <param name='len'>
	/// Length.
	/// </param>
    public static extern void GPG_SaveToCloud(int keyNum, byte[] bytes, int len);
	
	[DllImport("__Internal", CharSet = CharSet.Ansi)]
	/// <summary>
	/// GPs the g_ load from cloud.
	/// Note: in case of conflict the localdata always wins. change it in plugin if you want different behaviour
	/// </summary>
	/// <param name='keyNum'>
	/// Key number.
	/// </param>
	/// <param name='bytes'>
	/// Bytes.
	/// </param>
	/// <param name='len'>
	/// Length.
	/// </param>
    public static extern void GPG_LoadFromCloud(int keyNum, System.IntPtr bytes, int len);
	
	[DllImport("__Internal")]
	/// <summary>
	/// GPs the g_ has authoriser.
	/// </summary>
	/// <returns>
	/// true if user has authorised. false if not.
	/// </returns>
	public static extern bool GPG_HasAuthoriser();	
}
