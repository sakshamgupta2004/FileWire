package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

public class PC {
    private String PCName;
    private String PCAddress;
    private String productId;
    private boolean isActive;

    public PC(String PCName, String PCAddress, String productId) {
        this.PCName = PCName;
        this.PCAddress = PCAddress;
        this.productId = productId;
        isActive = false;
    }

    public String getPCName() {
        return PCName;
    }

    public void setPCName(String PCName) {
        this.PCName = PCName;
    }

    public String getPCAddress() {
        return PCAddress;
    }

    public void setPCAddress(String PCAddress) {
        this.PCAddress = PCAddress;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
