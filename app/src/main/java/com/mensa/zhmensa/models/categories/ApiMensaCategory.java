package com.mensa.zhmensa.models.categories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.menu.ApiMenu;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;


/**
 * Mensa Category that loads its menus from the mensazurich.ch API
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ApiMensaCategory extends MensaCategory {


    private static final String apiRoute = "http://mensazh.vsos.ethz.ch:8080/api/";
    private static final String callPath = "/getMensaForCurrentWeek";

    private final String categoryId;
    private int icon;

    public ApiMensaCategory(@NonNull String displayName, int pos, String categoryId, int icon) {
        super(displayName, pos);
        this.categoryId = categoryId;
        this.icon = icon;
    }

    /**
     * Converts a JSON response for a given mensa to a list with menus
     * @param mensa The mensa that contain these menus
     * @param mealType The mealtype (Dinner / Lunch)
     * @param array The Json respone
     * @return List with all menu items stored in the json array
     * @throws JSONException
     */
    @NonNull
    private List<Mensa> convertJsonResponseToList(JSONObject response) {
        List<Mensa> mensaList = new ArrayList<>();

        for(Iterator<String> it = response.keys(); it.hasNext(); ) {
            String key = it.next();

            try {
                JSONObject obj = response.getJSONObject(key);
                String name = obj.getString("name");
                Mensa currMensa = new Mensa(name, name);
                currMensa.setMensaCategory(this);
                mensaList.add(currMensa);


                try {
                  boolean closed = obj.getBoolean("isClosed");

                  if(closed) {
                      currMensa.setClosed(true);
                      continue;
                  }
                } catch (JSONException e){
                    Log.e("Json err", "error while checking if meensa is closed", e);
                }


                JSONObject days = obj.getJSONObject("weekdays");

                for(Iterator<String> day = days.keys(); day.hasNext(); ) {
                    JSONObject dayObj = days.getJSONObject(day.next());
                    int dayNr = dayObj.getInt("number");

                    Mensa.Weekday weekday = Mensa.Weekday.of(dayNr);

                    JSONObject mealTypes = dayObj.getJSONObject("mealTypes");

                    for(Iterator<String> mealTypeIt = mealTypes.keys(); mealTypeIt.hasNext(); ) {
                        String label = mealTypeIt.next();
                        JSONObject mealTypeObj = mealTypes.getJSONObject(label);


                        Mensa.MenuCategory mealType = Mensa.MenuCategory.from(label);
                        try {
                            JSONObject hours = mealTypeObj.getJSONObject("hours");
                            if(hours != null) {
                                String from = hours.getString("from");
                                String to = hours.getString("to");
                                if(from != null && to != null && !from.equals("null") && !to.equals("null")) {
                                    currMensa.addMealTypeTimeMapping(mealType, from +" - " + to);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("Json err", "error while checking meensa opening hours for mealtype " + label, e);
                        }


                        JSONArray menus = mealTypeObj.getJSONArray("menus");
                        currMensa.setMenuForDayAndCategory(weekday, mealType, getMenuListFromJsonArr(menus));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return mensaList;
    }

    private static List<IMenu> getMenuListFromJsonArr(JSONArray arr) throws JSONException {
        List<IMenu> retList = new ArrayList<>();

        for(int i = 0; i < arr.length(); i++) {
            JSONObject menuObj = arr.getJSONObject(i);
            retList.add(ApiMenu.fromJsonObj(menuObj));
        }

        return retList;
    }
    @NonNull
    @Override
    public List<MensaListObservable> loadMensasFromAPI(String languageCode) {
        final MensaListObservable observable = new MensaListObservable();

        final String url = apiRoute + languageCode + "/" + categoryId + callPath;
        Log.d("Mensa api request", "url: " + url);

        HttpUtils.getByUrl(url, new RequestParams(), new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, @NonNull JSONObject response) {
                        Log.d("ETH Mensa API Response", "-");
                observable.addNewMensaList(convertJsonResponseToList(response));
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, @Nullable Throwable throwable) {
                Log.e("error in get request", (throwable == null) ? "null" : throwable.getMessage());
            }

            public void onFailure(int statusCode, Header[] header, @Nullable Throwable t, JSONObject obj){
                Log.e("URL: ", url);
                Log.e("error in get request", (t == null) ? "null" : t.getMessage());
                Log.e("Response " + statusCode, String.valueOf(obj));
            }
        });


        return Collections.singletonList(observable);
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  MensaCategory)
            return getDisplayName().equals(((MensaCategory)obj).getDisplayName());
        return false;
    }

    @Nullable
    @Override
    public Integer getCategoryIconId() {
        return icon;
    }

    @Override
    public int hashCode() {
        return getDisplayName().hashCode();
    }
}
