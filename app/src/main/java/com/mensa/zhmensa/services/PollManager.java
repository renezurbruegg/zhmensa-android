package com.mensa.zhmensa.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.bottomsheets.BottomSheet;
import com.afollestad.materialdialogs.customview.DialogCustomViewExtKt;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.menu.FavoriteMenu;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.poll.Poll;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

/**
 * Class to manage different Polls.
 */
public class PollManager {


    @NonNls
    private static final String POLL_MANAGER_SHARED_PREF_KEY = "POLL_MANAGER_SHARED_PREF";
    public static final String USER_VOTED_POLLS = "USER_VOTED_POLLS";
    @NonNls
    private final String POLL_LIST_KEY = "active_poll_list";
    @NonNls
    private final String POLL_LIST_id_KEY = "active_poll_list_id";

    private List<Poll> activePolls = new ArrayList<>();

    private List<OnPollListChangeListener> listeners = new ArrayList<>();

    private final Set<String> idList;

    PollManager(Context ctx){

        idList =  new HashSet<>(ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(POLL_LIST_id_KEY, new HashSet<String>()));
    }


    public void reloadActivePollsWithCallback(Context ctx, final Function1<List<Poll>, Void> callback) {
        activePolls = null;
        getActivePollsWithCallback(ctx, callback);
    }

    private void getActivePollsWithCallback(Context ctx, final Function1<List<Poll>, Void> callback) {
        if(activePolls != null && !activePolls.isEmpty()) {
            callback.invoke(activePolls);
        } else {
            activePolls = new ArrayList<>();



            PollApi.getPollsForId(getIdList(ctx), ctx, new Function1<List<Poll>, Void>() {
                @Override
                public Void invoke(List<Poll> pollList) {
                    Log.d("got list", pollList == null ? "null" : pollList.toString());
                    if(pollList != null) {
                        for(Poll poll : pollList) {
                            if(!activePolls.contains(poll)) {
                                activePolls.add(poll);
                                notifyPollListChanged(poll);
                            }
                        }
                    }
                    callback.invoke(activePolls);
                    return null;
                }
            });
        }
    }

    public List<Poll> getActivePolls(Context ctx) {
        activePolls = new ArrayList<>();

        PollApi.getPollsForId(getIdList(ctx), ctx, new Function1<List<Poll>, Void>() {
            @Override
            public Void invoke(List<Poll> pollList) {
                Log.d("got list", pollList == null ? "null" : pollList.toString());
                if(pollList != null) {
                    for(Poll poll : pollList) {
                        if(poll.creationDate.getMillis() < (Helper.getStartOfWeek().getMillis() - 24*60*60*1000) ){
                            Log.d("PollManager", "Found Poll that was too old " + poll.label);
                            // TODO remove id
                        } else if(!activePolls.contains(poll)) {
                            activePolls.add(poll);
                            notifyPollListChanged(poll);
                        }
                    }
                }
                return null;
            }
        });

        return activePolls;
    }



    private boolean addMenusAsOptionToPoll(Poll poll, List<IMenu> menus, String defaultMensaId, Context ctx) {

        for(IMenu menu : menus) {
            if(menu instanceof FavoriteMenu) {
               poll.addNewOption(menu.getId(), ((FavoriteMenu) menu).getMensaId(), 0, ctx);
            } else {
               poll.addNewOption(menu.getId(), defaultMensaId, 0, ctx);
            }
        }
        return true;
    }


    private void addNewIdToList(String id, Context ctx) {
        idList.add(id);

        ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(POLL_LIST_id_KEY, idList)
                .apply();
    }


    private Set<String> getIdList(Context ctx) {
        Log.e("id list:", (ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(POLL_LIST_id_KEY, new HashSet<String>())).toString());

        return ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(POLL_LIST_id_KEY, new HashSet<String>());
    }

    private void addNewPoll(final Poll newPoll, final Context ctx, final Function1<String, Void> callback) {

        PollApi.addNewPollToApi(newPoll, new Function1<JsonElement, Void>() {
            @Override
            public Void invoke(JsonElement o) {

                if(o == null) {
                    callback.invoke(null);
                    return null;
                }

                String id = o.getAsJsonObject().get("id").getAsString();
                Log.d("ret", "xtrin:" + id);
                addNewIdToList(id, ctx);
                newPoll.id = id;
                activePolls.add(newPoll);
                notifyPollListChanged(newPoll);
                callback.invoke(id);
                return null;
            }
        }, ctx);



    }

    public void removeVoteForPollOption(final Poll poll, final Poll.PollOption option, final Context ctx, final Function1<Boolean, Void> callback) {
        // TODO check update
        PollApi.removeVoteForPollOption(ctx, poll, option, new Function1<PollApi.PollApiResponse, Void>(){
            @Override
            public Void invoke(PollApi.PollApiResponse response) {
                option.vote-=1;

                markPollOptionAsUserVoted(poll, option, ctx);

                callback.invoke(!response.error);
                return null;
            }
        });
    }


