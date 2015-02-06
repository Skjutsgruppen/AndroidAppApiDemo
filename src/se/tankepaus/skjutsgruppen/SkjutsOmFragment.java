package se.tankepaus.skjutsgruppen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SkjutsOmFragment extends Fragment {

	// TextView för texten om Skjutsgruppen
	TextView mOmSkjutsgruppen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate viewn
		View view = inflater.inflate(R.layout.fragment_om, container, false);
		
		// Koppla TextView till viewn
		mOmSkjutsgruppen = (TextView) view.findViewById(R.id.om_skjutsgruppen);

		// Returnera viewn
		return view;
	}
	
}