package com.agarwal.training1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
        private TextView textViewUser;
        private TextView textViewEmail;
        private String user_Uid, user_Email, user_Name;
        private ListView mUserList;
        private RecyclerView mrecyclerView;
        private ImageView user_Image;
        private FirebaseAuth firebaseAuth;
        private DatabaseReference mDatabaseRef, imageRef;
        private ProgressDialog progressDialog;
        private NavigationView navigationView;
        private StorageReference mStorage;
        private List<String> fileNameList;
        private List<String> fileDoneList;
        private ArrayList<String> images_filepath = new ArrayList<String>();
        private final int RESULT_UPLOAD_PROFILE_IMAGE = 1;
        private final int RESULT_UPLOAD_MULTIPLE_IMAGES = 2;
        private final static int MY_PERMISSION_STORAGE = 101;
        private FirebaseRecyclerAdapter<UserImages,UserImageHolder> imageAdapter;
        private FirebaseRecyclerAdapter<UserInfo,UserInfoHolder> infoAdapter;
        private UploadImageAdapter uploadImageAdapter;

        private String[] galleryPermissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home_screen);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.keepSynced(true);
        imageRef = FirebaseDatabase.getInstance().getReference();
        imageRef.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        navigationView = findViewById(R.id.nav_view);
        View header=navigationView.getHeaderView(0);
        mrecyclerView = findViewById(R.id.userImagesRecyclerView);
        mrecyclerView.setHasFixedSize(true);
        mrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseAuth = FirebaseAuth.getInstance();
        textViewUser = findViewById(R.id.userName);
        textViewEmail = header.findViewById(R.id.userEmailId);
        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        uploadImageAdapter = new UploadImageAdapter(fileNameList,fileDoneList);
        user_Image = header.findViewById(R.id.profileImageView);

        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        }

        profileInNavHeader();       //Setting User Profile Info to Navigation Header
        allUsersInfo();             // By Default Showing all Users

        /*--------------------Floating Action Button-------------------------------------*/

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab =  findViewById(R.id.uploadButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EasyPermissions.hasPermissions(HomeScreenActivity.this, galleryPermissions)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Images"),RESULT_UPLOAD_MULTIPLE_IMAGES);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSION_STORAGE);
                    }
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    /*---------------------------------------------------------*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_logout) {
            buildDialog(HomeScreenActivity.this,"Logout").show();
        }
        if (id == R.id.action_layout) {
           if(TextUtils.equals(item.getTitle(),"Grid View")){
               item.setTitle("List View");
               mrecyclerView.setLayoutManager(new GridLayoutManager(this,2 ));

           }
            else{
                item.setTitle("Grid View");
                mrecyclerView.setLayoutManager(new LinearLayoutManager(this));
           }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_user_image) {
            if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
                Toast.makeText(this,"Select Image",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_UPLOAD_PROFILE_IMAGE);

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSION_STORAGE);
                }
            }


        } else if (id == R.id.nav_gallery) {
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String userID = null;
            if(firebaseUser != null) {
                userID = firebaseUser.getUid();
            }
            allUserImages(userID);

        } else if (id == R.id.nav_all_users) {
            allUsersInfo();

        } else if (id == R.id.nav_change_password) {
            Intent intent = new Intent(HomeScreenActivity.this,ChangePasswordActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.quit){
            buildDialog(HomeScreenActivity.this,"Quit").show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*-------------------------Uploading Profile Pic to Firebase and imageView----------------------------------*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode){
            case RESULT_UPLOAD_PROFILE_IMAGE:
                if (requestCode == RESULT_UPLOAD_PROFILE_IMAGE && resultCode == RESULT_OK && null != data) {
                    progressDialog.setTitle("Updating");
                    progressDialog.setMessage("Updating Profile Picture please wait..");
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    StorageReference filepath = mStorage.child(userUid).child("Profile").child("profile_pic.jpg");

                    Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    byte[] byteData = baos.toByteArray();
                    UploadTask uploadTask2 = filepath.putBytes(byteData);

                    uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(HomeScreenActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeScreenActivity.this,"Something went wrong Please try again later",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    user_Image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }else{
                    Toast.makeText(this,"Something went Wrong PLease try again Later",Toast.LENGTH_SHORT).show();
                }
            break;

            case RESULT_UPLOAD_MULTIPLE_IMAGES:
                  if (resultCode == RESULT_OK && data != null) {
                    if(data.getClipData()!=null){
                        int SelectionCount = data.getClipData().getItemCount();
                        for(int i = 0 ; i<SelectionCount; i++){
                            Uri fileUri = data.getClipData().getItemAt(i).getUri();
                            String fileName = getFileName(fileUri);

                            Bitmap bmp = null;
                            try {
                                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),fileUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                            byte[] byteData = baos.toByteArray();

                            uploadImageAdapter.notifyDataSetChanged();
                            StorageReference StorageUri = mStorage.child(user_Uid).child("Images").child(fileName);

                            progressDialog.setTitle("Uploading");
                            progressDialog.setMessage("Uploading Images please wait..");
                            progressDialog.show();
                            progressDialog.setCancelable(false);

                            UploadTask uploadTask2 = StorageUri.putBytes(byteData);
                            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                     Toast.makeText(HomeScreenActivity.this,"Uploading Done",Toast.LENGTH_SHORT).show();
                                    mDatabaseRef.child(user_Uid+"/Images/").push().child("ImageURL").setValue(taskSnapshot.getDownloadUrl().toString());
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(HomeScreenActivity.this,"Something went wrong Please try again Later",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }

                    } else
                        if(data.getData()!=null){
                            Uri fileUri = data.getData();
                            String fileName = getFileName(fileUri);

                            Bitmap bmp = null;
                            try {
                                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),fileUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                            byte[] byteData = baos.toByteArray();

                            uploadImageAdapter.notifyDataSetChanged();
                            StorageReference StorageUri = mStorage.child(user_Uid).child("Images").child(fileName);
                            progressDialog.setTitle("Uploading");
                            progressDialog.setMessage("Uploading Images please wait..");
                            progressDialog.show();
                            progressDialog.setCancelable(false);

                            UploadTask uploadTask2 = StorageUri.putBytes(byteData);
                            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.w("value","Uploading Done ");
                                        Toast.makeText(HomeScreenActivity.this,"Uploading Done",Toast.LENGTH_SHORT).show();
                                        mDatabaseRef.child(user_Uid+"/Images/").push().child("ImageURL").setValue(taskSnapshot.getDownloadUrl().toString());
                                        progressDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(HomeScreenActivity.this,"Something went wrong Please try again Later",Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                        }
                }
                break;
        }
    }

    /*---------------------------Permission Result--------------------------------*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case MY_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select Images"),RESULT_UPLOAD_MULTIPLE_IMAGES);
                    }
                }
                else {
                    Toast.makeText(HomeScreenActivity.this,"Please allow the Permission to continue",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /*-------------------------Logout/Quit Alert Box--------------------------------*/

    public AlertDialog.Builder buildDialog(Context context, final String action) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(action);
        builder.setCancelable(false);
        builder.setMessage("Are you sure to "+action);

        if(TextUtils.equals(action,"Logout")){
            builder.setIcon(R.drawable.logout_dialog);
        }
        else if(TextUtils.equals(action,"Quit")){
            builder.setIcon(R.drawable.exit);
        }

         builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(TextUtils.equals(action,"Logout")){
                    firebaseAuth.signOut();
                    Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    dialog.dismiss();
                    finish();
                    startActivity(intent);
                }
                else if(TextUtils.equals(action,"Quit")){
                    if (Build.VERSION.SDK_INT >= 21) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder;
    }

    /*-------------------------getting File  Name--------------------------------*/

    public String getFileName(Uri uri) {
        String fileName = null;
        if(uri.getScheme().equals("content"))
            if(fileName == null){
                fileName = uri.getPath();

                int cut = fileName.lastIndexOf('/');
                if(cut != -1){
                    fileName = fileName.substring(cut + 1);
                }
            }
        return fileName;
    }


    /*-------------------------Displaying User profile In NavHeaderx--------------------------------*/

    private void profileInNavHeader() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            user_Uid = firebaseUser.getUid();
            user_Email = firebaseUser.getEmail();

            mStorage.child(user_Uid+"/Profile/profile_pic.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Picasso.get().load(uri.toString()).placeholder(R.drawable.navuser).into(user_Image);
                }
            });
        }
        /*------------------Displaying user name in Home Activity-----------------------*/
        mDatabaseRef.child(user_Uid+"/Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w("error",dataSnapshot.toString());
                user_Name = dataSnapshot.getValue().toString().trim();
                //user_Name = "Hello";
                textViewUser.setText(user_Name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        textViewEmail.setText(user_Email);
    }

    /*-------------------------All users info --------------------------------*/

    private void allUsersInfo() {
                 infoAdapter = new FirebaseRecyclerAdapter<UserInfo, UserInfoHolder>
                (UserInfo.class,R.layout.info_row,UserInfoHolder.class,mDatabaseRef) {
            @Override
            protected void populateViewHolder(final UserInfoHolder viewHolder, final UserInfo model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setGender(model.getGender());
                viewHolder.setDob(model.getDob());

                mStorage.child(model.getUuid()+"/Profile/profile_pic.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        viewHolder.setUid(uri.toString());
                    }
                });
            }
        };
        mrecyclerView.setAdapter(null);
        mrecyclerView.setAdapter(infoAdapter);
    }

                         /*--------UserInfoHolder class-----------*/

    public static class UserInfoHolder extends RecyclerView.ViewHolder{
        View mView;

        public  UserInfoHolder(View itemView){
            super(itemView);
            mView=itemView;
        }
        public void setName(String Name){
            TextView userNameInfo = mView.findViewById(R.id.userNameInfo);
            userNameInfo.setText(Name);
        }
        public void setEmail(String Email){
            TextView userEmailInfo = mView.findViewById(R.id.userEmailInfo);
            userEmailInfo.setText(Email);
        }
        public void setGender(String Gender){
            TextView userGenderInfo = mView.findViewById(R.id.userGenderInfo);
            userGenderInfo.setText(Gender);
        }
        public void setDob(String DOB){
            TextView userDobInfo = mView.findViewById(R.id.userDobInfo);
            userDobInfo.setText(DOB);
        }
        public void setUid(String url){
            ImageView image = mView.findViewById(R.id.cardImage);
            Picasso.get().load(url).placeholder(R.drawable.navuser).fit().into(image);
        }
    }

    /*-------------------------All user Images --------------------------------*/

    private void allUserImages(String userID) {
     imageRef = imageRef.child(userID).child("Images");
     imageAdapter = new FirebaseRecyclerAdapter<UserImages, UserImageHolder>
             (UserImages.class,R.layout.image_row,UserImageHolder.class,imageRef) {
            @Override
            protected void populateViewHolder(final UserImageHolder viewHolder, UserImages model, int position) {
                viewHolder.setURL(model.getImageURL());
            }
        };

        mrecyclerView.setAdapter(null);
        mrecyclerView.setAdapter(imageAdapter);
    }

                        /*--------UserImageHolder class-----------*/

    public static class UserImageHolder extends RecyclerView.ViewHolder{
        View mView;

        public  UserImageHolder(View itemView){
            super(itemView);
            mView=itemView;
        }

        public void setURL(String url){
            ImageView image = mView.findViewById(R.id.cardViewImage);
            Picasso.get().load(url).placeholder(R.drawable.placeholder).fit().into(image);
        }
    }

}

