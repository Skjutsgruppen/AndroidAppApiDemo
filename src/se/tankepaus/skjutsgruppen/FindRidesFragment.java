package se.tankepaus.skjutsgruppen;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Magnus
 *
 */
public class FindRidesFragment extends ListFragment {
	
	// Variabler för att hämta upp till/från genom intent
	public static final String EXTRA_DESTINATION_TO = "skjuts.DESTINATION_TO";
	public static final String EXTRA_DESTINATION_FROM = "skjuts.DESTINATION_FROM";
	
	// Lista över skjutsar
	private ArrayList<Ride> mRides = new ArrayList<Ride>();
	private String mToDestination = "";
	private String mFromDestination = "";
	private RideAdapter adapter;
	
	
	public static FindRidesFragment newInstance(String toDestination, String fromDestination) {
		Bundle args = new Bundle();
		// Till/Från-värdena ska kunna gå att läsas mellan fragment
		args.putSerializable(EXTRA_DESTINATION_TO, toDestination);
		args.putSerializable(EXTRA_DESTINATION_FROM, fromDestination);
		FindRidesFragment fragment = new FindRidesFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Koppla Till/Från-komponenterna till de inlästa värdena
		mToDestination = (String)getArguments().getSerializable(EXTRA_DESTINATION_TO);
		mFromDestination = (String)getArguments().getSerializable(EXTRA_DESTINATION_FROM);
		
		// Kör kopplingen mot API
		new getRidesFromAPI().execute();
		
		/*
		 * För testning och då det inte går att komma åt APIet kan man istället
		 * använda nedanstående. Jag valde att låta dem ligga kvar då appen är helt
		 * beroende av att APIet är uppe och då jag inte kan garantera det själv
		 * vill jag inte att rättningen av uppgiften ska bli lidande pga det.
		 */
		//String testResults = "[{\"id\":2500,\"passengers\":[2697,3023,311],\"journeyType\":\"offered\",\"dateTime\":\"2013-08-25T17:00:00+02:00\",\"from\":\"Göteborg\",\"to\":\"Stockholm\",\"townsAlongTheWay\":[],\"eventlog\":[2401,2384,2387,2350,2400,2353,2362,2365,2397,2386,2361,2364,2399,2385,2382,2383,2381,2396,2398,2456,2454,2453,2455,2366,2457],\"seats\":3,\"journeyLinkUrl\":\"http://skjutsgruppen.nu/t/1315\"},{\"id\":1366,\"passengers\":[1582,935],\"journeyType\":\"offered\",\"dateTime\":\"2013-08-26T08:00:00+02:00\",\"from\":\"Göteborg\",\"to\":\"Stockholm\",\"townsAlongTheWay\":[\"Borås\",\"Jönköping\"],\"eventlog\":[2521,2518,2519,2520,2522,2523,2524,2537,2538,2539,2541,2540],\"seats\":3,\"journeyLinkUrl\":\"http://skjutsgruppen.nu/t/1366\"},{\"id\":1900,\"passengers\":[],\"journeyType\":\"wanted\",\"dateTime\":\"2013-08-26T10:00:00+02:00\",\"from\":\"Göteborg\",\"to\":\"Stockholm\",\"townsAlongTheWay\":[],\"eventlog\":[],\"seats\":3,\"journeyLinkUrl\":\"http://skjutsgruppen.nu/t/1315\"},{\"id\":75,\"passengers\":[],\"journeyType\":\"wanted\",\"dateTime\":\"2013-08-27T07:00:00+02:00\",\"from\":\"Göteborg\",\"to\":\"Stockholm\",\"townsAlongTheWay\":[],\"eventlog\":[],\"seats\":3,\"journeyLinkUrl\":\"http://skjutsgruppen.nu/t/1315\"}]";
		//parseSearchResults(testResults);
        
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    super.onViewCreated(view, savedInstanceState);
	    
	    TextView tv = (TextView)getActivity().getLayoutInflater().inflate(R.layout.list_item_header, null);
	    tv.setText("Från " + mFromDestination + " till " + mToDestination);
	    
	    getListView().addHeaderView(tv);
	}
	
	
	@Override
	public void onDestroyView() {
		// Så att vi inte får exceptions om adaptern försöker läggas till igen
	    setListAdapter(null);
	    super.onDestroyView();
	}
	
	
	// Använder en adapter för att koppla ihop sökresultat och lägga dem i en lista i vyn	
	private class RideAdapter extends ArrayAdapter<Ride> {
        public RideAdapter(ArrayList<Ride> rides) {
            super(getActivity(), android.R.layout.simple_list_item_1, rides);
        }
        // Nytt Ride-objekt att fylla med värden
        Ride r;
        
