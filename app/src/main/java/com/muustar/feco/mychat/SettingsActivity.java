package com.muustar.feco.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final int GALLERY_PICK = 9;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersDatabase;
    private StorageReference mProfileImagesRef;

    private CircleImageView mImage;
    private TextView mDisplayname, mUserEmailAddress, mStatus, mDeleteProfile;
    private EditText mNewDisplayName, mNewStatus;
    private RelativeLayout mBackground;
    private Button mChangeEmailBtn;
    private ProgressBar mProgressBar;
    private Switch mEmailSwitch, mVibrateSwitch;
    private String status;
    private Uri resultUri;
    private String TAG = "FECO";
    private String displayname;
    private String mTaroltImage;
    private String mTaroltImage_thumb;
    private SharedPreferences mSharedProfileSettingsPref;
    private ValueEventListener felhasznaloAdataiValueListener;

    // splash time settings
    private SeekBar mSeekbarSplash;
    private TextView mTextSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.settings_appbar));
        getSupportActionBar().setTitle(R.string.profile_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedProfileSettingsPref = getSharedPreferences("Plinng", MODE_PRIVATE);

        mDisplayname = findViewById(R.id.settings_displayname);
        mUserEmailAddress = findViewById(R.id.settings_emailaddress);
        mStatus = findViewById(R.id.settings_status);
        mChangeEmailBtn = findViewById(R.id.settings_changeEmail);
        mImage = findViewById(R.id.settings_image);
        mProgressBar = findViewById(R.id.settings_progressBar);
        mEmailSwitch = findViewById(R.id.settings_switch_email);
        mVibrateSwitch = findViewById(R.id.settings_switch_vibrate);
        mDeleteProfile = findViewById(R.id.settings_deleteprofile);
        mNewDisplayName = (EditText) findViewById(R.id.settings_displayname_edit);
        mNewStatus = findViewById(R.id.settings_status_edttext);
        mBackground = (RelativeLayout) findViewById(R.id.settings_background);
        mSeekbarSplash = findViewById(R.id.settings_splash_time);
        mTextSplash = findViewById(R.id.settings_splash_text);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child
                (currentUserId);
        mUsersDatabase.keepSynced(true);
        mProfileImagesRef = FirebaseStorage.getInstance().getReference().child("profile_images");



        // amikor megnyílik az Activity ez a metódis tölti fel
        kezdetiAdatokBetoltese();

        // splash time beállító csúszka
        splashInit();

        // email csere gomb figyelése
        emailCsere();

        // email láthatóságának beállítása - figyelése
        emaiLathatosag();

        //vibrációs beállítások
       vibraciosBeallitasok();

        // kép megváltoztatása a képre kattintással
       kepcsereEljaras();

        //profil törlése gomb figyelése
        profilTorlesFigyeles();

        // status megváltoztatása
        statusAtirasEljaras();

        // Display name megváltoztatása
        displayNameAtirasEljaras();

        // háttéren való kattintás figyelés
        backgroundClick();
    }

    private void backgroundClick() {
        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewDisplayName.setVisibility(View.INVISIBLE);
                mDisplayname.setVisibility(View.VISIBLE);
                mNewStatus.setVisibility(View.INVISIBLE);
                mStatus.setVisibility(View.VISIBLE);
                //billentyűzet elrejtése
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mBackground.getWindowToken(), 0);
            }
        });
    }

    private void displayNameAtirasEljaras() {
        mDisplayname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayname = mDisplayname.getText().toString();
                mDisplayname.setVisibility(View.INVISIBLE);

                mNewDisplayName.setVisibility(View.VISIBLE);
                mNewDisplayName.setText(displayname);

                //show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                int textLength = mNewDisplayName.getText().length();
                mNewDisplayName.setSelection(0, textLength);
                mNewDisplayName.setCursorVisible(true);
            }
        });
        mNewDisplayName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mNewDisplayName.setVisibility(View.INVISIBLE);
                    mDisplayname.setVisibility(View.VISIBLE);
                }
            }
        });
        mNewDisplayName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        newNameValidate();
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });
    }

    private void profilTorlesFigyeles() {
        mDeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deleteprofileIntent = new Intent(SettingsActivity.this,
                        DeleteProfileActivity.class);
                finish();
                startActivity(deleteprofileIntent);
            }
        });
    }

    private void emailCsere() {
        mChangeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newemailIntent = new Intent(SettingsActivity.this, ChangeEmailActivity
                        .class);
                startActivity(newemailIntent);
                //finish();
            }
        });
    }

    private void emaiLathatosag(){
        mEmailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUsersDatabase.child("email_visible").setValue(isChecked);
            }
        });
    }

    private void kepcsereEljaras() {
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),
                        GALLERY_PICK);
            }
        });
    }

    private void statusAtirasEljaras() {
        // status változó tárolja
        mStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatus.setVisibility(View.INVISIBLE);
                mNewStatus.setVisibility(View.VISIBLE);
                mNewStatus.setText(status);

                //show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                int textLength = mNewStatus.getText().length();
                mNewStatus.setSelection(0, textLength);
                mNewStatus.setCursorVisible(true);
            }
        });
        mNewStatus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mNewStatus.setVisibility(View.INVISIBLE);
                    mStatus.setVisibility(View.VISIBLE);
                }
            }
        });
        mNewStatus.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        newStatusValidate();
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }

            private void newStatusValidate() {
                //beirt szöveg ellenőrzése, mentése és billenytűzet elrejtése, vissza állítjuk a
                // textview-t
                String newStatusText = mNewStatus.getText().toString().trim();
                if (!TextUtils.isEmpty(newStatusText)) {
                    mStatus.setText(newStatusText);
                    mNewStatus.setVisibility(View.GONE);
                    mStatus.setVisibility(View.VISIBLE);
                    //billentyűzet elrejtése
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context
                            .INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mNewDisplayName.getWindowToken(), 0);
                    ujStatusTarolas(newStatusText);
                }
            }

            private void ujStatusTarolas(String newStatusText) {
                if (!TextUtils.equals(displayname, newStatusText)) {
                    mUsersDatabase.child("status").setValue(newStatusText);
                }
            }
        });

    }

    private void vibraciosBeallitasok() {
        //vibrációs beállítások
        mVibrateSwitch.setChecked(mSharedProfileSettingsPref.getBoolean("vibrate", true));
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = mSharedProfileSettingsPref.edit();
                editor.putBoolean("vibrate", isChecked);
                editor.commit();
            }
        });
    }

    private void kezdetiAdatokBetoltese() {
        mProgressBar.setVisibility(View.VISIBLE);
        felhasznaloAdataiValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    User u = dataSnapshot.getValue(User.class);
                    String name = dataSnapshot.child("name").getValue().toString();
                    status = dataSnapshot.child("status").getValue().toString();
                    mTaroltImage = dataSnapshot.child("image").getValue().toString();
                    mTaroltImage_thumb = dataSnapshot.child("image_thumb").getValue().toString();
                    mUserEmailAddress.setText(u.getEmail());
                    if (dataSnapshot.child("email_visible").exists()) {
                        Boolean email_visible = u.getEmail_visible();
                        mEmailSwitch.setChecked(email_visible);
                    }

                    mDisplayname.setText(name);
                    mStatus.setText(status);

                    GlideApp.with(getApplicationContext())
                            .load(mTaroltImage_thumb)
                            .placeholder(R.mipmap.ic_placeholder_face)
                            .error(R.mipmap.ic_placeholder_face)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object
                                        model, Target<Drawable> target, boolean isFirstResource) {
                                    mProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target,
                                                               DataSource dataSource, boolean
                                                                       isFirstResource) {
                                    mProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(mImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void splashInit() {
         /*
        pozíciók:
        200
        500
        1000
        2000
         */

        switch (Constant.mVisibleTime) {
            case 200:
                mSeekbarSplash.setProgress(0);
                mTextSplash.setText("0.2 s");
                break;
            case 500:
                mSeekbarSplash.setProgress(1);
                mTextSplash.setText("0.5 s");
                break;
            case 1000:
                mSeekbarSplash.setProgress(2);
                mTextSplash.setText("1 s");
                break;
            case 2000:
                mSeekbarSplash.setProgress(3);
                mTextSplash.setText("2 s");
                break;
        }


        mSeekbarSplash.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        Constant.mVisibleTime = 200;
                        mTextSplash.setText("0.2 s");
                        break;
                    case 1:
                        Constant.mVisibleTime = 500;
                        mTextSplash.setText("0.5 s");
                        break;
                    case 2:
                        Constant.mVisibleTime = 1000;
                        mTextSplash.setText("1 s");
                        break;
                    case 3:
                        Constant.mVisibleTime = 2000;
                        mTextSplash.setText("2 s");
                        break;
                }
                SharedPreferences.Editor editor = mSharedProfileSettingsPref.edit();
                editor.putInt("splash_time", Constant.mVisibleTime);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void newNameValidate() {
        //beirt szöveg ellenőrzése, mentése és billenytűzet elrejtése, vissza állítjuk a textview-t
        String newDisplayName = mNewDisplayName.getText().toString().trim();
        if (!TextUtils.isEmpty(newDisplayName)) {
            mDisplayname.setText(newDisplayName);
            mNewDisplayName.setVisibility(View.GONE);
            mDisplayname.setVisibility(View.VISIBLE);
            //billentyűzet elrejtése
            InputMethodManager imm = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mNewDisplayName.getWindowToken(), 0);
            ujNevTarolas(newDisplayName);
        }
    }

    private void ujNevTarolas(String newName) {
        if (!TextUtils.equals(displayname, newName)) {
            mUsersDatabase.child("name").setValue(newName);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(data.getData())
                    .setCropShape(CropImageView.CropShape.OVAL)
                    //.setAspectRatio(16, 9)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setInitialCropWindowPaddingRatio(0)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);

                // elmentjük az új képet a szerveren, a régi képet nem kell törölni, mert azonos
                // névvel kerül mentésre az új kép, ez felülírja a korábbit.
                resultUri = result.getUri();
                //mImage.setImageURI(resultUri);

                // thumb kép tömörítése
                File thumb_filepath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(10)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                final StorageReference thumb_filePathRef = mProfileImagesRef.child("thumbs")
                        .child(mCurrentUser.getUid() + ".jpg");

                // jobb méretű kép tömörítése
                File img_filepath = new File(resultUri.getPath());
                Bitmap img_bitmap = null;
                try {
                    img_bitmap = new Compressor(this)
                            .setQuality(80)
                            .compressToBitmap(img_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final ByteArrayOutputStream imgbaos = new ByteArrayOutputStream();
                img_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgbaos);
                final byte[] img_byte = baos.toByteArray();

                StorageReference imgPath = mProfileImagesRef.child(mCurrentUser.getUid() + ".jpg");
                UploadTask uploadTask_imgPath = imgPath.putBytes(img_byte);
                uploadTask_imgPath.addOnCompleteListener(new OnCompleteListener<UploadTask
                        .TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // a nagyméretű profil kép mentve, bejegyezzük az url-jét
                            final String image_downloadUrl = task.getResult().getDownloadUrl()
                                    .toString();

                            // a thumb feltöltése
                            UploadTask uploadTask_thumbImage = thumb_filePathRef.putBytes
                                    (thumb_byte);
                            uploadTask_thumbImage.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot>
                                                               task) {
                                    if (task.isSuccessful()) {
                                        String image_thumbDownlUrl = task.getResult()
                                                .getDownloadUrl().toString();

                                        Map imageUrlMap = new HashMap<>();
                                        imageUrlMap.put("image", image_downloadUrl);
                                        imageUrlMap.put("image_thumb", image_thumbDownlUrl);

                                        mUsersDatabase.updateChildren(imageUrlMap);
                                        //mUsersDatabase.child("image_thumb").setValue
                                        // (image_thumbDownlUrl);
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, error.getMessage().toString());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // chehck the user logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child
                    ("Users").child(currentUser.getUid());
            mUserDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsersDatabase.addValueEventListener(felhasznaloAdataiValueListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUsersDatabase.removeEventListener(felhasznaloAdataiValueListener);
    }
}
