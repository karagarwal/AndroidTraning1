package com.agarwal.training1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class RegisterActivity extends Activity {
    private Spinner spinner;
    private EditText mName, mEmail, mPassword, mConfirmPassword, mDOB;
    //Spinner Gender;
    String name, email, password, confirmPassword, gender, dob;
    ArrayAdapter<CharSequence> adapter;
    private ProgressDialog progressDialog;
    //private TextView mDisplayDate;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private DatePickerDialog.OnDateSetListener mDateListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mName = findViewById(R.id.editName);
        mEmail = findViewById(R.id.editMail);
        mPassword = findViewById(R.id.editPass);
        mConfirmPassword = findViewById(R.id.editCPass);
       // Gender = findViewById(R.id.etGender);
        mDOB = findViewById(R.id.slDob);

        /*---------------Spinner----------*/
        spinner = findViewById(R.id.etGender);
        adapter = ArrayAdapter.createFromResource(this,R.array.select_gender,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Gender");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                 gender = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        /*---------------------*/

        mDOB = findViewById(R.id.slDob);

        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal =Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this,
                        android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
                        mDateListner,year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        mDateListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String date = day + "/" + month + "/" + year;
                mDOB.setText(date);

            }
        };
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void onRegister(View view) {

        name=mName.getText().toString().trim();
        email=mEmail.getText().toString().trim();
        password=mPassword.getText().toString().trim();
        confirmPassword=mConfirmPassword.getText().toString().trim();
        dob=mDOB.getText().toString().trim();

        if(TextUtils.isEmpty(name) ||TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(dob)){
            Toast.makeText(RegisterActivity.this, "Please fill all details", Toast.LENGTH_SHORT).show();
        }
        else{
            if(name.contains("'")){
                Toast.makeText(RegisterActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
            }else
            if(!isValidEmail(email)){
                Toast.makeText(this,"Enter Correct Mail ID",Toast.LENGTH_SHORT).show();
            }else
            if(password.length()<6){
                Toast.makeText(RegisterActivity.this, "Password at least 6 character long", Toast.LENGTH_SHORT).show();
            }else
            if(!TextUtils.equals(password,confirmPassword)){
                Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            }else{
                progressDialog.setMessage("Registering Please wait....");
                progressDialog.setTitle("Register");
                progressDialog.show();
                progressDialog.setCancelable(false);

                final HashMap<String, String> dataMap = new HashMap<String, String>();
                dataMap.put("Name",name);
                dataMap.put("Email",email);
                dataMap.put("Gender",gender);
                dataMap.put("DOB",dob);


                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String key = storeDatabase(email,password);
                        dataMap.put("UUid",key);
                        mDatabase.child(key).setValue(dataMap);
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(intent);

                    }
                    else{
                        progressDialog.dismiss();
                        try{
                            throw task.getException();
                        }
                        catch (FirebaseAuthUserCollisionException e){
                           Toast.makeText(RegisterActivity.this, "User Already Registered", Toast.LENGTH_SHORT).show();
                           mEmail.requestFocus();
                        } catch (Exception e) {
                           Toast.makeText(RegisterActivity.this, "Something went wrong Please try again Later", Toast.LENGTH_SHORT).show();
                        }
                        }
                    }
                });
            }
        }
    }

    private String storeDatabase(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password);
        String user_uid = firebaseAuth.getCurrentUser().getUid();
        firebaseAuth.signOut();
        return user_uid;
    }

    public void closeBox(View view)  {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        RegisterActivity.this.finish();
    }
}

