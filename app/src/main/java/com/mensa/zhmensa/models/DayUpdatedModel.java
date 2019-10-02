package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Model used to communicate if a fragments changes the selected day.
 */
public class DayUpdatedModel extends ViewModel {

    private final Map<String, MutableLiveData<Mensa.Weekday>> listeners = new HashMap<>();

    public DayUpdatedModel() {

    }

    @NonNull
    public MutableLiveData<Mensa.Weekday> getChangedDay(String mensaId) {
        MutableLiveData<Mensa.Weekday> list = listeners.get(mensaId);

        if (list == null) {
            list = new MutableLiveData<>();
            listeners.put(mensaId, list);
        }

        return list;
    }

    public void pushUpdate(Mensa.Weekday day, String mensaId) {
        Log.d("ViewModel", mensaId + " is pushing day update for day " + day);
        for (String key : listeners.keySet()) {
            if (!key.equals(mensaId)) {
                getChangedDay(key).postValue(day);
            }
        }
    }
}