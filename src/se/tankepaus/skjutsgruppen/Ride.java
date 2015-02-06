package se.tankepaus.skjutsgruppen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Klass som hanterar skjutsar
 */
public class Ride {
    private int mId;				// Id
    private String mFrom;			// Från
    private String mTo;				// Till
    private Date mDateTime;			// Datum
    private int mSeats;				// Antal platser
    private int[] mPassengers;		// Passagerare
    private String mJourneyLinkUrl;	// Länk till skjutsen
    private String mJourneyType;	// Typen av skjuts
    private int[] mTownsAlongTheWay;// Orter som passeras
	private int[] mEventlog;		// Lista över händelser

    public Ride() {
    }
    
    /*
     * Konstruktor för objekt i list-fragmentet
     */
    public Ride(int id, String from, String to, Date dateTime, String journeyType) {
        mId = id;
        mFrom = from;
        mTo = to;
        mDateTime = dateTime;
        mJourneyType = journeyType;        
    }
    
    /*
     * Konstruktor för objekt i detaljvisnings-fragmentet
     */
    public Ride(int id, String from, String to, Date dateTime, int seats, int[] passengers, String journeyLinkUrl, String journeyType, int[] townsAlongTheWay, int[] eventlog) {
        mId = id;
        mFrom = from;
        mTo = to;
        mDateTime = dateTime;
        mSeats = seats;
        mPassengers = passengers;
        mJourneyLinkUrl = journeyLinkUrl;
        mJourneyType = journeyType;
        mTownsAlongTheWay = townsAlongTheWay;
    	mEventlog = eventlog;
    }

	public int getId() {
		return mId;
	}

	public String getFrom() {
		return mFrom;
	}

	public String getTo() {
		return mTo;
	}

	public Date getDateTime() {
		return mDateTime;
	}
	
	public String getDateTimeFormatted() {
		// Skapa datumformat som är kort och enkelt att presentera i vyn
		SimpleDateFormat dateFormat = new SimpleDateFormat("'Den' dd/MM 'kl.' HH.mm", Locale.ENGLISH);
		String dateTime = dateFormat.format(mDateTime);
		return dateTime;
	}

	public int getSeats() {
		return mSeats;
	}

	public int[] getPassengers() {
		return mPassengers;
	}

	public String getJourneyLinkUrl() {
		return mJourneyLinkUrl;
	}

	public String getJourneyType() {
		return mJourneyType;
	}

    public int[] getTownsAlongTheWay() {
		return mTownsAlongTheWay;
	}

	public int[] getEventlog() {
		return mEventlog;
	}

}
