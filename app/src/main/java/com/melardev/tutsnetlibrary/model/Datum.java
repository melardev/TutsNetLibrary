package com.melardev.tutsnetlibrary.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by melardev on 6/20/2017.
 */

public class Datum {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("year")
    @Expose
    public Integer year;
    @SerializedName("pantone_value")
    @Expose
    public String pantoneValue;

    @Override
    public String toString() {
        return "id: " + id + ", name: " + name + ", year: " + year + ", pantone_value:" + pantoneValue;
    }
}
