# Frequently Asked Questions
**Disclaimer:** This document was written before release, and as such, none of these questions have actually been frequently asked.

## Why is this app called "Combustible"?
The name "Lemmy" makes me think of lemons. Lemons remind me of the famous ["combustible lemons" rant](https://www.youtube.com/watch?v=Dt6iTwVIiMM) from the game [Portal 2](https://en.wikipedia.org/wiki/Portal_2?useskin=vector). Thus, I named the app "Combustible."

This is also why the app's icon is a lemon slice and its primary color is yellow (the color of lemons!).

## Why is [Lemmy.world](https://lemmy.world) the recommended instance?
Quite simply, it's the largest Lemmy instance as of writing, and seems to have mostly sane moderation.

## Why are push notifications not supported?
As far as I am aware, push notifications can be implemented in one of three ways:

1. The app periodically checks Lemmy for any notifications.
   - This is terrible for battery life as it would require the device to wake up and contact Lemmy quite frequently for push notifications to be useful.
2. The Lemmy instance uses a notification service like [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging) or [UnifiedPush](https://unifiedpush.org/) to send notifications directly to the app.
   - This is completely out of my control as I do not develop Lemmy. There is an [open issue](https://github.com/LemmyNet/lemmy/issues/2631) for it though.
3. I operate a third-party server which periodically checks Lemmy for any notifications, before using a notification service to notify devices.
   - While this seems like a nice compromise between options #1 and #2, it is a *really* bad idea for multiple reasons.
     1. This would be complicated to implement. The server would have to keep track of every device and account connected and make sure to check notifications for every single one of them.
     2. While this might solve the battery life problem of option #1, something still has to do the work. And that something would be the server. Running a server that reliably checks every single user's notifications would not be cheap if a large enough number of people used this app.
     3. Last, but not least. This would be a security *nightmare*. For this to work, the server would need every single user's access token. If the server was ever hacked, every single user would also be hacked. Especially since Lemmy access tokens [cannot be revoked and have unlimited scope](https://github.com/LemmyNet/lemmy/issues/3499).

As you can see, there is no practical way for push notifications to be implemented at the moment. If there is something I missed, please contact me!