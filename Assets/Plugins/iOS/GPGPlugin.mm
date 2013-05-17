//
//  GPGPlugin.m
//  Unity-iPhone
//
//  Created by Faizan Naqvi on 5/17/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import "GPGPlugin.h"
#import "iPhone_View.h"
  //#include "string.h"

@implementation GPGPlugin

@synthesize gameObjectName;

static GPGPlugin *singletonInstance = nil;

+ (GPGPlugin*)getSingleton
{
  if(singletonInstance != nil) {
    return singletonInstance;
  }
  singletonInstance = [[GPGPlugin alloc] init];
  return singletonInstance;
}

-(id)init:(NSString*)clientId
{
  [super init];
  GPPSignIn *signIn = [GPPSignIn sharedInstance];
  
  signIn.clientID = clientId;
  signIn.scopes = [NSArray arrayWithObjects:
                   @"https://www.googleapis.com/auth/games",
                   @"https://www.googleapis.com/auth/appstate"
                   , nil];
  
  signIn.delegate = self;
  signIn.shouldFetchGoogleUserID =YES;
  return self;
}

  // Utility functions
- (void) GPG_UnitySendMessageSafe:(const char *)method  // name is made different as same func is in other plugins
                              msg:(const char *)msg
{
  if([self gameObjectName]!=NULL) // not sure if we even need this
  {
      // Unity crashes if method or msg is NULL
    if([[self gameObjectName] length] > 0 && method!=NULL && msg!=NULL)
    {
      UnitySendMessage([gameObjectName UTF8String], method, msg);      
    }
  }
}

extern "C" void GPG_GameSignIn()
{
  [[GPGManager sharedInstance] signIn:[GPPSignIn sharedInstance] 
                   reauthorizeHandler:^(BOOL requiresKeychainWipe, NSError *error) {
                     if(requiresKeychainWipe) {
                       NSLog(@"GoogleSignin requires keychainwipe");
                       [[GPPSignIn sharedInstance] signOut];
                     }
                     [[GPPSignIn sharedInstance] authenticate];
                   }];
}

  // Google Plus Signin delegates
- (void)finishedWithAuth:(GTMOAuth2Authentication *)auth error:(NSError *)error
{
  NSLog(@"Finished with auth.");
  if (error == nil && auth) {
    NSLog(@"Success signing in to Google! Auth object is %@", auth);
      // Eventually, you'll want to do something here.
    [self GPG_UnitySendMessageSafe:"GPGAuthResult" msg:"success"];
    GPG_GameSignIn(); // sign into game
  } else {
    NSLog(@"Failed to log into Google\n\tError=%@\n\tAuthObj=%@",error,auth);
    [self GPG_UnitySendMessageSafe:"GPGAuthResult" msg:"failed"];
  }
}


  // Google Play Game Leaderboard functions
- (void)leaderboardViewControllerDidFinish: (GPGLeaderboardController *)viewController {

    [UnityGetGLViewController() dismissModalViewControllerAnimated:YES];
}

- (void)leaderboardsViewControllerDidFinish: (GPGLeaderboardsController *)viewController {
  [UnityGetGLViewController() dismissModalViewControllerAnimated:YES];
}

- (void)achievementViewControllerDidFinish: (GPGAchievementController *)viewController {
    [UnityGetGLViewController() dismissModalViewControllerAnimated:YES];
}

extern "C" void GPG_Init(const char *clientId)
{
  NSString *str = [NSString stringWithUTF8String:clientId];
  [[GPGPlugin getSingleton] init:str];
}

extern "C" bool GPG_TrySilentSignIn()
{
 return [[GPPSignIn sharedInstance] trySilentAuthentication];
}

extern "C" void GPG_SignIn()
{
  [[GPPSignIn sharedInstance] authenticate];
}

extern "C" void GPG_SignOut()
{
  [[GPPSignIn sharedInstance] signOut];
}

extern "C" void GPG_SetGameObjectName(const char * name)
{
  [[GPGPlugin getSingleton] setGameObjectName:[NSString stringWithUTF8String:name]];
}

extern "C" void GPG_ShowAllLeaderBoards()
{
  GPGLeaderboardsController *leadsController = [[GPGLeaderboardsController alloc] init];
  

  leadsController.leaderboardsDelegate = [GPGPlugin getSingleton];
  [UnityGetGLViewController() presentModalViewController:leadsController animated:YES];
}

extern "C" void GPG_ShowLeaderBoards(const char * leaderBoardId)
{
  if(!leaderBoardId)
    return;
  NSString *strBoardId = [NSString stringWithUTF8String:leaderBoardId];
  GPGLeaderboardController *leadController = [[GPGLeaderboardController alloc] initWithLeaderboardId:strBoardId];
  leadController.leaderboardDelegate = [GPGPlugin getSingleton];
  [UnityGetGLViewController() presentModalViewController:leadController animated:YES];  
}
  // C#'s long is 64 bit where as long long in C is also 64 bit
