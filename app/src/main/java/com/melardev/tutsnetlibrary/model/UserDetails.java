package com.melardev.tutsnetlibrary.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melardev on 6/18/2017.
 */

public class UserDetails {
    @SerializedName("device_code")
    private String deviceCode;

    @SerializedName("user_code")
    private String userCode;

    @SerializedName("verification_url")
    private String verificationUrl;

    @SerializedName("expires_in")
    private Long expiresIn;

    private Integer interval;

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Integer getInterval() {
        return interval;
    }

    @Override
    public String toString() {

        return "UserCode{" +
                "deviceCode='" + deviceCode + '\'' +
                ", userCode='" + userCode + '\'' +
                ", verificationUrl='" + verificationUrl + '\'' +
                ", expiresIn=" + expiresIn +
                ", interval=" + interval +
                '}';
    }
}
