package com.waicung.wayfindingx.web_clients;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.waicung.wayfindingx.R;
import com.waicung.wayfindingx.handlers.HttpRequestHandler;
import com.waicung.wayfindingx.handlers.SharedPreferencesHandler;
import com.waicung.wayfindingx.handlers.WayFindingApiHandler;
import com.waicung.wayfindingx.models.AuthenNResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by waicung on 04/05/2016.
 */
public class AuthenNAsyncTask extends AsyncTask {
    private String TAG = "AuthenNAsyncTask";
    private String sentData;
    private Context context;
    private String api;

    public AuthenNAsyncTask(Context context){
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String username = (String)params[0];
        String password = (String)params[1];
        Log.i(TAG,"asking for authentication: " + "username: " + username +" password: " + password);
        try {
            sentData = "username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        WayFindingApiHandler apiHandler = new WayFindingApiHandler(context);
        api = apiHandler.getAuthenNApi();
        //Make a HTTP post request for authentication and route information
        HttpRequestHandler HH = new HttpRequestHandler();
        String jsonString = HH.postRequest(api,sentData);
        //Convert the response to accordance object
        Gson gson = new Gson();
        AuthenNResponse response = gson.fromJson(jsonString,AuthenNResponse.class);
        String result;
        //Check if the authentication is success
        if(response!=null&&response.getSuccess()){
            //store the response if it is a correct credential
            SharedPreferencesHandler mHandler = new SharedPreferencesHandler(context);
            mHandler.setNewAuthenNResponse(jsonString,username,password);
            result = context.getString(R.string.login_success);
        }
        else if (response==null){
            result = context.getString(R.string.connection_error);
        }
        else{
            result = context.getString(R.string.login_fail);
        }
        return result;

    }
}
