package com.example.blog.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blog.Activity.ArticleEditActivity;
import com.example.blog.InterFace.OnItemClickListener;
import com.example.blog.Models.ArticleModel;
import com.example.blog.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class YourArticleAdapter extends RecyclerView.Adapter<YourArticleAdapter.ViewHolder> {

    Context context;
    ArrayList<ArticleModel> articleModels;
    FirebaseFirestore firestore;
    private final OnItemClickListener listener;

    public YourArticleAdapter(Context context, ArrayList<ArticleModel> articleModels, FirebaseFirestore firestore, OnItemClickListener listener) {
        this.context = context;
        this.articleModels = articleModels;
        this.firestore = firestore;
        this.listener = listener;
        listenForRealTimeUpdates();
    }
    @NonNull
    @Override
    public YourArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.your_articles_items,parent,false);
        return new YourArticleAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull YourArticleAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ArticleModel article = articleModels.get(position);

        holder.title.setText(article.getArticleTitle());
        holder.author.setText(article.getUserName());
        holder.date.setText(article.getArticleCurrentDate());
        holder.content.setText(article.getArticleDescription());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ArticleEditActivity.class);
                context.startActivity(intent);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArticle(article,position);
            }
        });
        holder.readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null){
                    // Pass the  object and position
                    listener.onItemClick(articleModels,position);
                }
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditActivity(article);
            }
        });
    }
    private void openEditActivity(ArticleModel article) {
        Intent intent = new Intent(context, ArticleEditActivity.class);
        intent.putExtra("articleId", article.getArticleId());
        intent.putExtra("articleTitle", article.getArticleTitle());
        intent.putExtra("articleDescription", article.getArticleDescription());
        intent.putExtra("articleImage",article.getArticleImage());
        // Add other necessary extras if needed
        context.startActivity(intent);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void listenForRealTimeUpdates() {
        firestore.collection("Article")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(context, "Error fetching articles.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        ArrayList<ArticleModel> updatedArticles = new ArrayList<>();
                        for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
                            ArticleModel article = documentChange.getDocument().toObject(ArticleModel.class);
                            switch (documentChange.getType()) {
                                case ADDED:
                                    updatedArticles.add(article);
                                    break;
                                case MODIFIED:
                                    int index = findArticleIndexById(updatedArticles, article.getArticleId());
                                    if (index != -1) {
                                        updatedArticles.set(index, article);
                                    }
                                    break;
                                case REMOVED:
                                    updatedArticles.removeIf(a -> a.getArticleId().equals(article.getArticleId()));
                                    break;
                            }
                        }
                        articleModels.clear();
                        articleModels.addAll(updatedArticles);
                        notifyDataSetChanged();
                    }
                });
    }

    private int findArticleIndexById(ArrayList<ArticleModel> list, String articleId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getArticleId().equals(articleId)) {
                return i;
            }
        }
        return -1;
    }
    private void deleteArticle(ArticleModel article, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Article");
        builder.setMessage("Are you sure you want to delete this article? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {

            firestore.collection("Article").document(article.getArticleId())
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            articleModels.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Article deleted successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete article.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Handle negative button click
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public int getItemCount() {
        return articleModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title,author,date,content;
        AppCompatButton readMore,edit,delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.article_title_your);
            author = itemView.findViewById(R.id.author_your);
            date = itemView.findViewById(R.id.date_your);
            content = itemView.findViewById(R.id.article_content_your);

            readMore = itemView.findViewById(R.id.readMore_button);
            edit = itemView.findViewById(R.id.edit_button);
            delete = itemView.findViewById(R.id.delete_button);
        }
    }
}