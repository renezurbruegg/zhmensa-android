package com.mensa.zhmensa.component;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.poll.Poll;
import com.mensa.zhmensa.services.MensaManager;

import kotlin.jvm.functions.Function1;

public class PollOptionViewHolder extends RecyclerView.ViewHolder {




    public PollOptionViewHolder(@NonNull View itemView) {
        super(itemView);
     }

    /**
     * Binds the menu to a view.
     * @param viewHolder to view to bind to menu to
     * @param menu to menu
     * @param mensaId
     */
    public static void bind(PollOptionViewHolder viewHolder, final Poll mainPoll, final Poll.PollOption pollOption, @NonNull final Context ctx, final Poll.PollOptionChangedListener listener) {
        IMenu menu = pollOption.getPollMenu();
        if(menu == null)
            return;



        ( viewHolder.itemView.findViewById(R.id.card_title)).setVisibility(View.VISIBLE);
        ( viewHolder.itemView.findViewById(R.id.price_text)).setVisibility(View.VISIBLE);
        ( viewHolder.itemView.findViewById(R.id.card_content)).setVisibility(View.VISIBLE);
        viewHolder.itemView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        viewHolder.itemView.findViewById(R.id.imageButton).setVisibility(View.VISIBLE);


        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(pollOption.id.mensaName + ": " + menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setText(menu.getPrices());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());
        ((TextView) viewHolder.itemView.findViewById(R.id.allergene)).setText(menu.getAllergene(ctx));


        //final LinearLayout showMoreLayout = viewHolder.itemView.findViewById(R.id.showmore_layout);
        final TextView vegiView =  viewHolder.itemView.findViewById(R.id.vegi_badge);

        final ProgressBar pb = viewHolder.itemView.findViewById(R.id.progressBar);
        pb.setProgress(mainPoll.votes == 0 ? 0 : (int) ( 100.0 * pollOption.vote / mainPoll.votes ));

        final ImageButton likeButton = viewHolder.itemView.findViewById(R.id.imageButton);
        likeButton.setImageResource(pollOption.userVoted ? R.drawable.ic_thumb_up_black_24dp : R.drawable.ic_thumb_up_black_24dp_outline);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("votecount1", "On click: poll: " + mainPoll.id  + " numbers: " + mainPoll.votes + " option: " + pollOption.id + " number: " + pollOption.vote);

                if(pollOption.userVoted) {
                    MensaManager.getPollManagger(ctx).removeVoteForPollOption(mainPoll, pollOption, ctx, new Function1<Boolean, Void>() {
                        @Override
                        public Void invoke(Boolean sucess) {
                            if(sucess) {

                  //              pollOption.userVoted = !pollOption.userVoted;
                                likeButton.setImageResource(pollOption.userVoted ? R.drawable.ic_thumb_up_black_24dp : R.drawable.ic_thumb_up_black_24dp_outline);


                                pb.setProgress(mainPoll.votes == 0 ? 0 : (int) ( 100.0 * pollOption.vote / mainPoll.votes ));
                                Log.d("votecount2", "On click: poll: " + mainPoll.id  + " numbers: " + mainPoll.votes + " option: " + pollOption.id + " number: " + pollOption.vote);

                                if(listener != null) {
                                    listener.onPollOptionChanged(pollOption);
                                }
                                Log.d("votecount3", "On click: poll: " + mainPoll.id  + " numbers: " + mainPoll.votes + " option: " + pollOption.id + " number: " + pollOption.vote);

                            } else {
                                Log.e("err", "Error voting for Poll");
                            }
                            return null;
                        }
                    });
                } else {
                    MensaManager.getPollManagger(ctx).voteForPollOption(mainPoll, pollOption, ctx, new Function1<Boolean, Void>() {
                        @Override
                        public Void invoke(Boolean sucess) {
                            if(sucess) {

//                                pollOption.userVoted = !pollOption.userVoted;
                                likeButton.setImageResource(pollOption.userVoted ? R.drawable.ic_thumb_up_black_24dp : R.drawable.ic_thumb_up_black_24dp_outline);


                                pb.setProgress(mainPoll.votes == 0 ? 0 : (int) ( 100.0 * pollOption.vote / mainPoll.votes ));
                                Log.d("votecount2", "On click: poll: " + mainPoll.id  + " numbers: " + mainPoll.votes + " option: " + pollOption.id + " number: " + pollOption.vote);

                                if(listener != null) {
                                    listener.onPollOptionChanged(pollOption);
                                }
                                Log.d("votecount3", "On click: poll: " + mainPoll.id  + " numbers: " + mainPoll.votes + " option: " + pollOption.id + " number: " + pollOption.vote);

                            } else {
                                Log.e("err", "Error voting for Poll");
                            }
                            return null;
                        }
                    });
                }



            }
        });

        vegiView.setVisibility(menu.isVegi() ? View.VISIBLE : View.INVISIBLE);
    }

    public static void bindDummy(Context context, PollOptionViewHolder viewHolder) {
        ( viewHolder.itemView.findViewById(R.id.card_title)).setVisibility(View.GONE);
        ( viewHolder.itemView.findViewById(R.id.price_text)).setVisibility(View.GONE);
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(context.getString(R.string.no_menu_avaiable));
        viewHolder.itemView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.imageButton).setVisibility(View.GONE);

    }
}
