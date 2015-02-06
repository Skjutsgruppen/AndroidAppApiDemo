package se.tankepaus.skjutsgruppen;

import android.support.v4.app.Fragment;

public class FindRidesActivity extends SingleFragmentActivity {

	@Override
    protected Fragment createFragment() {
        
		// Intent f�r att skicka till/fr�n-v�rdena till FindRidesFragment
		String mToDestination = (String)getIntent().getSerializableExtra(FindRidesFragment.EXTRA_DESTINATION_TO);
        String mFromDestination = (String)getIntent().getSerializableExtra(FindRidesFragment.EXTRA_DESTINATION_FROM);
        
        return FindRidesFragment.newInstance(mToDestination, mFromDestination);
    }
	
}