package com.example.blog.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blog.InterFace.OnItemClickListener;
import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;
import com.example.blog.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeArticleAdapter extends RecyclerView.Adapter<HomeArticleAdapter.ViewHolder> {

    Context context;
    ArrayList<ArticleModel> articleModels;
    private final OnItemClickListener listener;
    FirebaseFirestore firestore;

    public HomeArticleAdapter(Context context, ArrayList<ArticleModel> articleModels, OnItemClickListener listener, FirebaseFirestore firestore) {
        this.context = context;
        this.articleModels = articleModels;
        this.listener = listener;
        this.firestore = firestore;
    }

    @NonNull
    @Override
    public HomeArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_items,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeArticleAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ArticleModel article = articleModels.get(position);
        if (article != null){
            holder.title.setText(article.getArticleTitle());
            holder.author.setText(article.getUserName());
            holder.date.setText(article.getArticleCurrentDate());
            holder.content.setText(article.getArticleDescription());
            // Fetch and display like count
            fetchAndDisplayLikeCount(holder, article.getArticleId());

            holder.readMore.setOnClickListener(view -> {
                if (listener != null){
                    // Pass the  object and position
                    listener.onItemClick(articleModels,position);
                }
            });
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null){
                String currentUserId = user.getUid();
                final String articleKey = articleModels.get(position).getArticleId();

                updateLikeStatus(holder, currentUserId, articleKey);

                holder.like.setOnClickListener(view -> handleLikeDislike(holder, currentUserId, articleKey));

                updateSaveStatus(holder,currentUserId,articleKey);
                holder.bookMark.setOnClickListener(view -> SaveArticle(holder,currentUserId,articleKey));
            }
        }
    }

    private void updateSaveStatus(ViewHolder holder, String userId, String articleId) {
        DocumentReference bookMarkRef = firestore.collection("Users").document(userId)
                .collection("bookMarks").document(articleId);

        bookMarkRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                // Handle error
                //Log.e("FireStoreError", "Error listening for BookMark status", e);
                return;
            }
            if (documentSnapshot != null  && documentSnapshot.exists()) {
                holder.bookMark.setImageResource(R.drawable.ic_bookmark); // Set to bookMark  icon
            } else {
                holder.bookMark.setImageResource(R.drawable.ic_inactive_bookmark); // Set to bookMark icon
            }
        });
    }

    private void SaveArticle(ViewHolder holder, String userId, String articleId) {
        DocumentReference bookMarkReference = firestore.collection("Users").document(userId)
                .collection("bookMarks").document(articleId);

        bookMarkReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()){
                    // Bookmark exists, remove it
                    bookMarkReference.delete().addOnSuccessListener(unused -> {
                        holder.bookMark.setImageResource(R.drawable.ic_inactive_bookmark);
                        //   Toast.makeText(context, "Removed bookmark", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
//            Log.e("FireStoreError", "Error removing bookmark", e);
//            Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    ArticleModel article = articleModels.get(holder.getAdapterPosition());
                    BookMarkModel bookMarkModel = new BookMarkModel(
                            userId,
                            articleId,
                            article.getUserName(),
                            article.getArticleTitle(),
                            article.getArticleImage(),
                            article.getArticleDescription(),
                            String.valueOf(article.getArticleLikes()), // Convert likes to String
                            article.getArticleCurrentDate()
                    );
                    bookMarkReference.set(bookMarkModel,SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                holder.bookMark.setImageResource(R.drawable.ic_bookmark);
                                //Toast.makeText(context, "bookMarked....", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
//                        Log.e("FireStoreError", "Error BookMark article", e);
//                        Toast.makeText(context, "Failed to BookMark article.", Toast.LENGTH_SHORT).show();
                            });
                }
            }else {
                //   Log.e("FireStoreError", "Error fetching bookmark status", task.getException());
            }
        }).addOnFailureListener(e -> {
            //  Log.e("FireStoreError", "Error fetching bookmark status", e);
        });
    }

    private void fetchAndDisplayLikeCount(ViewHolder holder, String articleId) {
        DocumentReference articleRef = firestore.collection("Article").document(articleId);
        // Log the exception
        articleRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long likeCount = documentSnapshot.getLong("articleLikes");
                        holder.txtLikeCount.setText(likeCount != null ? String.valueOf(likeCount) : "0");
//                        if (likeCount != null) {
//                            holder.txtLikeCount.setText(String.valueOf(likeCount));
//                        } else {
//                            holder.txtLikeCount.setText("0");
//                        }
                    } else {
                        holder.txtLikeCount.setText("0");
                    }
        }).addOnFailureListener(e -> {
           // Log.e("FireStoreError", "Error fetching like count", e);
            holder.txtLikeCount.setText("0");
        });
    }

    private void handleLikeDislike(ViewHolder holder, String userId, String articleId) {
        DocumentReference likeRef = firestore.collection("Article").document(articleId)
                .collection("likes").document(userId);

        likeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // User has liked the article, so unlike it
                    likeRef.delete().addOnSuccessListener(aVoid -> {
                        holder.like.setImageResource(R.drawable.ic_inactive_favorite); // Set to like icon
                        updateLikeCount(articleId, -1,holder);
                    }).addOnFailureListener(e -> {
                        //Log.e("FireStoreError", "Error unliking article", e);
                       // Toast.makeText(context, "Failed to unlike article.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // User has not liked the article, so like it
                    likeRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                        holder.like.setImageResource(R.drawable.ic_active_favorite); // Set to liked icon
                        updateLikeCount(articleId, 1,holder);
                    }).addOnFailureListener(e -> {
                        //Log.e("FireStoreError", "Error liking article", e);
                        Toast.makeText(context, "Failed to like article.", Toast.LENGTH_SHORT).show();
                    });
                }
            }else {
               // Log.e("FireStoreError", "Error fetching like status", task.getException());
               // Toast.makeText(context, "Failed to fetch like status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeStatus(ViewHolder holder, String userId, String articleId) {
        DocumentReference likeRef = firestore.collection("Article").document(articleId)
                .collection("likes").document(userId);

               likeRef.addSnapshotListener((documentSnapshot, e) -> {
                   if (e != null) {
                       // Handle error
                       //Log.e("FireStoreError", "Error listening for like status", e);
                       return;
                   }
                   if (documentSnapshot != null  && documentSnapshot.exists()) {
                       holder.like.setImageResource(R.drawable.ic_active_favorite); // Set to liked icon
                   } else {
                       holder.like.setImageResource(R.drawable.ic_inactive_favorite); // Set to like icon
                   }
               });
    }

    private void updateLikeCount(String articleId, int count,ViewHolder holder) {
        DocumentReference articleRef = firestore.collection("Article").document(articleId);
        articleRef.update("articleLikes", FieldValue.increment(count))
                .addOnSuccessListener(unused -> {
                    // Successfully updated like count
                    fetchAndDisplayLikeCount(holder,articleId);
                    //Toast.makeText(context, "Article like count updated", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    //Log.e("FireStoreError", "Error updating like count", e);
                    //Toast.makeText(context, "Failed to update like count.", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public int getItemCount() {
        return articleModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title,author,date,content,txtLikeCount;
        AppCompatButton readMore;
        ImageView like,bookMark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.article_title_your);
            author = itemView.findViewById(R.id.author_your);
            date = itemView.findViewById(R.id.date_your);
            content = itemView.findViewById(R.id.article_content_your);
            readMore = itemView.findViewById(R.id.read_more_button);
            like = itemView.findViewById(R.id.imageViewLike);
            bookMark = itemView.findViewById(R.id.imageViewBookMark);
            txtLikeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}