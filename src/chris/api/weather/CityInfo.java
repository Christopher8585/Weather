package chris.api.weather;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityInfo {
	 SharedPreferences prefs;
     
	    public CityInfo(Activity activity){
	        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
	    }
	    
	    String getCity(){
	        return prefs.getString("city", "San Jose, CA");        
	    }
	     
	    void setCity(String city){
	        prefs.edit().putString("city", city).commit();
	    }
	     
	}
