package com.pmmb.moneysway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.pmmb.moneysway.models.User;


public class SignUpActivity extends AppCompatActivity {

    private TextView signUpCountryCodeTextView;
    private EditText signUpPhoneNumberEditText, signUpOtpEditText;
    private Button signUpButton, signUpVerifyButton;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        signUpCountryCodeTextView = findViewById(R.id.signUpCountryCodeTextView);
        signUpPhoneNumberEditText = findViewById(R.id.signUpPhoneNumberEditText);
        signUpOtpEditText = findViewById(R.id.signUpOtpEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signUpVerifyButton = findViewById(R.id.signUpVerifyButton);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                String code = credential.getSmsCode();
                if (code != null) {
                    signUpOtpEditText.setText(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(SignUpActivity.this, "OTP has been sent!", Toast.LENGTH_SHORT).show();
                signUpPhoneNumberEditText.setEnabled(false);
                signUpButton.setVisibility(View.INVISIBLE);
                signUpVerifyButton.setVisibility(View.VISIBLE);
                signUpOtpEditText.setVisibility(View.VISIBLE);
                signUpOtpEditText.requestFocus();
                mVerificationId = s;
            }
        };
    }

    public void onClickSignInNowButton(View view) {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    public void onClickSignUpButton(View view) {
        final String phoneNumber = signUpPhoneNumberEditText.getText().toString();

        // Check if user is already registered

        mAuth
                .fetchSignInMethodsForEmail(phoneNumber + "@" + getResources().getString(R.string.app_name).toLowerCase() + ".com")
                .addOnCompleteListener( new OnCompleteListener<SignInMethodQueryResult>(){
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean exist = !task.getResult().getSignInMethods().isEmpty();
                        if(exist){
                            Snackbar.make(findViewById(R.id.signUpParentLayout), "This phone number is already registered. Try signing in!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        else{
                            if (phoneNumber.length() != 10) {
                                signUpPhoneNumberEditText.setError("Invalid Phone Number Format");
                            } else {
                                startVerification(phoneNumber);
                            }
                        }
                    }
                });
    }

    public void onClickSignUpVerifyButton(View view) {
        String code = signUpOtpEditText.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            signUpOtpEditText.setError("Enter valid code");
            signUpOtpEditText.requestFocus();
            return;
        }
        verifyVerificationCode(code);
    }

    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        credential.getSmsCode();
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Verification successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, PasswordAndUserDetailsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("phoneNumber", signUpPhoneNumberEditText.getText().toString());
                            startActivity(intent);

                        } else {

                            String message = "Verification unsuccessful! Something is wrong, we will fix it soon.";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Verification unsuccessful! Invalid code entered.";
                            }

                            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();


                        }
                    }
                });
    }

    private void startVerification(String phoneNumber) {
        String countryCode = signUpCountryCodeTextView.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                countryCode + phoneNumber,            // Phone number to verify
                60,                                   // Timeout duration
                TimeUnit.SECONDS,                        // Unit of timeout
                this,                             // Activity (for callback binding)
                mCallbacks);                             // OnVerificationStateChangedCallbacks
    }
}