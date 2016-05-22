package com.waicung.wayfindingx.web_clients;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.waicung.wayfindingx.handlers.HttpRequestHandler;
import com.waicung.wayfindingx.handlers.WayFindingApiHandler;
import com.waicung.wayfindingx.models.LocationRecord;
import com.waicung.wayfindingx.models.Route;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by waicung on 04/05/2016.
 */
public class UploadDataAsyncTask extends AsyncTask{
    private Context context;
    private String TAG = "UploadDataAsyncTask";
    ProgressDialog pd;

    public UploadDataAsyncTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage("Uploading data");
        pd.show();

    }

    @Override
    protected Object doInBackground(Object[] params) {
        String resource = (String)params[0];
        String api = null;
        String sentData = null;
        int route_id;
        Route route;
        int assignment_id;
        Gson gson = new Gson();
        ArrayList<LocationRecord> locations;
        WayFindingApiHandler apiHandler = new WayFindingApiHandler(context);
        try {
            switch (resource){
                case "route":
                    api = apiHandler.getUploadRouteApi();
                    route_id = (int) params[1];
                    route = (Route) params[2];
                    sentData = "route=" + URLEncoder.encode(gson.toJson(route),"UTF-8") +
                                "&route_id=" + route_id;
                    break;
                case "location":
                    api = apiHandler.getUploadLocationRecordApi();
                    assignment_id = (int) params[1];
                    locations = (ArrayList<LocationRecord>) params[2];
                    sentData = "locations=" + URLEncoder.encode(gson.toJson(locations),"UTF-8") +
                        "&assignment_id=" + assignment_id;
                    break;
                default:
                    break;
            }
            HttpRequestHandler HR = new HttpRequestHandler();
            String response = HR.postRequest(api,sentData);
            Log.i(TAG, response);
            return response;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (pd != null) {
            pd.dismiss();
        }
    }
}
