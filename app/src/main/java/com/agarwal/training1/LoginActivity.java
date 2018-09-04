package com.agarwal.training1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class LoginActivity extends Activity {

    EditText Email, Password;
    String email, password;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        Email = findViewById(R.id.etUsername);
        Password = findViewById(R.id.etPassword);
        Button close = (Button) findViewById(R.id.closeBt);
        Button login = (Button) findViewById(R.id.loginBt);

        if (isConnected(LoginActivity.this)==false){
            buildDialog(LoginActivity.this).show();
        }else {
            firebaseAuth = FirebaseAuth.getInstance();

            if(firebaseAuth.getCurrentUser()!=null){
                Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }



    }

    public void closeBox(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("EXIT");
        builder.setMessage("Are you sure want to exit? ");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.exit);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAndRemoveTask();
                else
                    finish();
                System.exit(0);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void createUser(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View view) {
         email = Email.getText().toString().trim();
         password = Password.getText().toString().trim();

            if(TextUtils.isEmpty(email)|| TextUtils.isEmpty(password)){
                Toast.makeText(LoginActivity.this,"Please fill Username and Password",Toast.LENGTH_SHORT).show();
            }else{
                progressDialog.setMessage("Logging Please wait....");
                progressDialog.setTitle("Login");
                progressDialog.show();
                progressDialog.setCancelable(false);

                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else {
                            progressDialog.dismiss();
                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e){
                                Toast.makeText(LoginActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                            }
                            catch (FirebaseAuthInvalidCredentialsException e){
                                Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                               Toast.makeText(LoginActivity.this, "Something went wrong Please try again Later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                /* TODO:Login Verification*/

            }


    }

    public void forgetPass(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgetPassword.class);
        startActivity(intent);
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            android.net.NetworkInfo bt = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()) || (bt != null && bt.isConnectedOrConnecting()))
                return true;
            else
                return false;
        } else
            return false;
    }
    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please enable WiFi or Mobile Data to Continue.");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.no_network);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAndRemoveTask();
                else
                    finish();
            }
        });
        builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!isConnected(LoginActivity.this)) {
                    buildDialog(LoginActivity.this).show();
                }
            }
        });

        return builder;
    }
}
