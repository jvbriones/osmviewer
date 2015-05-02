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
package team.osmviewer.activities;

import java.util.ArrayList;

import team.osmviewer.tileFactory.Tile;
import team.osmviewer.tileFactory.TileManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;



public class MapView extends View {
	
	public static String BROADCAST_ACTION = "team.osmviewer.broadcast";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";
	public static String ZOOM = "zoom";	

	private double latitude, longitude;
	private int zoom = 15; //Default zoom
	
	private MapViewBroadcastReceiver mBroadcastReceiver; 
	private Context context;
	
	protected final int TILE_SIZE = 256; //pixels
	protected TileManager tileManager;
	protected int maxXTiles, maxYTiles, firstX, firstY, screenHeight, screenWidth;
	protected Point pivotPoint;
	protected Point offsetPoint;
	protected ArrayList<Tile> futureTiles;
	protected ArrayList<Tile> currentTiles;
	
	protected boolean firstDraw = true;
	protected boolean hasGpsLocation = false;
	protected boolean onFollowMode;
	protected Tile realPivotTile;
	protected Tile scrollPivotTile;
	float startX,startY,deltaX,deltaY;
	int offTileX,offTileY,offX,offY,lastOffX,lastOffY;
	protected OpenStreetMapViewerActivity activity;
	//Hit and Misses Count and Ratio
	boolean calculateRatio=false;
	double cacheHits=0,cacheMisses=0;
	//For ZOOM IN OUT
	float oldDist=0;
	float newDist= 0;
	String mode=null;
	double scrollLat=0,scrollLon=0;
	boolean isZoom=false;
			

	public MapView (OpenStreetMapViewerActivity activity, int mscreenWidth, int mscreenHeight, int cacheSize) {
		super(activity);	
		this.activity = activity;
		context = activity;
		
		mBroadcastReceiver = new MapViewBroadcastReceiver();
		IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
		
    	context.registerReceiver(mBroadcastReceiver, filter);
    	
    	tileManager = new TileManager(this, cacheSize);
		
		futureTiles = new ArrayList<Tile>();
		currentTiles = new ArrayList<Tile>();
		
		offsetPoint = new Point(0,0);
		
		onFollowMode = true;
		//scrollPivotTile = new Tile(0,0,zoom);
		
		
	}
	
	public void changeZoom(){

		if(!onFollowMode){
		System.out.println("old lat, lon: "+latitude+","+longitude);	
		System.out.println("old tile x,y,z: "+scrollPivotTile.getX()+","+scrollPivotTile.getY()+","+scrollPivotTile.getZoom());	
		double newLat = tile2lat(scrollPivotTile.getY(), scrollPivotTile.getZoom());
		double newLon = tile2lon(scrollPivotTile.getX(), scrollPivotTile.getZoom());
		scrollPivotTile = calculateTile(newLat, newLon, zoom);
		//newLat += pivotPoint.y*deltaLatitudeInZoom(scrollPivotTile.getZoom())/TILE_SIZE;
		//newLon += pivotPoint.x*deltaLongitudeInZoom(scrollPivotTile.getZoom())/TILE_SIZE;

		pivotPoint = getPointInTile(newLat, newLon, zoom);
		System.out.println("new tile x,y,z: "+scrollPivotTile.getX()+","+scrollPivotTile.getY()+","+scrollPivotTile.getZoom());	
		System.out.println("new lat, lon: "+newLat+","+newLon);	
		}
	}
	public void setONonFollowMe(){
		onFollowMode = true;
		drawFromRealPivot();
	}
	
	public void increaseZoom(){		
		if(zoom<16)					
			zoom++;
		drawFromRealPivot();
		changeZoom();		
	}
	public void decreaseZoom(){
		if(zoom>7)					
			zoom--;		
		drawFromRealPivot();
		changeZoom();			
	}
	
	public void close(){
		context.unregisterReceiver(mBroadcastReceiver);
		tileManager.close();
	}	
	
