package com.mensa.zhmensa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.poll.Poll;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;
import com.mensa.zhmensa.services.PollManager;

import java.util.Collections;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * An activity representing a list of pollOption. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PollDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PollListActivity extends AppCompatActivity implements PollManager.OnPollListChangeListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private PollManager pollManager;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list);


        pollManager = MensaManager.getPollManagger(getApplicationContext());
//        pollManager.clear(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pollManager.showCreateNewPollDialog(Collections.<IMenu>emptyList(), "", PollListActivity.this, MensaManager.MEAL_TYPE, Mensa.Weekday.of(MensaManager.SELECTED_DAY));
                //pollManager.createNewDummyPoll(PollListActivity.this);
            }
        });


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.poll_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = findViewById(R.id.poll_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        pollManager.addListener(this);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.poll_list_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MensaManager.getPollManagger(getApplicationContext()).reloadActivePollsWithCallback(getApplication(), new Function1<List<Poll>, Void>() {
                    @Override
                    public Void invoke(List<Poll> pollList) {
                        swipeRefreshLayout.setRefreshing(false);
                        onPollListChanged(pollList);
                        return null;
                    };
                });
            }
        });
    }






    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, pollManager.getActivePolls(getApplicationContext()), mTwoPane, getApplicationContext()));
    }

    @Override
    public void onPollListChanged(List<Poll> pollList) {
        if(recyclerView != null)
            recyclerView.getAdapter().notifyDataSetChanged();
            //recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, pollManager.getActivePolls(getApplicationContext()), mTwoPane, getApplicationContext()));
    }

    @Override
    public void onPollInserted(Poll poll) {
        if(recyclerView != null)
            recyclerView.getAdapter().notifyDataSetChanged();

    }

    @Override
    public void onPollDeleted(Poll poll) {

    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PollListActivity mParentActivity;
        private Context ctx;

        private final List<Poll> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Poll selectedPoll = (Poll) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(PollDetailFragment.ARG_ITEM_ID, selectedPoll.id);
                    PollDetailFragment fragment = new PollDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.poll_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PollDetailActivity.class);
                    intent.putExtra(PollDetailFragment.ARG_ITEM_ID, selectedPoll.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PollListActivity parent,
                                      List<Poll> items,
                                      boolean twoPane, Context ctx) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
            this.ctx = ctx;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.poll_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Poll poll = mValues.get(position);
            holder.mIdView.setText(poll.label);

            if(poll.weekday != null)
                holder.mContentView.setText(Helper.getFullNameForDay(mValues.get(position).weekday, ctx));

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MensaManager.getPollManagger(ctx).removePoll(poll, ctx);
                    Snackbar.make(holder.deleteButton, (poll.label == null ? "null" : poll.label) + " entfernt", Snackbar.LENGTH_SHORT).show();
                    //mValues.remove(position);
                    notifyDataSetChanged();
                }
            });
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;
            final ImageButton deleteButton;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.weekday_label);
                deleteButton = view.findViewById(R.id.hide_poll);
            }
        }
    }



}
