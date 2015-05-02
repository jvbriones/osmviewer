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

import team.osmviewer.services.GpsInterface;
import team.osmviewer.services.ServiceGPS;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;



public class OpenStreetMapViewerActivity extends Activity {
	
    private final int CACHE_SIZE = 50;
	public static String BROADCAST_ACTION = "team3.mapviewer.broadcast";
    public static final String PREFS_NAME = "MapViewerPrefs";
    
	@SuppressWarnings("unused")
	private GpsInterface gpsInterface;
    private Intent GpsIntent;
    private MapView view;
    private boolean isBound = false; //Will be used for checking the status of the Service
    
    private Button zoomButtonIn;
	private Button zoomButtonOut;
	private Button returnOnFollowMe;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0); //Save the variable "isBound" to recover it the next time the  activity is run. 
        isBound = sharedPreferences.getBoolean("serviceConnected", false);
        if(!isBound){
        	System.out.println("start service");
            GpsIntent = new Intent(this, ServiceGPS.class);
        	startService(GpsIntent);
    		doBindService();
        }
        
        Display display = getWindowManager().getDefaultDisplay();    	
    	RelativeLayout relativeLayout = new RelativeLayout(this);   
        view = new MapView(this, display.getWidth(), display.getHeight(), CACHE_SIZE);        
        
        
        returnOnFollowMe = new Button(this);
        returnOnFollowMe.setText("FollowMe");
        returnOnFollowMe.setOnClickListener(FollowMeButtonListener);
        returnOnFollowMe.setId(2);
		        
        RelativeLayout.LayoutParams buttonOnFollowmeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonOnFollowmeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		buttonOnFollowmeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
        zoomButtonIn = new Button(this);	
        zoomButtonOut = new Button(this);
		zoomButtonIn.setOnClickListener(zoomButtonInListener);
		zoomButtonOut.setOnClickListener(zoomButtonOutListener);
		zoomButtonIn.setText("+");
		zoomButtonOut.setText("-");
		zoomButtonIn.setWidth(70);
		zoomButtonOut.setWidth(70);
		zoomButtonOut.setId(1);
		zoomButtonIn.setId(0);
        
		RelativeLayout.LayoutParams buttonOutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams buttonInParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonOutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		buttonInParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		buttonOutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		buttonInParams.addRule(RelativeLayout.LEFT_OF, zoomButtonOut.getId());
		
		RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);        
        relativeLayout.addView(view, viewParams);        
        relativeLayout.addView(zoomButtonOut, buttonOutParams);
        relativeLayout.addView(zoomButtonIn, buttonInParams);
        relativeLayout.addView(returnOnFollowMe,buttonOnFollowmeParams);
		
        zoomButtonOut.bringToFront();
        zoomButtonIn.bringToFront();
        returnOnFollowMe.bringToFront();
        returnOnFollowMe.setVisibility(Button.INVISIBLE);
        Button exitButton = new Button(this);
        exitButton.setText("Exit");
        exitButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        {
        finish();
        }
        });
        RelativeLayout.LayoutParams exitButtonParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        exitButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        relativeLayout.addView(exitButton,exitButtonParams);

        setContentView(relativeLayout);
    }
    
    private OnClickListener FollowMeButtonListener = new OnClickListener() {
        public void onClick(View v) {        
        	view.setONonFollowMe();
            returnOnFollowMe.setVisibility(Button.INVISIBLE);
        }        	
    };
    
    public void setVisibleFollowMeButton(){
        returnOnFollowMe.setVisibility(Button.VISIBLE);

    }
    
    private OnClickListener zoomButtonInListener = new OnClickListener() {
        public void onClick(View v) {        
        	view.increaseZoom();
        }        	
    };
    
    private OnClickListener zoomButtonOutListener = new OnClickListener() {
        public void onClick(View v) {
        	view.decreaseZoom();
        }        	
    };
    
    public void onResume() {
    	super.onResume();
    	SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        isBound = sharedPreferences.getBoolean("serviceConnected", false); //checks if service is running        														  
        														  //"false" is the default return value        													  
        if(isBound){
        	doBindService();	
        }               
    }
    
    public void onPause() {
    	super.onPause();    	
    	// We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0); //We have to save the variable isBound to recover it 
        SharedPreferences.Editor editor = sharedPreferences.edit();	 
        editor.putBoolean("serviceConnected", isBound);
        // Commit the edits!
        editor.commit();        
        if(isBound){
        	doUnbindService();  //must unbind it before pausing activity
        }       
    }
    
    void doBindService() {  
    	// Establish a connection with the service.  We use an explicit
    	// class name because we want a specific service implementation that
    	// we know will be running in our own process (and thus won't be
    	// supporting component replacement by other applications).		
		bindService(GpsIntent, myConnection, Context.BIND_AUTO_CREATE);
		isBound = true;				
	}
   
    void doUnbindService() {
    	unbindService(myConnection);
    	isBound = false;
	}
    
    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
        	gpsInterface = GpsInterface.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            gpsInterface = null;
        }
    };   
    
    public void onDestroy() {
    	super.onDestroy();
    	view.close();
    	doUnbindService();
		stopService(GpsIntent);
    }
}