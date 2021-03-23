package com.gurudattdahare.notesfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gurudattdahare.notesfirebase.util.JournalApi;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private  Button createAccountButton;
    private AutoCompleteTextView emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;

    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        loginButton=findViewById(R.id.email_sign_in_button);
        createAccountButton=findViewById(R.id.create_acct_button);
        emailEditText=findViewById(R.id.email);
        passwordEditText=findViewById(R.id.password);
        progressBar=findViewById(R.id.login_progress);

        firebaseAuth=FirebaseAuth.getInstance();

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CreateAccountActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email=emailEditText.getText().toString().trim();
                String password =passwordEditText.getText().toString().trim();

                loginfunction(email,password);
            }
        });
    }

    private void loginfunction(String email, String password) {
        if (!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        user=firebaseAuth.getCurrentUser();
                        assert user != null;
                        final String currentUserId=user.getUid();

    //                    JournalApi journalApi=JournalApi.getInstance();
    //                    journalApi.setUserID(currentUserId);
                        collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error !=null){
                                    // it means no error
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                                }
                                assert value != null;
                                if (!value.isEmpty()){

                                    for (QueryDocumentSnapshot snapshot:value){
                                        JournalApi journalApi=JournalApi.getInstance();
                                        journalApi.setUsername(snapshot.getString("username"));
                                        journalApi.setUserID(currentUserId);

                                        // go to listactivity
                                        startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                }

                            }
                        });
                   }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

        }else {
            Toast.makeText(getApplicationContext(),"fill the email and password",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}