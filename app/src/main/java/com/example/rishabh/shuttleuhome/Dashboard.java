package com.example.rishabh.shuttleuhome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dashboard extends AppCompatActivity {

    //Firebase Variables
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public DatabaseReference myRef;

    //Number of passengers limit
    int passengerLimit = 8;

    //Textfield for total number of passengers requested by the driver
    public EditText numbers;

    //Total Number of passerngers requested by the driver
    int numberOfPassengers = 0;

    public void buttonClick(View view){
        if(mAuth.getCurrentUser() != null)
            mAuth.signOut();
        else
            Log.i("info","Success");
        finish();
    }

    public void fetchData(View view){

        numberOfPassengers = Integer.parseInt(numbers.getText().toString());
        if(passengerLimit > numberOfPassengers)
            Toast.makeText(this, "Please enter within the limit of 8", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(),StudentsDetails.class);
        intent.putExtra("numberOfPassenger",numberOfPassengers);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Booking");

        numbers = (EditText) findViewById(R.id.numberOfPassengerRequested);
    }


}
