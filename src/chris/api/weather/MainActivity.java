package chris.api.weather;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	 Typeface weatherFont;
     
	    TextView cityField;
	    TextView updatedField;
	    TextView detailsField;
	    TextView currentTemperatureField;
	    TextView weatherIcon;
	     
	    Handler handler;
	 
	    public MainActivity(){   
	        handler = new Handler();
	    }
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        cityField = (TextView)findViewById(R.id.city_field);
        updatedField = (TextView)findViewById(R.id.updated_field);
        detailsField = (TextView)findViewById(R.id.details_field);
        currentTemperatureField = (TextView)findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "weather.ttf");  
        weatherIcon.setTypeface(weatherFont);
       
		changeCity(getCity());
        
	}
	
	private String getCity(){
		//Check if saved in Preferences
		String city=getGPSLocation();//use GPS
		if(city=="")
			return new CityInfo(this).getCity();
			
		return city;
	}
	
	private String getGPSLocation(){
		LocationTracker gps = new LocationTracker(this);
		String cityName="";
		if(gps.canGetLocation() && gps.getLatitude()!=0.0){
	        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
	        List<Address> addresses;
	        Toast.makeText(this, 
	        		"Inside getGPSLocation",
	                Toast.LENGTH_LONG).show();
			try {
				addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
				 //cityName = addresses.get(0).getLocality();
				cityName=addresses.get(0).getAddressLine(1);
				 
		         Toast.makeText(this, 
		            		"Your City-" + cityName +" " +  addresses.get(0).getAddressLine(2) +" ",
		                    Toast.LENGTH_LONG).show(); 
		         
			} catch (IOException e) {
				Toast.makeText(this, 
	            		"Error", 
	                    Toast.LENGTH_LONG).show(); 
			}
			finally{
				gps.stopUsingGPS();
				
			}
		}
		return cityName;
	}
	
	private void updateWeatherData(final String city, final Activity activity){
	    new Thread(){
	        public void run(){
	            final JSONObject json = WeatherAPI.getJSON(activity, city);
	            if(json == null){
	                handler.post(new Runnable(){
	                    public void run(){
	                        Toast.makeText(activity, 
	                        		activity.getString(R.string.place_not_found), 
	                                Toast.LENGTH_LONG).show(); 
	                    }
	                });
	            } else {
	                handler.post(new Runnable(){
	                    public void run(){
	                    	calculate(json);
	                    }
	                });
	            }               
	        }
	    }.start();
	}
	
	private void calculate(JSONObject json){try {
        cityField.setText(json.getString("name").toUpperCase(Locale.US) + 
                ", " + 
                json.getJSONObject("sys").getString("country"));
         
        JSONObject details = json.getJSONArray("weather").getJSONObject(0);
        JSONObject main = json.getJSONObject("main");
        detailsField.setText(
                details.getString("description").toUpperCase(Locale.US) +
                "\n" + "Humidity: " + main.getString("humidity") + "%" +
                "\n" + "Pressure: " + main.getString("pressure") + " hPa");
         
        currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " degree celsius");
        
        Date date=new Date(json.getLong("dt")*1000);
        String sdf = new SimpleDateFormat("dd/MM/yy HH:mm").format(date);

	    updatedField.setText("Last update: " +sdf);

       
        
        setWeatherIcon(details.getInt("id"),
                json.getJSONObject("sys").getLong("sunrise") * 1000,
                json.getJSONObject("sys").getLong("sunset") * 1000);
        
        Toast.makeText(this, 
    			"Weather updated for " + json.getString("name").toUpperCase(Locale.US), 
                Toast.LENGTH_LONG).show(); 
         
    }catch(Exception e){
    	Toast.makeText(this, 
    			this.getString(R.string.place_not_found), 
                Toast.LENGTH_LONG).show(); 
    }}
	
	private void setWeatherIcon(int actualId, long sunrise, long sunset){
	    int id = actualId / 100;
	    String icon = "";
	    if(actualId == 800){
	        long currentTime = new Date().getTime();
	        if(currentTime>=sunrise && currentTime<sunset) {
	            icon = this.getString(R.string.weather_sunny);
	        } else {
	            icon = this.getString(R.string.weather_clear_night);
	        }
	    } else {
	        switch(id) {
	        case 2 : icon = this.getString(R.string.weather_thunder);
	                 break;         
	        case 3 : icon = this.getString(R.string.weather_drizzle);
	                 break;     
	        case 7 : icon = this.getString(R.string.weather_foggy);
	                 break;
	        case 8 : icon = this.getString(R.string.weather_cloudy);
	                 break;
	        case 6 : icon = this.getString(R.string.weather_snowy);
	                 break;
	        case 5 : icon = this.getString(R.string.weather_rainy);
	                 break;
	        }
	    }
	    weatherIcon.setText(icon);
	    
	    //Background color
	    long currentTime = new Date().getTime();
        if(currentTime>=sunrise && currentTime<sunset) {
        	setActivityBackgroundColor(Color.YELLOW);
        } else {
        	setActivityBackgroundColor(Color.BLUE);
        }
	}
	
	public void setActivityBackgroundColor(int color) {
	    View view = this.getWindow().getDecorView();
	    view.setBackgroundColor(color);
	}
	
	public void changeCity(String city){
		new CityInfo(this).setCity(city);
	    updateWeatherData(city,this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if(id == R.id.refresh_option){
			updateWeatherData(new CityInfo(this).getCity(), this);
	    }
		if(id == R.id.change_city){
			showInputDialog();
	    }
		if (id == R.id.exit_option) {
			finish();	
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showInputDialog(){
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Change city");
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_CLASS_TEXT);
	    builder.setView(input);
	    builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            changeCity(input.getText().toString());
	        }
	    });
	    builder.show();
	}
	 
}
