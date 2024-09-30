package com.example.blog.Activity;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blog.Models.ArticleModel;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddArticleActivity extends AppCompatActivity {

    MaterialCardView select_image_blog;
    ImageView imageView_blog;
    AppCompatEditText title,description;
    AppCompatButton addBlog;
    ImageView back;

    private Uri ImageBlogUri;
    Bitmap bitmap;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    public String BlogPhotoUrl;
    private DocumentReference documentReference;
    ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_article);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //create Instances
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        select_image_blog = findViewById(R.id.select_image_blog);
        imageView_blog = findViewById(R.id.imageView_blog);
        title = findViewById(R.id.etTxt_blog_title);
        description = findViewById(R.id.etTxt_blog_description);
        addBlog = findViewById(R.id.btn_add_blog);
        back = findViewById(R.id.imageBack);
        progressBar = findViewById(R.id.add_article_progressBar);

        select_image_blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here make a method to pick image from gallery
                //here we forgot to check permission
                CheckForgotPermission();
                PickImageFromGallery();
            }
        });
        addBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void CheckForgotPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }else {
                PickImageFromGallery();
            }
        }else {
            PickImageFromGallery();
        }
    }

    private void PickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //now need to a launcher
        launcher.launch(intent);

    }
    ActivityResultLauncher<Intent> launcher
            = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
            result->{
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data != null && data.getData() != null){
                        ImageBlogUri = data.getData();
                        //now we need to convert our Image Into Bitmap
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(),
                                    ImageBlogUri
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // now we here set image into imageview
                    if (ImageBlogUri != null){
                        imageView_blog.setImageBitmap(bitmap);
                    }
                }
            });

    // Override this Methods into the onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // now i am going  to upload image into Firebase storage in store Image Url Firebase FireStore
    // make a method to upload Image into foreBase storage
    private void UploadImage(){
        // check ImageUri
        progressBar.setVisibility(View.VISIBLE);
        if (ImageBlogUri != null){

            final StorageReference myRef = storageReference.child("blogsImages/"+ImageBlogUri.getLastPathSegment());
            myRef.putFile(ImageBlogUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //here we need to getDownloadUri to store in String
                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null){
                                BlogPhotoUrl = uri.toString();
                                SaveArticleDataToFireStore();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            SaveArticleDataToFireStore();
        }
    }
    private void SaveArticleDataToFireStore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
         // Log.d("SaveArticle", "Current user UID: " + uid);

            firestore.collection("Users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("userName");
                        String email = document.getString("email");
                        String imageUrl = document.getString("user_image");

                        documentReference = firestore.collection("Article").document();
                        ArticleModel articleModel = new ArticleModel(uid,name,imageUrl,"",title.getText().toString(),BlogPhotoUrl,description.getText().toString(),0,"");

                        documentReference.set(articleModel, SetOptions.merge())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            //generate current Date in upload your Article
                                            Date currentDate = new Date();
                                            @SuppressLint("SimpleDateFormat")
                                            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
                                            String formattedDate = formatter.format(currentDate);
                                            articleModel.setArticleCurrentDate(formattedDate);

                                            String articleId = documentReference.getId();
//                                          Log.d("articleId", "articleId  UID: " + articleId);
                                            articleModel.setArticleId(articleId);

                                            documentReference.set(articleModel,SetOptions.merge())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(AddArticleActivity.this, "Article added successfully!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}