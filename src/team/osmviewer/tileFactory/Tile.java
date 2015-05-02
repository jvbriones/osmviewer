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

import java.net.MalformedURLException;
import java.net.URL;



public class Tile {
	private int x;
	private int y;
	private int zoom;
	
	public Tile(int x, int y, int zoom) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}
	
	public static boolean equalsTile(Tile one ,Tile otherTile){
		if(one.getX() == otherTile.getX() && one.getY() == otherTile.getY() && one.getZoom() == otherTile.getZoom())
			return true;
		else
			return false;
	}
	
	public static URL getURL(Tile thisTile){
		URL url = null;
		try {
			url = new URL("http://tile.openstreetmap.org/"+thisTile.getZoom()+"/"+thisTile.getX()+"/"+thisTile.getY()+".png");
		} catch (MalformedURLException e) {
			//e.printStackTrace();
		}
		return url;
	}
	
	public static String getCacheKey(Tile tile){
		return new String (tile.getX()+"#"+tile.getY()+"#"+tile.getZoom());
	}
	
}
