package com.example.rishabh.shuttleuhome;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class StudentsDetails extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    ArrayList<DisplayListviewUserDetails> arrayList = new ArrayList<DisplayListviewUserDetails>();
    CustomAdapter arrayAdapter;
    ListView listView;
    SharedPreferences sharedPreferences;

    public void dialog(final int position, final View view){

        final CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.textView1);

        if(!textView.isChecked()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Select a Option");

            alertDialog.setMessage("Drop or Navigate    ");

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Drop", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    textView.setChecked(true);
                    Toast.makeText(StudentsDetails.this, arrayList.get(position).getName().toString(), Toast.LENGTH_SHORT).show();

                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {


                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Navigate", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    sharedPreferences.edit().putString("address", arrayList.get(position).getAddress());
                    String address = arrayList.get(position).getAddress();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_details);

        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new CustomAdapter(this,R.layout.adapter_layout_view,arrayList);
        listView.setAdapter(arrayAdapter);
        sharedPreferences = this.getSharedPreferences("com.example.rishabh.shuttleuhome", Context.MODE_PRIVATE);

        myRef.child("Booking").orderByChild("Status").equalTo("waiting").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String address = dataSnapshot.child("Address").getValue(String.class);
                String name = dataSnapshot.child("Name").getValue(String.class);
                DisplayListviewUserDetails data = new DisplayListviewUserDetails(name,address);
                arrayList.add(data);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(position, view);
            }
        });
    }
}
