package com.example.rishabh.shuttleuhome;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public DatabaseReference myRef;
    EditText email; //Email variable
    EditText password; // Password variable

    //Login Button click
   public void loginButtonClick(View view){
        Log.i("info","Button Click");
        if(!isConnected(this))
            return;
        String loginEmail = email.getText().toString();
        String loginPassword = password.getText().toString();

        if(loginEmail.isEmpty()){
            Toast.makeText(this, "Email Can't be Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(loginPassword.isEmpty()){
            Toast.makeText(this, "Password Can't be Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                myRef.child("Users").child("Drivers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        int check = 0;
                                        for(DataSnapshot temp: dataSnapshot.getChildren()){
                                            if(temp.child("UserId").getValue(String.class).equals(mAuth.getUid())){
                                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(),Dashboard.class);
                                                startActivity(intent);
                                                check = 1;
                                            }
                                        }
                                        if(check == 0)
                                            Toast.makeText(MainActivity.this, "Email and Password not linked to driver's account", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                            } else {
                                Toast.makeText(MainActivity.this, "Incorrect LoginID and Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Update Google Play", Toast.LENGTH_SHORT).show();
        }

    }

   private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to Cellular Data")
                .setCancelable(false)
                .setPositiveButton("Connect to Intenet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

   public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            showDialog();
            return false;
        }
    }

       @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declare Variable
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        //Initialize Variable
        email.setText("driver@dps.com");
        password.setText("123456");

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //Check if Network Connection is open or not.
          // dummyData();

           /*Map<String,String> map = new HashMap<String,String>();
           map.put("Name","Rishabh Agrawal");
           map.put("SUID","850490879");
           map.put("Date","08-07-2018");
           map.put("Status","waiting");
           map.put("Time","02:13:12");
           map.put("Address","205 Lexington ave");
           myRef.child("Booking").push().setValue(map);

           Map<String,String> map1 = new HashMap<String,String>();
           map1.put("Name","Mukul Agrawal");
           map1.put("SUID","850490878");
           map1.put("Date","08-07-2018");
           map1.put("Status","waiting");
           map1.put("Time","02:13:13");
           map1.put("Address","141 Lexington ave");
           myRef.child("Booking").push().setValue(map1);

           Map<String,String> map2 = new HashMap<String,String>();
           map2.put("Name","Nitish Agrawal");
           map2.put("SUID","850490877");
           map2.put("Date","08-07-2018");
           map2.put("Status","waiting");
           map2.put("Time","02:13:14");
           map2.put("Address","135 Lexington ave");
           myRef.child("Booking").push().setValue(map2);

           Map<String,String> map3 = new HashMap<String,String>();
           map3.put("Name","Arsheen Agrawal");
           map3.put("SUID","850490876");
           map3.put("Date","08-07-2018");
           map3.put("Status","waiting");
           map3.put("Time","02:13:15");
           map3.put("Address","312 westcott ave");
           myRef.child("Booking").push().setValue(map3);

           Map<String,String> map4 = new HashMap<String,String>();
           map4.put("Name","Aashawaree Agrawal");
           map4.put("SUID","850490875");
           map4.put("Date","08-07-2018");
           map4.put("Status","waiting");
           map4.put("Time","02:13:16");
           map4.put("Address","1020 westcott ave");
           myRef.child("Booking").push().setValue(map4);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        //email.setText("");
        //password.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
