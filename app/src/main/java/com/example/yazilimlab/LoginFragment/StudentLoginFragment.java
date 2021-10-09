package com.example.yazilimlab.LoginFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.yazilimlab.AdminHomeActivity;
import com.example.yazilimlab.R;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.yazilimlab.RegisterActivity;
import com.example.yazilimlab.StudentHomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class StudentLoginFragment extends Fragment {

    private EditText editTextStudentLoginUserName, editTextStudentLoginPassword;
    private String strUser, strPassword;
    private TextView textForgetPassword, buttonStudentLoginRegisterPage;
    private Button buttonLogin;

    //Firebase
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference docRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_login, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextStudentLoginUserName = (EditText) view.findViewById(R.id.editTextStudentLoginUserName);
        editTextStudentLoginPassword = (EditText) view.findViewById(R.id.editTextStudentLoginPassword);
        textForgetPassword = (TextView) view.findViewById(R.id.textStudentLoginForgetPassword);
        buttonStudentLoginRegisterPage = (TextView) view.findViewById(R.id.buttonStudentLoginRegisterPage);
        buttonLogin = (Button) view.findViewById(R.id.studentLoginButtonLogin);

        //Firebase
        fAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //click Events start
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonLogin();
            }
        });

        textForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonForgetPasswordPage(view);
            }
        });

        buttonStudentLoginRegisterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonRegisterPage();
            }
        });
        //click Events end
    }

    // https://www.youtube.com/watch?v=7P9kMo5NbOY&t=584s
    private void setButtonForgetPasswordPage(View v) {

        EditText resetMail = new EditText(v.getContext());
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
        passwordResetDialog.setTitle("Şifre Sıfırlama");
        passwordResetDialog.setMessage("Mail Adresinizi Giriniz");
        passwordResetDialog.setView(resetMail);
        passwordResetDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(resetMail.getText().toString())) {
                    String mail = resetMail.getText().toString();
                    fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getActivity(), "Mail yollandı", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Mail Adresi Girilmedi", Toast.LENGTH_SHORT).show();
                }


            }
        });
        passwordResetDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Hayır oy ");
            }
        });
        passwordResetDialog.create().show();

    }
    // https://www.youtube.com/watch?v=7P9kMo5NbOY&t=584s


    private void setButtonLogin() {
        strUser = editTextStudentLoginUserName.getText().toString();
        strPassword = editTextStudentLoginPassword.getText().toString();

        if (!TextUtils.isEmpty(strUser) && !TextUtils.isEmpty(strPassword)) {
            fAuth.signInWithEmailAndPassword(strUser, strPassword)
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            fUser = fAuth.getCurrentUser();
                            docRef = firebaseFirestore.collection("Users").document(fUser.getUid());
                            docRef.get().addOnSuccessListener(getActivity(), new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if (documentSnapshot.getData().get("isStudent") != null) {
                                        Intent homeIntent = new Intent(getActivity(), StudentHomeActivity.class);
                                        startActivity(homeIntent);
                                    } else {
                                        Toast.makeText(getActivity(), "Kullanıcı adı veya şifre hatalı", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(getActivity(), "Kullanıcı adı veya şifre boş", Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonRegisterPage() {
        Intent registerIntent = new Intent(getContext(), RegisterActivity.class);
        startActivity(registerIntent);
    }

}