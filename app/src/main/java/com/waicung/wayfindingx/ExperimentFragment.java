package com.waicung.wayfindingx;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.waicung.wayfindingx.handlers.SharedPreferencesHandler;
import com.waicung.wayfindingx.models.Step;
import java.util.ArrayList;



/**
 * Created by waicung on 08/05/2016.
 */
public class ExperimentFragment extends Fragment{
    OnListFragmentInteractionListener mListener;
    private int STAGE_START = 0;
    private int STAGE_IN_PROGRESS = 1;
    private int stage;
    private Button start_button;
    private ListView listView;
    private ArrayList<Step> steps;
    private int currentStep=1;
    private ImageView gps_icon;
    CustomListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_experiment,container,false);
        gps_icon = (ImageView) rootView.findViewById(R.id.gps_indicator);
        listView = (ListView) rootView.findViewById(android.R.id.list);
        start_button = (Button) rootView.findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExperiment();
                showInstruction();
                onStartExperiment();
            }
        });
        getSteps();
        if(stage==0){
            start_button.setVisibility(View.VISIBLE);
        }
        else{
            startExperiment();
            showInstruction();
        }
        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        currentStep = args.getInt("currentStep");
        stage = args.getInt("stage");
        super.setArguments(args);
    }

    private void showInstruction(){
        adapter = new CustomListAdapter(getContext(),steps,currentStep,mListener);
        listView.setAdapter(adapter);
        setFocus();
    }

    private void startExperiment() {
        start_button.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        stage = STAGE_IN_PROGRESS;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onStartExperiment() {
        if (mListener != null) {
            mListener.startExperiment();
        }
    }

    private void setFocus(){
        listView.setSelection(currentStep-1);
    }


    private void getSteps(){
        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getContext());
        steps = sharedPreferencesHandler.getSteps();
    }

    public interface OnListFragmentInteractionListener {
        void onAchieve(int step);
        void onFeedBack();
        void startExperiment();
    }

    public void setGpsIndicator(boolean b){
        if(b){
            gps_icon.setVisibility(View.VISIBLE);
        }
        else {
            gps_icon.setVisibility(View.GONE);
        }

    }


}
