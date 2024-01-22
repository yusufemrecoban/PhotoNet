package com.example.afinal.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afinal.R;
import com.example.afinal.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    Button btn;
    EditText name,email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn=findViewById(R.id.btnRe);
        name=findViewById(R.id.etReName);
        email=findViewById(R.id.etReMail);
        password=findViewById(R.id.etRePass);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strname=name.getText().toString();
                String stremail=email.getText().toString().toLowerCase();
                String strpass=password.getText().toString();

                if(stremail.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Mail boş",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(strpass.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Şifre boş",Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth auth= FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(stremail,strpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String uid = task.getResult().getUser().getUid();
                            Toast.makeText(getApplicationContext(),"Kayıt olundu.",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                            FirebaseFirestore db =FirebaseFirestore.getInstance();
                            CollectionReference ref= db.collection("UserModel");

                            UserModel user=new UserModel(strname,stremail);
                            ref.add(user);
                        }
                        else{

                            Toast.makeText(getApplicationContext(),"Kayıt başarız.",Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });
    }
}