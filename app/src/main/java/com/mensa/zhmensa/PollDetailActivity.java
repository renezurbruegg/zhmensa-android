package com.mensa.zhmensa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mensa.zhmensa.activities.LanguageChangableActivity;
import com.mensa.zhmensa.models.PollOptionChangedModel;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.poll.Poll;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;
import com.mensa.zhmensa.services.PollManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;

/**
 * An activity representing a single poll detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PollListActivity}.
 */
public class PollDetailActivity extends LanguageChangableActivity implements Observer<Poll.PollOption>, PollManager.OnPollListChangeListener {


    private Poll mPoll;


    private final List<Pair<TextView, ProgressBar>> topPollOptions = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_detail);
        Toolbar toolbar =  findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, getBaseContext().getString(R.string.survey_share_heade) + mPoll.label + "\nhttp://mensazurich.ch/poll/" + mPoll.id);
                startActivity(Intent.createChooser(i, getBaseContext().getString(R.string.share)));
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        PollOptionChangedModel model = ViewModelProviders.of(this).get(PollOptionChangedModel.class);
        model.getChangedPollOption().observe(this, this);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //


        // Get intent and check if we opened a Link in the app
        Intent appLinkIntent = getIntent();

        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if(Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            // If Link opened -> Set ID as Poll ID
            Log.d("lastpath", appLinkData.getLastPathSegment());
            appLinkIntent.putExtra(PollDetailFragment.ARG_ITEM_ID, appLinkData.getLastPathSegment());
            MensaManager.initManager(getApplicationContext());

            for (MensaCategory category : MensaManager.getMensaCategories()) {
                MensaManager.loadMensasForCategory(category, true, getApplicationContext());
            }
        }


        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.



            mPoll = MensaManager.getPollManagger(getApplicationContext()).getPollForId(getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
            if(mPoll != null) {
                LinearLayout layout = findViewById(R.id.poll_sneek_peak_layout);
                updateSneakPeakLayout(layout, mPoll);
                Bundle arguments = new Bundle();
                arguments.putString(PollDetailFragment.ARG_ITEM_ID,
                        getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
                PollDetailFragment fragment = new PollDetailFragment();
                fragment.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.poll_detail_container, fragment)
                        .commit();

            } else {
                MensaManager.getPollManagger(getApplicationContext()).addPollId(getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID), getApplicationContext());

                MensaManager.getPollManagger(getApplicationContext()).reloadActivePollsWithCallback(getApplicationContext(), new Function1<List<Poll>, Void>() {
                    @Override
                    public Void invoke(List<Poll> pollList) {

                        mPoll = MensaManager.getPollManagger(getApplicationContext()).getPollForId(getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));

                        if(mPoll == null){
                            //Probably invalid id
                            Log.e("PollDetail", "Could not find poll with id: " + getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
                            return null;
                        }
                        LinearLayout layout = findViewById(R.id.poll_sneek_peak_layout);
                        updateSneakPeakLayout(layout, mPoll);

                        Bundle arguments = new Bundle();
                        arguments.putString(PollDetailFragment.ARG_ITEM_ID,
                                getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
                        PollDetailFragment fragment = new PollDetailFragment();
                        fragment.setArguments(arguments);

                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.poll_detail_container, fragment)
                                .commit();

                        return null;
                    }
                });
            }
        }


        MensaManager.getPollManagger(getApplicationContext()).addListener(this);

    }


    @Override
    public void onChanged(Poll.PollOption pollOption) {

        List<Poll.PollOption> pollOptions = new ArrayList<>(mPoll.getOptions());

        Collections.sort(pollOptions, new Comparator<Poll.PollOption>() {
            @Override
            public int compare(Poll.PollOption pollOption, Poll.PollOption t1) {
                return Integer.valueOf(t1.vote).compareTo(pollOption.vote);
            }
        });

        for(int i = 0; (i < topPollOptions.size() && i < pollOptions.size()); i++) {
            Pair<TextView, ProgressBar> sneakPeakEntry = topPollOptions.get(i);
            Poll.PollOption option = pollOptions.get(i);
            IMenu pollMenu = option.getPollMenu();

            if(pollMenu != null) {
                sneakPeakEntry.first.setText(pollMenu.getName());
                updatePb(mPoll, option, sneakPeakEntry.second);
            }

        }



       // ProgressBar pb = pollOptionToPbMap.get(pollOption);
       // updatePb(mPoll, pollOption, pb);
    }
/*
    private void updateLayout() {
        Collections.sort(mPoll.getOptions(), new Comparator<Poll.PollOption>() {
            @Override
            public int compare(Poll.PollOption pollOption, Poll.PollOption t1) {
                return Integer.valueOf(pollOption.vote).compareTo(t1.vote);
            }
        });
    }*/

    private void updatePb(Poll mPoll, Poll.PollOption option, ProgressBar pb) {
        if(pb == null) {
            Log.e("updatePb", "Progress bar for poll option " + option + " was null");
            return;
        }

        if(mPoll.votes == 0) {
            pb.setProgress(0);
        } else {
            pb.setProgress((int) (100.0 * option.vote / mPoll.votes));
        }
    }
    private void updateSneakPeakLayout(LinearLayout layout, Poll mPoll ) {

        LayoutInflater inflater = getLayoutInflater();
        int max = 3;
        int i = 0;

        // Sort Options
        List<Poll.PollOption> pollOptions = new ArrayList<>(mPoll.getOptions());
        Collections.sort(pollOptions, new Comparator<Poll.PollOption>() {
            @Override
            public int compare(Poll.PollOption pollOption, Poll.PollOption t1) {
                return Integer.valueOf(t1.vote).compareTo(pollOption.vote);
            }
        });


        for(Poll.PollOption option : pollOptions) {
            if(i == max)
                break;
            if(option.getPollMenu() == null)
                continue;

            View pollView = inflater.inflate(R.layout.poll_sneak_peak, null);
            final ProgressBar pb = pollView.findViewById(R.id.sneak_peak_pb);
            TextView tv = pollView.findViewById(R.id.sneak_peak_tv);

            tv.setText(Helper.padToLength(option.getPollMenu().getName(), 20));

            pb.setMax(100);
            updatePb(mPoll, option, pb);

            layout.addView(pollView);
            i++;

            topPollOptions.add(new Pair(tv, pb));
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, PollListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPollListChanged(List<Poll> pollList) {

    }

    @Override
    public void onPollInserted(Poll poll) {
        if(mPoll != null && mPoll.equals(poll)) {
            mPoll = poll;
            List<Poll.PollOption> options = poll.getOptions();

            if(!options.isEmpty()){
                // trigger reloading of sneak peak layout
                onChanged(options.get(0));
            }
        }
    }
}
