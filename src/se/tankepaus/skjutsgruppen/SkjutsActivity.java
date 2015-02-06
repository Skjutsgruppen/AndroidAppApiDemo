package se.tankepaus.skjutsgruppen;

import android.support.v4.app.Fragment;

public class SkjutsActivity extends SingleFragmentActivity {
	
	// Returnera fragmentet
    @Override
    protected Fragment createFragment() {
        return new SkjutsFragment();
    }
	
}