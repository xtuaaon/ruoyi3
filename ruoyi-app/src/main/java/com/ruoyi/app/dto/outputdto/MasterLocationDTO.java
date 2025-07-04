package com.ruoyi.app.dto.outputdto;

public class MasterLocationDTO {
    private String userName;
    private String phone;
    private double rating;
    private String specialty;
    private int serviceCount;
    private boolean onlineStatus;
    private double distance;
    private String distanceUnit;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * 设置电话号码，并进行隐私保护处理
     * 显示前4位和后4位，中间用星号替代
     *
     * @param phone 原始电话号码
     */
    public void setPhone(String phone) {
        if (phone == null || phone.length() <= 8) {
            this.phone = phone;
            return;
        }

        String prefix = phone.substring(0, 4);
        String suffix = phone.substring(phone.length() - 4);
        String stars = "*".repeat(phone.length() - 8);  // Java 11+

        this.phone = prefix + stars + suffix;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public int getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(int serviceCount) {
        this.serviceCount = serviceCount;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }
}
