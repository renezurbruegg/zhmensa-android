package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mensa.zhmensa.models.poll.Poll;

import java.util.HashMap;
import java.util.Map;

/**
 * Model used to communicate if a fragments changes the selected day.
 */
public class PollOptionChangedModel extends ViewModel {

    private final MutableLiveData<Poll.PollOption> changedPollOption = new MutableLiveData<>();

    public PollOptionChangedModel() {

    }

    @NonNull
    public MutableLiveData<Poll.PollOption>  getChangedPollOption() {
        return changedPollOption;
    }

    public void pushUpdate(Poll.PollOption option) {
       changedPollOption.setValue(option);
    }
}