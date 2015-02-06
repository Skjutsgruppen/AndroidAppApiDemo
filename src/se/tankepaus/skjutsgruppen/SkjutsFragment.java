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
	
	// F�r att hitta anv�ndarens position
	private LocationManager mLocationManager;
	private Location mLastLocation;
	private String mLocationProvider;
	
	// Lista �ver st�der
	private String[] cities;
	// Till/fr�n
	private AutoCompleteTextView mFromDestination;
	private AutoCompleteTextView mToDestination;
	
	private Button searchBtn;
	// Typsnitt som anv�nds f�r s�kknappen
	protected Typeface font;
	
	// Knappar som anv�nds f�r att ta bort all text p� en rad
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
		
		// S�k-knapp
		searchBtn = (Button) view.findViewById(R.id.searchButton);
		
		// Knappar f�r att t�mma text i f�lten
		clearFromDestination = (Button) view.findViewById(R.id.fromDestinationClearText);
		clearToDestination = (Button) view.findViewById(R.id.toDestinationClearText);
		
		// Knapp och lyssnare f�r Fr�n-f�ltet
		mFromDestination = (AutoCompleteTextView) view.findViewById(R.id.fromDestination);
		mFromDestination.addTextChangedListener(new TextWatcher(){
			// Enbart onTextChanged-metoden beh�ver implementeras i detta fall
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	// Visa knappen som t�mmer textf�ltet om anv�ndaren har b�rjat skriva n�got 
	        	clearFromDestination.setVisibility(View.VISIBLE);
	        }
			public void afterTextChanged(Editable s) {}
	    });
		// Om Fr�n-f�ltet �r tomt s� d�lj knappen som t�mmer textf�ltet
    	if(mFromDestination.getText().length() == 0) {
			clearFromDestination.setVisibility(View.GONE);
		}
		
		// Knapp och lyssnare f�r Till-f�ltet
		mToDestination = (AutoCompleteTextView) view.findViewById(R.id.toDestination);
		mToDestination.addTextChangedListener(new TextWatcher(){
			// Enbart onTextChanged-metoden beh�ver implementeras i detta fall
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	// Visa knappen som t�mmer textf�ltet om anv�ndaren har b�rjat skriva n�got
	        	clearToDestination.setVisibility(View.VISIBLE);
	        }
			public void afterTextChanged(Editable s) {}
	    });
		// Om Till-f�ltet �r tomt s� d�lj knappen som t�mmer textf�ltet
		if(mToDestination.getText().length() == 0) {
			clearToDestination.setVisibility(View.GONE);
		}
		
		// Anropa LocationManager och h�mta ut senaste position
		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		// Anv�nd GPS och inte WIFI
		mLocationProvider = LocationManager.GPS_PROVIDER;
		
		// �ndra typsnittet f�r s�kknappen f�r att f�lja designen
		searchBtn.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "Tall_Films_Expanded.ttf"));
		
		// Skapa adaptern som ska kopplas till AutoCompleteTextView
		cities = getResources().getStringArray(R.array.cities_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, cities);
		
		// B�de Till/Fr�n ska anv�nda AutoCompleteTextView f�r att underl�tta f�r anv�ndaren
		mToDestination.setAdapter(adapter);
		mFromDestination.setAdapter(adapter);
		
		// Lyssnare f�r s�k-knappen
		searchBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchForRides();
			}
		});
		
		// H�mta senaste k�nda plats
		mLastLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
		
		// Kontrollera att vi har f�tt en position fr�n GPS:en,
		// annars skippa att f�rs�ka hitta namnet p� orten
		if (mLastLocation != null) {
			new getCityNameFromGPSCoordinates().execute();
		}
		
		
		
		// Lyssnare f�r knappen som t�mmer Fr�n-f�ltet
		clearFromDestination.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// T�m textf�ltet
				mFromDestination.setText("");
				// D�lj knappen
				clearFromDestination.setVisibility(View.GONE);
				// Beg�r fokus
				mFromDestination.requestFocus();
				// Visa tangentbordet
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mFromDestination, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		
		// Lyssnare f�r knappen som t�mmer Till-f�ltet
		clearToDestination.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// T�m textf�ltet
				mToDestination.setText("");
				// D�lj knappen
				clearToDestination.setVisibility(View.GONE);
				// Beg�r fokus
				mToDestination.requestFocus();
				// Visa tangentbordet
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mToDestination, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		
		// Lyssnare f�r att kunna klicka p� s�k-knappen direkt fr�n tangentbordet
		mToDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				// Om s�kknappen p� soft-tangentbordet har klickats p�
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	// Simulera ett klick p� OK-knappen i viewn
		        	searchBtn.performClick();
		            return true;
		        }
		        return false;
			}
		});
		
		// F�r testning
		//mToDestination.setText("G�teborg");
		//mFromDestination.setText("Stockholm");
		
		// Slutligen, returnera vyn
		return view;
	}
	
	
	/**
	 * H�mtar namnet p� orten fr�n GPS-koordinater
	 * K�rs i en separat tr�d
	 * Anv�nder Google Maps API
	 */
	private class getCityNameFromGPSCoordinates extends	AsyncTask<Void, Void, String> {
		
		// K�r tr�den i bakgrunden
		@Override
		protected String doInBackground(Void... params) {
			// Skapa URLen som anv�nds f�r att anropa API
			HttpGet httpGet = new HttpGet(
					"https://maps.googleapis.com/maps/api/geocode/json?latlng="
							+ mLastLocation.getLatitude() + ","
							+ mLastLocation.getLongitude() + "&sensor=false");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;

			List<String> retList = null;
			JSONObject jsonObject = new JSONObject();
			try {
				// Testa att l�sa in svaret fr�n ett HttpGet-objekt
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				// H�mta resultatet som UTF-8 och parsa det
				String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);
				// Skapa JSON objekt
				jsonObject = new JSONObject(jsonText.toString());
				// Listan �ver resultaten
				retList = new ArrayList<String>();

				// Om det gick att h�mta n�got som API
				if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
					// Parsa resultatet som en JSON-array
					JSONArray ja = jsonObject.getJSONArray("results");
					for (int i = 0; i < 1; i++) {
						JSONObject jo = ja.getJSONObject(i);
						// H�mta ut adress-delen
						JSONArray jaa = jo.getJSONArray("address_components");
						// Loopa igenom f�r att hitta namnet p� orten
						for (int j = 0; j < jaa.length(); j++) {
							JSONObject jotwo = jaa.getJSONObject(j);
							// H�mta ut det korta namnet p� orten och inte exvis kommunen
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

			// Lite av en speciall�sning d� Googles API returnerar v�ldigt mycket data
			// om specifika GPS-koordinater. Just i detta fall �r jag bara intresserad
			// av att h�mta ut namnet p� staden, inte namnet p� kommunen, l�net, landet osv.
			// F�r att inte returnera gatuadress m�ste det n�st sista (-1), n�st mest specifika
			// objektet returneras.
			
			// Kontrollera att listan inte �r tom
			if (retList != null) {
				String ret = retList.get(retList.size() - 1).toString();
				// Kontrollera att namnet p� staden inte �r tomt
				return (ret.isEmpty()) ? "" : ret ;
			}
			else {
				return "";
			}
			

		}

		// N�r tr�den har k�rt klart
		protected void onPostExecute(String results) {
			// Om allting g�tt bra
			if (results != null) {
				// L�gg resultatet i Fr�n-f�ltet
				mFromDestination.setText(results);
			}
		}

	}
		
	

	/**
	 * S�k efter skjuts
	 */
	public void searchForRides() {
		// L�s in lista �ver orter i Sverige
		List<String> list = Arrays.asList(cities);
		// L�s in v�rdena i Till/Fr�n-f�lten
		String mToDestinationString = mToDestination.getText().toString();
		String mFromDestinationString = mFromDestination.getText().toString();

		// Om n�gon av Till/Fr�n-f�lten �r tomma
		if (mToDestinationString.isEmpty() || mFromDestinationString.isEmpty()) {
        	Toast toast = Toast.makeText(getActivity(), "Du m�ste ange var du vill �ka.", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
		
		// Om b�da f�lten inneh�ller samma v�rde
		else if (mToDestinationString.equals(mFromDestinationString)) {
        	Toast toast = Toast.makeText(getActivity(), "Kan inte vara samma ort.", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
		
		// Om b�da orterna finns med -> s�k
		else if (list.contains(mToDestinationString) && list.contains(mFromDestinationString)) {
			// Kontrollera att vi har n�tverksaccess
	        if (isNetworkAvailable())
	        {
	        	// Skapa intent som skickar med till/fr�n-v�rdena till FindRidesFragment
	        	Intent i = new Intent(getActivity(), FindRidesActivity.class);
	            i.putExtra(FindRidesFragment.EXTRA_DESTINATION_TO, mToDestinationString);
	            i.putExtra(FindRidesFragment.EXTRA_DESTINATION_FROM, mFromDestinationString);
	            startActivityForResult(i, 0);
	        }
	        // Om det inte g�r att ansluta till tj�nsten
	        else {
	        	Toast toast = Toast.makeText(getActivity(), "Kan inte ansluta till tj�nsten. Kontrollera internetuppkopplingen.", Toast.LENGTH_LONG);
	        	toast.setGravity(Gravity.CENTER, 0, 0);
	        	toast.show();
	        }
		}
		
		// Om orten inte finns med i listan
		else {
        	Toast toast = Toast.makeText(getActivity(), "Hittade tyv�rr inte " + mToDestinationString + ".", Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
	}

	
	/*
	 * Metod f�r att kontrollera om mobilen har n�gon typ av access till internet
	 */
	public boolean isNetworkAvailable() {
		getActivity();
		// Koppla upp ConnectivityManager
	    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	    // Kontrollera den aktiva n�tverksanslutningen
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    // Kontrollera om den aktiva n�tverksanslutningen har access eller inte
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}

	
}
