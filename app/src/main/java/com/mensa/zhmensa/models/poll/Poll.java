package com.mensa.zhmensa.models.poll;

import android.content.Context;
import android.util.Log;

import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.filters.MenuIdFilter;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Poll {

    public static class MenuIdentifier {

        public final String mensaName;
        public final String menuId;

        public MenuIdentifier(String menuId,String mensaName) {
            this.mensaName = mensaName;
            this.menuId = menuId;
        }

        @Override
        public String toString() {
            return "mensa:"+mensaName + "-menu:" +menuId;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof MenuIdentifier)
                return this.toString().equals(obj.toString());
            return false;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    public String label;
    public String id;

    public int votes;

    public Mensa.Weekday weekday;
    public Mensa.MenuCategory menuCategory;


    public List<PollOption> pollOption = new ArrayList<>();

    public DateTime creationDate;

    public Poll(String label, String id, Mensa.MenuCategory menuCategory, Mensa.Weekday weekday, String creationTime) {
        this.label = label;
        this.menuCategory = menuCategory;
        this.id = id;
        this.weekday = weekday;
        if(creationTime == null)
            this.creationDate = DateTime.now();
        else
            this.creationDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(creationTime);
    }



    public List<PollOption> getOptions() {
        return pollOption;
    }


    public void addNewOption(String menuId, String mensaId, int votes, Context ctx) {
        MenuIdentifier id = new MenuIdentifier(menuId, mensaId);
        PollOption option = new PollOption(id, votes, menuCategory, weekday);

        MensaManager.getPollManagger(ctx).updateUserVoted(this, option, ctx);

        Log.d("addNewOpt", "New option added. Id: " + id.toString() + " voted: " + option.userVoted);
        if(!pollOption.contains(option))
            pollOption.add(option);

    }

    public interface PollOptionChangedListener {
        void onPollOptionChanged(PollOption poll);
    }


    public static class PollOption {

        public final MenuIdentifier id;
        public int vote;
        private IMenu pollMenu = null;

        public boolean userVoted = new Random().nextBoolean();
        private Mensa.MenuCategory menuCategory;
        private Mensa.Weekday weekday;

        PollOption(MenuIdentifier id, int vote, Mensa.MenuCategory menuCategory, Mensa.Weekday weekday) {
            this.id = id;
            this.vote = vote;
            this.weekday = weekday;
            this.menuCategory = menuCategory;
        }

        public IMenu getPollMenu() {
            if(pollMenu == null) {
                MenuFilter filter = new MenuIdFilter(id.menuId);
                pollMenu = MensaManager.getFirstMenuForFiter(id.mensaName, menuCategory, weekday, filter);
                if(pollMenu == null) {
                    Log.e("Poll", "Could not find menu for infos: " + id.mensaName + " - " + id.menuId +" - cat " + menuCategory + " - day " + weekday);
                } /*else {
                    Mensa m = MensaManager.getMensaForId(id.mensaName);
                    if(m == null) {
                        Log.e("getMenuList", "Could not find Mensa for id: " + id.mensaName);
                    } else {
                        pollMenu =
                    }
                }*/
            }

            return pollMenu;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PollOption) {
               return ((PollOption) obj).id.equals(id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
        /*
        public IMenu pollMenu;
        public String mensaLabel;

        public boolean userVoted;
        public int votes = 3;

        public PollOption(IMenu pollMenu, String mensaLabel, int votes) {
            this.pollMenu = pollMenu;
            this.mensaLabel = mensaLabel;
            this.votes = votes;
            userVoted = new Random().nextBoolean();
        }
        */
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Poll) {
            return Helper.firstNonNull(id, "").equals(((Poll) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "id" + id + " name: " + label + "\n menus: " + pollOption;
    }
}
