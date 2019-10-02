package com.mensa.zhmensa.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.InterceptAllVerticalSwipesViewPager;
import com.mensa.zhmensa.component.fragments.MensaOverviewFragment;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaUpdateModel;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.PollListActivity;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;
import com.mensa.zhmensa.services.MensaManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import kotlin.jvm.functions.Function1;


/**
 * Entry point of the application
 */
public class MainActivity extends LanguageChangableActivity
        implements NavigationView.OnNavigationItemSelectedListener, MensaManager.OnMensaLoadedListener {


    /**
     *
     Main Viewpager that is swipeable left and right
     */
    @Nullable
    private InterceptAllVerticalSwipesViewPager viewPager;

    /**
     * Textfield in the sidebar to show which week was loaded
     */
    @Nullable
    private TextView currentDayTextView;

    /**
     * Model to communicate with fragments when a mensa got updated
     */
    @Nullable
    private MensaUpdateModel updateModel;


    /**
     * Adapter containing all OverviewFragments for all mensas
     */
    @Nullable
    private MainViewpagerAdapter mainViewpagerAdapter;

    /**
     * Amount of mensas that were loaded
     */
    private int mensaCount = 0;

    /**
     * Sidebar navigation
     */
    @Nullable
    private ExpandableListView expandableListView;


    /**
     * Adapter used to show avaiable mensa in sidebar
     */
    @Nullable
    private NavigationExpandableListAdapter expandableListAdapter;

    /**
     * Main toolbar containing title and acttions
     */
    @Nullable
    private Toolbar toolbar;

    /**
     * Mensa that is currently selected
     */
    @Nullable
    private Mensa selectedMensa;


    // This flag should be set to true to enable VectorDrawable support for API < 21
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * Loads and initializes all components used for the sidebar navigation and the actionbar.
     */
    private void initializeSidebarDrawer() {
        // Set up sidebar navigation
        expandableListView = findViewById(R.id.expandableListView);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View myLayout = navigationView.getHeaderView(0);
        //View myView = myLayout.findViewById( R.id.someinnerview ); // id of a view
        if (myLayout != null)
            currentDayTextView = myLayout.findViewById(R.id.current_day_navgiation);

        if (currentDayTextView != null)
            currentDayTextView.setText(String.format(getString(R.string.week_of), Helper.getHumanReadableDay(0)));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

    }


    /**
     * Sets up sidebar navigation. Adds items and listener to it
     */
    private void populateExpandableList() {
        // -------- Navigation Drawer -------------
        expandableListAdapter = new NavigationExpandableListAdapter(this);
        expandableListView.setAdapter(expandableListAdapter);

        // Add click listener for favorite
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (expandableListAdapter.getGroup(groupPosition).isStandaloneItem()) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                    selectMensa(MensaManager.getFavoritesMensa());
                }
                return false;
            }
        });

        // Add click listener for Mensas
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                NavigationMenuChild model = expandableListAdapter.getChild(groupPosition, childPosition);

                if (model != null) {
                    selectMensa(model.mensa);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    Log.e("MainAct.onchildclick", "Model for position group: " + groupPosition + " child " + childPosition + " was empty");
                }

                return false;
            }
        });
    }


    private String buidJsonObject(Mensa selectedMensa) throws JSONException {

        StringBuffer options = new StringBuffer("[");

        for(IMenu menu : selectedMensa.getMenusForDayAndCategory(Mensa.Weekday.of(MensaManager.SELECTED_DAY), MensaManager.MEAL_TYPE)) {
            options.append("\"" + menu.getName() + ": " + menu.getDescription()).append("\",");
        }
        options.replace(options.length() - 1, options.length(), "]");
        Log.d("jsonObj", options.toString());

        JSONObject jsonObject = new JSONObject();


        return options.toString();
    }


    @SuppressLint("StaticFieldLeak")
    private void sharePoll() {


            // Start share action
        final Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");



        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Log.d("req123", "starting req");
                    int id = HttpUtils.postByUrl("https://www.strawpoll.me/api/v2/pollOption", buidJsonObject(selectedMensa));
                    Log.e("msg", "id:" + id);
                    return "https://www.strawpoll.me/" + id;

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("error", "",e);
                } catch (JSONException e) {
                    Log.e("error", "",e);
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String returnValue) {
                i.putExtra(android.content.Intent.EXTRA_TEXT, returnValue);
                startActivity(Intent.createChooser(i, "Share"));
            }
        }.execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity.onCr", "on create called in main activity");

        setContentView(R.layout.activity_main);


        // Set up MensaManager (load favorites, hidden menus etc.)
        MensaManager.initManager(getApplicationContext());

        updateModel = ViewModelProviders.of(this).get(MensaUpdateModel.class);

        toolbar = findViewById(R.id.toolbar);

        // Initializes HTTP Client. If API <= 22, a custom SSL certificate has to be used to call the UZH API
        HttpUtils.setupClient(getApplicationContext());

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.favorites_title));

        initializeSidebarDrawer();

        expandableListAdapter = new NavigationExpandableListAdapter(getApplicationContext());

        populateExpandableList();

        // Listen on new mensa loaded callback
        MensaManager.setOnMensaLoadListener(this);

        if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(MensaManager.CLEAR_CACHE_REQ, true)) {
            MensaManager.clearCache(getBaseContext());
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(MensaManager.CLEAR_CACHE_REQ, false).apply();
        }

        //MensaManager.clearCache(getBaseContext());
        // Load all mensas
        for (MensaCategory category : MensaManager.getMensaCategories()) {
            MensaManager.loadMensasForCategory(category, false, getApplicationContext());
        }


        viewPager = findViewById(R.id.OverviewViewPager);

        mainViewpagerAdapter = new MainViewpagerAdapter(getSupportFragmentManager());

        viewPager.setSaveEnabled(false);

        final SwipeRefreshLayout swipeView = findViewById(R.id.refresh_layout);

        viewPager.setAdapter(mainViewpagerAdapter);

        /**
         * Change Title of actionbar everytime the viewpager gets scrolled
         */
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Set title to current mensa name
                selectedMensa = MensaManager.getMensaForId(getMensaIdForPosition(position));

                if (selectedMensa == null) {
                    Log.e("On page change listener", "Mensa was null");
                    return;
                }
                String title = selectedMensa.getDisplayName();

                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Minor fix to enable smooth scrolling when a refresh layout is listening for touches
                if (swipeView != null)
                    swipeView.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        final int padding = (int) (50 * getResources().getDisplayMetrics().density);
        // Progressbar should be under actionbar
        swipeView.setProgressViewOffset(true, padding, (int) (padding * 1.5));

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                Log.d("Swipview", "Got refresh action + adapter: ");

                Helper.clearDayCache();

                final Handler handler = new Handler();

                if (mainViewpagerAdapter != null) {
                    if(mainViewpagerAdapter.getCount() == 0) {
                        MensaManager.clearState();
                    }
                    if (selectedMensa != null) {
                        MensaManager.invalidateMensa(getSelectedMensa().getUniqueId());
                        final Observable onLoadedObservable = MensaManager.loadMensasForCategoryFromInternet(selectedMensa.getCategory(), getApplicationContext());
                        onLoadedObservable.addObserver(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                Log.d("MainACt, updObs", "Updated all mensas succesfully");
                                swipeView.setRefreshing(false);
                                if (currentDayTextView != null)
                                    currentDayTextView.setText(String.format(getString(R.string.week_of), Helper.getHumanReadableDay(0)));
                                // Cancel handler so multiple refreshes won't result in error message
                                handler.removeCallbacksAndMessages(null);

                            }
                        });
                    }

                }
                // Countdown of 10s. If mensas were not loaded, assume that there are internet connection problems
                // TODO UGLY, FIX USING CALLBACK IF HTTPCLIENT FAILS
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeView.isRefreshing()) {
                            Log.d("postDelayed", "Swipe view was still refreshing after 5 seconds");

                            swipeView.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), R.string.error_download_mensas, Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        });

        selectedMensa = MensaManager.getFavoritesMensa();
    }


    @Override
    public void onNewMensaLoaded(@NonNull List<Mensa> mensas) {
        if(mensas.isEmpty())
            return;

        Log.d("OnNewMensaLoaded", "listener triggered");

        if (expandableListAdapter != null) {
            expandableListAdapter.addAll(mensas);
            mensaCount = expandableListAdapter.getAllChildrenCount();
        } else {
            Log.e("OnNewMensaLoaded", "Adapter was null");
        }

        if (mainViewpagerAdapter != null) {
            // If no menus were cached, fragments might be out of sync the first time the application starts.
            mainViewpagerAdapter.syncMenuIds();
            mainViewpagerAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onMensaUpdated(@NonNull Mensa mensa) {
        Log.d("MainActivity.omu", "Got update for Mensa: " + mensa.getDisplayName());

        Mensa selectedMensa = getSelectedMensa();

        Log.d("MainActivity.omu", "Got update for Mensa: " + mensa.getDisplayName());

        if (selectedMensa != null && selectedMensa.getUniqueId().equals(mensa.getUniqueId())) {
            selectMensa(selectedMensa);
        }

        // Notify fragments that we updated a mensa
        if (updateModel != null)
            updateModel.pushUpdate(mensa.getUniqueId());
    }


    @Override
    public void onMensaOpeningChanged() {
        expandableListAdapter.notifyDataSetChanged();
    }


    /**
     * @return the mensa that is current selected
     */
    @Nullable
    private Mensa getSelectedMensa() {
        return selectedMensa;

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Remove cache from MensaManager to trigger reload
            // Otherwise UI ends up out of sync
            MensaManager.clearState();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_only_vegi).setChecked(MensaManager.isVegiFilterEnabled(getBaseContext()));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            // Start settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_share_mensa_to_poll) {
            MensaManager.getPollManagger(getApplicationContext()).addMensaToPoll(selectedMensa, Mensa.Weekday.of(MensaManager.SELECTED_DAY), MensaManager.MEAL_TYPE, MainActivity.this, new Function1<String, Void>() {
                @Override
                public Void invoke(String s) {
                    // TODO
                    return null;
                }
            });
        } else if (id == R.id.action_share_mensa_to_device) {

            if (selectedMensa == null) {
                Log.e("MainACtivity-itemSel", "Mensa wasn null");
            } else {
                // Start share action
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                if (selectedMensa != null) {
                    Mensa.Weekday day = Mensa.Weekday.of(MensaManager.SELECTED_DAY);
                    i.putExtra(android.content.Intent.EXTRA_TEXT, selectedMensa.getAsSharableString(day, Helper.firstNonNull(mainViewpagerAdapter.getSelectedMealType(selectedMensa.getUniqueId()), MensaManager.MEAL_TYPE)));
                }
                startActivity(Intent.createChooser(i, "Share"));
            }
        } else if(id == R.id.menu_only_vegi) {

            item.setChecked(!item.isChecked());
            MensaManager.updateMenuFilter(item.isChecked(), getBaseContext());
        } else if(id == R.id.action_share_poll) {

            Intent intent = new Intent(this, PollListActivity.class);
            startActivity(intent);
            //sharePoll();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Not needed
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If no fragments are stored in adapter, trigger refresh by clearing state of mensa manager
        if(mainViewpagerAdapter != null && mainViewpagerAdapter.getCount() == 0)
            MensaManager.clearState();

        Helper.clearDayCache();
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity.onPause", "Pausing activity. Saving Mensa list to shared preferences");
        // Store mensa to cache
        MensaManager.storeAllMensasToCache(getBaseContext());
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity.Stop", "Stopping activity. Clearing state of mensa manager");
    }



    /**
     * Selects a given mensa and displays its menus inside the cards.
     * Gets called when a mensa from the sidebar is selected
     *
     * @param mensa the mensa which should be shown
     */
    private void selectMensa(@Nullable Mensa mensa) {
        if (mensa == null) {
            Log.d("selectMensa", "Mensa was null");
            return;
        }
        selectedMensa = mensa;

        Log.d("Select Mensa: ", "Mensa: " + mensa.toString());

        getSupportActionBar().setTitle(mensa.getDisplayName());
        int pos = getMensaPosForId(mensa.getUniqueId());
        if (pos == -1) {
            Log.e("MainActivity.select", "got invalid mensa id: " + mensa.getUniqueId());
            return;
        }

        if (viewPager.getAdapter().getCount() > pos)
            viewPager.setCurrentItem(pos);
        else
            Log.e("MainActivity.selectM", "Position : " + pos + " is not stored in veiwpager");

        onMensaSelected();
    }



    private void onMensaSelected() {
        // Not needed
    }

    /**
     *
     * @param position the position in the viewpager
     * @return the mensaId that matches this position
     */
    @Nullable
    private String getMensaIdForPosition(int position) {
        if (expandableListAdapter == null) {
            Log.e("MainAct.getMensaidfp", "Expandable list adapter was null");
            return null;
        }
        return expandableListAdapter.getIdForPosition(position);
    }

    /**
     *
     * @param mensaId the mensaId
     * @return the position in the viewpager / sidebar navigation
     */
    private int getMensaPosForId(@NonNull String mensaId) {
        if (expandableListAdapter == null) {
            Log.e("MainAct.getMensapfid", "Expandable list adapter was null");
            return -1;
        }

        return expandableListAdapter.getPositionForMensaId(mensaId);
    }


    /**
     * Adapter for the main viewpager containing all mensas
     */
    private class MainViewpagerAdapter extends FragmentStatePagerAdapter {
        @NonNull
        private final Map<Integer, MensaOverviewFragment> positionToFragment;


        @SuppressLint("UseSparseArrays")
        MainViewpagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            positionToFragment = new HashMap<>();
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {

            if (positionToFragment.get(position) == null) {
                MensaOverviewFragment frag = MensaOverviewFragment.newInstance(getMensaIdForPosition(position));
                positionToFragment.put(position, frag);
            }

            // Retrun cached fragment
            return positionToFragment.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            //add this line to remove fragments wiped from ViewPager cache
            positionToFragment.remove(position);
        }

        @Nullable
        Mensa.MenuCategory getSelectedMealType(String mensaId) {

            for (MensaOverviewFragment frag : positionToFragment.values()) {
                if (frag.getMensaId().equals(mensaId)) {
                    return frag.getSelectedMealType();
                }
            }
            return null;
        }


        @Override
        public int getCount() {
            // mensa count + one favorite mensa
            // Log.d("getcount", "mc " + mensaCount);
            return mensaCount;
        }


        /**
         * Iterates over all cached fragments and makes sure that the mensaId and the position match
         * Needs to be called after start up, since adding new mensas change the position in sidebar
         * and therefore the fragment needs to be refreshed
         */
        void syncMenuIds() {

            for (Integer key : positionToFragment.keySet()) {
                MensaOverviewFragment frag = positionToFragment.get(key);

                if (frag == null)
                    continue;

                String id = frag.getMensaId();
                String shouldBeId = getMensaIdForPosition(key);

                if (id != null && !id.equals(shouldBeId)) {
                    Log.d("MainACtivity.chmids", "Found wrong ids: " + id + " : " + shouldBeId);
                    frag.setMensaId(shouldBeId);
                   frag.notifyDatasetChanged();
                }
            }
        }
    }
}
