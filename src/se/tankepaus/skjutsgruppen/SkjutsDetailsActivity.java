package se.tankepaus.skjutsgruppen;

import android.support.v4.app.Fragment;

public class SkjutsDetailsActivity extends SingleFragmentActivity {

	// Returnera ny instans av fragmentet
	@Override
    protected Fragment createFragment() {

		// Skicka id för skjutsen via intent
		String journeyId = (String)getIntent().getSerializableExtra(SkjutsDetailsFragment.SKJUTS_ID);
        
        return SkjutsDetailsFragment.newInstance(journeyId);
    }

}