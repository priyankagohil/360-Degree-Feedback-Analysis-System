package com.example.dell.a360degreefeedback;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kotlin.jvm.internal.FloatSpreadBuilder;

import static java.lang.Float.valueOf;

public class Report extends AppCompatActivity {
    private DatabaseReference ref;
    BarChart barChart;
    String userEmail;
    int i=0;
    float communication, emotionalquotient, leadership, timemanagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.Theme_DEFAULT);
        setContentView(R.layout.activity_report);

        barChart=(BarChart) findViewById(R.id.bargraph);
        ref = FirebaseDatabase.getInstance().getReference("/3/report");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userEmail =user.getEmail();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               // Toast.makeText(getApplicationContext(),"in here",Toast.LENGTH_LONG).show();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                  //  Toast.makeText(getApplicationContext(),"in here"+userEmail,Toast.LENGTH_LONG).show();
                    reports user = postSnapshot.getValue(reports.class);
                    String email=user.getEmail();
                    if(email.equals(userEmail))
                    {
                        user.getCommunication();
                        communication=user.getCommunication();
                        emotionalquotient=user.getEmotionalquotient();
                        leadership=user.getLeadership();
                        timemanagement=user.getTimemanagement();
                      //  Toast.makeText(getApplicationContext(),communication+" "+emotionalquotient+" "+leadership+" "+timemanagement,Toast.LENGTH_LONG).show();
                        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
                        bargroup1.add(new BarEntry(leadership, 0));
                        bargroup1.add(new BarEntry(communication,1));
                        bargroup1.add(new BarEntry(timemanagement,2));
                        bargroup1.add(new BarEntry(emotionalquotient, 3));
                        // creating dataset for Bar Group1
                        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "Bar Group 1");
                        //barDataSet1.setColor(Color.rgb(0, 155, 0));
                        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

                        ArrayList<String> labels = new ArrayList<>();
                        labels.add("Leader");
                        labels.add("CS");
                        labels.add("Manage");
                        labels.add("EQ");

                        BarData data = new BarData(labels,barDataSet1);
                        barChart.setData(data);
                        barChart.animateY(5000);

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No report found",Toast.LENGTH_LONG).show();
                       // startActivity(new Intent(getApplicationContext(), EmpPanel.class));
                        //finish();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
             //   Toast.makeText(getApplicationContext(),"in here",Toast.LENGTH_LONG).show();

            }
        });


    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),EmpPanel.class));
    }
}
