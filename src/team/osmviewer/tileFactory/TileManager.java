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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import team.osmviewer.activities.MapView;
import team.osmviewer.util.ThreadPool;
import android.graphics.Bitmap;



public class TileManager {
	
	protected final int THREAD_POOL_SIZE = 2; //http://wiki.openstreetmap.org/wiki/Tile_usage_policy  --> 2 thread max allowed.
	protected TileCache tileCache;
	protected ThreadPool threadPoolHttp;
	protected MapView mapView;
	protected ArrayList<Tile> futureTiles = new ArrayList<Tile>(); //files that should be prefetched
	protected ArrayList<Tile> currentTiles = new ArrayList<Tile>(); //files that are currently being shown on the screen
	protected ArrayList<Tile> tilesDownloading; //tiles that are currently being downloaded by the threads
	
	public TileManager(MapView mapView, int cacheSize) {
		this.mapView  = mapView;
		tileCache = new TileCache(cacheSize);
		threadPoolHttp = new ThreadPool(THREAD_POOL_SIZE);
		tilesDownloading = new ArrayList<Tile>();
	}
	
	public void close() {
		threadPoolHttp.close();
	}	
	
	public Bitmap getTile(Tile tile) {
		Bitmap bitmap = tileCache.getTile(tile);

		if (bitmap != null){ //cache has the tile
			if(isInArray(tilesDownloading, tile)){
				removeTile(tilesDownloading, tile); //tile has finished downloading
			}
			prefetch();
			return bitmap;	
			}
			if(!isInArray(tilesDownloading, tile) ){	
				if(tile.getX()<Math.pow(2,tile.getZoom()) && tile.getY()<Math.pow(2,tile.getZoom())){ //checks if tile exists (in the server)
					addToCurrentTiles(tile);
					threadPoolHttp.execute(new Petition(tile,true));	
					tilesDownloading.add(tile);
			}
		}	

		return null;
		}	
		
	public void removeTile(ArrayList<Tile> tileArray, Tile tile){
		for(Tile til : tileArray){
			if(Tile.equalsTile(til,tile)){
				tileArray.remove(til);
				break;
			}
		}
	}
	
	public boolean isInArray(ArrayList<Tile> tileArray, Tile tile){
		for(Tile til : tileArray){
			if(Tile.equalsTile(til,tile))
				return true;
		}
		return false;
	}
	
	public class Petition implements Runnable {
		private int retryDownload=0; 
		Tile tile;
		boolean updateView;
		public Petition(Tile tile, boolean updateView) {
			this.tile = tile;
			this.updateView = updateView;
		}
		
		public void run() {
			URL url = Tile.getURL(tile);
			HttpURLConnection conn = null;
			InputStream is = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				is = conn.getInputStream();	
				
				tileCache.put(tile, is); //send the stream to the cache
				if(updateView)
					mapView.postInvalidate(); //redraw view
				
			} catch (IOException e) {
				//e.printStackTrace();
				retryDownload++;
				if(retryDownload<4) //prevent stack overflow in case of server problems, retry only up to 4 times
					this.run(); //restart the thread (attempt to download tile again)
			}
			
		}		
	}
	
	public void addToFutureTiles(int x, int y, int zoom){
		Tile newTile = new Tile(x,y,zoom);
		if(!isInArray(futureTiles,newTile))
			futureTiles.add(newTile);
	}
	
	
	public void addToCurrentTiles(Tile tile){
		if(!isInArray(currentTiles,tile))
			currentTiles.add(tile);
	}
	
	public void prefetch(){
		if(tilesDownloading.size()==0 && currentTiles.size()>0){

			futureTiles = new ArrayList<Tile>();
			for(Tile tile : currentTiles){
				int x = tile.getX();
				int y = tile.getY();
				int zoom = tile.getZoom();
				
				//add neighbor tile in every direction (8 neighbor tiles)
				addToFutureTiles(x+1,y,zoom);
				addToFutureTiles(x,y+1,zoom);
				addToFutureTiles(x+1,y+1,zoom);
				addToFutureTiles(x-1,y,zoom);
				addToFutureTiles(x,y-1,zoom);
				addToFutureTiles(x-1,y-1,zoom);
				addToFutureTiles(x+1,y-1,zoom);
				addToFutureTiles(x-1,y+1,zoom);
			}
			
			for(Tile tile : futureTiles){ 
				if(!tileCache.hasTile(tile)){ //if cache doesn't have this tile yet
					threadPoolHttp.execute(new Petition(tile,false));	
				}
			}
			futureTiles = new ArrayList<Tile>(); //clear the arrays
			currentTiles = new ArrayList<Tile>();
		}
	}
		 
}
