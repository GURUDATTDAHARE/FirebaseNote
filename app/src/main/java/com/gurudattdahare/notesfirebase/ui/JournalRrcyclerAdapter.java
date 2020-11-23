package com.gurudattdahare.notesfirebase.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gurudattdahare.notesfirebase.R;
import com.gurudattdahare.notesfirebase.modal.Journal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRrcyclerAdapter extends RecyclerView.Adapter<JournalRrcyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalRrcyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRrcyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRrcyclerAdapter.ViewHolder holder, int position) {
        Journal journal=journalList.get(position);
        String imageURL;
        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThoughts());
          imageURL=journal.getImageUrl();

        Picasso.get().load(imageURL).placeholder(R.drawable.three).fit().into(holder.image);

        String timeago= (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds()*1000);
        holder.dateAdded.setText(timeago);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title,thoughts,dateAdded,name;
        public ImageView image;
        public String userId;
        public  String username;
        public ViewHolder(@NonNull View itemView,Context ctx) {
            super(itemView);
            context =ctx;

            title=itemView.findViewById(R.id.journal_title_list);
            thoughts=itemView.findViewById(R.id.journal_thoughts_list);
            dateAdded=itemView.findViewById(R.id.journal_data_list);
            image=itemView.findViewById(R.id.Journal_image_list);
        }
    }
}