    public void updateUserVoted(Poll poll, Poll.PollOption pollOption, Context ctx) {
        Set<String> votedIds = new HashSet<String>(ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(USER_VOTED_POLLS, new HashSet<String>()));

        if(!votedIds.contains(poll.id)) {
           pollOption.userVoted = false;
        } else {
            Set<String> votedOptions = new HashSet<String>(ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    .getStringSet(poll.id, new HashSet<String>()));

            pollOption.userVoted = votedOptions.contains(pollOption.id.toString());
        }

    }



    private void markPollOptionAsUserVoted(Poll poll, Poll.PollOption option, Context ctx) {
        Set<String> votedIds = new HashSet<String>(ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(USER_VOTED_POLLS, new HashSet<String>()));

        if(!votedIds.contains(poll.id)) {
            votedIds.add(poll.id);
            ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(USER_VOTED_POLLS, votedIds)
                    .apply();
        }

        String option_id = option.id.toString();
        Set<String> votedOptions = new HashSet<String>(ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(poll.id, new HashSet<String>()));

        if(!option.userVoted) {
            votedOptions.add(option_id);
        } else {
            votedOptions.remove(option_id);
        }
        option.userVoted = !option.userVoted;

        ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(poll.id, votedOptions)
                .apply();



    }

    private boolean userVotedForPoll(final Poll poll, Context ctx) {
        return ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getStringSet(USER_VOTED_POLLS, new HashSet<String>())
                .contains(poll.id);
    }

    public void voteForPollOption(final Poll poll, final Poll.PollOption option, final Context ctx, final Function1<Boolean, Void> callback) {
        final boolean voted = userVotedForPoll(poll, ctx); // todo
        Log.d("user voted?", "user voted: " + voted);
        // TODO check update
        PollApi.voteForPollOption(ctx, poll, option, !voted, new Function1<PollApi.PollApiResponse, Void>(){
            @Override
            public Void invoke(PollApi.PollApiResponse response) {
                if(!response.error) {
                    if(!voted)
                        poll.votes += 1;
                    option.vote+=1;

                    markPollOptionAsUserVoted(poll, option, ctx);
                }
                callback.invoke(!response.error);
                return null;
            }
        });
    }
    private void notifyPollListChanged(@Nullable Poll insertedPoll){
        for(OnPollListChangeListener listener : listeners){
            listener.onPollListChanged(activePolls);
            if(insertedPoll != null)
                listener.onPollInserted(insertedPoll);
        }
      }
    public void addListener(OnPollListChangeListener listener){
        this.listeners.add(listener);
    }


    private void storePolls(Context ctx) {
        Set<String> jsonSet = new HashSet<>();
        Gson gson = new Gson();

        for(Poll poll : activePolls) {
            jsonSet.add(gson.toJson(poll, Poll.class));
        }


        ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(POLL_LIST_KEY, jsonSet)
                    .apply();
    }


    @Nullable public Poll getPollForId(String id) {
        if(id == null)
            return null;

        for (Poll poll: activePolls){
            if(poll.id.equals(id))
                return poll;

        }

        return null;
    }

