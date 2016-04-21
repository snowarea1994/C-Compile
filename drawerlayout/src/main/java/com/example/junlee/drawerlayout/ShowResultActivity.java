package com.example.junlee.drawerlayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;

public class ShowResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        TextView result = (TextView) findViewById(R.id.result);

        Intent intent = getIntent();
        String str = "";
        try {
            JSONArray jsonArray = new JSONArray(intent.getStringExtra("result"));
            for (int i=0 ;i<jsonArray.length();i++){
                str += jsonArray.getString(i);
            }
            result.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
