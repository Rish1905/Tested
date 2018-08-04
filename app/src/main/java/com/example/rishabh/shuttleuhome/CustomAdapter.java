package com.example.rishabh.shuttleuhome;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<DisplayListviewUserDetails> {

    private Context mContext;
    private int mResource;

    public CustomAdapter (Context context, int resource, ArrayList<DisplayListviewUserDetails> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        String name = getItem(position).getName();
        String address = getItem(position).getAddress();

        //Create the person object with the information
        DisplayListviewUserDetails person = new DisplayListviewUserDetails(name,address);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        CheckedTextView tvAddress = (CheckedTextView) convertView.findViewById(R.id.textView1);
        TextView tvName = (TextView) convertView.findViewById(R.id.textView2);

        tvName.setText(name);
        tvAddress.setText(address);

        return convertView;
    }
}
