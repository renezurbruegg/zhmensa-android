package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mensa.zhmensa.services.Helper.firstNonNull;

/**
 * AClass that defines a mensa.
 * The display Name will be displayed as title and in the navigation drawer.
 */
public class Mensa implements Comparable<Mensa> {



    private Map<MenuCategory, String> mealTypeTimeMapping = new HashMap<>();

    private long lastUpdated;

    private boolean loadedFromCache = false;

    @Nullable
    private final String displayName;

    @NonNull
    private final String mensaId;

    @NonNull
    final Map<Weekday, Map<MenuCategory, Set<IMenu>>> meals;
    private MensaCategory category;

    private boolean closed = false;

    public boolean loadedFromCache(){
        return loadedFromCache;
    }

    public void setLoadedFromCache(boolean loadedFromCache){
        this.loadedFromCache = loadedFromCache;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Mensa(@Nullable  String displayName, @Nullable  String id, boolean closed) {
        this(displayName, id);
        this.closed = closed;
    }

    public Mensa(@Nullable  String displayName, @Nullable  String id) {
        meals = new HashMap<>();
        this.mensaId = Helper.firstNonNull(id, "null");

        // Convert first character to upper case
        if(displayName.length() > 0 && Character.isLowerCase(displayName.charAt(0))) {
            displayName = Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
        }

        this.displayName = displayName;
    }

    public void setMensaCategory(MensaCategory category) {
        this.category = category;
    }

    public MensaCategory getCategory(){
        return this.category;
    }

    public void setMenuForDayAndCategory(Weekday weekday, MenuCategory category, @NonNull List<IMenu> menus) {
        Log.d("Adding menus " + getDisplayName(), "cat " + category + " menus: " + menus) ;


        Map<MenuCategory,Set<IMenu>> map = firstNonNull(meals.get(weekday), new HashMap<MenuCategory, Set<IMenu>>());

        //Map<MenuCategory,List<IMenu>> map = new HashMap<>();
        map.put(category, new HashSet<>(menus));

        meals.put(weekday,map);
    }


    public void addMealTypeTimeMapping(MenuCategory mealType, String mapping) {
        Log.d("t", "t");
        if(mealTypeTimeMapping == null)
            mealTypeTimeMapping = new HashMap<>();
        mealTypeTimeMapping.put(mealType, mapping);
    }

    public String getMealTypeTime(MenuCategory mealType){
        if(mealTypeTimeMapping == null)
            mealTypeTimeMapping = new HashMap<>();
        return mealTypeTimeMapping.get(mealType);
    }

    public void addMenuForDayAndCategory(Weekday weekday, MenuCategory category, @NonNull List<IMenu> menus) {
        Log.d("Adding menus " + getDisplayName(), "cat " + category + " menus: " + menus) ;

        Map<MenuCategory,Set<IMenu>> map = firstNonNull(meals.get(weekday), new HashMap<MenuCategory, Set<IMenu>>());

        //Map<MenuCategory,List<IMenu>> map = new HashMap<>();

        Set<IMenu> storedMenus = firstNonNull(map.get(category), new HashSet<IMenu>());

        Set<IMenu> set = new HashSet<>(storedMenus);

        set.addAll(menus);

        storedMenus.addAll(menus);
        map.put(category, set);

        meals.put(weekday,map);
    }
    /**
     *
     * @return a list with all menus currently server by this mensa. Returns never null, but empty list if nothing is found
     */
    @NonNull
    public List<IMenu> getMenusForDayAndCategory(Weekday weekday, MenuCategory category) {
        Map<MenuCategory, Set<IMenu>> map = firstNonNull(meals.get(weekday), Collections.<MenuCategory, Set<IMenu>>emptyMap());

        List<IMenu> returnList = new ArrayList<>(firstNonNull(map.get(category), Collections.<IMenu>emptyList()));

        Collections.sort(returnList);
        return returnList;
    }


    public @NonNull Set<MenuCategory> getAvaiableCategoriesForDay(Weekday day) {
        return firstNonNull(meals.get(day), Collections.<MenuCategory, Set<IMenu>>emptyMap()).keySet();
    }


    @NonNull
    public String getDisplayName() {
        return Helper.firstNonNull(displayName, "null");
    }

    @NonNull
    public String getUniqueId() {
        return mensaId;
    }

    @Override
    public int compareTo(@Nullable  Mensa m) {
        if(m == null)
            return 1;

        return getDisplayName().toLowerCase().compareTo(m.getDisplayName().toLowerCase());
    }

    @NonNull
    public String getAsSharableString(@NonNull  Weekday day, @NonNull MenuCategory mealType) {

        String date = Helper.getHumanReadableDay(day.day);
        StringBuilder sb = new StringBuilder();

        List<IMenu> menus = getMenusForDayAndCategory(day, mealType);
        sb.append(getDisplayName()).append(" - ").append(date).append("\n");

        for(IMenu menu : menus) {
            sb.append(menu.getSharableString());
            sb.append("\n \n");
        }

        return sb.toString();
    }

    public void clearMenus() {
        meals.clear();
    }


    public boolean hasOpeningHours() {
        return  mealTypeTimeMapping.isEmpty();
    }

    public Map<MenuCategory, String> getMealTypeTimeMapping() {
        return mealTypeTimeMapping;
    }

    public void setMealTypeTimeMapping( Map<MenuCategory, String> mealTypeTimeMapping) {
        this. mealTypeTimeMapping = mealTypeTimeMapping;
    }

    public enum MenuCategory {
        LUNCH, DINNER, ALL_DAY;


        public static MenuCategory from(String label) {
            Log.d("mcf",label);
            if(label.equalsIgnoreCase("LUNCH") || label.equalsIgnoreCase("Mittagessen"))
                return LUNCH;
            if(label.equalsIgnoreCase("DINNER") || label.equalsIgnoreCase("Abendessen"))
                return DINNER;
            return ALL_DAY;
        }
    }

    public enum Weekday {
        MONDAY(0), TUESDAY(1), WEDNESDAY(2), THURSDAY(3), FRIDAY(4);

        public final int day;

        Weekday(int day) {
            this.day = day;
        }

        public static Weekday of(int day) {

            for (Weekday knownDay : Weekday.values()) {
                if (knownDay.day == day)
                    return knownDay;
            }

            return values()[0];
        }

    }

    public void setLastUpdated(long lastUpdated){
        this.lastUpdated = lastUpdated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @NonNull
    public String toString() {
        return "Name: " + getDisplayName() + " Id: " + getUniqueId() + " \n " + meals.toString();
    }


    @NonNull
    public List<IMenu> getMenusForDayAndCategory(Weekday weekday, MenuCategory category, @NonNull MenuFilter filter) {
        Map<MenuCategory, Set<IMenu>> map = firstNonNull(meals.get(weekday), Collections.<MenuCategory, Set<IMenu>>emptyMap());

        List<IMenu> returnList = new ArrayList<>();
        for (IMenu menu: firstNonNull(map.get(category), Collections.<IMenu>emptySet())) {
            if(filter.apply(menu))
                returnList.add(menu);
        }
        return returnList;
    }
}
