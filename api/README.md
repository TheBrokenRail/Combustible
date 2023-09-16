# API Library
This library contains Combustible's bindings for the Lemmy API.

It consists of three main components:
- [Auto-generated classes](https://thebrokenrail.github.io/Combustible/com/thebrokenrail/combustible/api/method/package-summary.html) from [`lemmy-js-client`](https://github.com/LemmyNet/lemmy-js-client).
- [`Connection`](https://thebrokenrail.github.io/Combustible/com/thebrokenrail/combustible/api/Connection.html): A class that uses [OkHttp](https://github.com/square/okhttp) and [Moshi](https://github.com/square/moshi) to simplify connecting to and interacting with Lemmy instances.
- [`Pictrs`](https://thebrokenrail.github.io/Combustible/com/thebrokenrail/combustible/api/Pictrs.html): Similar to `Connection`, but for Lemmy [Pictrs](https://crates.io/crates/pict-rs) instances.

## Usage
This library can be downloaded from [GitHub Packages](https://github.com/TheBrokenRail/Combustible/packages/1947148).

## Example
```java
Connection connection = new Connection();
GetSite method = new GetSite();
connection.send(method, getSiteResponse -> {
    // Success
}, () -> {
    // Error
});
```

## Threading
By default, all callbacks are executed on a new thread. This can be changed using the `Connection.setCallbackHelper` API.

For instance, if you want every callback to run on Android's UI thread, you could do:
```java
connection.setCallbackHelper(action -> {
    activity.runOnUiThread(action);
});
```