        // Definiera standarvyn för varje skjuts i listan
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_ride, null);
            }
            
            // Hämta Ride-objektet på positionen i listan
            r = getItem(position);
            
            // Titeltexten
            //TextView titleTextView = (TextView)convertView.findViewById(R.id.ride_list_item_titleTextView);
            //titleTextView.setText("Från " + r.getFrom() + " till " + r.getTo());
            
            // Typen av skjuts
            ImageView journeyType = (ImageView)convertView.findViewById(R.id.journeyType);
            // Om skjutsen är av typen erbjuds eller ber om
        	// Uppdatera bilden i listan därefter
            if (r.getJourneyType().equals("offered")) {
            	journeyType.setImageResource(R.drawable.ic_journeytype_offer);
            }
            else {
            	journeyType.setImageResource(R.drawable.ic_journeytype_wanted);
            }
            
            // Datum och typ av skjuts
            TextView dateTextView = (TextView)convertView.findViewById(R.id.ride_list_item_dateTextView);
            String journeyTypeFormatted = (r.getJourneyType().equals("offered")) ? "erbjuder" : "ber om";
            dateTextView.setText(r.getDateTimeFormatted());
            
            TextView personTextView = (TextView)convertView.findViewById(R.id.ride_list_item_personTextView);
            personTextView.setText(
					//+ String.valueOf(r.getId()) + " "
					String.valueOf("En person ")
					+ journeyTypeFormatted + " "
					+ "skjuts");
            
            // Sätt taggen till id't för skjutsen så att vi kan skicka det till detaljvisnings-fragmentet
            convertView.setTag(r.getId());
            
            // Lyssnaren för klick på något av objekten i listan
            convertView.setOnClickListener(new OnClickListener() {           
                @Override
                public void onClick(View v) {
                	// Skicka intent med ID för skjutsen till SkjutsDetailsFragment om usern klickar i listan 
                	Intent i = new Intent(getActivity(), SkjutsDetailsActivity.class);
                    i.putExtra(SkjutsDetailsFragment.SKJUTS_ID, String.valueOf(v.getTag()));
                    startActivityForResult(i, 0);
                }
            });
            
            return convertView;
        }
        
    }
	
	
	/**
	 * getRidesFromAPI
	 * Hämtar skjutsar från APIet som JSON
	 * Körs i en separat tråd
	 */
	private class getRidesFromAPI extends AsyncTask<Void, Void, String> {
		
		// Datumformatet som APIet förväntar sig
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		String dateToday = dateFormat.format(new Date());
		
		// Metod för att läsa in data från HttpEntity
		protected String getContentFromHttpEntity(HttpEntity entity)
				throws IllegalStateException, IOException {
			// Skapa en InputStream och läs in data
			InputStream in = entity.getContent();
			// Strängen som ska returneras
			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);
				if (n > 0)
					out.append(new String(b, 0, n));
			}
			return out.toString();
		}
		
		// Kör tråden i bakgrunden
		@Override
		protected String doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			// Skapa URLen som används för att anropa API
			String url = "http://skjutsgruppen.nu/api/v1/lift/search?"
						+ "from="
						+ mFromDestination
						+ "&to="
						+ mToDestination
						+ "&datetime="
						+ dateToday
						+ "&searchoroffer=true";
			HttpGet httpGet = new HttpGet(url);
			String text = null;
			try {
				// Testa att läsa in svaret från ett HttpGet-objekt
				HttpResponse response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
				text = getContentFromHttpEntity(entity);
			}
			catch (Exception e) { }
			
			return text;
		}
		// När tråden har kört klart
		protected void onPostExecute(String results) {
			// Om allting gått bra
			if (results != null) {
				parseSearchResults(results);
			}
		}
		
	}


	
	
	/**
	 * Behandla sökresultaten
	 * @param results 
	 */
	public void parseSearchResults(String results) {

		// Datumformatet
		SimpleDateFormat longDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.ENGLISH);

		// Antal skjutsar som returneras från API
		int numberOfOfferedRides = 0;
		
		// Om resultatet inte är ett tomt JSON-objekt
		if (!results.equals("{}")) {
			try {
				JSONArray jsonArray = new JSONArray(results);
				// Loopa igenom alla objekt i JSON-arrayen 
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					// Skapa nytt Ride-objekt utifrån datan från API
					Ride r = new Ride(
							Integer.parseInt(jsonObject.getString("id")),
							jsonObject.getString("from"),
							jsonObject.getString("to"),
							longDateTimeFormat.parse(jsonObject.getString("dateTime")),
							jsonObject.getString("journeyType"));
					mRides.add(r);
	
					numberOfOfferedRides++;
				}
			}
			catch (Exception e) { }
		}

		// Visa meddelande om inga resor hittades
		if (numberOfOfferedRides == 0) {
			Toast toast = Toast.makeText(getActivity(), "Hittade inga resor mellan " + mFromDestination + " och " + mToDestination + ".", Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
		}
		
		/*
		 * Kopplingen till adaptern måste göras här och inte direkt i onCreate
		 * då getRidesFromAPI körs i en egen tråd och huvudtråden därför inte
		 * väntar på att det ska komma något resultat innan adaptern kopplas upp
		 */
		adapter = new RideAdapter(mRides);
        setListAdapter(adapter);
		
	}
	
		
}
