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


Features

Google Cloud Save
Leaderboards
Achievements
Signin


Usage

1-- On first usage, run bootstrap.sh to download the necessary SDKs. If newer SDKs are available than the one in bootstrap then download those and rename paths in Assets/Editor/googleplay_xcode.py
2-- Setup GooglePlayAppID and bundleId of your app/game in googleplay_xcode
3-- To test the sample App, in GCGui.cs file setup clientId from googleplay and rest of leaderboardip
4-- run and build app on ipad. click on Init GPG before doing anything else. Try silentSignin and if it fails it will display SignIn button. Use that to do proper auto with switching.

Trouble Shooting

*Signin doesn't work.*
The plugin expects google chrome in application:openUrl part. Change code there to allow any type of browser. 
Make sure bundled, gpgappid etc are all setup properly in both project and google dev console web guy.


Todo

Add support for multiplayer matchmaking
Add support for sharing on google+

