package com.mensa.zhmensa;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mensa.zhmensa.component.PollOptionViewHolder;
import com.mensa.zhmensa.models.PollOptionChangedModel;
import com.mensa.zhmensa.models.poll.Poll;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * A fragment representing a single poll detail screen.
 * This fragment is either contained in a {@link PollListActivity}
 * in two-pane mode (on tablets) or a {@link PollDetailActivity}
 * on handsets.
 */
public class PollDetailFragment extends Fragment implements Poll.PollOptionChangedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Poll mPoll;

    private RecyclerView mRecyclerView;
    private CollapsingToolbarLayout appBarLayout;
    private TextView weekdayTextView;
    private TextView votesTextView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PollDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mPoll = MensaManager.getPollManagger(getContext()).getPollForId(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mPoll.label);
            }


        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.poll_detail_recycler);

        weekdayTextView =  view.findViewById(R.id.poll_detail_weekday);
        votesTextView =  view.findViewById(R.id.poll_detail_votes);

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refresh_layout_poll);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("swipe", "Refreshing");
                MensaManager.getPollManagger(getContext()).reloadActivePollsWithCallback(getContext(), new Function1<List<Poll>, Void>() {
                    @Override
                    public Void invoke(List<Poll> pollList) {
                        mPoll = MensaManager.getPollManagger(getContext()).getPollForId(getArguments().getString(ARG_ITEM_ID));

                        if(mPoll == null)
                            return null;

                        if(mRecyclerView != null) {
                            mRecyclerView.setAdapter(new PollCardAdapter(mPoll, PollDetailFragment.this));
                        }
                        if (appBarLayout != null) {
                            appBarLayout.setTitle(mPoll.label);
                        }
                        if(votesTextView != null) {
                            votesTextView.setText(String.format(getContext().getString(R.string.votes), mPoll.votes));
                        }
                        if(weekdayTextView != null) {
                            weekdayTextView.setText(Helper.getFullNameForDay(mPoll.weekday, getContext()));
                        }


                        swipeRefreshLayout.setRefreshing(false);
                        return null;
                    }
                });
            }
        });
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layout);
        // Show the dummy content as text in a TextView.
        if (mPoll != null) {
            mRecyclerView.setAdapter(new PollCardAdapter(mPoll, this));

            if(votesTextView != null) {
                votesTextView.setText(String.format(getContext().getString(R.string.votes), mPoll.votes));
            }
            if(weekdayTextView != null) {
                weekdayTextView.setText(Helper.getFullNameForDay(mPoll.weekday, getContext()));
            }
           // mRecyclerView.getAdapter().notifyDataSetChanged();
          //  mRecyclerView.getLayoutManager().setAutoMeasureEnabled(true);
            //((TextView) rootView.findViewById(R.id.poll_detail)).setText(mPoll.label);
        }

        //mRecyclerView.setAdapter(new MenuCardAdapter(mensa.getMenusForDayAndCategory(Mensa.Weekday.MONDAY, Mensa.MenuCategory.LUNCH), mensa.getUniqueId(), MensaManager.getMenuFilter(getContext()), getContext()));
        //Toast.makeText(getContext(), "it: " + mRecyclerView.getAdapter().getItemCount() ,Toast.LENGTH_LONG).show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.poll_detail, container, false);

      return rootView;
    }

    @Override
    public void onPollOptionChanged(Poll.PollOption poll) {
        PollOptionChangedModel model = ViewModelProviders.of(getActivity()).get(PollOptionChangedModel.class);
        model.pushUpdate(poll);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mPoll.label);
        }
        if(votesTextView != null) {
            votesTextView.setText(String.format(getContext().getString(R.string.votes), mPoll.votes));
        }

        if(weekdayTextView != null) {
            weekdayTextView.setText(Helper.getFullNameForDay(mPoll.weekday, getContext()));
        }

    }


    /**
     * Adapter class that holds all menu views for a Mensa.
     * Is used inside the recycler view to display different menus.
     */
    public class PollCardAdapter extends RecyclerView.Adapter<PollOptionViewHolder> {


        private Context context;
        private Poll.PollOptionChangedListener listener;
        private Poll mainPoll;
        private List<Poll.PollOption> pollOptions;

        public PollCardAdapter(Poll mainPoll, Poll.PollOptionChangedListener listener) {
            this.mainPoll = mainPoll;
            pollOptions = mainPoll.getOptions();
            Log.d("polloptions", pollOptions.toString());
            this.listener = listener;

        }



        @NonNull
        @Override
        public PollOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poll_card, parent, false);
            return new PollOptionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PollOptionViewHolder holder, int position) {
            if(pollOptions.isEmpty()) {
                PollOptionViewHolder.bindDummy(context, holder);
                return;
            }
            Poll.PollOption option = pollOptions.get(position);
            PollOptionViewHolder.bind(holder, mainPoll, option, context, listener);
        }

        @Override
        public int getItemCount() {
            return pollOptions.isEmpty() ? 1 : pollOptions.size();
        }


    }



}
