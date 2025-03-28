package com.example.blog.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ArticleEditActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    String title,username,date1, description,articleImage,articleId;
    MaterialCardView select_image_blog_edit;
    ImageView back,articleEditImage;
    AppCompatEditText title_edit,description_edit;
    AppCompatButton save;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        //find the id in findViewById
        title_edit = findViewById(R.id.etTxt_blog_title_edit);
        description_edit = findViewById(R.id.etTxt_blog_description_edit);
        save = findViewById(R.id.btn_edit_blog);
        back = findViewById(R.id.imageBack_1);
        articleEditImage = findViewById(R.id.imageView_blog_edit);
        select_image_blog_edit = findViewById(R.id.select_image_blog_edit);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ArticleEditActivity.this, YourArticlesActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            title = bundle.getString("articleTitle");
            articleId = bundle.getString("articleId");
            description = bundle.getString("articleDescription");
            articleImage = bundle.getString("articleImage");

            title_edit.setText(title);
            description_edit.setText(description);
            Glide.with(getApplicationContext())
                    .load(articleImage)
                    .into(articleEditImage);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveArticleChanges();
            }
        });

    }

    private void SaveArticleChanges() {
        String updateTitle = title_edit.getText().toString().trim();
        String updateDescription = description_edit.getText().toString().trim();

        if (updateTitle.isEmpty() || updateDescription.isEmpty()){
            Toast.makeText(this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("Article").document(articleId)
                .update("articleTitle",updateTitle,"articleDescription",updateDescription)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Article updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ArticleEditActivity.this,YourArticlesActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(), "Failed to update article", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}