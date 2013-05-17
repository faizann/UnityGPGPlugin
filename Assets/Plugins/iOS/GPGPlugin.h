//
//  GPGPlugin.h
//  Unity-iPhone
//
//  Created by Faizan Naqvi on 5/17/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <GooglePlus/GooglePlus.h>
#import <PlayGameServices/PlayGameServices.h>

@interface GPGPlugin : NSObject<GPPSignInDelegate,GPGLeaderboardsControllerDelegate,
  GPGLeaderboardControllerDelegate,
  GPGAchievementControllerDelegate>

@property (nonatomic, retain) NSString *gameObjectName;

@end
