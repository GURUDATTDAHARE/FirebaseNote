package com.gurudattdahare.notesfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
  private Button getStartButton;
  private FirebaseAuth firebaseAuth;
  private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        getStartButton=findViewById(R.id.startButton);
        getStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
              firebaseAuth=FirebaseAuth.getInstance();
                   user  =firebaseAuth.getCurrentUser();
                   if(user!=null){
                       startActivity(new Intent(MainActivity.this,JournalListActivity.class));
                       finish();
                   }
    }
}