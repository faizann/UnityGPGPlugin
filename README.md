UnityGPGPlugin
==============

Unity3D Google Play Services Plugin

Copyright (C) 2011 Nerdiacs Pte Limited  http://www.Nerdiacs.com

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


## Features

* Google Cloud Save
* Leaderboards
* Achievements
* Signin


## Usage

* Read the iOS SDK docs to understand how all fits in. https://developers.google.com/games/services/ios/quickstart
* On first usage, run bootstrap.sh to download the necessary SDKs. If newer SDKs are available than the one in bootstrap then download those and rename paths in Assets/Editor/googleplay_xcode.py.
* Setup GooglePlayAppID and bundleId of your app/game in googleplay_xcode.
* To test the sample App, in GCGui.cs file setup clientId from googleplay and rest of leaderboardip.
* Run and build app on ipad. click on Init GPG before doing anything else. Try silentSignin and if it fails it will display SignIn button. Use that to do proper auto with switching.

## Trouble Shooting

### Signin doesn't work

The plugin expects google chrome in application:openUrl part. Change code there to allow any type of browser. 
Make sure bundled, gpgappid etc are all setup properly in both project and google dev console web guy.

### Xcode Project Corrupted

There is PostBuildProcess script that adds frameworks and configures the overall project. Try removing that and add frameworks/bundles yourself using googleplay docs
Use the quickstart guide link https://developers.google.com/games/services/ios/quickstart for more information

### Conflicts with other plugins
* If there is another PostBuildProcess from another plugin (facebook etc) then chain script from this one so that it runs as well.
* The postbuildprocess patches AppController.m file by adding handler for google signin

```objc
(BOOL)application:(UIApplication *)application
             openURL:(NSURL *)url
   sourceApplication:(NSString *)sourceApplication
          annotation:(id)annotation 
```

This might conflict with facebook and you would need to remove the patch from PostBuildProcess and add this code manually each time project is generated. You coud also make a combined patch for google and other plugins.


## TODO

* Add support for multiplayer matchmaking
* Add support for sharing on google+

