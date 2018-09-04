package com.agarwal.training1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends Activity {

    private FirebaseUser firebaseUser;
    private EditText etNewPassword, etNewCPassword;
    private ProgressDialog progressDialog;
    String pass,Cpass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_password);

        progressDialog = new ProgressDialog(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        etNewPassword = findViewById(R.id.etPass);
        etNewCPassword = findViewById(R.id.etCPass);

    }

    public void changePassword(View view) {

        pass = etNewPassword.getText().toString().trim();
        Cpass = etNewCPassword.getText().toString().trim();

         if(TextUtils.isEmpty(pass) || TextUtils.isEmpty(Cpass)){
            Toast.makeText(ChangePasswordActivity.this,"Please fill all details",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.getTrimmedLength(pass) < 6){
            Toast.makeText(ChangePasswordActivity.this,"Password atleast 6 character long" ,Toast.LENGTH_SHORT).show();
        }
        else  if(!TextUtils.equals(pass,Cpass)){
            Toast.makeText(ChangePasswordActivity.this,"Password doesn't match",Toast.LENGTH_SHORT).show();
        }
        else{
            progressDialog.setMessage("Please Wait while password is updated");
            progressDialog.setTitle("Updating Password");
            progressDialog.setCancelable(false);
            progressDialog.show();

             firebaseUser.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this,"Password updated Successfully",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        etNewPassword.setText(null);
                        etNewCPassword.setText(null);
                        etNewCPassword.clearFocus();
                        etNewPassword.clearFocus();
                    }
                    else {
                       try{
                           throw task.getException();
                       }catch (Exception e){

                           Toast.makeText(ChangePasswordActivity.this, "Something went wrong Please try again Later", Toast.LENGTH_SHORT).show();
                           progressDialog.dismiss();

                       }
                    }
                }
            });
        }
    }
}
