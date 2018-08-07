package com.example.rishabh.shuttleuhome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    //Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //Number of passengers limit
    private int passengerLimit = 8;

    //Total Number of passerngers requested by the driver
    private int numberOfPassengers = 0;

    //Total number of passenger waiting
    private int totalNumberOfPassengerWaiting = 0;

    //Textfield for total number of passengers requested by the driver
    private EditText numberOfPassengerRequested;

    //TextField for driver's name
    private TextView driverName;

    //Button for fetching data of students
    private Button requestPassengersButton;

    //Button for Signout
    private Button signout;

    //TextView for number of passengers waiting
    private TextView numberOfPassengerWaiting;

    //SUID List for waiting passengers
    List<String> SUID = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        init();
        fetchDriverName();
        fetchNumberOfPassengersWaiting();
        addSignoutButtonFunction();
        addRequestPassengersButtonFunction();
    }

    //Function for initialize the variables declare above
    private void init(){
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        numberOfPassengerRequested = (EditText) findViewById(R.id.numberOfPassengerRequested);
        requestPassengersButton = (Button) findViewById(R.id.requestPassengersButton);
        signout = (Button) findViewById(R.id.signout);
        driverName = (TextView) findViewById(R.id.driverName);
        numberOfPassengerWaiting = (TextView) findViewById(R.id.numberOfPassengerWaiting);

        numberOfPassengerWaiting.setText(""+totalNumberOfPassengerWaiting);
    }

    //Fetch Driver Name
    private void fetchDriverName(){
        myRef.child("Users").child("Drivers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot temp: dataSnapshot.getChildren()){
                    if(temp.getKey().equals(mAuth.getUid())){
                        driverName.setText("Hello, "+temp.child("Name").getValue(String.class));
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Fetching the number of passengers waiting for the shuttle
    private void fetchNumberOfPassengersWaiting(){

        myRef.child("Booking").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.child("Status").getValue(String.class).equals("waiting")) {
                    totalNumberOfPassengerWaiting++;
                    SUID.add(dataSnapshot.child("SUID").getValue(String.class));
                    numberOfPassengerWaiting.setText("" + totalNumberOfPassengerWaiting);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String tempSUID = dataSnapshot.child("SUID").getValue(String.class);
                if(SUID.contains(tempSUID)) {
                    SUID.remove(tempSUID);
                    totalNumberOfPassengerWaiting--;
                    numberOfPassengerWaiting.setText(""+totalNumberOfPassengerWaiting);
                }
                else if(dataSnapshot.child("Status").getValue(String.class).equals("waiting")){
                    totalNumberOfPassengerWaiting++;
                    SUID.add(tempSUID);
                    numberOfPassengerWaiting.setText(""+totalNumberOfPassengerWaiting);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String tempSUID = dataSnapshot.child("SUID").getValue(String.class);
                if(SUID.contains(tempSUID)){
                    SUID.remove(tempSUID);
                    totalNumberOfPassengerWaiting--;
                    numberOfPassengerWaiting.setText(""+totalNumberOfPassengerWaiting);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String tempSUID = dataSnapshot.child("SUID").getValue(String.class);
                if(SUID.contains(tempSUID)){
                    SUID.remove(tempSUID);
                    totalNumberOfPassengerWaiting--;
                    numberOfPassengerWaiting.setText(""+totalNumberOfPassengerWaiting);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Dashboard.this, "No Such Table", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Function for sigout button
    public void addSignoutButtonFunction(){
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                    Toast.makeText(Dashboard.this, "User Singout", Toast.LENGTH_SHORT).show();
                }
                else
                    Log.i("info","Success");
                finish();
            }
        });
    }

    public void addRequestPassengersButtonFunction(){
        requestPassengersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfPassengerRequested.getText().toString().isEmpty()){
                    Toast.makeText(Dashboard.this, "Please Enter number of passengers", Toast.LENGTH_SHORT).show();
                    return;
                }
                numberOfPassengers = Integer.parseInt(numberOfPassengerRequested.getText().toString());
                if(numberOfPassengers > totalNumberOfPassengerWaiting){
                    Toast.makeText(Dashboard.this, "Number of Passengers are less than requested", Toast.LENGTH_SHORT).show();
                    numberOfPassengerRequested.setText("");
                    numberOfPassengers = 0;
                    return;
                }
                else{
                    Intent intent = new Intent(Dashboard.this,StudentsDetails.class);
                    intent.putExtra("Passengers",numberOfPassengers);
                    startActivity(intent);
                }
            }
        });
    }
}
