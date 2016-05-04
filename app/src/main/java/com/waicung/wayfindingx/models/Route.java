package com.waicung.wayfindingx.models;

import java.util.ArrayList;

/**
 * Created by waicung on 04/05/2016.
 */
public class Route {
    private int route_id;
    private Point start_point;
    private Point end_point;
    private int distance;
    private int duration;
    private ArrayList<Step> steps= new ArrayList<Step>();

    public Route(int route_id, Point start_point, Point end_point, int duration, int distance, ArrayList<Step> steps){
        if(route_id>=0){
            this.route_id = route_id;
        }
        this.start_point = start_point;
        this.end_point = end_point;
        this.distance = distance;
        this.duration = duration;
        for(Step s: steps){
            this.steps.add(s.clone());
        }
    }

    public ArrayList<Step> getSteps(){
        return this.steps;
    }

    public Point getPoint(int end){
        switch(end) {
            case 0:
                return this.start_point;
            case 1:
                return this.end_point;
            default:
                return this.start_point;
        }
    }

}
