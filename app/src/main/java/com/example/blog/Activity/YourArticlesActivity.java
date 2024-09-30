package com.example.blog.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blog.Adapter.YourArticleAdapter;
import com.example.blog.InterFace.OnItemClickListener;
import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class YourArticlesActivity extends AppCompatActivity implements OnItemClickListener {

    FirebaseFirestore firestore;
    ImageView back;
    RecyclerView recyclerView;
    ArrayList<ArticleModel> articleModels;
    YourArticleAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_articles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        back = findViewById(R.id.image_Back);
        recyclerView = findViewById(R.id.your_articles_recyclerView);

        firestore = FirebaseFirestore.getInstance();
        articleModels = new ArrayList<>();
        adapter = new YourArticleAdapter(this,articleModels,firestore,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(YourArticlesActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        FetchYourArticle();
    }

    private void FetchYourArticle() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){

            String uid = currentUser.getUid();
            firestore.collection("Article")
                    .whereEqualTo("userId",uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                articleModels.clear();
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    ArticleModel article = documentSnapshot.toObject(ArticleModel.class);
                                    articleModels.add(article);
                                }
                                adapter.notifyDataSetChanged();
                            }else {
                                Toast.makeText(getApplicationContext(), "Failed to fetch articles.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onItemClick(ArrayList<ArticleModel> articleModels, int position) {
        Intent intent = new Intent(this, ReadMoreActivity.class);
        intent.putExtra("userId",articleModels.get(position).getUserId());
        intent.putExtra("userName",articleModels.get(position).getUserName());
        intent.putExtra("userProfileImage",articleModels.get(position).getUserProfileImage());

        intent.putExtra("articleId",articleModels.get(position).getArticleId());
        intent.putExtra("articleTitle",articleModels.get(position).getArticleTitle());
        intent.putExtra("articleImage",articleModels.get(position).getArticleImage());
        intent.putExtra("articleDescription",articleModels.get(position).getArticleDescription());

        intent.putExtra("articleLikes",articleModels.get(position).getArticleLikes());
        intent.putExtra("articleCurrentDate",articleModels.get(position).getArticleCurrentDate());
        startActivity(intent);
    }

    @Override
    public void onItemClickBookMark(ArrayList<BookMarkModel> bookMarkModels, int position) {

    }
}