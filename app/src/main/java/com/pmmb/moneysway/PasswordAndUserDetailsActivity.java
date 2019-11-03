package com.pmmb.moneysway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;


public class PasswordAndUserDetailsActivity extends AppCompatActivity {

    private EditText signUpUserNameEditText, signUpPasswordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_and_user_details);

        mAuth = FirebaseAuth.getInstance();

        signUpUserNameEditText = findViewById(R.id.signUpUserNameEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);

    }

    public void onClickDoneButton(View view) {
        String phoneNumber, name, password;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            phoneNumber = getIntent().getExtras().getString("phoneNumber");
            name = signUpUserNameEditText.getText().toString();
            password = signUpPasswordEditText.getText().toString();
            if (name.length()<3) {
                signUpUserNameEditText.setError("Enter name (atleast 3 characters)");
            }
            if (password.length()<6) {
                signUpPasswordEditText.setError("Enter password (atleast 6 characters)");
            }
            if (name.length()>=3 && password.length()>=6) {
                mAuth.createUserWithEmailAndPassword(phoneNumber + "@" + getResources().getString(R.string.app_name).toLowerCase() + ".com", password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Sign up successful!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(PasswordAndUserDetailsActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                else {
                                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                    Log.e("SignUp", "Failed Registration", e);
                                    Toast.makeText(getApplicationContext(), "Sign up failed! Please try again later", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
        else {
            Toast.makeText(this, "Something went wrong! Please try again later", Toast.LENGTH_LONG).show();
        }
    }
}
