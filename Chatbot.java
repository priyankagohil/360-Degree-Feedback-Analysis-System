package com.example.dell.a360degreefeedback;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.UUID;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;


public class Chatbot extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int USER = 10001;
    private static final int BOT = 10002;
    private String uuid = UUID.randomUUID().toString();
    private LinearLayout chatLayout;
    private EditText queryEditText;
    private AIRequest aiRequest;
    private AIDataService aiDataService;
    private AIServiceContext customAIServiceContext;
    final String  ClientAccessToken="5e9267228fa2446eb18a354d6771c498";
    DatabaseReference mDatabase,m2Database,rootRef;
    String emailid;
    String action, receiverEmail, giverEmail, user;
    String parameter;
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.Theme_DEFAULT);
        setContentView(R.layout.activity_chatbot);
        Intent intent = getIntent();
        receiverEmail = intent.getStringExtra("receiver_email");
        giverEmail = intent.getStringExtra("giver_email");
        user = intent.getStringExtra("user_id");
        emailid=receiverEmail;

        final ScrollView scrollview = (ScrollView) findViewById(R.id.chatScrollView);
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        chatLayout = (LinearLayout) findViewById(R.id.chatLayout);
        ImageView sendBtn =(ImageView)findViewById(R.id.sendBtn);


      scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(sendBtn);
            }
        });

       queryEditText.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        sendMessage(sendBtn);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        initChatbot();
    }

    private void initChatbot() {
        final AIConfiguration config = new AIConfiguration(ClientAccessToken,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid);
        aiRequest = new AIRequest();
    }

    private void sendMessage(View view) {
        msg = queryEditText.getText().toString();
        if (msg.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your message!", Toast.LENGTH_LONG).show();
        } else {
            //uploading to db

            showTextView(msg, USER);
            queryEditText.setText("");
            aiRequest.setQuery(msg);
            RequestTask requestTask = new RequestTask(Chatbot.this, aiDataService, customAIServiceContext);
            requestTask.execute(aiRequest);
        }
    }

    private void userPush(final String msg, final String emailid) {
        rootRef = FirebaseDatabase.getInstance().getReference("/2/feedbacks/");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    feedbacks user = postSnapshot.getValue(feedbacks.class);
                    String fetch = user.getEmail();
                    if (emailid.equals(fetch)) {
                         feedbacks upload = new feedbacks(msg);
                        String id = postSnapshot.getKey();

                        rootRef.child(id).child(parameter).push().setValue(upload);

                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
              //  Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void callback(AIResponse aiResponse) {
        if (aiResponse != null) {
            String botReply = aiResponse.getResult().getFulfillment().getSpeech();
            Log.d(TAG, "Bot Reply: " + botReply);
            showTextView(botReply, BOT);
        } else {
            Log.d(TAG, "Bot Reply: Null");
            showTextView("There was some communication issue. Please Try again!", BOT);
        }
    }

    public void onResult(final AIResponse response) {
        Result result = response.getResult();

        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        action=result.getAction();
        switch (action){
            case "communication":
                parameter=action;
                userPush(msg, emailid);
                break;
            case "leadership":
                parameter=action;
                userPush(msg, emailid);
                break;
            case "management":
                parameter=action;
                userPush(msg, emailid);
                break;
            case "emotionalquotient":
                parameter=action;
                userPush(msg, emailid);
                break;
            default:
                parameter="noAction";
                break;

        }
//        Toast.makeText(getApplicationContext(),"Query:" + result.getResolvedQuery() +
//                "\nAction: " + result.getAction() + "\nContext: " + result.getContexts()+
//                "\nParameters: " + parameterString,Toast.LENGTH_LONG).show();

    }

    private void showTextView(String message, int type) {
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                break;
            case BOT:
                layout = getBotLayout();
                break;
            default:
                layout = getBotLayout();
                break;
        }
        layout.setFocusableInTouchMode(true);
        chatLayout.addView(layout);
        TextView tv = layout.findViewById(R.id.chatMsg);
        tv.setText(message);
        layout.requestFocus();
        queryEditText.requestFocus();
    }

    FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(Chatbot.this);
        return (FrameLayout) inflater.inflate(R.layout.user_msg_layout, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(Chatbot.this);
        return (FrameLayout) inflater.inflate(R.layout.bot_msg_layout, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getBaseContext(), ChatbotFinisher.class);
        intent.putExtra("receiver_email", receiverEmail);
        intent.putExtra("user_id",user);
        intent.putExtra("giver_email",giverEmail);
        startActivity(intent);
        finish();
    }
}