extern "C" void GPG_SubmitScore(const char * leaderBoardId, long long value) 
{
  if(!leaderBoardId)
    return; // no leaderboard given
  NSString *strBoardId = [NSString stringWithUTF8String:leaderBoardId];
  GPGScore *score = [[GPGScore alloc] initWithLeaderboardId:strBoardId];
  score.value = value;
  
  [score submitScoreWithCompletionHandler:^(GPGScoreReport *report, NSError *error) {
    if(error) {
      NSString *strErr = [error localizedDescription];
      NSLog(@"Error in submit score %@", strErr);
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGSubmitScoreResult" msg:[strErr UTF8String]];
    } else {
      NSLog(@"Score submitted");
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGSubmitScoreResult" msg:"success"];
    }
  }];
}
extern "C" void GPG_ShowAchievements()
{
  GPGAchievementController *achController = [[GPGAchievementController alloc] init];
  achController.achievementDelegate = [GPGPlugin getSingleton];
  [UnityGetGLViewController() presentModalViewController:achController animated:YES];
}


extern "C" void GPG_UnlocAchievement(const char *achievementId)
{
  if(!achievementId) 
    return; // no ach id
  NSString *strAchId = [NSString stringWithUTF8String:achievementId];
  GPGAchievement *achId = [GPGAchievement achievementWithId:strAchId];
  [achId unlockAchievementWithCompletionHandler:^(BOOL newlyUnlocked, NSError *error) {
    if(error) {
      NSString *strErr = [error localizedDescription];
      NSLog(@"Error in unlocking achievement %@", strErr);
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGUnlockAchievementResult" msg:[strErr UTF8String]];
    } else {
      NSLog(@"Achievement unlocked");
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGUnlockAchievementResult" msg:"success"];
    }
  }];
}

extern "C" void GPG_SaveToCloud(int keyNum, const char *bytes, int len)
{
  if(len<1 || bytes==nil)
    return; // can't do much without proper data
  
  NSData *nsdata = [[NSData alloc] initWithBytes:bytes length:len];
  
  GPGAppStateModel *model = [GPGManager sharedInstance].applicationModel.appState;
  
  NSNumber *dataKey = [NSNumber numberWithInt:keyNum]; // 0-3 available
  
  
  [model setStateData:nsdata forKey:dataKey];
  
  [model updateForKey:dataKey completionHandler:^(GPGAppStateWriteStatus status, NSError *error) {
    if (status == GPGAppStateWriteStatusSuccess) {
      NSLog(@"Hooray! Cloud update is complete");
      NSString *strRes = [NSString stringWithFormat:@"success;%@",dataKey];
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGCloudSaveResult" msg:[strRes UTF8String]];
    }
  } conflictHandler:^NSData *(NSNumber *key, NSData *localState, NSData *remoteState) {
    NSLog(@"Found conflict in data");
    NSString *strRes = [NSString stringWithFormat:@"conflict;%@",dataKey];
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGCloudSaveResult" msg:[strRes UTF8String]];
    return localState;
  }];
}

extern "C" void GPG_LoadFromCloud(int keyNum, const char *bytes, int len)
{
  GPGAppStateModel *model = [GPGManager sharedInstance].applicationModel.appState;
  
  NSNumber *dataKey = [NSNumber numberWithInt:keyNum]; // 0-3 available
  
  [model loadForKey:dataKey completionHandler:^(GPGAppStateLoadStatus status, NSError *error) {
    if (status == GPGAppStateLoadStatusNotFound) {
        // Data for this key does not exist. This must be the first time our user
        // played this game
        //      [self loadUpDefaultPlayerAvatar];
      NSLog(@"Data not found");
    } else if (status == GPGAppStateLoadStatusSuccess) {
      NSLog(@"Data loaded for key %@",dataKey);
      NSData *data = [model stateDataForKey:dataKey];
        //      NSString *str = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        //      NSLog(@"Load data successful %@", str);
      int dataLen = [data length]/sizeof(char);
      int lenCopy = (dataLen>len?len:dataLen); // dataLen if it is less than len else truncate to len
      memset((void*)bytes, 0, len); // set all data to null 
      memcpy((void*)bytes, (const void*)[data bytes],lenCopy);
        // we are being cheap here and using the sendmessage to specify that data is updated
        // we need to inform about key for which this data belongs and length of it
      NSString *strRes = [NSString stringWithFormat:@"success;%@;%d",dataKey,lenCopy];
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGCloudLoadResult" msg:[strRes UTF8String]];
    } else if (status == GPGAppStateLoadStatusUnknownError) {
        // Handle the error
      NSLog(@"Unknown error in loading");
      NSString *strRes = [NSString stringWithFormat:@"error;%@;%d",dataKey,len];
      [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGCloudLoadResult" msg:[strRes UTF8String]];
    }
  } conflictHandler:^NSData *(NSNumber *key, NSData *localState, NSData *remoteState) {
          NSString *strRes = [NSString stringWithFormat:@"conflict;%@;%d",dataKey,len];
          [[GPGPlugin getSingleton] GPG_UnitySendMessageSafe:"OnGPGCloudLoadResult" msg:[strRes UTF8String]];
    return localState;
  }];
}

extern "C" bool GPG_HasAuthoriser()
{
  return [[GPGManager sharedInstance] hasAuthorizer];
}
@end
