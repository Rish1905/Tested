package com.example.rishabh.shuttleuhome;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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

    //Google API Key
    private final String key = "AIzaSyCoL4g2nqEaCEWMnovBFVbKqgOBpnKstqA";

    //Passengers reamining for address
    private ArrayList<DisplayListviewUserDetails> passengersAddress;

    //Order of Passengers drop
    private ArrayList<String> orderOfPassengerDrop;

    //Button for Complete Trip
    private Button completeTrip;

    //Date Format
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    //Time Format
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_details);

        init();
        collectIntentData();
        fetchPassengersToListview(); // internal calling of createCurrentTripFirebase() function and UpdateStatus fucction;
        addCompleteTripButtonFunction();

        passengersListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(position, view);
            }
        });

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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

        passengersAddress = new ArrayList<DisplayListviewUserDetails>();
        orderOfPassengerDrop = new ArrayList<String>();
        completeTrip = (Button) findViewById(R.id.completeTrip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                cancelTrip();
                break;
            case R.id.cancelTrip:
                cancelTrip();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void collectIntentData(){
        Intent intent = getIntent();
        numberOfPassengers = intent.getIntExtra("Passengers",0);
    }

    private void fetchPassengersToListview(){

        //Fetching the passengers from Booking table
        myRef.child("Booking").orderByChild("Status").equalTo("waiting").limitToFirst(numberOfPassengers).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    DisplayListviewUserDetails temp = new DisplayListviewUserDetails(
                            data.child("HouseNo").getValue(String.class)+" "+data.child("StreetAddress").getValue(String.class),
                            data.child("Name").getValue(String.class),data.child("Date").getValue(String.class),
                            data.child("SUID").getValue(String.class),data.child("Time").getValue(String.class),data.getKey(),
                            data.child("SupervisorName").getValue(String.class));
                    passengersList.add(temp);
                    passengersAddress.add(temp);
                }
                googleAPIforCallingWaitTime("43.039563,-76.131628");
                createCurrentTripFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createCurrentTripFirebase(){

        //Fetch Driver's Name
        final String uid = mAuth.getUid();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String,Object> dataObject = new HashMap<String,Object>();

                //Fetch Date
                dataObject.put("Date",dateFormat.format(new Date()));

                //Fetch Time
                dataObject.put("DepartureTime",timeFormat.format(new Date()));

                dataObject.put("NumberOfPassengers",passengersList.size());

                Map<String,Object> students = new HashMap<String,Object>();
                for(int i = 0; i < passengersList.size() ; i++){
                    Map<String,String> studentDetail = new HashMap<String,String>();
                    studentDetail.put("Address",passengersList.get(i).getAddress());
                    studentDetail.put("Date",passengersList.get(i).getDate());
                    studentDetail.put("Name",passengersList.get(i).getName());
                    studentDetail.put("SigninTime",passengersList.get(i).getTime());
                    studentDetail.put("SupervisorName",passengersList.get(i).getSupervisorName());
                    students.put(passengersList.get(i).getSUID(),studentDetail);
                }
                dataObject.put("Students",students);

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.getKey().equals("Users"))
                        for(DataSnapshot data1 : data.child("Drivers").getChildren()){
                            if(data1.child("UserId").equals(mAuth.getUid()))
                                dataObject.put("DriverName",data1.child("Name").getValue(String.class));
                        }
                    else if(data.getKey().equals("SupervisorName"))
                        dataObject.put("SupervisorName",data.child("Name").getValue().toString());
                }

                myRef.child("CurrentTrip").setValue(dataObject); //Push Current Trip
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateStatusOfPassengers(){
        for(int i = 0 ; i < passengersList.size() ; i++){
            myRef.child("Booking").child(passengersList.get(i).getUID()).child("Status").setValue("Travelling");
        }
    }

    private void addCompleteTripButtonFunction(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        completeTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.setTitle("Are you sure !");

                if(passengersAddress.size() != 0)
                    alertDialog.setMessage("Some passengers are pending to be dropped, Are you sure you want to complete trip ?");
                else
                    alertDialog.setMessage("Do you want to complete Trip ?");

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Complete Trip", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        if(passengersAddress.size() != 0){
                            for(int i = 0 ; i < passengersAddress.size() ; i++){
                                myRef.child("CurrentTrip").child("Students").child(passengersAddress.get(i).getSUID()).child("DropOffTime").setValue(timeFormat.format(new Date()));
                            }
                        }

                        //Database Insertion
                        myRef.child("CurrentTrip").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String,Object> databaseObject = new HashMap<String,Object>();
                                try{
                                    String date = dataSnapshot.child("Date").getValue(String.class);
                                    databaseObject = (Map<String,Object>)dataSnapshot.getValue();
                                    databaseObject.put("ReturnTime",timeFormat.format(new Date()));
                                    databaseObject.remove("Date");
                                    myRef.child("Database").child(date).push().setValue(databaseObject);
                                }
                                catch(Exception e){
                                    Toast.makeText(StudentsDetails.this, "Something Went Wrong !", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                //Delete Current Trip
                                myRef.child("CurrentTrip").removeValue();

                                myRef.child("Booking").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot data:dataSnapshot.getChildren()){
                                            String status = data.child("Status").getValue(String.class);
                                            if(status.equals("Travelling") || status.equals("Safely Transported"))
                                                myRef.child("Booking").child(data.getKey()).removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void googleAPIforCallingWaitTime(String origin){
        String url = "https://maps.googleapis.com/maps/api/directions/json?"; //Google Direction API
        url += "origin="+origin+"&"; //Origin DPS Office
        url += "destination=43.039563,-76.131628&"; //Destination DPS office
        url += "waypoints=optimize:true"; //Waypoints optimization True

        for(int i = 0; i < passengersAddress.size() ; i++){
            String add = passengersAddress.get(i).getAddress().trim().replace(" ","+");
            url+="|"+add+"+Syracuse,NY";
        }
        url += "&key=" + key;
        Log.i("URL",url);
        StringRequest request = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.get("status").equals("OK")){
                        JSONArray waypoint_order = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order");
                        ArrayList<DisplayListviewUserDetails> optimizePassengersList = new ArrayList<DisplayListviewUserDetails>();
                        for(int i = 0 ; i < waypoint_order.length() ; i++){
                            optimizePassengersList.add(passengersList.get(waypoint_order.getInt(i)));
                        }
                        passengersList.clear();
                        passengersList.addAll(optimizePassengersList);
                        arrayAdapter.notifyDataSetChanged();

                        updateStatusOfPassengers();
                    }
                    else{
                        Toast.makeText(StudentsDetails.this, "Error! Try Again!", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                arrayAdapter.notifyDataSetChanged();
                updateStatusOfPassengers();
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void dialog(final int position, final View view){

        final CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.textView1);

        if(!textView.isChecked()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Select a Option");

            alertDialog.setMessage("Drop or Navigate");

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Drop", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    textView.setChecked(true);

                    //DropoffTime
                    myRef.child("CurrentTrip").child("Students").child(passengersList.get(position).getSUID()).child("DropOffTime").setValue(timeFormat.format(new Date()));
                    orderOfPassengerDrop.add(passengersList.get(position).getSUID());
                    for(int i = 0 ; i < passengersAddress.size() ; i++){
                        if(passengersAddress.get(i).getSUID().equals(passengersList.get(position).getSUID())){
                            passengersAddress.remove(i);
                        }
                    }
                    myRef.child("Booking").child(passengersList.get(position).getUID()).child("Status").setValue("Safely Transported");
                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Navigate", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

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
                    passengersAddress.add(passengersList.get(position));
                    orderOfPassengerDrop.remove(passengersList.get(position).getSUID());
                    myRef.child("Booking").child(passengersList.get(position).getUID()).child("Status").setValue("Travelling");
                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            alertDialog.show();
        }
    }

    private void cancelTrip(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

                alertDialog.setTitle("Are you sure !");

                alertDialog.setMessage("Are you sure you want to cancel current trip?");

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel Trip", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        for(int i = 0 ; i < passengersList.size() ; i++){
                            myRef.child("Booking").child(passengersList.get(i).getUID()).child("Status").setValue("waiting");
                        }

                        myRef.child("CurrentTrip").removeValue();
                        finish();
                    }
                });
                alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        cancelTrip();
    }
}
