package com.example.afinal.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afinal.MainActivity;
import com.example.afinal.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    Button btn;
    EditText email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn=findViewById(R.id.btnLog);
        email=findViewById(R.id.etLogMail);
        password=findViewById(R.id.etLogPass);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String stremail=email.getText().toString().toLowerCase();
                String strpassword=password.getText().toString();

                if(stremail.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Mail girişini doldurun lütfen ",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(strpassword.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Şifre girişini doldurun lütfen ",Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth auth=FirebaseAuth.getInstance();
                auth.signInWithEmailAndPassword(stremail,strpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Hoş geldiniz. ",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }else{
                        Toast.makeText(getApplicationContext(),"Hata bulunmakta bilgileriniz kontrol edin. ",Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            }
        });
    }
}