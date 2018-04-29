package com.example.feco.lapitchat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final int GALLERY_PICK = 9;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersDatabase;
    private StorageReference mStorageRef;

    private CircleImageView mImage;
    private TextView mDisplayname, mStatus;
    private Button mChangeImageBtn, mChangeStatusBtn;
    private String status;
    private Uri resultUri;
    private String TAG = "FECO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayname = findViewById(R.id.settings_displayname);
        mStatus = findViewById(R.id.settings_status);
        mChangeImageBtn = findViewById(R.id.settings_changeImage);
        mChangeStatusBtn = findViewById(R.id.settings_changeStatus);
        mImage = findViewById(R.id.settings_image);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String image_thumb = dataSnapshot.child("image_thumb").getValue().toString();

                mDisplayname.setText(name);
                mStatus.setText(status);
                Glide.with(SettingsActivity.this).load(image_thumb).into(mImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
            }
        });

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status", status);
                startActivity(statusIntent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(data.getData())
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mImage.setImageURI(resultUri);
                StorageReference filePath = mStorageRef.child(mCurrentUser.getUid() + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                mUsersDatabase.child("image").setValue(downloadUrl);
                                mUsersDatabase.child("image_thumb").setValue(downloadUrl);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, error.getMessage().toString());
            }
        }
    }
}
