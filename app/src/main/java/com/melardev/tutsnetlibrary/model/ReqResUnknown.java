package com.melardev.tutsnetlibrary.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by melardev on 6/20/2017.
 */

public class ReqResUnknown {
    //http://www.jsonschema2pojo.org/

    @SerializedName("page")
    @Expose
    public Integer page;
    @SerializedName("per_page")
    @Expose
    public Integer perPage;
    @SerializedName("total")
    @Expose
    public Integer total;
    @SerializedName("total_pages")
    @Expose
    public Integer totalPages;
    @SerializedName("data")
    @Expose
    public List<Datum> data = null;

    @Override
    public String toString() {
        return "page: " + page + ", perPage: " + perPage + ", total: " + total + ", totalPages:" + totalPages;
    }
}
