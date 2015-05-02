# osmviewer
www.openstreetmap.org (OSM) is a collaborative project that creates and provides free geographic data and mapping to anyone who wants it.
This app is just a viewer demo of these open maps for Android.

## License

This project is released under the Creative Commons Attribution-NonCommercial 3.0 Unported (CC BY-NC 3.0) License.
More info at http://creativecommons.org/licenses/by-nc/3.0/

## Instructions

Just generate the .apk and install it in your Android device.

How the app works?

- Takes the current geographic longitude and latitude of the device
- Takes the horizontal/vertical display size into account
- Calculates the required neighborhood map tiles to fill out the display
- Downloads the required tiles via http according to the OSM conventions 
- Composes map view, centering the map around the current position (for the “Follow Me” map view mode)
- Refreshes the map view if the position of mobile user changes or the map view has been scrolled

You can download (via http protocol according to the OSM conventions) the geographic area of the world as tiles: bitmap with quadratic size of 256 x 256 pixels.
For a given geographic coordinate (longitude, latitude) and a certain zoom level, the relevant map tile can be calculated mathematically
see: http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames for further information.

The Threadpool is for downloading the map tiles and to avoid blocking http calls.
Prefetch the most probable required tiles in the near future and cache the already downloaded tiles on local device.
The cache is a LRU strategy, see http://docs.oracle.com/javase/1.4.2/docs/api/java/util/LinkedHashMap.html for further information.

— App info —

Api level: 10 – All the tests were done on Android 2.3.3 Gingerbread.

Permissions:
- Location (GPS)
- Network communication (Internet)

## Contributors
Started by [Javier Briones](https://github.com/jvbriones), Saqib Hanif and Julia Amaya

