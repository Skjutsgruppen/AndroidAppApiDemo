package se.tankepaus.skjutsgruppen;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;

public abstract class SingleFragmentActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
    
	// F�r att kunna spara var anv�ndaren befinner sig vid exvis rotation
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	protected static final String FRAGMENT_TAG = "SingleFragmentActivity.Fragment";
	
	// Fragment f�r att s�ka skjuts
	Fragment fragment_skjuts = new SkjutsFragment();
	// Fragment som visar information om Skjutsgruppen
	Fragment fragment_om = new SkjutsOmFragment();
	// Fragment som s�ker upp skjutsar
	Fragment fragment_findrides = new FindRidesFragment();
	
	// Om det �r f�rsta k�rningen eller inte
	boolean firstTime;
	
    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        firstTime = true;
        
        FrameLayout fl = new FrameLayout(this);
        fl.setId(R.id.fragmentContainer);
        setContentView(fl);
        
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();
        }
        

        // Konfiguera action bar att visa en dropdownlista
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Konfiguerar dropdownlistan
		actionBar.setListNavigationCallbacks(
			new ArrayAdapter<String>(getActionBarThemedContextCompat(),
				android.R.layout.simple_list_item_1,
				android.R.id.text1, new String[] {
				getString(R.string.skjuts_menu_section1),
				getString(R.string.skjuts_menu_section2), }),
				this);
    }
        
    
    /**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}
	
	
	// �terst�ll menyvalet
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}
	
	// Spara menyvalet
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Spara vilket menyval anv�ndaren �r p�
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}
	
	// Hantera knapptryckningar p� action bar
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		Fragment fragment;
		
		// G�r s� att det �r enbart vid val av anv�ndaren sj�lv som
		// nya fragment skapas och inte automatiskt direkt vid start
		if (firstTime != true) {
			// Om val 0 har klickats p� visa S�k skjuts
			if (position == 0) {
				// Skapa nytt fragment och ers�tt det aktuella med det nya
				fragment = new SkjutsFragment();
				getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, fragment_skjuts)
				.addToBackStack(null)
				.commit();
			}
			// Om val 1 har klickats p� visa Om
			else if (position == 1) {
				// Skapa nytt fragment och ers�tt det aktuella med det nya
				fragment = new Fragment();
				getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, fragment_om)
				.addToBackStack(null)
				.commit();
			}
		}
		firstTime = false;
		
		return true;
	}
    
    
    
}
