package com.gurudattdahare.notesfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gurudattdahare.notesfirebase.modal.Journal;
import com.gurudattdahare.notesfirebase.util.JournalApi;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private ImageView postImage;
    private TextView postUsername;
    private TextView postDate;
    private EditText postTitle;
    private EditText postThought;
    private ProgressBar progressBar;
    private Button  postSaveButton;
    private ImageView backgroundImage;

    private String currentUsername;
    private String currentUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference=db.collection("Journal");
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        postImage=findViewById(R.id.postCameraButton);
        postUsername=findViewById(R.id.post_username_textview);
        postDate =findViewById(R.id.post_date_textview);
        postTitle=findViewById(R.id.post_title_et);
        postThought=findViewById(R.id.post_thought_et);
        progressBar=findViewById(R.id.post_progressBar);
        postSaveButton=findViewById(R.id.post_save);
        backgroundImage=findViewById(R.id.imageView3);
       // backgroundImage=findViewById(R.id.backgroundImage);

        //firebase
        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        postSaveButton.setOnClickListener(this);
        postImage.setOnClickListener(this);

        if (JournalApi.getInstance()!=null)
        {
            currentUserId=JournalApi.getInstance().getUserID();
            currentUsername=JournalApi.getInstance().getUsername();
            postUsername.setText(currentUsername);
        }

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser=firebaseAuth.getCurrentUser();
                if (firebaseUser!=null){

                }else {

                }
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.post_save:
                progressBar.setVisibility(View.VISIBLE);
                saveJournal();
                break;
            case R.id.postCameraButton:
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
         final String title=postTitle.getText().toString().trim();
         final String thoughts=postThought.getText().toString().trim();
        if (!TextUtils.isEmpty(title)&& !TextUtils.isEmpty(thoughts)&& imageUri !=null){

             final StorageReference filepath=storageReference.child("journal_images")
                    .child("my_image_"+ Timestamp.now().getSeconds());
                  filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                          filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                              @Override
                              public void onSuccess(Uri uri) {
                                  //Todo:create a Journal Object  - modal
                                  Journal journal=new Journal();
                                  journal.setTitle(title);
                                  journal.setImageUrl(uri.toString());
                                  journal.setTimeAdded(new Timestamp(new Date()));
                                  journal.setUsername(currentUsername);
                                  journal.setUserId(currentUserId);
                                  journal.setThoughts(thoughts);
                                  //Todo:invoke our collectionReference
                                  collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                      @Override
                                      public void onSuccess(DocumentReference documentReference) {
                                          progressBar.setVisibility(View.INVISIBLE);
                                          startActivity(new Intent(getApplicationContext(),JournalListActivity.class));
                                          finish();
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          progressBar.setVisibility(View.INVISIBLE);
                                          Toast.makeText(getApplicationContext(),"connect your device to internet",Toast.LENGTH_LONG).show();
                                      }
                                  });
                                  //Todo: and save a journal instance
                              }
                          });



                      }
                  }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          progressBar.setVisibility(View.INVISIBLE);
                          Toast.makeText(getApplicationContext(),"connect your device to internet",Toast.LENGTH_LONG).show();
                      }
                  });
        }else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"fill all the data and set image",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_CODE&&resultCode==RESULT_OK){
            if (data!=null){
                imageUri=data.getData();
                backgroundImage.setImageURI(imageUri);

            }
        }
    }
}