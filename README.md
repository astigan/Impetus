Get Lost
=======
![Get Lost](http://i.imgur.com/IdCywSh.png)

This is the source code for the **Get Lost** app, which picks a random point on a map and tracks the user's progress in getting there.
If you want to create your own fork, you will need to get your own API keys for Google Maps and Crashlytics, but otherwise everything should work out of the box.

Why Github?
=======
This project started out as a joke in which I attempted to make the most useless app possible.
There's something delightfully absurd about a navigation app that deliberately gets its users lost.

However, I've had great fun actually making this app and have learnt a lot about Android along the way.
So if you're making a map application and find this code useful, feel free to use it.
If you think I've done something incredibly stupid in the code that makes no sense, feel free to let me know by raising an issue.

How it works
=======
The app consists of one activity which basically acts like a controller.
The activity communicates with a Service that handles updates to the user's location and the device's GPS status.
The service also keeps track of various information about a journey - such as when it was started, where the destination is, and what location updates have happened along the way.

There are 3 fragments in the app. Two of these are dedicated to starting/stopping journeys, and notify the activity when a button is pressed for example.
The third fragment is a subclassed MapFragment with convenient functions for updating map markers, zooming to the user's current location, etc.

The destination itself is calculated when the activity is notified by the creation fragment.
The algorithm takes the approximate distance requested by the user, and adds or removes anywhere up to 30% of that value.
A random angle from 0-360 is then chosen, which determines the direction of travel.
The distance a user must travel can then be calculated separately for the X & Y axis.
All this information is then used to construct a LatLng coordinate, which is displayed to the user on the map.