	public static Tile calculateTile(final double lat, final double lon, final int zoom) {
		   int x = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) );
		   int y = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) );
		   return new Tile(x,y,zoom);
	}
	
	public void initializeViewAttributes(){
		screenWidth = getWidth();
		screenHeight = getHeight();
		System.out.println("width: "+screenWidth+ "height: "+screenHeight);
		maxXTiles = 1 + screenWidth / TILE_SIZE;
		if(screenWidth % TILE_SIZE > 1){ // case that the screen supports a fraction of a tile
						  //example: if the width corresponds to 4.5 tiles, the maximum tiles
					      //you could have across the width would be 6 (0.2+1+1+1+1+0.3)
			maxXTiles++;			
		}
		maxYTiles = 1 + screenHeight / TILE_SIZE;
		
		if(screenHeight % TILE_SIZE > 1){
			maxYTiles++;
		}	
		
		if(screenWidth%TILE_SIZE == 0 && maxXTiles%2 == 0)
			offsetPoint.x += TILE_SIZE/2;
		if(screenWidth%TILE_SIZE != 0 && maxXTiles%2 != 0)
			offsetPoint.x += TILE_SIZE/2 - (screenWidth%TILE_SIZE)/2;
		if(screenWidth%TILE_SIZE != 0 && maxXTiles%2 == 0)
			offsetPoint.x += TILE_SIZE/2 + (screenWidth%TILE_SIZE)/2;
		
		if(screenHeight%TILE_SIZE == 0 && maxYTiles%2 == 0)
			offsetPoint.y += TILE_SIZE/2;
		if(screenHeight%TILE_SIZE != 0 && maxYTiles%2 != 0)
			offsetPoint.y += TILE_SIZE/2 - (screenHeight%TILE_SIZE)/2;
		if(screenHeight%TILE_SIZE != 0 && maxYTiles%2 == 0)
			offsetPoint.y += TILE_SIZE - (screenHeight%TILE_SIZE)/2;
	}
	
	public void onDraw(final Canvas canvas){		
		
		if(hasGpsLocation){			
			if (onFollowMode){				
				firstX = realPivotTile.getX() - maxXTiles / 2 ;
				firstY = realPivotTile.getY() -  maxYTiles / 2;
				
				
				drawMap(canvas);				
			}		
		}
		
		if (!onFollowMode){

			firstX = scrollPivotTile.getX() - maxXTiles / 2 ;
			firstY = scrollPivotTile.getY() -  maxYTiles / 2;
			
			drawMap(canvas);			
		}
		
	}	
	
	public void drawMap(Canvas canvas) {		
			
		for(int x = firstX , i = 0 ; i < maxXTiles ; x++, i++ ) {
			for(int y = firstY, j = 0 ; j < maxYTiles; y++ , j++) {
				Tile tile = new Tile(x, y, zoom);	
				Bitmap bitmap = tileManager.getTile(tile);	
				currentTiles.add(tile);
				if(bitmap != null) {
					if(calculateRatio==true)
						cacheHits++;
					canvas.drawBitmap(bitmap, -pivotPoint.x - offsetPoint.x + i*TILE_SIZE, 
									-pivotPoint.y - offsetPoint.y + j*TILE_SIZE, new Paint()); 
					
					if(Tile.equalsTile(tile,realPivotTile)){
						Paint paint = new Paint();
						paint.setColor(Color.RED);
						//canvas.drawRect(-offsetPoint.x + i*TILE_SIZE-4, -offsetPoint.y + j*TILE_SIZE -4, 
							//	-offsetPoint.x + i*TILE_SIZE+4, -offsetPoint.y + j*TILE_SIZE +4, paint);
						
						Point userPositionPoint = getPointInTile(latitude, longitude, zoom);
						canvas.drawRect(-offsetPoint.x -pivotPoint.x + userPositionPoint.x + i*TILE_SIZE-4, -offsetPoint.y -pivotPoint.y + userPositionPoint.y + j*TILE_SIZE -4,								
									-offsetPoint.x -pivotPoint.x + userPositionPoint.x + i*TILE_SIZE+4, -offsetPoint.y  -pivotPoint.y + userPositionPoint.y  + j*TILE_SIZE +4, paint);
					}
						
				}else {	
					if(calculateRatio==true)						
						cacheMisses++;
					//threadPoolDraw.execute(new DrawBitmap(canvas, tile, pivotPoint, i, j));
					//System.out.println("bitmap NULL, x="+x+" y="+y+" zoom="+zoom);
					System.out.println("bitmapnull");
				}
			}
		}
		if(calculateRatio==true)
		{
			calculateRatio=false;
			System.out.println("Cache Hit Ratio:"+cacheHits/(cacheHits+cacheMisses));
			
		}
			
		
		
	}
	/*public void calculateFutureTiles() {
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
	}
	
	public void addToFutureTiles(int x, int y, int zoom){
		Tile newTile = new Tile(x,y,zoom);
		if(!futureTiles.contains(newTile))
			futureTiles.add(newTile);
	}*/

	public static Point getPointInTile(final double lat, final double lon, final int zoom) {
		
		Tile tile = calculateTile(lat, lon, zoom);
		double north, south, west, east;
		north = tile2lat(tile.getY(), zoom);
	    south = tile2lat(tile.getY() + 1, zoom);
	    west = tile2lon(tile.getX(), zoom);
	    east = tile2lon(tile.getX() + 1, zoom);
	    // pixelX  ---  256
	    // lat     ---  south-north
	    
	    // 256  ---   east-west
	    // pixelY --- lon-east
	    
	    
	    //256 --- north-south
	    //pixelY --- north-lat
	    //pixelX = (256*(lon-west))/(east-west);
	    //int pixelX = (int) (256*lat/Math.abs(north-south));
	    
	    int pixelX = (int) ((256 * (lon - west)) / (east - west));
	    int pixelY = (int) ((256 * (north - lat)) / (north - south));
	    
	    return new Point(pixelX,pixelY);
	}



	public static double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}
	 
	public static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}	
	
	public class MapViewBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			calculateRatio=true;
			if(!hasGpsLocation) {
				hasGpsLocation = true;
				initializeViewAttributes();
			}
			latitude = intent.getDoubleExtra(LATITUDE, 0);
			longitude = intent.getDoubleExtra(LONGITUDE, 0);
			//Toast toast = Toast.makeText(context, 
				//	"onReceive reached.", Toast.LENGTH_SHORT);
			//toast.show();
			

			drawFromRealPivot();
			//tileManager.updateCache(currentTiles);
		}
    	
    }	
	
	public boolean getOnFollowMode(){
		return onFollowMode;
	}
	
	public void drawFromRealPivot(){
		
		
		//System.out.println("lat: "+latitude+" lon:"+longitude+" zoom:" + zoom);
		realPivotTile = calculateTile(latitude, longitude, zoom);		
		//System.out.println("pivot tile, x="+realPivotTile.getX()+" y="+realPivotTile.getY()+" zoom="+realPivotTile.getZoom());
		System.out.println("hasGpsLocation");
		
		if(onFollowMode)
		{
		
			pivotPoint = getPointInTile(latitude, longitude, zoom);
			//scrollPivotTile.setX(realPivotTile.getX());
			//scrollPivotTile.setY(realPivotTile.getY());
			scrollPivotTile = new Tile(realPivotTile.getX(),realPivotTile.getY(),zoom);
			lastOffX=pivotPoint.x;
			lastOffY=pivotPoint.y;		
		
		}
		else if(isZoom==true)
		{
			pivotPoint = getPointInTile(latitude, longitude, zoom);
			//scrollPivotTile.setX(realPivotTile.getX());
			//scrollPivotTile.setY(realPivotTile.getY());
			//scrollPivotTile = new Tile(realPivotTile.getX(),realPivotTile.getY(),zoom);
			scrollPivotTile = new Tile(realPivotTile.getX(),realPivotTile.getY(),zoom);
			lastOffX=pivotPoint.x;
			lastOffY=pivotPoint.y;	
		
			isZoom=false;
			
		}
		
		invalidate();
		
	}
	
	public void restoreBundle(Bundle bundle){
		
		if(bundle!=null){
			onFollowMode = bundle.getBoolean("FollowMode",true);
			System.out.println("realPTileX from bundle" + bundle.getInt("realpivottilex"));
			
			if(!onFollowMode){
				pivotPoint = new Point(bundle.getInt("pivotPoint.x"),bundle.getInt("pivotPoint.y"));
				scrollPivotTile = new Tile(bundle.getInt("scrollPivotTile.x"),bundle.getInt("scrollPivotTile.y"),bundle.getInt("scrollPivotTile.zoom"));
				//scrollPivotTile.setX(bundle.getInt("scrollPivotTile.x"));
				//scrollPivotTile.setY(bundle.getInt("scrollPivotTile.y"));
				//scrollPivotTile.setZoom(bundle.getInt("scrollPivotTile.zoom"));
				realPivotTile = new Tile(0,0,0);
				realPivotTile.setX(bundle.getInt("realPivotTile.x"));
				realPivotTile.setY(bundle.getInt("realPivotTile.y"));
				realPivotTile.setZoom(bundle.getInt("realPivotTile.zoom"));
				initializeViewAttributes();
				//lastOffX=pivotPoint.x-offsetPoint.x;
				//lastOffY=pivotPoint.y-offsetPoint.y;	
				lastOffX = bundle.getInt("lastOffsetX")-offsetPoint.x;
				lastOffX = bundle.getInt("lastOffsetY")-offsetPoint.y;
				activity.setVisibleFollowMeButton();
				
				invalidate();
			} 
		}
	}
	
	
	public Bundle saveBundle(Bundle bundle){
		if(!onFollowMode){
		bundle.putInt("pivotPoint.x", pivotPoint.x);
		bundle.putInt("pivotPoint.y", pivotPoint.y);
		bundle.putInt("scrollPivotTile.x", scrollPivotTile.getX());
		bundle.putInt("scrollPivotTile.y", scrollPivotTile.getY());
		bundle.putInt("scrollPivotTile.zoom", scrollPivotTile.getZoom());
		bundle.putBoolean("FollowMode", onFollowMode);
		bundle.putInt("realpivottilex", realPivotTile.getX());
		bundle.putInt("realPivotTile.y", realPivotTile.getY());
		bundle.putInt("realPivotTile.zoom", realPivotTile.getZoom());
		bundle.putInt("lastOffsetX", lastOffX);
		bundle.putInt("lastOffsetY", lastOffY);
		System.out.println("realPTileX" + realPivotTile.getX());
		} 
		
		return bundle;
	}
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
		}
	public boolean onTouchEvent(final MotionEvent event)
	{	
		if(event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN || event.getAction()==MotionEvent.ACTION_POINTER_DOWN)
		 {
			oldDist=0;
			oldDist = spacing(event); 			
			if (oldDist > 10f) 
			{
				mode=ZOOM;
			}
			else
			{
				mode=null;
			}
		 }
		else if(event.getAction() == MotionEvent.ACTION_DOWN){ //Start of touch screen (i.e. finger hammer on)			
			if(mode==null)
			{
				startX = event.getX();
				startY = event.getY();			
				onFollowMode = false;			
				activity.setVisibleFollowMeButton();
				return true;
			}
			
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			//End of touch screen (i.e. finger pull off)
			if(mode==null)
			{
			lastOffX=pivotPoint.x;
			lastOffY=pivotPoint.y;
			}
			return true;
			
		} else if(event.getAction() == MotionEvent.ACTION_MOVE)
		{
			if(mode==null)
			{
			deltaX = (event.getX() - startX); 
			deltaY = (event.getY() - startY); 		
												//				//					//
												// Control the 8 possible direction	//
												//				//					//			
			
			if ((deltaX < 0) && (deltaY == 0)) {	// X axis scroll, Y the same.		
				
				offX = (lastOffX -(int)deltaX);

				if ((offX+ offsetPoint.x > TILE_SIZE)){
					int pivX = scrollPivotTile.getX()+1; 
					scrollPivotTile.setX(pivX);
					
					offX = offX - TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);				
						
				
			}else if ((deltaX > 0) && (deltaY == 0)) {			
				
				offX = (lastOffX -(int)deltaX);
				
				if ((offX + offsetPoint.x < 0)){
					int pivX = scrollPivotTile.getX()-1;
					scrollPivotTile.setX(pivX);
					
					offX = offX + TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);			
						
				
			}
			else if ((deltaX == 0) && (deltaY > 0)) {		// Y axis scroll, X the same.		
						
				offY = (lastOffY -(int)deltaY);
				
				if ((offY + offsetPoint.y < 0)){
					int pivY = scrollPivotTile.getY()-1;
					scrollPivotTile.setY(pivY);
					
					offY = offY + TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);	
				
			}else if ((deltaX == 0) && (deltaY < 0)) {	

				offY = (lastOffY -(int)deltaY);

				if ((offY+ offsetPoint.y > TILE_SIZE)){
					int pivY = scrollPivotTile.getY()+1; 
					scrollPivotTile.setY(pivY);
					
					offY = offY - TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);					

			}else if ((deltaX > 0) && (deltaY > 0)) { 	// The 4 corner.. 
				
				offX = (lastOffX -(int)deltaX);
				
				if ((offX + offsetPoint.x < 0)){
					int pivX = scrollPivotTile.getX()-1;
					scrollPivotTile.setX(pivX);
					
					offX = offX + TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);	
				
				offY = (lastOffY -(int)deltaY);
				
				if ((offY + offsetPoint.y < 0)){
					int pivY = scrollPivotTile.getY()-1;
					scrollPivotTile.setY(pivY);
					
					offY = offY + TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);	
				
			}else if ((deltaX > 0) && (deltaY < 0)) {
				
				offX = (lastOffX -(int)deltaX);
				
				if ((offX + offsetPoint.x < 0)){
					int pivX = scrollPivotTile.getX()-1;
					scrollPivotTile.setX(pivX);
					
					offX = offX + TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);
				
				offY = (lastOffY -(int)deltaY);

				if ((offY+ offsetPoint.y > TILE_SIZE)){
					int pivY = scrollPivotTile.getY()+1; 
					scrollPivotTile.setY(pivY);
					
					offY = offY - TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);					
				
			}else if ((deltaX < 0) && (deltaY > 0)) { 
				
				offX = (lastOffX -(int)deltaX);

				if ((offX+ offsetPoint.x > TILE_SIZE)){
					int pivX = scrollPivotTile.getX()+1; 
					scrollPivotTile.setX(pivX);
					
					offX = offX - TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);
				
				offY = (lastOffY -(int)deltaY);
				
				if ((offY + offsetPoint.y < 0)){
					int pivY = scrollPivotTile.getY()-1;
					scrollPivotTile.setY(pivY);
					
					offY = offY + TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);	
				
			}else if ((deltaX < 0) && (deltaY < 0)) { 
				
				offX = (lastOffX -(int)deltaX);

				if ((offX+ offsetPoint.x > TILE_SIZE)){
					int pivX = scrollPivotTile.getX()+1; 
					scrollPivotTile.setX(pivX);
					
					offX = offX - TILE_SIZE;						
					pivotPoint.set(offX, pivotPoint.y);				
					lastOffX=offX;
					startX= startX +deltaX;
				}else		
					pivotPoint.set(offX, pivotPoint.y);
				
				offY = (lastOffY -(int)deltaY);

				if ((offY+ offsetPoint.y > TILE_SIZE)){
					int pivY = scrollPivotTile.getY()+1; 
					scrollPivotTile.setY(pivY);
					
					offY = offY - TILE_SIZE;						
					pivotPoint.set(pivotPoint.x, offY);				
					lastOffY=offY;
					startY= startY +deltaY;
				}else		
					pivotPoint.set(pivotPoint.x, offY);	
				
			} 
			calculateRatio=true;
			invalidate();			
			return true;
		}
		}
		else
			//End of IF
		{
			 if(mode == ZOOM)
			 {
				 System.out.println("Zoom Leve"+zoom);
				mode=null;
				newDist=0;
				newDist= spacing(event);
				
				
				if (newDist > 10f) 
				{
					if(oldDist > newDist)
					{						
						if(zoom>1)
						{
						zoom--;				
						drawFromRealPivot();
						changeZoom();	
						return true;			
						}
						else
						{
						zoom=1;
						drawFromRealPivot();
						changeZoom();
						return true;
						}
					}
					else if(oldDist < newDist)
					{
						if(zoom<18)
						{							
						zoom++;	
						drawFromRealPivot();
						changeZoom();														
						return true;
						}
						else
						{
						zoom=17;							
						drawFromRealPivot();
						changeZoom();
						return false;
						}												
					}
					if(zoom<1)	
					{
					  zoom=1;
					  drawFromRealPivot();
						changeZoom();
					}
					else if(zoom>19)
					{
					zoom=17;
					drawFromRealPivot();
					changeZoom();
					}					
				
				}		
			
			}	

 				
	return true;
			
		}
		return false;
	}// End onTouchEvent
	
	
}
