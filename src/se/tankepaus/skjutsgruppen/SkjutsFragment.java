package se.tankepaus.skjutsgruppen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * SkjutsFragment
 * 
 * @author Magnus
 * 
 */
public class SkjutsFragment extends Fragment {
	
	// För att hitta användarens position
	private LocationManager mLocationManager;
	private Location mLastLocation;
	private String mLocationProvider;
	
	// Lista över städer
	private String[] cities;
	// Till/från
	private AutoCompleteTextView mFromDestination;
	private AutoCompleteTextView mToDestination;
	
	private Button searchBtn;
	// Typsnitt som används för sökknappen
	protected Typeface font;
	
	// Knappar som används för att ta bort all text på en rad
	private Button clearFromDestination;
	private Button clearToDestination;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate viewn
		View view = inflater.inflate(R.layout.fragment_skjuts, container, false);
		
		// Sök-knapp
		searchBtn = (Button) view.findViewById(R.id.searchButton);
		
		// Knappar för att tömma text i fälten
		clearFromDestination = (Button) view.findViewById(R.id.fromDestinationClearText);
		clearToDestination = (Button) view.findViewById(R.id.toDestinationClearText);
		
		// Knapp och lyssnare för Från-fältet
		mFromDestination = (AutoCompleteTextView) view.findViewById(R.id.fromDestination);
		mFromDestination.addTextChangedListener(new TextWatcher(){
			// Enbart onTextChanged-metoden behöver implementeras i detta fall
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	// Visa knappen som tömmer textfältet om användaren har börjat skriva något 
	        	clearFromDestination.setVisibility(View.VISIBLE);
	        }
			public void afterTextChanged(Editable s) {}
	    });
		// Om Från-fältet är tomt så dölj knappen som tömmer textfältet
    	if(mFromDestination.getText().length() == 0) {
			clearFromDestination.setVisibility(View.GONE);
		}
		
		// Knapp och lyssnare för Till-fältet
		mToDestination = (AutoCompleteTextView) view.findViewById(R.id.toDestination);
		mToDestination.addTextChangedListener(new TextWatcher(){
			// Enbart onTextChanged-metoden behöver implementeras i detta fall
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	// Visa knappen som tömmer textfältet om användaren har börjat skriva något
	        	clearToDestination.setVisibility(View.VISIBLE);
	        }
			public void afterTextChanged(Editable s) {}
	    });
		// Om Till-fältet är tomt så dölj knappen som tömmer textfältet
		if(mToDestination.getText().length() == 0) {
			clearToDestination.setVisibility(View.GONE);
		}
		
		// Anropa LocationManager och hämta ut senaste position
		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		// Använd GPS och inte WIFI
		mLocationProvider = LocationManager.GPS_PROVIDER;
		
		// Ändra typsnittet för sökknappen för att följa designen
		searchBtn.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "Tall_Films_Expanded.ttf"));
		
		// Skapa adaptern som ska kopplas till AutoCompleteTextView
		cities = getResources().getStringArray(R.array.cities_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, cities);
		
		// Både Till/Från ska använda AutoCompleteTextView för att underlätta för användaren
		mToDestination.setAdapter(adapter);
		mFromDestination.setAdapter(adapter);
		
		// Lyssnare för sök-knappen
		searchBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchForRides();
			}
		});
		
		// Hämta senaste kända plats
		mLastLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
		
		// Kontrollera att vi har fått en position från GPS:en,
		// annars skippa att försöka hitta namnet på orten
		if (mLastLocation != null) {
			new getCityNameFromGPSCoordinates().execute();
		}
		
		
		
		// Lyssnare för knappen som tömmer Från-fältet
		clearFromDestination.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Töm textfältet
				mFromDestination.setText("");
				// Dölj knappen
				clearFromDestination.setVisibility(View.GONE);
				// Begär fokus
				mFromDestination.requestFocus();
				// Visa tangentbordet
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mFromDestination, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		
		// Lyssnare för knappen som tömmer Till-fältet
		clearToDestination.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Töm textfältet
				mToDestination.setText("");
				// Dölj knappen
				clearToDestination.setVisibility(View.GONE);
				// Begär fokus
				mToDestination.requestFocus();
				// Visa tangentbordet
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mToDestination, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		
		// Lyssnare för att kunna klicka på sök-knappen direkt från tangentbordet
		mToDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				// Om sökknappen på soft-tangentbordet har klickats på
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	// Simulera ett klick på OK-knappen i viewn
		        	searchBtn.performClick();
		            return true;
		        }
		        return false;
			}
		});
		
		// För testning
		//mToDestination.setText("Göteborg");
		//mFromDestination.setText("Stockholm");
		
		// Slutligen, returnera vyn
		return view;
	}
	
	
	/**
	 * Hämtar namnet på orten från GPS-koordinater
	 * Körs i en separat tråd
	 * Använder Google Maps API
	 */
	private class getCityNameFromGPSCoordinates extends	AsyncTask<Void, Void, String> {
		
		// Kör tråden i bakgrunden
		@Override
		protected String doInBackground(Void... params) {
			// Skapa URLen som används för att anropa API
			HttpGet httpGet = new HttpGet(
					"https://maps.googleapis.com/maps/api/geocode/json?latlng="
							+ mLastLocation.getLatitude() + ","
							+ mLastLocation.getLongitude() + "&sensor=false");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;

			List<String> retList = null;
			JSONObject jsonObject = new JSONObject();
			try {
				// Testa att läsa in svaret från ett HttpGet-objekt
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				// Hämta resultatet som UTF-8 och parsa det
				String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);
				// Skapa JSON objekt
				jsonObject = new JSONObject(jsonText.toString());
				// Listan över resultaten
				retList = new ArrayList<String>();

				// Om det gick att hämta något som API
				if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
					// Parsa resultatet som en JSON-array
					JSONArray ja = jsonObject.getJSONArray("results");
					for (int i = 0; i < 1; i++) {
						JSONObject jo = ja.getJSONObject(i);
						// Hämta ut adress-delen
						JSONArray jaa = jo.getJSONArray("address_components");
						// Loopa igenom för att hitta namnet på orten
						for (int j = 0; j < jaa.length(); j++) {
							JSONObject jotwo = jaa.getJSONObject(j);
							// Hämta ut det korta namnet på orten och inte exvis kommunen
							String indiStr = jotwo.getString("short_name");
							if (!indiStr.isEmpty()) {
								retList.add(indiStr);	
							}
							
						}
					}
				}
			}
			catch (ClientProtocolException e) { }
			catch (IOException e) {	}
			catch (JSONException e) { }

			// Lite av en speciallösning då Googles API returnerar väldigt mycket data
			// om specifika GPS-koordinater. Just i detta fall är jag bara intresserad
			// av att hämta ut namnet på staden, inte namnet på kommunen, länet, landet osv.
			// För att inte returnera gatuadress måste det näst sista (-1), näst mest specifika
			// objektet returneras.
			
			// Kontrollera att listan inte är tom
			if (retList != null) {
				String ret = retList.get(retList.size() - 1).toString();
				// Kontrollera att namnet på staden inte är tomt
				return (ret.isEmpty()) ? "" : ret ;
			}
			else {
				return "";
			}
			

		}

		// När tråden har kört klart
		protected void onPostExecute(String results) {
			// Om allting gått bra
			if (results != null) {
				// Lägg resultatet i Från-fältet
				mFromDestination.setText(results);
			}
		}

	}
		
	

	/**
	 * Sök efter skjuts
	 */
	public void searchForRides() {
		// Läs in lista över orter i Sverige
		List<String> list = Arrays.asList(cities);
		// Läs in värdena i Till/Från-fälten
		String mToDestinationString = mToDestination.getText().toString();
		String mFromDestinationString = mFromDestination.getText().toString();

		// Om någon av Till/Från-fälten är tomma
		if (mToDestinationString.isEmpty() || mFromDestinationString.isEmpty()) {
        	Toast toast = Toast.makeText(getActivity(), "Du måste ange var du vill åka.", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
		
		// Om båda fälten innehåller samma värde
		else if (mToDestinationString.equals(mFromDestinationString)) {
        	Toast toast = Toast.makeText(getActivity(), "Kan inte vara samma ort.", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
		
		// Om båda orterna finns med -> sök
		else if (list.contains(mToDestinationString) && list.contains(mFromDestinationString)) {
			// Kontrollera att vi har nätverksaccess
	        if (isNetworkAvailable())
	        {
	        	// Skapa intent som skickar med till/från-värdena till FindRidesFragment
	        	Intent i = new Intent(getActivity(), FindRidesActivity.class);
	            i.putExtra(FindRidesFragment.EXTRA_DESTINATION_TO, mToDestinationString);
	            i.putExtra(FindRidesFragment.EXTRA_DESTINATION_FROM, mFromDestinationString);
	            startActivityForResult(i, 0);
	        }
	        // Om det inte går att ansluta till tjänsten
	        else {
	        	Toast toast = Toast.makeText(getActivity(), "Kan inte ansluta till tjänsten. Kontrollera internetuppkopplingen.", Toast.LENGTH_LONG);
	        	toast.setGravity(Gravity.CENTER, 0, 0);
	        	toast.show();
	        }
		}
		
		// Om orten inte finns med i listan
		else {
        	Toast toast = Toast.makeText(getActivity(), "Hittade tyvärr inte " + mToDestinationString + ".", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
	}

	
	/*
	 * Metod för att kontrollera om mobilen har någon typ av access till internet
	 */
	public boolean isNetworkAvailable() {
		getActivity();
		// Koppla upp ConnectivityManager
	    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	    // Kontrollera den aktiva nätverksanslutningen
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    // Kontrollera om den aktiva nätverksanslutningen har access eller inte
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}

	
}
