package com.agarwal.training1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPassword extends Activity {
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private EditText Mail;
    String mailEntered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forget_password);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        Mail = findViewById(R.id.etMail);

    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void resetpassword(View view) {
        mailEntered=Mail.getText().toString();

        if(mailEntered.isEmpty()){
            Toast.makeText(ForgetPassword.this, "Please Enter Mail ID", Toast.LENGTH_SHORT).show();
        }
        else{
           if(!isValidEmail(mailEntered)){
               Toast.makeText(this,"Enter Correct Mail ID",Toast.LENGTH_SHORT).show();
           }
           else{
               progressDialog.setMessage("Sending Password Reset Link....");
               progressDialog.setTitle("Sending");
               progressDialog.show();

                firebaseAuth.sendPasswordResetEmail(mailEntered).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ForgetPassword.this,"Check your Email for Password Reset Link",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ForgetPassword.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            progressDialog.dismiss();
                            startActivity(intent);
                            finish();
                        }else {
                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e){
                                Toast.makeText(ForgetPassword.this, "Invalid User", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            catch (Exception e) {
                                Toast.makeText(ForgetPassword.this, "Something went wrong Please try again Later", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    }
                });


           }
        }


    }
}
