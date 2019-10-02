package com.mensa.zhmensa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.models.PollOptionChangedModel;
import com.mensa.zhmensa.models.poll.Poll;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * An activity representing a single poll detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PollListActivity}.
 */
public class PollDetailActivity extends AppCompatActivity implements Observer<Poll.PollOption> {


    private Map<Poll.PollOption, ProgressBar> pollOptionToPbMap = new HashMap<>();
    private Poll mPoll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_detail);
      Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
       setSupportActionBar(toolbar);

      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(PollDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
            PollDetailFragment fragment = new PollDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.poll_detail_container, fragment)
                    .commit();


            mPoll = MensaManager.getPollManagger(getApplicationContext()).getPollForId(getIntent().getStringExtra(PollDetailFragment.ARG_ITEM_ID));
            LinearLayout layout = findViewById(R.id.poll_sneek_peak_layout);
            updateSneakPeakLayout(layout, mPoll);
        }


    }


    @Override
    public void onChanged(Poll.PollOption pollOption) {
        updateLayout();
        ProgressBar pb = pollOptionToPbMap.get(pollOption);
        updatePb(mPoll, pollOption, pb);
    }

    private void updateLayout() {
        Collections.sort(mPoll.getOptions(), new Comparator<Poll.PollOption>() {
            @Override
            public int compare(Poll.PollOption pollOption, Poll.PollOption t1) {
                return Integer.valueOf(pollOption.vote).compareTo(t1.vote);
            }
        });
    }

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
    private void updateSneakPeakLayout(LinearLayout layout, Poll mPoll) {

        LayoutInflater inflater = getLayoutInflater();
        int max = 3;
        int i = 0;

        for(Poll.PollOption option : mPoll.getOptions()) {
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

            pollOptionToPbMap.put(option, pb);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, PollListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
