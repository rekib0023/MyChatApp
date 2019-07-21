package com.example.mychatapp;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ProgressDialog mProgress;

    private ImageView mImageView;
    private TextView mProfileName, mProfileStatus, mProfileFriends;
    private Button mRequestBtn, mDeclineBtn;

    private DatabaseReference mRef;
    private DatabaseReference mFriendReqRef;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationRef;

    private FirebaseUser mCurrent_userr;

    private String mCurrent_state="";

    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mFriendReqRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationRef = FirebaseDatabase.getInstance().getReference().child("notifications");


        mCurrent_userr = FirebaseAuth.getInstance().getCurrentUser();


        mImageView = findViewById(R.id.user_profile_imageView);
        mProfileName = findViewById(R.id.user_profile_name);
        mProfileStatus = findViewById(R.id.user_profile_status);
        mRequestBtn = findViewById(R.id.user_sendRequest_btn);
        mDeclineBtn = findViewById(R.id.user_declineRequest_btn);


        if(!mCurrent_state.equals("req_sent")) {
            mCurrent_state = "not_friends";

            mDeclineBtn.setVisibility(View.INVISIBLE);
            mDeclineBtn.setEnabled(false);
        }


        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading user data...");
        mProgress.setMessage("Please wait while we load the user data");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("thumb").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);
                Picasso.get().setIndicatorsEnabled(false);
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar)
                        .into(mImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mImageView);
                            }
                        });


                // -------------- FRIENDS LIST / REQUEST FEATURE ---------------
                mFriendReqRef.child(mCurrent_userr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mRequestBtn.setBackgroundColor(Color.rgb(88,179,104));
                                mRequestBtn.setText("Accept Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if(req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mRequestBtn.setText("Cancel Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            mProgress.dismiss();
                        } else {
                            mProgress.dismiss();
                            mFriendsDatabase.child(mCurrent_userr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mRequestBtn.setBackgroundColor(Color.rgb(255, 85, 33));
                                        mRequestBtn.setText("Unfriend " + name);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgress.dismiss();
                    }
                });

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRequestBtn.setEnabled(false);

                // ------------- NOT FRIENDS STATE --------------

                if(mCurrent_state.equals("not_friends")){
                    mFriendReqRef.child(mCurrent_userr.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mFriendReqRef.child(user_id).child(mCurrent_userr.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> mNotificationData = new HashMap<>();
                                        mNotificationData.put("sent from", mCurrent_userr.getUid());
                                        mNotificationData.put("type", "request");

                                        mNotificationRef.child(user_id).push().setValue(mNotificationData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mCurrent_state = "req_sent";
                                                        mRequestBtn.setText("Cancel Request");

                                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                                        mDeclineBtn.setEnabled(false);

                                                    }
                                                });



                                        //Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_LONG).show();

                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_LONG).show();
                            }

                            mRequestBtn.setEnabled(true);

                        }
                    });
                }


                // ------------- REQUEST SENT STATE --------------

                if(mCurrent_state.equals("req_sent")){
                    mFriendReqRef.child(mCurrent_userr.getUid()).child(user_id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqRef.child(user_id).child(mCurrent_userr.getUid())
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mRequestBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mRequestBtn.setText("Send Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }


                // -------------- REQUEST RECEIVED --------------

                if(mCurrent_state.equals("req_received")){
                    //final String currentDate = DateFormat.getDateInstance().format(new Date());
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDatabase.child(mCurrent_userr.getUid()).child(user_id)
                            .setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsDatabase.child(user_id).child(mCurrent_userr.getUid())
                                            .setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqRef.child(mCurrent_userr.getUid()).child(user_id)
                                                            .removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqRef.child(user_id).child(mCurrent_userr.getUid())
                                                                    .removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mRequestBtn.setEnabled(true);
                                                                    mCurrent_state = "friends";
                                                                    mRequestBtn.setBackgroundColor(Color.rgb(255, 85, 33));
                                                                    mRequestBtn.setText("Unfriend " + name);

                                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                                    mDeclineBtn.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }




                // ---------------------FRIENDS STATE---------------
                if(mCurrent_state.equals("friends")){
                    mFriendsDatabase.child(mCurrent_userr.getUid()).child(user_id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsDatabase.child(user_id).child(mCurrent_userr.getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mRequestBtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mRequestBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                                    mRequestBtn.setText("Send Request");

                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineBtn.setEnabled(false);

                                                }
                                            });
                                }
                            });
                }


            }
        });


        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendReqRef.child(mCurrent_userr.getUid()).child(user_id)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendReqRef.child(user_id).child(mCurrent_userr.getUid())
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mRequestBtn.setEnabled(true);
                                                mCurrent_state = "not_friends";
                                                mRequestBtn.setText("Send Request");

                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);
                                            }
                                        });
                            }
                        });
            }
        });


    }
}
