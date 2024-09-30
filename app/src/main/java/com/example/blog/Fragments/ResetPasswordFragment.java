package com.example.blog.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ResetPasswordFragment extends Fragment {


    AppCompatButton btnResetPassword;
    TextView responseMessage;
    ProgressBar resetPasswordProgressBar;
    AppCompatEditText email;
    ImageView back;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;


    public ResetPasswordFragment() {
        // Required empty public constructor
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        email = view.findViewById(R.id.etEmail);
        responseMessage = view.findViewById(R.id.responseMessage);
        resetPasswordProgressBar = view.findViewById(R.id.resetPasswordProgressBar);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        back = view.findViewById(R.id.imageBack_2);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Ensure FireStore is initialized

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SignInFragment.class);
                startActivity(intent);
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetPassword();
            }
        });
    }

    private void ResetPassword() {
        if (email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")){
            resetPasswordProgressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                responseMessage.setText("Check Your Email.");
                                responseMessage.setTextColor(getResources().getColor(R.color.green));
                            }else {
                                responseMessage.setText("There is an issue Sending Email.");
                                responseMessage.setTextColor(getResources().getColor(R.color.text_color_red));
                            }
                            resetPasswordProgressBar.setVisibility(View.INVISIBLE);
                            responseMessage.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            resetPasswordProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            Toast.makeText(getActivity(), "Invalid Email Pattern!", Toast.LENGTH_SHORT).show();
            btnResetPassword.setEnabled(true);
        }
    }
    private void checkInputs() {
        if (!email.getText().toString().isEmpty()) {
            btnResetPassword.setEnabled(true);
        } else {
            btnResetPassword.setEnabled(false);
        }
    }
}