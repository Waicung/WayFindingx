package com.waicung.wayfindingx;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.TextView;
import com.waicung.wayfindingx.ExperimentFragment.*;

import com.waicung.wayfindingx.models.Step;

import java.util.ArrayList;

/**
 * Created by waicung on 08/05/2016.
 */
public class CustomListAdapter extends ArrayAdapter {
    private int currentStep;
    private final OnListFragmentInteractionListener mListener;
    String TAG = "cAdapter";

    public CustomListAdapter(Context context, ArrayList<Step> steps, int currentStep,
                             ExperimentFragment.OnListFragmentInteractionListener listener) {
        super(context, R.layout.fragment_experiment_item, steps);
        this.mListener = listener;
        this.currentStep = currentStep;
        Log.i(TAG, "CustomListAdapter: " + currentStep);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Step step = (Step) getItem(position);
        int step_id = step.getStep_number();
        int lastStep = getCount();
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_experiment_item,parent,false);
            viewHolder = new ViewHolder(convertView);
            //convertView.setTag(viewHolder);

        }else {
            //viewHolder = (ViewHolder) convertView.getTag();
            viewHolder = new ViewHolder(convertView);
        }

        if(step_id==this.currentStep){
            Log.i(TAG, "step show: " + step_id);
            viewHolder.mView.setVisibility(View.VISIBLE);
            viewHolder.mPanel.setVisibility(View.VISIBLE);
        }
        else {
            if (step_id > currentStep+1) {
                Log.i(TAG, "Hide all: " + step_id);
                viewHolder.mView.setVisibility(View.GONE);
            }
            else {
                Log.i(TAG, "position hide: " + step_id);
                viewHolder.mView.setVisibility(View.VISIBLE);
                viewHolder.mPanel.setVisibility(View.GONE);
            }
        }

        if(step_id == lastStep){
            viewHolder.feedback_button.setVisibility(View.GONE);
        }

        viewHolder.mInstruction.setText(step.getInstruction());

        viewHolder.achieve_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mListener.onAchieve(currentStep);
            }
        });

        viewHolder.feedback_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFeedBack();

            }
        });
        return convertView;
    }

    //View container class
    public class ViewHolder{
        View mView;
        TextView mInstruction;
        View mPanel;
        Button achieve_button;
        Button feedback_button;

        public ViewHolder(View view) {
            mView = view;
            mInstruction = (TextView) view.findViewById(R.id.instruction_textView);
            mPanel = view.findViewById(R.id.panel);
            achieve_button = (Button) view.findViewById(R.id.achieve_button);
            feedback_button = (Button) view.findViewById(R.id.feedback_button);
        }
    }
}