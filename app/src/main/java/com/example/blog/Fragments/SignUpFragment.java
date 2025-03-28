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
import android.util.Log;
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
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

public class SignUpFragment extends Fragment {

    MaterialCardView selectPhotos;
    AppCompatEditText userName, email, password;
    AppCompatButton register;
    private Uri imageUri;
    Bitmap bitmap;
    ImageView imageViewProfile;
    ProgressBar signUpProgressBar;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    public String photoUrl;
    private DocumentReference documentReference;
    private static final String TAG = "SignUpFragment";

    public SignUpFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        userName = view.findViewById(R.id.etTxtUserName);
        email = view.findViewById(R.id.etTxtEmail);
        password = view.findViewById(R.id.etTxtPassword);
        register = view.findViewById(R.id.btnRegister_signUp);
        selectPhotos = view.findViewById(R.id.selectPhoto);
        imageViewProfile = view.findViewById(R.id.imageView_Profile);
        signUpProgressBar = view.findViewById(R.id.sign_up_progressBar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectPhotos.setOnClickListener(view1 -> {
            if (checkPermission()) {
                pickImageFromGallery();
            } else {
                requestPermission();
            }
        });

        // EditText TextChangeListeners
        userName.addTextChangedListener(new InputTextWatcher());
        email.addTextChangedListener(new InputTextWatcher());
        password.addTextChangedListener(new InputTextWatcher());

        register.setOnClickListener(view12 -> signUpWithFirebase());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void checkInputs() {
        register.setEnabled(!userName.getText().toString().isEmpty() &&
                !email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty());
    }

    private void requestPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, 1);
        } else {
            pickImageFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(getActivity(), "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        imageUri = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            imageViewProfile.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                        }
                    }
                }
            }
    );

    private void uploadImage(final String userId) {
        if (imageUri != null) {
            StorageReference myRef = storageReference.child("usersPhotos/" + imageUri.getLastPathSegment());
            Log.d(TAG, "Uploading image to Firebase: " + imageUri);
            myRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "Image uploaded successfully");
                        myRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            photoUrl = uri.toString();
                            saveUserDataToFirestore(userId, photoUrl);
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                            handleImageUploadFailure(e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image upload failed: " + e.getMessage());
                        handleImageUploadFailure(e.getMessage());
                    });
        } else {
            // No image selected, save user without image URL
            saveUserDataToFirestore(userId, photoUrl);
        }
    }

    private void signUpWithFirebase() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        if (userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            signUpProgressBar.setVisibility(View.VISIBLE);
            register.setEnabled(false);
            selectPhotos.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();
                            uploadImage(userId);
                        } else {
                            handleSignUpFailure(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserDataToFirestore(String userId, String photoUrl) {
        documentReference = firestore.collection("Users").document(userId);
        UserModel userModel = new UserModel(userId, userName.getText().toString(), email.getText().toString(), photoUrl, password.getText().toString());

        documentReference.set(userModel, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    signUpProgressBar.setVisibility(View.GONE);
                    register.setEnabled(true);
                    selectPhotos.setEnabled(true);
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        handleFirestoreFailure(task.getException().getMessage());
                    }
                }).addOnFailureListener(e -> handleFirestoreFailure(e.getMessage()));
    }

    private void handleImageUploadFailure(String message) {
        signUpProgressBar.setVisibility(View.GONE);
        register.setEnabled(true);
        selectPhotos.setEnabled(true);
        Toast.makeText(getActivity(), "Image upload failed: " + message, Toast.LENGTH_SHORT).show();
    }

    private void handleSignUpFailure(String message) {
        signUpProgressBar.setVisibility(View.GONE);
        register.setEnabled(true);
        selectPhotos.setEnabled(true);
        Toast.makeText(getActivity(), "Sign Up Failed: " + message, Toast.LENGTH_SHORT).show();
    }

    private void handleFirestoreFailure(String message) {
        signUpProgressBar.setVisibility(View.GONE);
        register.setEnabled(true);
        selectPhotos.setEnabled(true);
        Toast.makeText(getActivity(), "Error saving data: " + message, Toast.LENGTH_SHORT).show();
    }

    private class InputTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkInputs();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}