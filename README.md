#Proof Of Concept: AeroGear Unified Push Server... 
**NOTE:** THIS IS NOT YET READY, it's just evaluating some ideas

### Some guidance ...

#### Registration

Register a ```PushApplication```
```
curl -v -H "Accept: application/json"  \
  -H "Content-type: application/json"  \ 
  -X POST  \ 
  -d '{"description":"just a test", "name":"TestApp"}'\
  http://localhost:8080/applications
```
__The response returns an PUSH-APP-ID....__

Add an ```iOS``` Mobile Application:
```
curl -v -H "Accept: application/json"  \
  -H "Content-type: application/json"  \
  -X POST  \
  -d '{"certificate":"/Users/matzew/Desktop/PUSHER.p12", "passphrase":"foo"}'  \
  http://localhost:8080/applications/{PushAppID}/iOS
```
__The response returns an MOBILE-APP-ID....__

**Note**: Use a path - upload not yet supported..................

Add an ```Android``` Mobile Application:
```
curl -v -H "Accept: application/json"  \
  -H "Content-type: application/json"  \
  -X POST  \
  -d '{"google-api-key" : "MY GOOGLE ID"}'  \
  http://localhost:8080/applications/{PushAppID}/android
```
__The response returns an MOBILE-APP-ID....__

#### Registration of an installation, on a device (iOS)

Client-side example for how to register an installation:

```ObjectiveC
- (void)application:(UIApplication*)application
  didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSString *tokenStr = [deviceToken description];
    NSString *pushToken = [[[tokenStr
      stringByReplacingOccurrencesOfString:@"<" withString:@""]
      stringByReplacingOccurrencesOfString:@">" withString:@""]
      stringByReplacingOccurrencesOfString:@" " withString:@""];

  // TODO: use https
    AFHTTPClient *client =
	  [[AFHTTPClient alloc] initWithBaseURL:[NSURL URLWithString:@"http://192.168.0.114:8080/"]];
    client.parameterEncoding = AFJSONParameterEncoding;

    // set the AG headers....
    [client setDefaultHeader:@"AG-PUSH-APP" 
	   value:@"SOME ID..."];
    [client setDefaultHeader:@"AG-Mobile-APP"
	   value:@"SOME OTHER ID..."];



    [client postPath:@"/registry/device"
	  parameters:@{@"token": pushToken, @"os": @"iOS"}
	  success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"\nSUCCESS....\n");
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"%@", error);
    }];
}
```
__No real client SDK, YET!!!!__


#### Sender

Send broadcast push message to ALL mobile apps of a certain Push APP......:

```
curl -v -H "Accept: application/json"  \
  -H "Content-type: application/json"  \
  -X POST  \
  -d '{"key":"blah", "alert":"Test...."}'  \
  http://localhost:8080/sender/broadcast/{PushAppID}
```


## More details

Concepts and ideas are also being developed...:

See:
https://gist.github.com/matzew/69d33a18d4fac9fdedd4

REST APIs

* Registry: https://gist.github.com/matzew/2da6fc349a4aaf629bce
* Sender: https://gist.github.com/matzew/b21c1404cc093825f0fb
