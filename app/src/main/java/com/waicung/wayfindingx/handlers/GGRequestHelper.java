package com.waicung.wayfindingx.handlers;

import android.util.Log;
import android.util.Printer;

import com.waicung.wayfindingx.models.Point;
import com.waicung.wayfindingx.models.Route;
import com.waicung.wayfindingx.models.Step;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by waicung on 04/05/2016.
 * configuration for request sending for google direction Api
 * and response handling
 */
public class GGRequestHelper {
    private String GOOGLE_API_KEY = "AIzaSyAZEyaeSOnH8dcVq646GIyUQbxGKHza_dc";
    public Point origin;
    public Point destination;
    private final String travelMode = "walking";
    private String GGparameter = "";
    private URL requestUrl;
    private final String TAG = "GGRequestHelper";

    public GGRequestHelper(Point origin, Point destination){
        //a constructor got the longitude and latitude of the origin and destination
        this.origin = origin.clone();
        this.destination = destination.clone();
        GGparameter = "origin="+origin+"&destination="+destination+"&mode="+travelMode+"&key="+GOOGLE_API_KEY;
        try {
            requestUrl = new URL("https://maps.googleapis.com/maps/api/directions/json?"+GGparameter);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public URL getRequestUrl(){
        return this.requestUrl;
    }

    public Route processResponse(String jsonString){
        Route route;
        int distance = 0, duration = 0;
        Point start_location = new Point();
        Point end_location = new Point();
        ArrayList<Step> stepsList = new ArrayList<>();
        String[] route_info = {"distance", "duration"};
        String[] route_point = {"end_location", "start_location"};

        try {
            JSONObject response = new JSONObject(jsonString);
            JSONArray routes = response.getJSONArray("routes");
            JSONObject sub_routes = routes.getJSONObject(0);
            JSONArray legs = sub_routes.getJSONArray("legs");
            JSONObject sub_legs = legs.getJSONObject(0);
            //get route information from legs
            for(String n: route_info){
                JSONObject property = sub_legs.getJSONObject(n);
                int value = property.getInt("value");
                switch (n){
                    case "distance":
                        distance = value;
                        break;
                    case "duration":
                        duration = value;
                        break;
                    default:
                        break;
                }

            }

            for(String n: route_point){
                JSONObject property = sub_legs.getJSONObject(n);
                Double lng = property.getDouble("lng");
                Double lat = property.getDouble("lat");
                switch (n){
                    case "end_location":
                        end_location = new Point(lat, lng);
                        break;
                    case "start_location":
                        start_location = new Point(lat, lng);
                        break;
                    default:
                        break;
                }
            }

            JSONArray steps = sub_legs.getJSONArray("steps");
            Point start_point = null;
            Point end_point = null;
            for(int i=0; i<steps.length();i++){
                JSONObject step = steps.getJSONObject(i);
                //Add html_instructions to a List without html tags
                String html_instruction = step.getString("html_instructions").replaceAll("\\<.*?\\>", "");
                int step_distance = step.getJSONObject("distance").getInt("value");
                int step_duration = step.getJSONObject("duration").getInt("value");
                for(String n: route_point){
                    JSONObject property = step.getJSONObject(n);
                    Double lng = property.getDouble("lng");
                    Double lat = property.getDouble("lat");
                    switch (n){
                        case "end_location":
                            end_point = new Point(lng,lat);
                            break;
                        case "start_location":
                            start_point= new Point(lng, lat);
                            break;
                        default:
                            break;
                    }
                }
                //last instruction contain 'destination information', which needs to be split
                if(i==steps.length()-1&&html_instruction.indexOf("Destination")>0){
                    Log.i(TAG,"last instruction: " + html_instruction);
                    int destination = html_instruction.indexOf("Destination");
                    String sub1 = html_instruction.substring(0,destination);
                    String sub2 = html_instruction.substring(destination,html_instruction.length());
                    stepsList.add(new Step(i,start_point,end_point,sub1,step_duration,step_distance));
                    Step last = new Step();
                    last.setInstruction(sub2);
                    stepsList.add(last);
                }
                else {
                    stepsList.add(new Step(i,start_point,end_point,html_instruction,step_duration,step_distance));
                }

            }
            Log.i(TAG,"Google direction output: " + stepsList);
        }
        catch(JSONException e){
            Log.i(TAG,"JSON Exception: " + e.getMessage());
        }
        route = new Route(0,start_location,end_location,duration,distance,stepsList);
        return route;
    }

}
