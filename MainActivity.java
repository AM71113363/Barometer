package sss.am71113363.barometer;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.hardware.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import android.graphics.*;
import android.content.res.ColorStateList;
import android.location.*;
import android.content.pm.PackageManager;
import android.Manifest;
//import android.widget.Toast.*;

public class MainActivity extends Activity implements SensorEventListener
{
	private Context ctx;
	private SensorManager sMsg;
	private Sensor sensor;
	private Sensor prox;
    private WEATHER weather;
	private final static int CODE_MAINMENU = 1000;
	private final static int CODE_OPEN = 4000;
	private final static int CODE_CLOSE = 8000; //called to close any pending stuff
    private boolean has3DFix = false; // if true, update & save the new Altitude
	
	
    private SharedPreferences prefs=null;

	private boolean isDialogActive = false;
	private boolean isGpsActive=false;

	private TextView hTitle=null;
	private TextView hLat=null;
	private TextView hLong=null;
	private TextView hAlt=null;
	private TextView hNrSat=null;
	private ProgressBar hNrSatBar=null;
	private Button load=null;
	private AlertDialog dialog=null;
	//private float gpsAltitude=0.0f; //default
	private float gpsAccuracy=0;
	private double GpsAltitude = -1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.main);
		weather = findViewById(R.id.weather);
		
		sMsg = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		prox = sMsg.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		sensor=sMsg.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if(sensor == null)
		{
			weather.update(0,"Error");
		}
		else
		{
		   prefs = getSharedPreferences("Barometer",MODE_PRIVATE);
		   float temp= prefs.getFloat("ALTITUDE",0.0f);
		   weather.update(temp,String.format("%.0fm",temp));
		}
		
    }

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if(event.sensor.getType() == Sensor.TYPE_PRESSURE)
		{
			weather.update(event.values[0]);
		}
		else if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
		{
			if(event.values[0] < prox.getMaximumRange())
			{
				finish();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor p1, int p2)
	{ }

	@Override
	protected void onResume()
	{
		super.onResume();
		if(sensor!=null)
		{
			sMsg.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(prox!=null)
		{
			sMsg.registerListener(this,prox,SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if(isDialogActive == false)
		  finish();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onDestroy()
	{
		if(sensor != null)// || prox != null)
		{
			sMsg.unregisterListener(this);
		}
	    if(isDialogActive)
		hnd.sendEmptyMessage(CODE_CLOSE);
		super.onDestroy();
	}
    public void done(View v)
	{
		//finish();
		hnd.sendEmptyMessage(CODE_MAINMENU);
	}
    
	private TextView makeField(String text,int gravity)
	{
		TextView temp = new TextView(this);
		temp.setText(text);
	    temp.setTextColor(Color.BLACK);
		temp.setTextSize(16);
        temp.setGravity(gravity);
		temp.setBackgroundColor(Color.WHITE);
		return temp;
	}
	private void status(String title,String ms,boolean _isError)
	{

		final AlertDialog.Builder asp=new AlertDialog.Builder(this);
		LinearLayout lnout=new LinearLayout(this);
		lnout.setOrientation(LinearLayout.VERTICAL);
		lnout.setBackgroundColor(Color.WHITE);
		lnout.setPadding(16,16,16,16);
		
		hLat = makeField("Latitude:",Gravity.LEFT);
		lnout.addView(hLat);
		
		hLong = makeField("Longitude:",Gravity.LEFT);
		lnout.addView(hLong);
		
		hAlt = makeField("Altitude:",Gravity.LEFT);
		lnout.addView(hAlt);
		
		hNrSat = makeField(ms,Gravity.CENTER);
		hNrSat.setTextColor(Color.BLUE);
		lnout.addView(hNrSat);
		
		
		hNrSatBar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
		hNrSatBar.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
		lnout.addView(hNrSatBar);
		asp.setView(lnout);
		hNrSatBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
		hNrSatBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.RED));
      
		hTitle = new TextView(this);
		hTitle.setText(title);
		hTitle.setGravity(Gravity.LEFT);
		hTitle.setPadding(18,8,18,8);
		hTitle.setTextSize(18);
		hTitle.setTextColor(Color.BLACK);
        hTitle.setBackgroundColor(Color.WHITE);
		asp.setCustomTitle(hTitle);
		load=new Button(this);
		LinearLayout.LayoutParams loadPrm=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		loadPrm.topMargin=20;
		load.setText("Use GPS");
		load.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{
					hnd.sendEmptyMessage(CODE_OPEN);
				}
			});
		lnout.addView(load,loadPrm);
		dialog=asp.create();

		dialog.setCancelable(false);
		
		dialog.setButton("Back",new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dg,int jf)
				{
					dialog.dismiss();
	                dialog = null;
					hnd.sendEmptyMessage(CODE_CLOSE);
				}
			});
		dialog.setButton2("Exit",new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dg,int jf)
				{
					dialog.dismiss();
					dialog = null;
					hnd.sendEmptyMessage(CODE_CLOSE);
					finish();
				}
			});
		
	    isDialogActive=true;
		isGpsActive = false;
		gpsAccuracy=0;
		has3DFix = false; //sat 3Dfix not acqired yet
		GpsAltitude = -1; //not set yet
		dialog.show();
	}
	
	private LocationManager loc=null;
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(requestCode == 1)
		{
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
                hnd.sendEmptyMessage(CODE_OPEN);
			}
		}
	}
	
	private boolean checkLocationPermission()
	{
		if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
		{
			requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},1);
			return true;
		}
		return false;
	}
	private Handler hnd=new Handler()
	{
		@Override
		public void handleMessage(Message ms)
		{

			switch(ms.what)
			{
				case CODE_MAINMENU:
				{
                       status("Use GPS to find the Altitude?","Number of Satellites ( 0 )",false);
				}break;
				case CODE_OPEN:
				{
					if(has3DFix)
					{
						hnd.sendEmptyMessage(CODE_CLOSE);
						break;
					}
					if(isGpsActive){break;} //sanity check
					try
					{
					   if(checkLocationPermission()==true)
						   break;
					   
						loc=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
						if( loc == null || //!loc.isLocationEnabled() || 
						    !loc.isProviderEnabled(LocationManager.GPS_PROVIDER))
						{
							android.widget.Toast.makeText(ctx,"Enable GPS",android.widget.Toast.LENGTH_SHORT).show();
							break;
						}
						loc.registerGnssStatusCallback(gnssCallback);
						
						loc.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,gpsLoc);
						
						load.setText("Searching...");
						load.setEnabled(false);
						isGpsActive = true;
						
					}catch(Throwable e){}
				}break;

				case CODE_CLOSE:
				{
					//remove location callbacks
					if(isGpsActive == true)
					{
						loc.unregisterGnssStatusCallback(gnssCallback);
						loc.removeUpdates(gpsLoc);
						isGpsActive = false;
					}
					//close dialog
					if(dialog != null)
					{
						dialog.dismiss();
						dialog = null;
					}
					//update and save altitude value, if Barometer sensor is not available prefs=null
					if(has3DFix == true && prefs != null)
					{
						float extra =(float)GpsAltitude;
						if(extra > 0)
						{
						   weather.update(extra,String.format("%.0fm",extra));
						   SharedPreferences.Editor editor=prefs.edit();
					       editor.putFloat("ALTITUDE",extra);
						   editor.apply();
						}
					}
					isDialogActive = false;
					
				}break;
	        }
		}
	};
	
	private GnssStatus.Callback gnssCallback= new GnssStatus.Callback()
	{

		@Override
		public void onFirstFix(int ttffMillis)
		{
			load.setText("OK");
			load.setEnabled(true);
			hTitle.setText("Click OK whenever you want.");
			has3DFix = true;
			//super.onFirstFix(ttffMillis);
		}

		@Override
		public void onStopped()
		{
			hnd.sendEmptyMessage(CODE_CLOSE);
			//super.onStopped();
		}
		
		@Override
		public void onSatelliteStatusChanged(GnssStatus status)
		{
			super.onSatelliteStatusChanged(status);
			int satInView=status.getSatelliteCount();
			int satInFix=0; 
			int satUsable=0;
			for(int i=0;i<satInView;i++)
			{
				if(status.getCn0DbHz(i) > 0)
				{
					satUsable++;
				    if(status.usedInFix(i))
				    {
					   satInFix++;
				    }
				}
			}
		    hNrSat.setText("Number of Satellites ( "+ satUsable +" )");
			if(satUsable>0)
			hNrSatBar.setMax(satUsable);
			hNrSatBar.setProgress(satInFix);
		}
	};
	private LocationListener gpsLoc = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location l)
		{
			if(l.hasAltitude() && has3DFix)
			{
				float now = l.getAccuracy();
				if(GpsAltitude < 0)
				{
					gpsAccuracy = now + 1;
				}
				if(now < gpsAccuracy)
				{
				   hLat.setText("Latitude: "+l.getLatitude());
				   hLong.setText("Longitude: "+l.getLongitude());
				   GpsAltitude = l.getAltitude();
				   if(GpsAltitude < 0)
				   {
					   GpsAltitude =0;
					   hnd.sendEmptyMessage(CODE_CLOSE);
				   }
				   hAlt.setText("Altitude: "+GpsAltitude+String.format(" (+/- %.0fm)",now));
				   gpsAccuracy = now;
				}
			}
		}
	    //@Override
        public void onProviderEnabled(String prv)
		{
			
		}
		//@Override
		public void onProviderDisabled(String prv)
		{

		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){ }
    };
}

