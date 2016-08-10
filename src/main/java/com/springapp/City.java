package com.springapp;

/**
 * Created by xinhuan on 2016/3/11.
 */
public class City {
    private long id;// id
    private String title;// 姓名
    private double lat;// 纬度
    private double lon;// 经度
    private double[] location;// hashcode
    private String city;

    public City(long id,String city,  double lat, double lon,String title) {
        super();
        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lon = lon;
        this.city=city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public double[] getLocation() {
        return location;
    }
    public void setLocation(double[] location) {
        this.location = location;
    }
}
