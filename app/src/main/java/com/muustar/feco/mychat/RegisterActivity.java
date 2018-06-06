package com.muustar.feco.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private TextInputLayout mDisplayname, mEmail, mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;

    private String DEFAULT_IMAGE_ON_STORAGE = "https://firebasestorage.googleapis.com/v0/b/lapitchat-9a229.appspot.com/o/profile_images%2Fdefault%2Fdefault.png?alt=media&token=c25f4f16-74c7-4952-8657-efad8a7f3098";
    private String[] mFunnyStrings;
    private Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_register);

        funnyStringInicializalas();

        //firebase section
        mAuth = FirebaseAuth.getInstance();


        // displeay elements imitializing
        mProgressBar = findViewById(R.id.reg_progressbar);
        mToolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.create_account);
        // setDisplayHomeAsUpEnabled(true) engedélyezi, hogy az appbaron ott legyen bal oldalt a kicsi nyíl
        // a manifestben meg kell határozni, ki a parent, oda mutat a visszanyíl
        //  <activity android:name=".RegisterActivity"
        //      android:parentActivityName=".StartActivity"></activity>
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDisplayname = findViewById(R.id.reg_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreateBtn = findViewById(R.id.reg_cerate_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayname.getEditText().getText().toString().trim();
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    register_user(display_name, email, password);
                }
            }
        });

    }


    private void register_user(final String display_name, String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String uid = currentUser.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            // a statusnak beállítunk egy véletlen funnystringet
                            int funnyPosition = r.nextInt(mFunnyStrings.length) + 1;
                            String funnyStatus = mFunnyStrings[funnyPosition];

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status", funnyStatus);
                            userMap.put("image", DEFAULT_IMAGE_ON_STORAGE);
                            userMap.put("image_thumb", DEFAULT_IMAGE_ON_STORAGE);
                            userMap.put("email", currentUser.getEmail());
                            userMap.put("uid", currentUser.getUid());
                            userMap.put("device_token", deviceToken);
                            userMap.put("email_visible", false);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        tovabbAFokepernyore();
                                    }
                                }
                            });


                        } else {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void tovabbAFokepernyore() {
        Toast.makeText(RegisterActivity.this, "sikeres regisztráció ", Toast.LENGTH_SHORT).show();
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }


    private void funnyStringInicializalas() {
        mFunnyStrings = new String[]{

                "A big shout out to ATM fees for making me buy my own money!",
                "Hi I'm James, let's bond.",
                "T.G.I.A. (Thank goodness I'm awesome!)",
                "Sometimes I prefer to use my face as emoticons.",
                "I think it's really cool how the word \"OK\" is a sideways person!",
                "Today is the first day of the rest of your life...And if that doesn't work out for you, tomorrow is the first day of the rest of your life...",
                "Keep calm and know Google can help you find a way to fix almost every problem. If not it will tell you who can fix it.",
                "It hurts when you go to unfriend someone and you find they've beat you to it!",
                "If twitter wasn't around in the olden days why is there a hashtag button on landlines?",
                "Time is precious. Waste it wisely.",
                "If something's not going right, try left.",
                "About to dance my feet silly!",
                "Why bother reading books? We have Eminem he can read a whole story in 4 minutes.",
                "I tried being awesome today, but I was just so tired from being awesome yesterday.",
                "Wife: I'm pregnant, what do you want it to be? Husband: A joke.",
                "Everyone is normal until you add them as your Facebook friend.",
                "Everyone is normal until you find them on Twitter.",
                "Relationship Status: COMING SOON",
                "Back in 5 minutes (If not, read this status again).",
                "LIKE if you hate it when someone tags you in a photo you look horrible in because they happen to look so good in it.",
                "Phew! Thank you, warning label. I was actually considering using my toaster in the shower this morning.",
                "Looking at school books and thinking - What a waste of a tree!",
                "Nobody around here treats me like a glamour model, so I'm just going to sit here taking selfies by myself.",
                "Why didn't you reply to my text? Well, how am I supposed to reply to LOL?",
                "Line dancing was originally invented by women waiting in line for the bathroom.",
                "Don't tell me the sky's the limit when there are footprints on the moon.",
                "Nothing is illegal...Until you get caught.",
                "Friends are like boobs... Some are real some are fake.",
                "Birthdays are good for your health. Studies show those who have more Birthdays live longer.",
                "Food is an important part of a balanced diet.",
                "When I get a pimple on my tongue I always feel guilty in case I've told a white lie.",
                "I dance like a car dealerships inflatable tube man.",
                "I forgot to work out today. That's 5 years in a row!",
                "If I went to hell, it would take me a week to realise I wasn't at work.",
                "I hate it when I'm singing a song and the artist gets the words wrong.",
                "That moment when you try talking to someone you're hot for and you say GFBLQRINABAH instead of \"I'm good thanks!\"",
                "You look like I need a drink.",
                "I wasn't drunk, I was just testing if the plant was as soft as my bed.",
                "That awkward moment when you have a crush on the most inconvenient person possible.",
                "I've got a dig bick. You read that wrong. The awkward when you read that wrong too and said 'Moment ' when it wasn' t there.",
                "I put the 'Me' in 'Someone' and things get awkward.",
                "Stop calling yourself hot, the only thing you turn on is the microwave !",
                "That moment when someone you met for 3 seconds sends you a Facebook friend request.",
                "I tried being normal once. Most boring hour of my life.",
                "You didn't notice that that I used a word twice in this sentence.",
                "A fact of life: After Monday and Tuesday even the calender says W T F.",
                "The first 5 days after the weekend are always hard.",
                "I am 100% done with today and about 37% done with tomorrow.",
                "At first I didn't like my beard, then it grew on me.",
                "Broken pencils are pointless.",
                "\"What's up cake?\" - \"Muffin much\".",
                "I don't have goals. Goals are for soccer. I' m not soccer."

        };
    }
}
