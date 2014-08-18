# Growth Replay SDK for Android

[Growth Replay](https://growthreplay.com/) is usability testing tool for mobile apps.

## Usage 

1. Install [Growthbeat Core SDK](https://github.com/SIROK/growthbeat-core-android).

1. Add growthreplay.jar and android-support-v4.jar into libs directory in your project.

1. Write initialization code.

	```java
	GrowthReplay.getInstance().initialize(getApplicationContext(), "APPLICATION_ID", "CREDENTIAL_ID");
	```

	You can get the APPLICATION_ID and CREDENTIAL_ID on web site of GrowthReplay. 

1. Start to capture screen with following code.

	```java
	GrowthReplay.getInstance().start();
	```

## Growthbeat Full SDK

You can use Growthbeat SDK instead of this SDK. Growthbeat is growth hack tool for mobile apps. You can use full functions include Growth Replay when you use the following SDK.

* [Growthbeat SDK for iOS](https://github.com/SIROK/growthbeat-ios/)
* [Growthbeat SDK for Android](https://github.com/SIROK/growthbeat-android/)

## License

Apache License, Version 2.0
