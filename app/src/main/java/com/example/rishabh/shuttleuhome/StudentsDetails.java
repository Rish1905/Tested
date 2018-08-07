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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

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

    //Waiting Time Variable
    private int waitingTime = 0;

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
                            data.child("SUID").getValue(String.class),data.child("Time").getValue(String.class),data.getKey());
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
                    students.put(passengersList.get(i).getSUID(),studentDetail);
                }
                dataObject.put("Students",students);

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.getKey().equals("Users"))
                        dataObject.put("DriverName",data.child("Drivers").child(uid).child("Name").getValue().toString());
                    else if(data.getKey().equals("SupervisorName"))
                        dataObject.put("SupervisorName",data.getValue().toString());
                }

                myRef.child("CurrentTrip").setValue(dataObject);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateStatusOfPassengers(){
        for(int i = 0 ; i < passengersList.size() ; i++){
            myRef.child("Booking").child(passengersList.get(i).getUID()).child("Status").setValue("travelling");
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

                                myRef.child("CurrentTrip").removeValue();

                                myRef.child("Booking").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot data:dataSnapshot.getChildren()){
                                            String status = data.child("Status").getValue(String.class);
                                            if(status.equals("travelling") || status.equals("dropped"))
                                                myRef.child("Booking").child(data.getKey()).removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                myRef.child("WaitingTime").setValue(0);
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
            url+="|"+passengersAddress.get(i).getAddress()+" Syracuse,NY";
        }
        url += "&key=" + key;
        Log.i("URL",url);
        StringRequest request =  new StringRequest(url, new Response.Listener<String>() {
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

                        for(int i = 0 ; i < jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").length() ; i++){
                            String tempWaitTime = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                                    .getJSONObject(i).getJSONObject("duration").getString("text");
                            waitingTime += Integer.parseInt(tempWaitTime.split(" ")[0]);
                        }
                        myRef.child("WaitingTime").setValue(waitingTime);

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
                Toast.makeText(StudentsDetails.this, "Check the internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void calculateReaminingWaitingTime(String origin){

        if(!origin.equals("43.039563,-76.131628"))
            origin += " Syracuse,NY";

        String url = "https://maps.googleapis.com/maps/api/directions/json?"; //Google Direction API
        url += "origin="+origin+"&"; //Origin DPS Office
        url += "destination=43.039563,-76.131628"; //Destination DPS office
        if(passengersAddress.size() != 0)
            url += "&waypoints=optimize:true"; //Waypoints optimization True

        for(int i = 0; i < passengersAddress.size() ; i++){
            url+="|"+passengersAddress.get(i).getAddress()+" Syracuse,NY";
        }
        url += "&key=" + key;

        StringRequest request =  new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.get("status").equals("OK")){
                        waitingTime = 0;
                        for(int i = 0 ; i < jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").length() ; i++){
                            String tempWaitTime = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                                    .getJSONObject(i).getJSONObject("duration").getString("text");
                            waitingTime += Integer.parseInt(tempWaitTime.split(" ")[0]);
                        }
                        myRef.child("WaitingTime").setValue(waitingTime);
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
                Toast.makeText(StudentsDetails.this, "Check the internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

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
                    myRef.child("Booking").child(passengersList.get(position).getUID()).child("Status").setValue("dropped");
                    calculateReaminingWaitingTime(passengersList.get(position).getAddress());
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
                    if(orderOfPassengerDrop.isEmpty())
                        calculateReaminingWaitingTime("43.039563,-76.131628");
                    else {
                        for(int i = 0; i < passengersList.size() ; i++){
                            if(passengersList.get(i).getSUID().equals(orderOfPassengerDrop.get(orderOfPassengerDrop.size()-1))){
                                calculateReaminingWaitingTime(passengersList.get(i).getAddress());
                                break;
                            }
                        }
                    }
                    myRef.child("Booking").child(passengersList.get(position).getUID()).child("Status").setValue("travelling");
                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            alertDialog.show();
        }
    }
}
