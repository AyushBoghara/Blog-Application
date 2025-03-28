package com.example.blog.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blog.Adapter.HomeArticleAdapter;
import com.example.blog.Adapter.SavedArticlesAdapter;
import com.example.blog.InterFace.OnItemClickListener;
import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SavedArticlesActivity extends AppCompatActivity implements OnItemClickListener{

    FirebaseFirestore firestore;
    RecyclerView recycler_View;
    ArrayList<BookMarkModel> bookMarkModels;
    SavedArticlesAdapter adapter;
    FirebaseAuth mAuth;
    ImageView back;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_articles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recycler_View = findViewById(R.id.recyclerView_savedArticles);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.back_saveArticle);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bookMarkModels = new ArrayList<>();
        adapter = new SavedArticlesAdapter( SavedArticlesActivity.this,bookMarkModels, (OnItemClickListener) SavedArticlesActivity.this,firestore);
        recycler_View.setLayoutManager(new LinearLayoutManager(SavedArticlesActivity.this));
        recycler_View.setAdapter(adapter);

        FetchAllBookMarksArticles();
    }

    private void FetchAllBookMarksArticles() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            firestore.collection("Users")
                    .document(user.getUid())
                    .collection("bookMarks")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null){
                                bookMarkModels.clear();
                                for (DocumentSnapshot documentSnapshot :task.getResult() ) {
                                    BookMarkModel bookMarkArticles = documentSnapshot.toObject(BookMarkModel.class);
                                    if (bookMarkArticles != null){
                                        bookMarkModels.add(bookMarkArticles);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }else {
                                Log.e("FirebaseError", "Error fetching documents: ", task.getException());
                                Toast.makeText(getApplicationContext(), "Failed to fetch articles.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FirebaseError", "Error fetching documents: "+e.getMessage());
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onItemClick(ArrayList<ArticleModel> articleModels, int position) {

    }

    @Override
    public void onItemClickBookMark(ArrayList<BookMarkModel> bookMarkModels, int position) {
        Intent intent = new Intent(this, ReadMoreActivity.class);
        intent.putExtra("userId",bookMarkModels.get(position).getUserId());
        intent.putExtra("userName",bookMarkModels.get(position).getArticleAuthor());

        intent.putExtra("articleId",bookMarkModels.get(position).getArticleId());
        intent.putExtra("articleTitle",bookMarkModels.get(position).getArticleTitle());
        intent.putExtra("articleImage",bookMarkModels.get(position).getArticleImage());
        intent.putExtra("articleDescription",bookMarkModels.get(position).getArticleDescription());

        intent.putExtra("articleLikes",bookMarkModels.get(position).getArticleLikes());
        intent.putExtra("articleCurrentDate",bookMarkModels.get(position).getArticleCurrentDate());
        startActivity(intent);
    }
}