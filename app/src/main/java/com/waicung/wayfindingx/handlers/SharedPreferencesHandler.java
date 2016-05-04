package com.waicung.wayfindingx.handlers;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.waicung.wayfindingx.R;
import com.waicung.wayfindingx.models.AuthenNResponse;


/**
 * Created by waicung on 04/05/2016.
 */
public class SharedPreferencesHandler {
    private Context context;
    private SharedPreferences mSharedPreference;

    public  SharedPreferencesHandler(Context context){
        this.context = context;
        mSharedPreference = context.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
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




}
