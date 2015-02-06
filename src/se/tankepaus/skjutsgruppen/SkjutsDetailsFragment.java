package se.tankepaus.skjutsgruppen;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SkjutsDetailsFragment extends Fragment {

	// Variabel för att hämta upp id genom intent
	public static final String SKJUTS_ID = "skjuts.ID";
	private String skjutsId;			// Skjutsens ID
	
	private TextView mFromDestination;	// Från
	private String from;
	
	private TextView mToDestination;	// Till 
	private String to;
	
	private TextView mJourneyLinkUrl;	// Länkadrss
	private String journeyLinkUrl;
	
	private TextView mJourneyType;		// Typ av skjuts
	private String journeyType;
	
	private TextView mDateTime;			// Datum/tid
	private String dateTime;
	
	private TextView mTownsAlongTheWay;	// Städer som passeras
	private JSONArray townsAlongTheWay;
	
	private TextView mCommentsText;		// Kommentarer
	private JSONArray commentsArray;
	private JSONObject commentsObject;
	private String commentsText;
	
	//private TextView mAuthor;			// Författare
	private String author;
	
	private TextView mSeats;			// Antal platser
	private String seats;
	
	
	public static SkjutsDetailsFragment newInstance(String skjutsId) {
		Bundle args = new Bundle();
		// Lägg in skjutsens ID i en lokal variabel
		args.putSerializable(SKJUTS_ID, skjutsId);
		SkjutsDetailsFragment fragment = new SkjutsDetailsFragment();
		fragment.setArguments(args);
		return fragment;
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Läs in skjutsens ID till en lokal variabel
		skjutsId = (String)getArguments().getSerializable(SKJUTS_ID);
		
		// Kör kopplingen mot API
		new getJourneyFromAPI().execute();
		
		/*
		 * För testning och då det inte går att komma åt APIet kan man istället
		 * använda nedanstående. Jag valde att låta dem ligga kvar då appen är helt
		 * beroende av att APIet är uppe och då jag inte kan garantera det själv
		 * vill jag inte att rättningen av uppgiften ska bli lidande pga det.
		 */
		//String testResults = "{\"journey\":{\"id\":1290,\"journeyType\":\"wanted\",\"dateTime\":\"2013-08-27T12:02:00+02:00\",\"from\":\"Stockholm\",\"to\":\"Malmö\",\"townsAlongTheWay\":[],\"eventlog\":[],\"comments\":[{\"text\":\"Jag skulle behöva komma neråt i landet från och med tisdag/onsdag och tillbaka norrut på söndagen. \r\nJag är en öppensinnad person som tror att vi kan ha en del att prata om! \r\n\r\nDet jag har att komma med är goda samtalsämnen, allt mellan himmel och jord, bensinpeng och även hjälp med körningen så att det inte blir att för jobbigt att köra själv. \",\"author\":\"Ahmed\"}],\"seats\":null,\"friends\":[],\"journeyLinkUrl\":\"http://skjutsgruppen.nu/t/1290\"}}";
		//parseJourneyResults(testResults);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_skjuts_details, container, false);
		
		// Koppla upp komponenter 
		mFromDestination = (TextView) v.findViewById(R.id.fromDestination);
		mToDestination = (TextView) v.findViewById(R.id.toDestination);
		mJourneyType = (TextView) v.findViewById(R.id.journeyType);
		mDateTime = (TextView) v.findViewById(R.id.dateTime);
		mTownsAlongTheWay = (TextView) v.findViewById(R.id.townsAlongTheWay);
		mCommentsText = (TextView) v.findViewById(R.id.commentsText);
		mSeats = (TextView) v.findViewById(R.id.seats);
		
		// Gör länken i vyn klickbar
		mJourneyLinkUrl = (TextView) v.findViewById(R.id.journeyLinkUrl);
		// Om länken inte är tom
		if (mJourneyLinkUrl != null) {
			mJourneyLinkUrl.setMovementMethod(LinkMovementMethod.getInstance());
		}
				
		return v;
	}


	/**
	 * getJourneyFromAPI
	 * Hämtar skjuts från APIet som JSON
	 * Körs i en separat tråd
	 */
	private class getJourneyFromAPI extends AsyncTask<Void, Void, String> {
		
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
			String url = "http://skjutsgruppen.nu/api/v1/journeys/"
						+ skjutsId
						+ "?"
						+ "email=ENTER_YOUR_EMAIL"
						+ "&password=ENTER_YOUR_PASSWORD";
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
				parseJourneyResults(results);
			}
		}
		
	}
	
	
	

	/**
	 * Behandla sökresultaten
	 * @param results 
	 */
	public void parseJourneyResults(String results) {

		// Datumformatet
		SimpleDateFormat longDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.ENGLISH);
		
		// Om resultatet inte är ett tomt JSON-objekt
		if (!results.equals("{}")) {
			try {
				// Skapa JSON objekt från resultaten
				JSONObject jsonObject = new JSONObject(results);
				// Hämta journey-objektet
				JSONObject jsonJourney = jsonObject.getJSONObject("journey");
				
				// Parsa typen av skjuts
				journeyType = jsonJourney.getString("journeyType");
				// Parsa datumet enligt datumformat ovan
				dateTime = longDateTimeFormat.parse(jsonJourney.getString("dateTime")).toString();
				// Parsa från
				from = jsonJourney.getString("from");
				// Parsa till
				to = jsonJourney.getString("to");
				// Parsa städer som passeras
				townsAlongTheWay = jsonJourney.getJSONArray("townsAlongTheWay");
				// Parsa kommentarer
				commentsArray = jsonJourney.getJSONArray("comments");
				// Det går just nu bara att hämta den första kommentaren
				// Det är den som den som erbjudit skjutsen skrivit
				commentsObject = commentsArray.getJSONObject(0);
				commentsText = commentsObject.getString("text");
				// Parsa författare
				author = commentsObject.getString("author");
				// Parsa antal platser
				seats = jsonJourney.getString("seats");
				// Parsa länkadress
				journeyLinkUrl = jsonJourney.getString("journeyLinkUrl");
				
				// Uppdatera värdena i vyn
				// Nödvändigt att göra det i en egen metod för att värdena ska uppdateras i maintråden
				// och inte i bakgrundstråden som inte får göra det.
				updateJourneyValuesInView();
			}
			catch (Exception e) { }
		}
	}


	// Uppdatera fält med värden från API
	private void updateJourneyValuesInView() {
		// Till/från
		mFromDestination.setText(from + " - ");
		mToDestination.setText(to);

		// Typ av skjuts
		if (journeyType.equals("offered")) {
			mJourneyType.setText(author + " erbjuder skjuts");			
		}
		else if (journeyType.equals("wanted")) {
			mJourneyType.setText(author + " ber om skjuts");
		}
		
		// Datum
		//String dateTimeDisplay = dateTime;
		Date date = null;
		try {
			date = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH).parse(dateTime);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("'Den' dd/MM 'kl.' HH.mm", Locale.ENGLISH);
		String dateTime = dateFormat.format(date);
		mDateTime.setText(dateTime);
		
		// Passerade orter
		if (journeyType.equals("wanted") && !townsAlongTheWay.isNull(0)) {
			mTownsAlongTheWay.setText(townsAlongTheWay.toString());
		}
		else {
			mTownsAlongTheWay.setText("");
		}
		
		// Kommentar
		//mCommentsText.setText(author + ": " + commentsText);
		mCommentsText.setText(commentsText);
		
		// Antal platser som erbjuds
		if (journeyType.equals("offered")) {
			mSeats.setText(seats + " platser erbjuds");			
		}
		else {
			mSeats.setText("");
		}
		
		// Länk
		if (journeyType.equals("offered")) {
			mJourneyLinkUrl.setText("Boka skjuts på\n" + journeyLinkUrl);			
		}
		else if (journeyType.equals("wanted")) {
			mJourneyLinkUrl.setText("Erbjud " + author + " skjuts på\n" + journeyLinkUrl);
		}
		
		
	}
	
	
	
}
