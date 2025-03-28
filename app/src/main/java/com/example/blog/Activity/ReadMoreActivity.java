package com.example.blog.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.blog.Adapter.HomeArticleAdapter;
import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;
import com.example.blog.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;

public class ReadMoreActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ImageView back,iv_blog;
    TextView article_title,author,date,article_content;
    String title,username,date1, description,articleImage,UserId,ArticleId,like;

    FloatingActionButton fab_bookmark,fab_favorite;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_more);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.backProfile);
        iv_blog = findViewById(R.id.iv_blog);
        article_title = findViewById(R.id.article_title_your);
        author = findViewById(R.id.author_your);
        date = findViewById(R.id.date_your);
        article_content = findViewById(R.id.article_content_your);
        fab_bookmark = findViewById(R.id.fab_bookmark);
        fab_favorite = findViewById(R.id.fab_favorite);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            title = bundle.getString("articleTitle");
            username = bundle.getString("userName");
            date1 = bundle.getString("articleCurrentDate");
            articleImage = bundle.getString("articleImage");
            description = bundle.getString("articleDescription");
            UserId = bundle.getString("userId");
            ArticleId =  bundle.getString("articleId");
            like = bundle.getString("articleLikes");
        }
        article_title.setText(title);
        author.setText(username);
        date.setText(date1);
        article_content.setText(description);
        Glide.with(this)
                .load(articleImage)
                .into(iv_blog);
        // check the current states in a like or not
        updateLikeStatus( UserId, ArticleId);
        fab_favorite.setOnClickListener(view -> handleLikeDislike( UserId, ArticleId));
        // check the current states in a bookmark or not
        updateSaveStatus(UserId,ArticleId);
        fab_bookmark.setOnClickListener(view -> SaveArticle(UserId,ArticleId));

    }
    private void updateSaveStatus(String userId, String articleId) {
        DocumentReference bookMarkRef = firestore.collection("Users").document(userId)
                .collection("bookMarks").document(articleId);

        //Log.d("FireStoreDebug", "UserID: " + userId + " ArticleID: " + articleId);
        bookMarkRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                // Handle error
                //Log.e("FireStoreError", "Error listening for BookMark status", e);
                return;
            }
            if (documentSnapshot != null  && documentSnapshot.exists()) {
                fab_bookmark.setImageResource(R.drawable.ic_bookmark); // Set to bookMark  icon
                fab_bookmark.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.text_color_red));
            } else {
                fab_bookmark.setImageResource(R.drawable.ic_inactive_bookmark); // Set to bookMark icon
                fab_bookmark.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.black));
            }
        });
    }

    private void SaveArticle(String userId, String articleId) {
        DocumentReference bookMarkReference = firestore.collection("Users").document(userId)
                .collection("bookMarks").document(articleId);
       // Log.d("FireStoreDebug", "UserID: " + userId + " ArticleID: " + articleId);
        bookMarkReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()){
                    // Bookmark exists, remove it
                    bookMarkReference.delete().addOnSuccessListener(unused -> {
                        fab_bookmark.setImageResource(R.drawable.ic_inactive_bookmark);
                        fab_bookmark.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.black));
//                        Toast.makeText(getApplicationContext(), "Removed bookmark", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
//                      Log.e("FireStoreError", "Error removing bookmark", e);
//                      Toast.makeText(getApplicationContext(), "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    BookMarkModel bookMarkModel = new BookMarkModel(
                            userId,
                            articleId,
                            username,
                            title,
                            articleImage,
                            description,
                            like,
                            date1
                    );
                    bookMarkReference.set(bookMarkModel, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                fab_bookmark.setImageResource(R.drawable.ic_bookmark);
                                fab_bookmark.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.text_color_red));
//                                Toast.makeText(getApplicationContext(), "bookMarked....", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
//                                Log.e("FireStoreError", "Error BookMark article", e);
//                                Toast.makeText(getApplicationContext(), "Failed to BookMark article.", Toast.LENGTH_SHORT).show();
                            });
                }
            }else {
//                   Log.e("FireStoreError", "Error fetching bookmark status", task.getException());
            }
        }).addOnFailureListener(e -> {
//              Log.e("FireStoreError", "Error fetching bookmark status", e);
        });
    }

    private void updateLikeStatus(String userId, String articleId) {
        DocumentReference likeRef = firestore.collection("Article").document(articleId)
                .collection("likes").document(userId);

        likeRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                // Handle error
//               Log.e("FireStoreError", "Error listening for like status", e);
                return;
            }
            if (documentSnapshot != null  && documentSnapshot.exists()) {
                fab_favorite.setImageResource(R.drawable.ic_active_favorite); // Set to liked icon
                fab_favorite.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.text_color_red));

            } else {
                fab_favorite.setImageResource(R.drawable.ic_inactive_favorite); // Set to like icon
                fab_favorite.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.black));
            }
        });
    }
    private void handleLikeDislike(String userId, String articleId) {
        DocumentReference likeRef = firestore.collection("Article").document(articleId)
                .collection("likes").document(userId);

        likeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // User has liked the article, so unlike it
                    likeRef.delete().addOnSuccessListener(aVoid -> {
                        fab_favorite.setImageResource(R.drawable.ic_inactive_favorite); // Set to like icon
                        fab_favorite.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.black));
                        updateLikeCount(articleId, -1);
                    }).addOnFailureListener(e -> {
//                        Log.e("FireStoreError", "Error unliking article", e);
//                        Toast.makeText(getApplicationContext(), "Failed to unlike article.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // User has not liked the article, so like it
                    likeRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                        fab_favorite.setImageResource(R.drawable.ic_active_favorite); // Set to liked icon
                        fab_favorite.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.text_color_red));
                        updateLikeCount(articleId, 1);
                    }).addOnFailureListener(e -> {
//                        Log.e("FireStoreError", "Error liking article", e);
//                        Toast.makeText(getApplicationContext(), "Failed to like article.", Toast.LENGTH_SHORT).show();
                    });
                }
            }else {
//                Log.e("FireStoreError", "Error fetching like status", task.getException());
//                Toast.makeText(getApplicationContext(), "Failed to fetch like status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateLikeCount(String articleId, int count) {
        DocumentReference articleRef = firestore.collection("Article").document(articleId);
        articleRef.update("articleLikes", FieldValue.increment(count))
                .addOnSuccessListener(unused -> {
                    // Successfully updated like count
                    fetchAndDisplayLikeCount(articleId);
//                   Toast.makeText(getApplicationContext(), "Article like count updated", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
//                  Log.e("FireStoreError", "Error updating like count", e);
//                  Toast.makeText(getApplicationContext(), "Failed to update like count.", Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchAndDisplayLikeCount( String articleId) {
        DocumentReference articleRef = firestore.collection("Article").document(articleId);
        // Log the exception
        articleRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long likeCount = documentSnapshot.getLong("articleLikes");
                    } else {
                    }
                }).addOnFailureListener(e -> {
                    // Log.e("FireStoreError", "Error fetching like count", e);
                });
    }
}