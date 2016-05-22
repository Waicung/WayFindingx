package com.waicung.wayfindingx.handlers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by waicung on 04/05/2016.
 */
public class HttpRequestHandler {
    private URL requestUrl;
    private String sentData;
    private HttpURLConnection httpConn;
    private final String TAG = "HttpRequestHandler";

    public HttpRequestHandler(){}

    public String getRequest(URL url_data){
        StringBuilder Str = new StringBuilder();
        try {
            httpConn = (HttpURLConnection) url_data.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line = in.readLine();
            while(line!=null){
                Str.append(line+'\n');
                line=in.readLine();
            }
        }
        catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "getRequest: " + Str.toString());
        return Str.toString();
    }

    public String postRequest(String api, String parameters){
        try {
            this.requestUrl = new URL(api);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }
        sentData = parameters;
        String response;
        StringBuilder Str = new StringBuilder();
        try{
            HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
            //set properties of the connection
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(1000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            //set post data(change it to byte data)
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(sentData);
            wr.flush();
            wr.close();
            //get response from the connection
            Log.i(TAG, Integer.valueOf(conn.getResponseCode()).toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            //read response as string
            while(line!=null){
                Str.append(line + '\n');
                line=reader.readLine();
            }
        }
        catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
        response = Str.toString();
        Log.i(TAG , response);
        return response;
    }

}
