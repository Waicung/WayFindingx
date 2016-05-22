package com.waicung.wayfindingx;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final int TYPE_WELCOME = 1;
    private static final int TYPE_UPLOAD = 2;
    private static final int TYPE_THANKS = 3;
    private int fragment_usage;
    private TextView message_tv;
    OnReuploadListener mListener;
    private static final String TAG = "MainActivityFragment";

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // TODO: 07/05/2016 choose layout by usage
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        message_tv = (TextView) rootView.findViewById(R.id.main_textView);
        Button upload_button = (Button) rootView.findViewById(R.id.upload_button);
        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onReuppload();
            }
        });
        if (fragment_usage == TYPE_THANKS) {
            message_tv.setText(getString(R.string.thank_you));
        }
        if (fragment_usage == TYPE_UPLOAD){
            message_tv.setVisibility(View.GONE);
            upload_button.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        try {
            fragment_usage = args.getInt("usage");
        }catch (NullPointerException e){
            Log.e(TAG, "setArguments: ");
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnReuploadListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OnReuploadListener");
        }
    }

    public interface OnReuploadListener {
        void onReuppload();
    }

}
