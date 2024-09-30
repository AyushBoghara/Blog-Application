package com.example.blog.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blog.Adapter.HomeArticleAdapter;
import com.example.blog.Activity.AddArticleActivity;
import com.example.blog.InterFace.OnItemClickListener;
import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;
import com.example.blog.R;
import com.example.blog.Activity.ReadMoreActivity;
import com.example.blog.Activity.SavedArticlesActivity;
import com.example.blog.Activity.UserProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public  class HomeFragment extends Fragment implements OnItemClickListener {

    FirebaseFirestore firestore;

    ExtendedFloatingActionButton floatingActionButton;
    MaterialCardView select_image_profile,select_image_bookmark;
    RecyclerView recyclerView;
    ImageView imageProfile;
    ArrayList<ArticleModel> articleModels;
    HomeArticleAdapter adapter;
    FirebaseAuth mAuth;
    SearchView searchView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        floatingActionButton = view.findViewById(R.id.create_fab);
        recyclerView = view.findViewById(R.id.main_recyclerView);
        imageProfile = view.findViewById(R.id.image_profile);
        select_image_profile = view.findViewById(R.id.select_image_profile);
        select_image_bookmark = view.findViewById(R.id.select_image_bookmark);
        searchView = view.findViewById(R.id.searchView);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        articleModels = new ArrayList<>();
        adapter = new HomeArticleAdapter(getContext(),articleModels, this,firestore);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatingActionButton.setOnClickListener(view12 -> {
            Intent intent = new Intent(getActivity(), AddArticleActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        select_image_profile.setOnClickListener(view1 -> {
            Intent i = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(i);
        });

        select_image_bookmark.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), SavedArticlesActivity.class);
            startActivity(intent);
        });

        // Fetch Current User in app
        CurrentUser();
        // fetch all articles
        FetchAllArticle();
        // create a search view in setOnQueryTextListener is used
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                TxtSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                TxtSearch(s);
                return false;
            }
        });
    }
    private void TxtSearch(String str){
        firestore.collection("Article")
                .orderBy("articleTitle")
                .startAt(str)
                .endAt(str+"\uf8ff")
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
                            if (articleModels.isEmpty()) {
                                Toast.makeText(getActivity(), "No articles Search.", Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(getActivity(), "Failed to fetch articles.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void FetchAllArticle() {
        firestore.collection("Article")
                .orderBy("articleCurrentDate", Query.Direction.DESCENDING) // Get latest articles first
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
                            Toast.makeText(getActivity(), "Failed to fetch articles.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void CurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            firestore.collection("Users").document(user.getUid()).addSnapshotListener((document, error) -> {
                if (error != null) {
                    Toast.makeText(getActivity(), "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (document != null && document.exists()) {
                    String imageUrl = document.getString("user_image");
                    Glide.with(getActivity())
                            .load(imageUrl)
                            .into(imageProfile);
                } else {
                    Toast.makeText(getActivity(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when the fragment is resumed
        FetchAllArticle();
        CurrentUser();
    }
    @Override
    public void onItemClick(ArrayList<ArticleModel> articleModels, int position) {
        Intent intent = new Intent(getContext(), ReadMoreActivity.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        intent.putExtra("userId",user.getUid());
        intent.putExtra("userName",articleModels.get(position).getUserName());
        intent.putExtra("userProfileImage",articleModels.get(position).getUserProfileImage());

        intent.putExtra("articleId",articleModels.get(position).getArticleId());
        intent.putExtra("articleTitle",articleModels.get(position).getArticleTitle());
        intent.putExtra("articleImage",articleModels.get(position).getArticleImage());
        intent.putExtra("articleDescription",articleModels.get(position).getArticleDescription());

        intent.putExtra("articleLikes",articleModels.get(position).getArticleLikes());
        intent.putExtra("articleCurrentDate",articleModels.get(position).getArticleCurrentDate());
        getContext().startActivity(intent);
    }

    @Override
    public void onItemClickBookMark(ArrayList<BookMarkModel> bookMarkModels, int position) {}
}