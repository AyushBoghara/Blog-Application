package com.example.blog.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blog.Activity.MainActivity;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {

    AppCompatEditText email,password;
    AppCompatButton login,register;
    TextView forget_Password;

    ProgressBar sign_in_progressBar;
    FrameLayout frameLayout;
    private FirebaseAuth mAuth;

    public SignInFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        frameLayout = getActivity().findViewById(R.id.register_frameLayout);

        mAuth = FirebaseAuth.getInstance();

        register = view.findViewById(R.id.btnRegister);
        login = view.findViewById(R.id.btnLogin);
        email = view.findViewById(R.id.etTxtEmail);
        password = view.findViewById(R.id.etTxt_password);
        sign_in_progressBar = view.findViewById(R.id.sign_in_progressBar);
        forget_Password = view.findViewById(R.id.txt_forget_Password);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(new SignUpFragment());
            }
        });

        forget_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(new ResetPasswordFragment());
            }
        });

        // editText per use for addTextChangeListener
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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithFirebase();
            }
        });
    }

    private void checkInputs() {
        if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
            login.setEnabled(true);
        }else {
            login.setEnabled(false);
        }
    }

    private void signInWithFirebase() {
        if(email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")){
            sign_in_progressBar.setVisibility(View.VISIBLE); // Show progress bar
            login.setEnabled(false); // Disable register button

            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(getActivity(),MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }else {
                                Toast.makeText(getActivity(), "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sign_in_progressBar.setVisibility(View.GONE); // Show progress bar
                            login.setEnabled(true); // Disable register button
                            if (e.getMessage().contains("The password is invalid")) {
                                Toast.makeText(getActivity(), "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                            } else if (e.getMessage().contains("There is no user record")) {
                                Toast.makeText(getActivity(), "No account found with this email.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            Toast.makeText(getActivity(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
            sign_in_progressBar.setVisibility(View.GONE); // Show progress bar
            login.setEnabled(true); // Disable register button
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_left,R.anim.out_from_right);
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}