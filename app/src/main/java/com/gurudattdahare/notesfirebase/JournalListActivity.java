package com.gurudattdahare.notesfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.gurudattdahare.notesfirebase.modal.Journal;
import com.gurudattdahare.notesfirebase.ui.JournalRrcyclerAdapter;
import com.gurudattdahare.notesfirebase.util.JournalApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage;
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRrcyclerAdapter journalRrcyclerAdapter;
    private CollectionReference collectionReference=db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

         recyclerView=findViewById(R.id.recyclerview);
         recyclerView.setHasFixedSize(true);
         recyclerView.setLayoutManager(new LinearLayoutManager(this));



        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        journalList=new ArrayList<>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                if (firebaseUser!=null&&firebaseAuth!=null){
                    startActivity(new Intent(getApplicationContext(),PostJournalActivity.class));
                   // finish();
                }
                break;
            case R.id.action_Signout:
                if (firebaseUser!=null&& firebaseAuth!=null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
       collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserID()).get()
               .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
               if (!queryDocumentSnapshots.isEmpty()){
                   for (QueryDocumentSnapshot documentSnapshot :queryDocumentSnapshots){
                       Journal journal =documentSnapshot.toObject(Journal.class);
                       journalList.add(journal);
                   }
                   journalRrcyclerAdapter=new JournalRrcyclerAdapter(getApplicationContext(),journalList);
                   recyclerView.setAdapter(journalRrcyclerAdapter);
                   journalRrcyclerAdapter.notifyDataSetChanged();
               }

           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {

           }
       });
    }

    @Override
    protected void onPause() {
        super.onPause();
        journalList.clear();
    }
}