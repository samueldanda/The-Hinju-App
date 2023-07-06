package com.samtechs.thehinjuapp.objects;

import androidx.annotation.NonNull;

public class TokenCard {
    private String tokenID;
    private String token;
    private String fuelAmount;
    private String fuelName;
    private String totalCost;
    private String dateCreated;
    private String dateUsed;
    private boolean used;

    public TokenCard() {
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFuelAmount() {
        return fuelAmount;
    }

    public void setFuelAmount(String fuelAmount) {
        this.fuelAmount = fuelAmount;
    }

    public String getFuelName() {
        return fuelName;
    }

    public void setFuelName(String fuelName) {
        this.fuelName = fuelName;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateUsed() {
        return dateUsed;
    }

    public void setDateUsed(String dateUsed) {
        this.dateUsed = dateUsed;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @NonNull
    @Override
    public String toString() {
        return "TokenCard{" +
                "tokenID='" + tokenID + '\'' +
                ", token='" + token + '\'' +
                ", fuelAmount='" + fuelAmount + '\'' +
                ", fuelName='" + fuelName + '\'' +
                ", totalCost='" + totalCost + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", dateUsed='" + dateUsed + '\'' +
                ", used=" + used +
                '}';
    }
}
