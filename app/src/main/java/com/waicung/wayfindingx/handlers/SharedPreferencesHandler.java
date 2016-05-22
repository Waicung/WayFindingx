package com.waicung.wayfindingx.handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.waicung.wayfindingx.R;
import com.waicung.wayfindingx.models.AuthenNResponse;
import com.waicung.wayfindingx.models.Step;

import java.util.ArrayList;


/**
 * Created by waicung on 04/05/2016.
 */
public class SharedPreferencesHandler {
    private Context context;
    private SharedPreferences mSharedPreference;
    String TAG = "SharedPreferenceHandler";

    public  SharedPreferencesHandler(Context context){
        this.context = context;
        mSharedPreference = context.getSharedPreferences(context.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
    }

    public SharedPreferences getmSharedPreference(){
        return this.mSharedPreference;
    }

    public AuthenNResponse getAuthenNResponse(){
        String auth = mSharedPreference.getString(context.getString(R.string.preference_authenN_response), null);
        Gson gson = new Gson();
        AuthenNResponse response = gson.fromJson(auth, AuthenNResponse.class);
        return response;
    }

    public void setNewAuthenNResponse(String jsonString, String username, String password){
        Log.i(TAG, "Save response" + jsonString);
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.clear();
        editor.commit();
        editor.putString(context.getString(R.string.preference_authenN_response), jsonString);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
    }

    public int getStatusCode(){
        return getAuthenNResponse().getStatus();
    }

    public void clearValues(){
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.clear();
        editor.commit();
    }


    public SharedPreferencesHandler update() {
        return new SharedPreferencesHandler(this.context);
    }

    public ArrayList<Step> getSteps(){
        return (ArrayList<Step>) getAuthenNResponse().getSteps();
    }
}
