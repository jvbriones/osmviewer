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
package team.osmviewer.services;

import team.osmviewer.activities.MapView;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;



public class ServiceGPS extends Service{
	
	protected double latitude;
    protected double longitude;
    private LocationManager locationManager; //This class provides access to the system location services.
    private LocationListener subscriber; //Used for receiving notifications from the LocationManager when the location has changed.
	
    //Control variables of the frequency of notification or new locations
	//To obtain notifications as frequently as possible, set both parameters to 0.
	protected int interval = 0;  
	protected int minDistance = 0;	
	
	//Return the communication channel to the service, which clients can call on to the service.
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onCreate() { 
		super.onCreate();
		locationManager = (LocationManager)
        		getSystemService(Context.LOCATION_SERVICE);
        subscriber = new Subscriber();
        
        locationManager.requestLocationUpdates( //Registers the current activity to be notified periodically
    		//	LocationManager.GPS_PROVIDER,
        		LocationManager.NETWORK_PROVIDER, //will get the user geographic position using the network provider instead of the GPS device.
    			interval,
    			minDistance,
    			subscriber);
        
	}  
			//Provide the interface to the activity.
	private final GpsInterface.Stub mBinder = new GpsInterface.Stub() {
		};
	
	public void onDestroy() {
		locationManager.removeUpdates(subscriber); //Removes any current registration for location updates. 
		super.onDestroy();
	}
	
	public class Subscriber implements LocationListener {
	
						//Called when the location has changed.
		public void onLocationChanged(Location location) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
	    	Intent broadcastReceiverIntent = new Intent();
	        broadcastReceiverIntent.setAction(MapView.BROADCAST_ACTION);
			broadcastReceiverIntent.putExtra(MapView.LATITUDE, latitude);
			broadcastReceiverIntent.putExtra(MapView.LONGITUDE, longitude);
	        sendBroadcast(broadcastReceiverIntent);
			
		}
		
		//Called when the provider is disabled by the user.
		public void onProviderDisabled(String provider) {
		}
		//Called when the provider is enabled by the user.
		public void onProviderEnabled(String provider) {
		}
		//Called when the provider status changes.
		public void onStatusChanged(String provider, int status, Bundle extras) {			
		}
		
	}//end LocationListener	

} 
