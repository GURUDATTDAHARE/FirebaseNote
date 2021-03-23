package com.gurudattdahare.notesfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private Task<Void> firebaseStorage;
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRrcyclerAdapter journalRrcyclerAdapter;
    private CollectionReference collectionReference=db.collection("Journal");

    private Journal deletedJournal=null;
    private int deletedPosion;


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

        // swipe to delete  items
        ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deletedPosion=viewHolder.getAdapterPosition();
                deletedJournal=journalList.get(deletedPosion);
                journalList.remove(deletedPosion);
                journalRrcyclerAdapter.notifyDataSetChanged();

                Snackbar.make(recyclerView,"Data deleted.",Snackbar.LENGTH_SHORT).setAction("Unod", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       journalList.add(deletedPosion,deletedJournal);
                       journalRrcyclerAdapter.notifyDataSetChanged();
                    }
                }).addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                       if (event==Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                           DeleteItem(deletedJournal);
                       }
                    }

                }).show();

            }
        };
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

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
    public void DeleteItem(final Journal deletedJournal){
         final CollectionReference collection=db.collection("Journal");

          collection.whereEqualTo("userId",JournalApi.getInstance().getUserID())
          .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                  if(!queryDocumentSnapshots.isEmpty()){
                      String docId = null;
                      for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){

                          if(deletedJournal.getTimeAdded().toString().equals(snapshot.toObject(Journal.class).getTimeAdded().toString())){
        //                      Log.d("testing","titals:"+snapshot.toObject(Journal.class).getTitle()+"doc: "+snapshot.getId());
                              docId=snapshot.getId();
                          }

                      }
                      db.collection("Journal").document(docId).delete();
                      firebaseStorage=FirebaseStorage.getInstance().getReference("journal_images").child("my_image_"+JournalApi.getInstance().getUserID()
                       +"_"+deletedJournal.getImageUploadTime()).delete();
  //                   Log.d("stor","->"+"journal_image"+" my_image_"+JournalApi.getInstance().getUserID()+"_"+deletedJournal.getImageUploadTime());
                  }
              }
          });

    }
}