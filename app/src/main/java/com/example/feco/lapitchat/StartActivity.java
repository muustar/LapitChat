package com.example.feco.lapitchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StartActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private Button regBtn, loginBtn;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "FECO";
    private SignInButton mGoogleSignIn;


    FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private Random r = new Random();
    private String[] mFunnyStrings;
    private DatabaseReference mUsersDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        funnyStringInicializalas();

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        regBtn = (Button) findViewById(R.id.start_reg_btn);
        loginBtn = (Button) findViewById(R.id.start_login_btn);
        mGoogleSignIn = (SignInButton) findViewById(R.id.start_google_signin);


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
                //finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        mGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }


    private void signIn() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Toast.makeText(this, "email: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String display_name = acct.getDisplayName();
                                String image = acct.getPhotoUrl().toString();

                                // a statusnak beállítunk egy véletlen funnystringet
                                int funnyPosition = r.nextInt(mFunnyStrings.length)+1;
                                String funnyStatus = mFunnyStrings[funnyPosition];

                                final Map<String, String> userMap = new HashMap<>();
                                userMap.put("name", display_name);
                                userMap.put("status", funnyStatus);
                                userMap.put("image",image);
                                userMap.put("image_thumb",image);
                                userMap.put("email", user.getEmail());
                                userMap.put("uid", user.getUid());
                                userMap.put("device_token", deviceToken);

                                DatabaseReference mLetezike = FirebaseDatabase.getInstance().getReference().child("Users");

                                mLetezike.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()){
                                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                       tovabbAFokepernyore();


                                                    }
                                                }
                                            });
                                        }else{

                                            // ------------- device token ---------------------

                                            mUsersDatabase.child(user.getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    tovabbAFokepernyore();

                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void tovabbAFokepernyore() {
        Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
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
