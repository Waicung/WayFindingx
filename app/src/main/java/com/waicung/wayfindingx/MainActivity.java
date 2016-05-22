package com.waicung.wayfindingx;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.waicung.wayfindingx.handlers.DatabaseHandler;
import com.waicung.wayfindingx.handlers.SharedPreferencesHandler;
import com.waicung.wayfindingx.models.AuthenNResponse;
import com.waicung.wayfindingx.models.LocationRecord;
import com.waicung.wayfindingx.models.Point;
import com.waicung.wayfindingx.models.Route;
import com.waicung.wayfindingx.web_clients.GGRequestAsyncTask;
import com.waicung.wayfindingx.web_clients.UploadDataAsyncTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
        UserInfoFragment.OnFragmentInteractionListener, ExperimentFragment.OnListFragmentInteractionListener,
        FeedbackFragment.OnFragmentInteractionListener, NoticeFragment.NoticeDialogListener,
        MainActivityFragment.OnReuploadListener {


    private final int STAGE_START = 0;
    private final int STAGE_LOGIN = 1;
    private final int STAGE_USER_INFO = 2;
    private final int STAGE_EXPERIMENT = 3;
    private int status;
    private MenuItem action_login;
    private Fragment contentFragment;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private static final String TAG = "MainActivity";
    private boolean loginStatus;
    private int statusCode;
    private GPStrackingService tService; // tracing service
    private boolean mBound; // whether the service is bound
    private int currentStep = 1;
    private int lastStep;
    private int route_id;
    private int assignment_id;

    private ServiceConnection serviceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            //bound to LocalService, cast the IBinder and get LocalService instance
            GPStrackingService.LocalBinder binder = (GPStrackingService.LocalBinder) service;
            tService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferencesHandler = new SharedPreferencesHandler(this);
        status = STAGE_START;
        setLoginStatus();
        if (loginStatus) {
            if (statusCode == 1110) {
                status = STAGE_EXPERIMENT;
                showExperiment();
            } else if (statusCode == 1000) {
                uploadRoute();
            } else {
                String warning = statusCodeToMessage(statusCode);
                Snackbar snackBar = Snackbar.make(findViewById(R.id.main_container), warning, Snackbar.LENGTH_LONG);
                snackBar.show();
            }
        } else {
            status = STAGE_START;
            showWelcome();
        }

    }

    private void uploadRoute() {
        setRouteInfo();
        Point start_point = sharedPreferencesHandler.getAuthenNResponse().getStart();
        Point end_point = sharedPreferencesHandler.getAuthenNResponse().getEnd();
        try {
            Route route = (Route) new GGRequestAsyncTask(this).execute(start_point, end_point).get();
            new UploadDataAsyncTask(this).execute("route", route_id, route);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean uploadResult() {
        DatabaseHandler DB = new DatabaseHandler(this);
        setRouteInfo();
        ArrayList<LocationRecord> locations = DB.getData();
        try {
            String result = (String) new UploadDataAsyncTask(this).execute("location", assignment_id, locations).get();
            return successUpload(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean successUpload(String result) {
        Gson gson = new Gson();
        if (result == null || result == "") {
            return false;
        } else {
            AuthenNResponse response = gson.fromJson(result, AuthenNResponse.class);
            return response.getSuccess();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        action_login = menu.findItem(R.id.action_login);
        if (loginStatus) {
            action_login.setIcon(R.drawable.ic_user);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_login) {
            if (!loginStatus) {
                Log.i(TAG, "onOptionsItemSelected: action_login");
                showLogin();
                status = STAGE_LOGIN;
            } else {
                Log.i(TAG, "onOptionsItemSelected: action_user_info");
                showUserInfo();
                status = STAGE_USER_INFO;
            }
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    //listener method for LoginFragment
    //When user click login
    public void onLogin(int result) {
        if (result == 0) {
            //login failed
        }
        if (result == 1) {
            //login success
            action_login.setIcon(R.drawable.ic_user);
            status = STAGE_EXPERIMENT;

            int statusCode = sharedPreferencesHandler.getStatusCode();
            if (statusCode == 1110) {
                //show experiment fragment
                showExperiment();

            } else {
                showWelcome();
                String warning = statusCodeToMessage(statusCode);
                Snackbar snackBar = Snackbar.make(findViewById(R.id.main_container), warning, Snackbar.LENGTH_LONG);
                snackBar.show();
            }
        }
        setLoginStatus();
    }

    @Override
    //listener for LoginFragment
    //When user click cancel login
    public void onCancel() {
        showWelcome();
    }

    @Override
    public void onLogout() {
        sharedPreferencesHandler.clearValues();
        action_login.setIcon(R.drawable.ic_new_user);
        showWelcome();
        setLoginStatus();

    }

    @Override
    public void startExperiment() {
        // TODO: 08/05/2016 bind tracingService
        Intent tracingIntent = new Intent(getApplicationContext(), GPStrackingService.class);
        bindService(tracingIntent, serviceConn, Context.BIND_AUTO_CREATE);
        setRouteInfo();
        lastStep = sharedPreferencesHandler.getSteps().size();

    }

    @Override
    public void onAchieve(int step) {
        // TODO: 08/05/2016 move to next step
        if (step == lastStep) {
            showNotice("Are you arrived at destination?");
        } else {
            tService.setStep(currentStep+=1);
            refreshExperiment();
        }
    }

    private void showNotice(String message) {
        NoticeFragment dialog = new NoticeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "NoticeFragment");
    }

    @Override
    public void onFeedBack() {
        // TODO: 08/05/2016 show feedback fragment
        showFeedback();
    }

    @Override
    public void onDialogPositiveClick(String event) {
        // TODO: 08/05/2016 record and start next steps
        tService.setLog(currentStep+=1, event);
        refreshExperiment();

    }

    @Override
    public void onDialogNegativeClick(String event) {
        endExperiment();

    }

    //use new fragment for content
    public void newFragmentContent(Fragment fragment, boolean backstack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_fragment, fragment, "Experiment");
        if (backstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }


    //convert status from Authentication sharedValues to warning message
    public String statusCodeToMessage(int statusCode) {
        String message;
        switch (statusCode) {
            case 0000:
                message = "Please contact administrator";
                break;
            case 1100:
                message = "Instructions are preparing";
                break;
            case 1111:
                message = "All test finished";
                break;
            default:
                message = "unknown error";
                break;
        }
        return message;
    }

    public void setFragment() {
        if (status == STAGE_EXPERIMENT) {
            showExperiment();
        } else if (status == STAGE_START) {
            showWelcome();
        }
    }

    //show thank you at the end of the experiment
    private void showMessage(int usage) {
        contentFragment = new MainActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("usage", usage);
        contentFragment.setArguments(bundle);
        newFragmentContent(contentFragment, false);
    }

    //display user info
    private void showUserInfo() {
        contentFragment = new UserInfoFragment();
        newFragmentContent(contentFragment, true);

    }

    private void refreshExperiment() {
        contentFragment = new ExperimentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("currentStep", currentStep);
        bundle.putInt("stage", 1);
        contentFragment.setArguments(bundle);
        newFragmentContent(contentFragment, false);

    }

    private void showExperiment() {
        contentFragment = new ExperimentFragment();
        newFragmentContent(contentFragment, false);
    }

    private void showLogin() {
        contentFragment = new LoginFragment();
        newFragmentContent(contentFragment, true);
    }

    private void showWelcome() {
        contentFragment = new MainActivityFragment();
        newFragmentContent(contentFragment, false);
    }

    private void showFeedback() {
        FeedbackFragment dialog = new FeedbackFragment();
        dialog.show(getFragmentManager(), "FeedbackFragment");
    }

    private void setLoginStatus() {
        //get the updated value set
        sharedPreferencesHandler.update();
        try {
            statusCode = sharedPreferencesHandler.getStatusCode();
            loginStatus = true;
        } catch (NullPointerException e) {
            loginStatus = false;
        }
    }

    private void setRouteInfo() {
        sharedPreferencesHandler.update();
        route_id = Integer.parseInt(sharedPreferencesHandler.getAuthenNResponse().getRoute_id());
        assignment_id = Integer.parseInt(sharedPreferencesHandler.getAuthenNResponse().getAssignment_id());

    }


    private void setIcon() {
        if (loginStatus) {
            action_login.setIcon(R.drawable.ic_user);
        } else {
            action_login.setIcon(R.drawable.ic_new_user);
        }
    }


    //listener for @NoticeFragment
    @Override
    public void onDialogPositiveClick() {
        endExperiment();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    private void endExperiment() {
        tService.setStep(currentStep);
        unbindService(serviceConn);
        if (uploadResult()) {
            //successfully upload
            showMessage(3);
            sharedPreferencesHandler.clearValues();
        } else {
            showMessage(2);
        }

    }

    @Override
    public void onReuppload() {
        if (uploadResult()) {
            //successfully upload
            showMessage(3);
            sharedPreferencesHandler.clearValues();
        } else {
            showMessage(2);
        }
    }

}
