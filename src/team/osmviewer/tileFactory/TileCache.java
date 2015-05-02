/********************************************************************************
 *
 * Open Street Map Viewer
 *
 ********************************************************************************
 *		R E V I S I O N   H I S T O R Y
 ********************************************************************************
 *
 * Date        	Author  	  Description
 * ---------    ---------  	  ---------------------------------------------------
 * JUN 12		team		  Initial Version v0.1
 *
 *
 *******************************************************************************/
package team.osmviewer.tileFactory;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//http://docs.oracle.com/javase/1.4.2/docs/api/java/util/LinkedHashMap.html --> resource for cache implementation.

public class TileCache {
	
	private Map<String, Bitmap> cacheLRU;
	private int MAX_ENTRIES;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TileCache(int cacheSize) {
		MAX_ENTRIES = cacheSize;
		//Constructs a new LinkedHashMap instance with the specified capacity, 
		//load factor(default value is .75F) and a flag specifying the ordering behavior (true->based on the last access)
		cacheLRU = new LinkedHashMap(MAX_ENTRIES+1, .75F, true) {			
			private static final long serialVersionUID = 1L;
			
			// This method is called just after a new entry has been added (i.e after a put() call)
			// and delete the least reference element in the map if is needed.
			public boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_ENTRIES;
			}
		};
		//if we use the cache with different threads
		//cacheLRU = (Map)Collections.synchronizedMap(cacheLRU);
	}
	
	
	public Bitmap getTile(Tile tile) {		
		Bitmap bitmap = cacheLRU.get(Tile.getCacheKey(tile));
		if (bitmap != null) 	
			return bitmap;
		return null;	
	}
	
	public boolean hasTile(Tile tile){
		if( cacheLRU.get(Tile.getCacheKey(tile)) != null)
			return true;
		return false;
	}
	
	public void put(Tile tile, InputStream is){ //receives the stream from http (this method is called by TileManager)
		cacheLRU.put(Tile.getCacheKey(tile), BitmapFactory.decodeStream(is));
	}	
}