    public void removePoll(Poll poll, Context ctx) {
        idList.remove(poll.id);
        ctx.getSharedPreferences(POLL_MANAGER_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(POLL_LIST_id_KEY, idList)
                .apply();

        for(int i = 0; i <activePolls.size(); i ++) {

            if(activePolls.get(i) == poll) {
                activePolls.remove(i);

                storePolls(ctx);
                notifyPollListChanged(null);

                return;
            }
        }
    }

    public void addMensaToPoll(Mensa selectedMensa, Mensa.Weekday weekday, Mensa.MenuCategory mealType, Context ctx, final Function2<String, Boolean, Void> callbackFunction) {
        if(selectedMensa == null) {
            Log.e("addMensaToPoll", "mensa was null");
            callbackFunction.invoke(ctx.getString(R.string.mensa_not_defined), true);
            return;
        }

        List<IMenu> menus = selectedMensa.getMenusForDayAndCategory(weekday,mealType, MensaManager.getMenuFilter(ctx));
        showAddMenusToPollDialog(menus, selectedMensa.getUniqueId(),  ctx, callbackFunction);
    }

    public void addPollId(String id, Context ctx) {
        addNewIdToList(id, ctx);
    }


    public interface OnPollListChangeListener {

        void onPollListChanged(List<Poll> pollList);

        void onPollInserted(Poll poll);

    }














    /*
        -------------------------------------------------------------------
                        User interface functions
        -------------------------------------------------------------------
     */
    @SuppressLint("CheckResult")
    public void showAddMenusToPollDialog(final @NonNull List<IMenu> menus, final String mensaId, final Context ctx, final Function2<String , Boolean, Void> callbackFunction) {
        getActivePollsWithCallback(ctx, new Function1<List<Poll>, Void>() {
            @Override
            public Void invoke(List<Poll> pollList) {
                _showAddMenusToPollDialog(menus, mensaId, ctx, callbackFunction);
                return null;
            }
        });
    }
    /**
     * Creates
     * @param menus
     * @param mensaId
     * @param ctx
     * @param callbackFunction
     */
    @SuppressLint("CheckResult")
    private void _showAddMenusToPollDialog(final List<IMenu> menus, final String mensaId, final Context ctx, final Function2<String, Boolean, Void> callbackFunction) {
        final List<String> list = new ArrayList<>();
        final List<Poll> polls = new ArrayList<>();

        list.add(ctx.getString(R.string.create_new_poll));

        final Mensa.Weekday currentDay = Mensa.Weekday.of(MensaManager.SELECTED_DAY);
        if(menus.isEmpty()) {
            Log.e("_showAddMenuDia", "Menus to add to poll was empty");
            callbackFunction.invoke(ctx.getString(R.string.error_add_menus), true);
            return;
        }

        final Mensa.MenuCategory mealType = MensaManager.MEAL_TYPE;
        Log.d("_showAddMenuDia", "meal type: " + mealType);

        for(Poll poll: activePolls) {
            if(currentDay == poll.weekday && mealType == poll.menuCategory ) {
                list.add(poll.label);
                polls.add(poll);
            }
        }

        if(list.isEmpty()) {
            showCreateNewPollDialog(menus, mensaId, ctx, mealType, currentDay, callbackFunction);
            return;
        }

        final MaterialDialog dialog = new MaterialDialog(ctx, new BottomSheet());
        dialog.title(null, ctx.getString(R.string.add_to_poll));

        DialogSingleChoiceExtKt.listItemsSingleChoice(dialog, null, list, null, 0, true, new Function3<MaterialDialog, Integer, String, Unit>() {
            @Override
            public Unit invoke(MaterialDialog materialDialog, Integer integer, String s) {
                if(integer == 0) {
                    showCreateNewPollDialog(menus, mensaId, ctx, mealType, currentDay, callbackFunction);
                    return null;
                }

                final Poll poll = polls.get(integer - 1);

                if(!addMenusAsOptionToPoll(poll, menus, mensaId, ctx)) {
                   callbackFunction.invoke(ctx.getString(R.string.err_add_options), true);
                   return null;
                }

                PollApi.updatePoll(poll, new Function1<PollApi.PollApiResponse, Void>() {
                    @Override
                    public Void invoke(PollApi.PollApiResponse pollApiResponse) {
                        if(pollApiResponse.error) {
                            Log.e("PollManager.update", "Error while updating poll", pollApiResponse.throwable);
                            callbackFunction.invoke(ctx.getString(R.string.error_internet_conn), true);
                        } else {
                            callbackFunction.invoke(poll.id, false);
                            storePolls(ctx);
                        }
                        return null;
                    }
                }, ctx);

                return null;
            }
        });
        dialog.positiveButton(null, ctx.getString(R.string.select), null);

        dialog.negativeButton(null, ctx.getString(R.string.cancel), null);
        dialog.show();

    }





    public void showCreateNewPollDialog(final List<IMenu> menus, final String mensaId, final Context ctx, Mensa.MenuCategory mealType, Mensa.Weekday day, final Function2<String, Boolean, Void> callback) {
        @NonNls MaterialDialog dialog = new MaterialDialog(ctx, new BottomSheet());
        DialogCustomViewExtKt.customView(dialog, R.layout.custom_view, /*customView*/ null, false, false, true, true);

        EditText et = dialog.getView().getContentLayout().getCustomView().findViewById(R.id.pollNameEt);
        et.setText(String.format(ctx.getString(R.string.poll_from), Helper.getDayForPattern(day.day, "dd-MM")));

        dialog.positiveButton(null, "OK", new Function1<MaterialDialog, Unit>() {
            @Override
            public Unit invoke(MaterialDialog materialDialog) {
                View customView = materialDialog.getView().getContentLayout().getCustomView();

                Spinner daySpinner = customView.findViewById(R.id.weekdaySpinner);
                Spinner spinner2 = customView.findViewById(R.id.mealTypeSpinner);

                EditText et = customView.findViewById(R.id.pollNameEt);
                Mensa.Weekday day = Mensa.Weekday.of(daySpinner.getSelectedItemPosition());
                Mensa.MenuCategory mealType = Mensa.MenuCategory.from(spinner2.getSelectedItem().toString().toLowerCase());

                Poll poll = new Poll(et.getText().toString(), null, mealType, day, null);
                addMenusAsOptionToPoll(poll, menus, mensaId, ctx);

                addNewPoll(poll, ctx, new Function1<String, Void>() {
                    @Override
                    public Void invoke(String s) {
                        if(s == null)
                            callback.invoke(ctx.getString(R.string.error_create_poll), true);
                        else
                            callback.invoke(s, false);
                        return null;
                    }
                });
                return null;
            }
        });

        dialog.negativeButton(null, ctx.getString(R.string.cancel), null);

        dialog.show();

        Spinner wdSpinner = dialog.getView().getContentLayout().getCustomView().findViewById(R.id.weekdaySpinner);
        wdSpinner.setSelection(day.day);
    }

}
