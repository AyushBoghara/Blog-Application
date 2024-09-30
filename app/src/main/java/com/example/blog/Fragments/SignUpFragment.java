package com.example.blog.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blog.Activity.MainActivity;
import com.example.blog.Models.UserModel;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;


public class SignUpFragment extends Fragment {

    // here i am using cardView as a selectPhoto Button
    MaterialCardView selectPhotos;
    AppCompatEditText userName,email,password;
    AppCompatButton register;
    private Uri ImageUri;
    Bitmap bitmap;
    ImageView imageView_Profile;
    ProgressBar sign_up_progressBar;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    public String PhotoUrl;
    private DocumentReference documentReference;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        //create Instances
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        userName = view.findViewById(R.id.etTxtUserName);
        email = view.findViewById(R.id.etTxtEmail);
        password = view.findViewById(R.id.etTxtPassword);
        register = view.findViewById(R.id.btnRegister_signUp);
        selectPhotos = view.findViewById(R.id.selectPhoto);
        imageView_Profile = view.findViewById(R.id.imageView_Profile);
        sign_up_progressBar = view.findViewById(R.id.sign_up_progressBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectPhotos.setOnClickListener(view1 -> {
            //here make a method to pick image from gallery
            //here we forgot to check permission
            CheckForgotPermission();
            PickImageFromGallery();
        });

        // editText per use for addTextChangeListener
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpWithFirebase();
            }
        });

    }

    private void checkInputs() {
        if (!userName.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            register.setEnabled(true);
        } else {
            register.setEnabled(false);
        }
    }
    private void CheckForgotPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            } else {
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
        // now we need to a launcher
        launcher.launch(intent);

    }
    ActivityResultLauncher<Intent> launcher
            = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
            result ->{
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            if (data != null && data.getData() != null){
                                ImageUri = data.getData();
                                // now we need to convert our Image Into BitMap
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(
                                            getActivity().getContentResolver(),
                                            ImageUri
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // now we here set image into imageview
                            if (ImageUri != null){
                                imageView_Profile.setImageBitmap(bitmap);
                            }
                        }
            }
    );

    // now i am going  to upload image into Firebase storage in store Image Url Firebase FireStore
    // make a method to upload Image into foreBase storage
    private void UploadImage(final String userId){
        // check ImageUri
        if(ImageUri != null){
            //create storage Instances
            final StorageReference myRef = storageReference.child("usersPhotos/"+ImageUri.getLastPathSegment());
            myRef.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //here we need to getDownloadUri to store in String
                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null){
                                PhotoUrl = uri.toString();
                                SaveUserDataToFireStore(userId); // Save user data after image upload
                            }else {
                                sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                register.setEnabled(true); // Re-enable register button
                                selectPhotos.setEnabled(true); // Re-enable photo selection
                                Toast.makeText(getActivity(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                            register.setEnabled(true); // Re-enable register button
                            selectPhotos.setEnabled(true); // Re-enable photo selection
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                    register.setEnabled(true); // Re-enable register button
                    selectPhotos.setEnabled(true); // Re-enable photo selection
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            SaveUserDataToFireStore(userId); // Save user data if no image
        }
    }
    private void signUpWithFirebase() {
        if(email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")){

            sign_up_progressBar.setVisibility(View.VISIBLE); // Show progress bar
            register.setEnabled(false); // Disable register button
            selectPhotos.setEnabled(false); // Disable photo selection

            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String userId = Objects.requireNonNull(task.getResult()).getUser().getUid();
                                UploadImage(userId);
                            }else {
                                sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                register.setEnabled(true); // Re-enable register button
                                selectPhotos.setEnabled(true); // Re-enable photo selection
                                Toast.makeText(getActivity(), "Sign Up Failed:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            Toast.makeText(getActivity(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveUserDataToFireStore(String userId) {
        documentReference = firestore.collection("Users").document(userId);
        UserModel userModel = new UserModel(userId,userName.getText().toString(),email.getText().toString(),PhotoUrl,password.getText().toString());
        documentReference.set(userModel, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                                // now this user id will send into firebase
                                documentReference.set(userModel,SetOptions.merge())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                        register.setEnabled(true); // Re-enable register button
                                        selectPhotos.setEnabled(true); // Re-enable photo selection
                                        Intent intent = new Intent(getActivity(),MainActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        sign_up_progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                        register.setEnabled(true); // Re-enable register button
                                        selectPhotos.setEnabled(true); // Re-enable photo selection
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}