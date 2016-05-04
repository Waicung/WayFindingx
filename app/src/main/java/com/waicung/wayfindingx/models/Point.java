package com.waicung.wayfindingx.models;

/**
 * Created by waicung on 04/05/2016.
 */
public class Point {
    private Double lat;
    private Double lng;

    public Point(Double lat, Double lon){
        this.lat = lat;
        this.lng = lon;
    }

    public Point(){}

    public String toString(){
        String location = lat.toString() + "," + lng.toString();
        return location;
    }

    public Point clone(){
        Double new_lat = this.lat;
        Double new_lon = this.lng;
        return new Point(new_lat, new_lon);
    }
}
