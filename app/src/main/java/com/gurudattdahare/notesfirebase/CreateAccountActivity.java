package com.gurudattdahare.notesfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gurudattdahare.notesfirebase.util.JournalApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {
    private AutoCompleteTextView emailview;
    private EditText passwordview;
    private EditText usernameview;
    private Button createButton;
    private ProgressBar progressBar;

    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference collectionReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        emailview=findViewById(R.id.create_email);
        passwordview=findViewById(R.id.create_password);
        usernameview=findViewById(R.id.username_acct);
        createButton=findViewById(R.id.create_email_sign_in_button);
        progressBar=findViewById(R.id.create_acct_progress);

        //fire
        firebaseAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        collectionReference=db.collection("Users");

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user=firebaseAuth.getCurrentUser();

            }
        };

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String email= emailview.getText().toString().trim();
               String password=passwordview.getText().toString().trim();
               String username=usernameview.getText().toString().trim();
  //              Log.d("guru","hi "+email+" "+password+" "+username);

                createUserEmailAccount(email,password,username);
            }
        });


    }
   public void createUserEmailAccount(String email, String password, final String username){
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                            user=firebaseAuth.getCurrentUser();
                        assert user != null;
                        final String currentUserId=user.getUid();
                        //create a user Map for creating a user collection
                        Map<String,String>map=new HashMap<>();
                        map.put("userId",currentUserId);
                        map.put("username",username);
                        //adding username in firestore
                        collectionReference.add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (Objects.requireNonNull(task.getResult()).exists()){
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name=task.getResult().getString("username");
                                    // we can use Intant puteextra but we use a globle singletun

                                            JournalApi journalApi =JournalApi.getInstance();
                                            journalApi.setUserID(currentUserId);
                                            journalApi.setUsername(name);
                                            Intent intent=new Intent(getApplicationContext(),PostJournalActivity.class);
//                                            intent.putExtra("username",name);
//                                            intent.putExtra("userId",user.getUid());
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                      Log.d("alok","erroe: "+e.getMessage());
                            }
                        });

                    }else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(),"retry..",Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else {
            Toast.makeText(getApplicationContext(),"Empty Fields not Allowed",Toast.LENGTH_LONG).show();
        }
   }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}