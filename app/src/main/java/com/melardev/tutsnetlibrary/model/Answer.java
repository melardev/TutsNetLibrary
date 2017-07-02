package com.melardev.tutsnetlibrary.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melardev on 6/18/2017.
 */

public class Answer {
    @SerializedName("answer_id")
    public int answerId;

    @SerializedName("is_accepted")
    public boolean accepted;

    public int score;

    @Override
    public String toString() {
        return answerId + " - Score: " + " - Accepted: " + (accepted ? "Yes" : "No");
    }
}
