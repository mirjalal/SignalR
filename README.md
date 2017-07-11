# SignalR
Repository includes both server and client apps.

The projects in this repo demonstrates how to use SignalR library to create Android client app and to build C# server.

Minimum requirements
------------
<ul>
<li>Visual Studio 2015 or higher</li>
<li>Java SDK 8 (Java 1.8) or higher</li>
<li>Android Studio 3.0 (Canary 6) or higher</li>
<li>Gradle 3.0.0-alpha6 or higher</li>
</ul>

Usage
------------
To build C# project open `.sln` of the solution in your Visual Studio. In `WinFormsServer.cs` change change the value of <i>serverURI </i> variable as you want (btw, replace 27 port number with your own number in `CreateFirewallRule()` method in the same file). Select <b>WinFormsServer</b> application and from `Debug` menu in your IDE select `Start without debugging`. Hit `Start` button in running application to start the SignalR server.

To build [Android app](https://github.com/mirjalal/SignalR/tree/master/Client) open it in your Android Studio. Generate a key and keystore and [sign your app](https://developer.android.com/studio/publish/app-signing.html). Close look to [16th line](https://github.com/mirjalal/SignalR/blob/master/Client/app/build.gradle#L16) and <b>DO NOT CHANGE</b> it, otherwise application will not work and try to do not make unnecessary change.

Contributing
------------
The projects are [open source](https://github.com/mirjalal/SignalR/blob/master/LICENSE.md) and powered by me (Mirjalal Talishinski). Feel free and report bugs without hesitations. ;) :) 
