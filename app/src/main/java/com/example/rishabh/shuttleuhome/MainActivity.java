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
                                            if(temp.getKey().equals(mAuth.getUid())){
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

   /* private void showDialog()
    {
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
    } */

   /* public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            return true;
        } else {
            showDialog();
            return false;
        }
    } */

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
        // isConnected(this);
          // dummyData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //email.setText("");
        //password.setText("");
    }
}
