#!/bin/sh

mkdir GooglePlaySDKs
cd GooglePlaySDKs
echo "Downloading Google Plus iOS SDK"
wget --no-check-certificate "https://developers.google.com/+/mobile/ios/sdk/google-plus-ios-sdk-1.3.0.zip"
unzip "google-plus-ios-sdk-1.3.0.zip"
echo "Downloading Google Game Services SDK"
wget --no-check-certificate "https://developers.google.com/games/services/downloads/PlayGameServices.v1.0.zip"
unzip "PlayGameServices.v1.0.zip"
echo "Finished"
