package com.ruoyi.common.core.domain.model;

public class LocationPoint {
    private final double longitude; // 经度
    private final double latitude;  // 纬度

    public LocationPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }

    // 转换为空间向量
    public double[] toCartesian() {
        double[] vector = new double[3];
        double latRad = Math.toRadians(latitude);
        double lonRad = Math.toRadians(longitude);

        // 假设地球半径为1
        vector[0] = Math.cos(latRad) * Math.cos(lonRad); // x
        vector[1] = Math.cos(latRad) * Math.sin(lonRad); // y
        vector[2] = Math.sin(latRad);                    // z

        return vector;
    }
}


