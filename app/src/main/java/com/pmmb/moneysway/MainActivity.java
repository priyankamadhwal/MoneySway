package com.pmmb.moneysway;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            Log.e("MainActivity", "Something went wrong!", e);
        }

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (currentUser!=null) {

                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }
                else  {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        }, 1500L);
    }
}
