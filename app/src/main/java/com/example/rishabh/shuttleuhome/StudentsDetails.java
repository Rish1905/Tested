package com.example.rishabh.shuttleuhome;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StudentsDetails extends AppCompatActivity {

    //Firebase Variables
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    //ArrayList for storeing of Passengers details
    private ArrayList<DisplayListviewUserDetails> passengersList;

    //Listview for passengers
    private ListView passengersListview;

    //Custom Adapter for Listview
    private CustomAdapter arrayAdapter;

    //Number of passengers Requested
    private int numberOfPassengers;

    //SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_details);

        init();
        collectIntentData();
        fetchPassengersToListview();
        updateStatusOfPassengers();
        createCurrentTripFirebase();

        passengersListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(position, view);
            }
        });

       // sharedPreferences = this.getSharedPreferences("com.example.rishabh.shuttleuhome", Context.MODE_PRIVATE);
    }

    private void init(){

        //Firebase variables initialize
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //Initialize variables for ListView
        passengersList = new ArrayList<DisplayListviewUserDetails>();
        passengersListview = (ListView) findViewById(R.id.passengersListview);
        arrayAdapter = new CustomAdapter(this,R.layout.adapter_layout_view,passengersList);
        passengersListview.setAdapter(arrayAdapter);

    }

    private void collectIntentData(){
        Intent intent = getIntent();
        numberOfPassengers = intent.getIntExtra("Passengers",0);
    }

    private void fetchPassengersToListview(){

        myRef.child("Booking").orderByChild("Status").equalTo("waiting").limitToFirst(numberOfPassengers).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    DisplayListviewUserDetails temp = new DisplayListviewUserDetails(data.child("Address").getValue(String.class),
                            data.child("Name").getValue(String.class),data.child("Date").getValue(String.class),
                            data.child("SUID").getValue(String.class),data.child("Time").getValue(String.class));
                    passengersList.add(temp);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateStatusOfPassengers(){

    }

    private void createCurrentTripFirebase(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String departureTime = sdf.format(new Date());

        String date = passengersList.get(0).getDate();

        String uid = mAuth.getUid();
        String driverName = myRef.child("Users").child("Drivers").child(uid).child("Name").toString();
        try{
            Log.i("info",driverName);
        }
        catch (Exception e){

        }

    }

    public void dialog(final int position, final View view){

        final CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.textView1);

        if(!textView.isChecked()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Select a Option");

            alertDialog.setMessage("Drop or Navigate    ");

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Drop", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    textView.setChecked(true);
                    Toast.makeText(StudentsDetails.this, passengersList.get(position).getName().toString(), Toast.LENGTH_SHORT).show();

                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {


                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Navigate", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    //sharedPreferences.edit().putString("address", arrayList.get(position).getAddress());
                    String address = passengersList.get(position).getAddress();
                    String[] split = address.split(" ");
                    address = "";
                    for (int i = 0; i < split.length - 1; i++) {
                        address = address + split[i] + "+";
                    }
                    address = address + split[split.length - 1];
                    Log.i("info", address);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }
            });

            alertDialog.show();
        }
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Dialog Button");

            alertDialog.setMessage("This is a three-button dialog!");

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel Drop", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    textView.setChecked(false);

                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    //...

                }
            });

            alertDialog.show();
        }
    }
}
