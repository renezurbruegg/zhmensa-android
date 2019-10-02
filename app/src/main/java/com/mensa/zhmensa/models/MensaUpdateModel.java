package com.mensa.zhmensa.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Model used to update fragments when a mensa gets updated */
public class MensaUpdateModel extends ViewModel {
    @NonNull
    private final MutableLiveData<String> updatedMensaId = new MutableLiveData<>();

    public MensaUpdateModel() {

    }

    @NonNull
    public MutableLiveData<String> getUpdatedMensaId() {
        return updatedMensaId;
    }

    public void pushUpdate(String mensaId) {
        updatedMensaId.setValue(mensaId);
    }
}