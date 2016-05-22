package com.waicung.wayfindingx.web_clients;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.waicung.wayfindingx.handlers.GGRequestHelper;
import com.waicung.wayfindingx.handlers.HttpRequestHandler;
import com.waicung.wayfindingx.models.Point;
import com.waicung.wayfindingx.models.Route;

/**
 * Created by waicung on 04/05/2016.
 */
public class GGRequestAsyncTask extends AsyncTask{
    ProgressDialog pd;
    private String TAG = "GGRequestTask";
    private Context context;

    public GGRequestAsyncTask(Context context){
        this.context = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage("Requesting Instruction");
        pd.show();

    }

    @Override
    protected Object doInBackground(Object[] params) {
        Point start_point = (Point)params[0];
        Point end_point = (Point)params[1];
        GGRequestHelper GH = new GGRequestHelper(start_point, end_point);
        HttpRequestHandler HR = new HttpRequestHandler();
        String response = HR.getRequest(GH.getRequestUrl());
        Route route = GH.processResponse(response);
        return route;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (pd != null) {
            pd.dismiss();
        }
    }
}
