package chris.api.weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;

public class WeatherAPI {
	  private static final String OPEN_WEATHER_MAP_API = 
	            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
			  //"http://api.openweathermap.org/data/2.5/forcast/daily?q=%s&units=metric&cnt=7";
	    public static JSONObject getJSON(Context context, String city){
	        try {
	            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));           
	            HttpURLConnection connection = 
	                    (HttpURLConnection)url.openConnection();
	             
	            connection.addRequestProperty("eee4b56dc1ba683c1ee4342086f96b38", 
	                    context.getString(R.string.open_weather_maps_app_id));
	             
	            BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(connection.getInputStream()));
	             
	            StringBuffer json = new StringBuffer(1024);
	            String tmp="";
	            while((tmp=reader.readLine())!=null)
	                json.append(tmp).append("\n");
	            reader.close();
	             
	            JSONObject data = new JSONObject(json.toString());
	             
	            // This value will be 404 if the request was not
	            // successful
	            if(data.getInt("cod") != 200){
	                return null;
	            }
	             
	            return data;
	        }catch(Exception e){
	            return null;
	        }
	    }   
	}