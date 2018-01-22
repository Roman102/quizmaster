package com.example.android.quizmaster;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    private Context context;

    private EditText editQuizDataUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        context = getApplicationContext();

        editQuizDataUrl = findViewById(R.id.quiz_data_url);
    }

    public void downloadQuizClick(View view) {
        /*
         *  (1) Instantiate the RequestQueue.
         *
         *  (2) Request a string response from the provided URL.
         *
         *  (3) Add the request to the RequestQueue.
         */
        requestQueue.add(new StringRequest(Request.Method.GET, editQuizDataUrl.getText().toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.v("MainActivity", "Response is: " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("MainActivity", error.toString());

                        Toast.makeText(context, getString(R.string.error_failed_to_download_data), Toast.LENGTH_SHORT).show();
                    }
                }
                )
        );
    }

}
