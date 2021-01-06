package com.example.dell.a360degreefeedback;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatbotFinisher extends AppCompatActivity {
    DatabaseReference ref,m2Database,userRef,reportRef;
    float communication=0, leadership=0, management=0, emotionalquotient=0;
    float sum, sentiment;
    ArrayList<String> communcationSkills=new ArrayList<String>();
    ArrayList<Float> sumList=new ArrayList<Float>();
    ArrayList<String> leadershipSkills=new ArrayList<String>();
    ArrayList<String> timeManagement=new ArrayList<String>();
    ArrayList<String> emotionalQuotient=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot_finisher);

        Intent intent = getIntent();
        final String receiverEmail = intent.getStringExtra("receiver_email");
        final String giverEmail = intent.getStringExtra("giver_email");
        final String user = intent.getStringExtra("user_id");
        //Toast.makeText(getApplicationContext(), "yay"+receiverEmail + " " + giverEmail, Toast.LENGTH_LONG).show();

        //  changing givers status in users tree
        userRef = FirebaseDatabase.getInstance().getReference("/0/users").child(user).child("feedbackReceivers");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    feedbackGroup receivers = postSnapshot.getValue(feedbackGroup.class);
                    String receiver_Email = receivers.getrEmail();

                    if (receiver_Email.equals(receiverEmail)) {
                        // Toast.makeText(getApplicationContext(), " in here "+receiverEmail + " " + receiver_Email, Toast.LENGTH_LONG).show();
                        postSnapshot.getRef().child("status").setValue(1);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Changing attributes in feedbackGroupDetails and checking if completed
        ref = FirebaseDatabase.getInstance().getReference("/1/feedbackGroupDetails");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    feedbackGroup receivers = postSnapshot.getValue(feedbackGroup.class);
                    String rEmail = receivers.getrEmail();
                    String groupRef = postSnapshot.getKey();
                    if (rEmail.equals(receiverEmail)) {
                        int total = receivers.getTotal();
                        int completed = receivers.getCompleted();
                        // Toast.makeText(getApplicationContext(),receiverEmail+" "+rEmail+" "+total+" "+completed,Toast.LENGTH_SHORT).show();
                        postSnapshot.getRef().child("completed").setValue(++completed);
                        m2Database = FirebaseDatabase.getInstance().getReference("/1/feedbackGroupDetails").child(groupRef).child("giver");
                        m2Database.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    feedbackGroup giver = postSnapshot.getValue(feedbackGroup.class);
                                    String email = giver.getgEmail();
                                    if (email.equals(giverEmail)) {
                                        postSnapshot.getRef().child("status").setValue(1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        if (total == completed) {
                            postSnapshot.getRef().child("status").setValue(1);
                            sentimentAnalysis(receiverEmail);
                            //  Toast.makeText(getApplicationContext(), "Group Completed", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        startActivity(new Intent(getApplicationContext(),EmpPanel.class));
    }


    public void sentimentAnalysis(String receiverEmail)
    {       ref=FirebaseDatabase.getInstance().getReference("/2/feedbacks");
        String finalReceiverEmail = receiverEmail;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    feedbacks user = postSnapshot.getValue(feedbacks.class);
                    String email =user.getEmail();
                    if(email.equals(finalReceiverEmail))
                    {   reportRef=FirebaseDatabase.getInstance().getReference("/3/report");
                        reports report = new reports(finalReceiverEmail,communication,emotionalquotient,leadership,management);
                        String key = reportRef.push().getKey();
                        reportRef.child(key).setValue(report);

                        for (DataSnapshot Snapshot : postSnapshot.child("communication").getChildren()) {
                            feedbacks messages = Snapshot.getValue(feedbacks.class);
                            String reply= messages.getMsg();
                            communcationSkills.add(reply);
                        }
                        //                   communication=(int)makeconnection(communcationSkills);
                        for (int i = 0; i < communcationSkills.size(); i++) {
                            getSentiment(communcationSkills.get(i), new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String result) {
                                    //    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    sentiment = Float.parseFloat(result.trim());
                                    communication=communication+((sentiment+1f)*5f);
                                    reportRef.child(key).child("communication").setValue(communication/communcationSkills.size());
                                }
                            });
                        }
                        for (DataSnapshot Snapshot : postSnapshot.child("leadership").getChildren()) {
                            feedbacks messages = Snapshot.getValue(feedbacks.class);
                            String reply= messages.getMsg();
                            leadershipSkills.add(reply);
                        }
                        for (int i = 0; i < leadershipSkills.size(); i++) {
                            getSentiment(leadershipSkills.get(i), new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String result) {
                                    //    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    sentiment = Float.parseFloat(result.trim());
                                    leadership=leadership+((sentiment+1f)*5f);
                                    reportRef.child(key).child("leadership").setValue(leadership/leadershipSkills.size());
                                }
                            });
                        }


                        for (DataSnapshot Snapshot : postSnapshot.child("management").getChildren()) {
                            feedbacks messages = Snapshot.getValue(feedbacks.class);
                            String reply= messages.getMsg();
                            timeManagement.add(reply);
                        }
                        for (int i = 0; i < timeManagement.size(); i++) {
                            getSentiment(timeManagement.get(i), new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String result) {
                                    //    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    sentiment = Float.parseFloat(result.trim());
                                    management=management+((sentiment+1f)*5f);
                                    reportRef.child(key).child("timemanagement").setValue(management/timeManagement.size());
                                }
                            });
                        }

                        for (DataSnapshot Snapshot : postSnapshot.child("emotionalquotient").getChildren()) {
                            feedbacks messages = Snapshot.getValue(feedbacks.class);
                            String reply= messages.getMsg();
                            emotionalQuotient.add(reply);
                        }
                        for (int i = 0; i < emotionalQuotient.size(); i++) {
                            getSentiment(emotionalQuotient.get(i), new VolleyCallback() {
                                @Override
                                public void onSuccessResponse(String result) {
                                    //    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    sentiment = Float.parseFloat(result.trim());
                                    emotionalquotient=emotionalquotient+((sentiment+1f)*5f);
                                    reportRef.child(key).child("emotionalquotient").setValue(emotionalquotient/emotionalQuotient.size());
                                }
                            });
                        }



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public interface VolleyCallback {
        void onSuccessResponse(String result);
    }



    public void getSentiment(String feedback, final VolleyCallback callback)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.104:5000/" + feedback;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    callback.onSuccessResponse(response);
                }
                , error -> Toast.makeText(getApplicationContext(),"error in conn",Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }
}