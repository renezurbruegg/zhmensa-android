package com.mensa.zhmensa.models.menu;

import androidx.annotation.Nullable;

import com.mensa.zhmensa.services.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ApiMenu extends Menu {

    public static ApiMenu fromJsonObj(JSONObject obj) throws JSONException {
        JSONArray descArray = obj.getJSONArray("description");
        StringBuilder description = new StringBuilder();

        for(int i = 0; i < descArray.length(); i++) {
            description.append(descArray.getString(i)).append("\n");
        }

        JSONObject pricesObj =  obj.getJSONObject("prices");
        StringBuilder pricesBuilder = new StringBuilder();

        for(Iterator<String> it = pricesObj.keys(); it.hasNext(); ) {
            String priceString = pricesObj.getString(it.next());

            if(priceString.equals("null") || priceString.equals("0.00"))
                continue;

            pricesBuilder.append(pricesBuilder.length() == 0 ? "" : "/").append(priceString);
        }


        //StringBuilder pricesBuilder = new StringBuilder();
        StringBuilder allergeneBuilder = new StringBuilder();
        JSONArray allergneArr = obj.getJSONArray("allergene");
        for(int i = 0; i < allergneArr.length(); i++){
            allergeneBuilder.append(allergeneBuilder.length() == 0 ? "" : ", ").append(allergneArr.getString(i));
        }
        return new ApiMenu(obj.getString("id"), obj.getString("name"), description.toString(), pricesBuilder.toString(), allergeneBuilder.toString(), obj.getBoolean("isVegi"));
    }




    private ApiMenu(String id, String name, String description, String prices, String allergene, boolean isVegi) {
        super(id, name, description, prices, allergene);
        setVegi(isVegi);
    }

    @Nullable
    @Override
    public String getPrices() {
        //noinspection HardCodedStringLiteral
        return Helper.firstNonNull(super.getPrices(),"").replace("| CHF ","");
    }


}
