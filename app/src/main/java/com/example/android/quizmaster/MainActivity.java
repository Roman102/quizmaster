package com.example.android.quizmaster;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    private Context context;

    private EditText editQuizDataUrl;
    private Button downloadQuizButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        context = getApplicationContext();

        editQuizDataUrl = findViewById(R.id.quiz_data_url);
        downloadQuizButton = findViewById(R.id.download_quiz_button);
    }

    private void buildQuizUI(String response) {
        response = response.trim();

        BufferedReader responseReader = new BufferedReader(new StringReader(response));

        String line;

        try {
            while ((line = responseReader.readLine()) != null) {
                line = line.trim();

                if(line.isEmpty()) {
                    Log.v("buildQuizUI", "The line is empty.");
                } else {
                    String elementType = line.substring(0,2);
                    String elementText = line.substring(2);

                    Log.v("buildQuizUI - eType", elementType);
                    Log.v("buildQuizUI - eText", elementText);
                }
            }
        } catch (IOException error) {
            Log.v("MainActivity", error.toString());

            Toast.makeText(context, getString(R.string.error_while_parsing_quiz_data), Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadQuizClick(View view) {
        downloadQuizButton.setEnabled(false);
        editQuizDataUrl.setEnabled(false);

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
                                downloadQuizButton.setEnabled(true);
                                editQuizDataUrl.setEnabled(true);

                                buildQuizUI(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("MainActivity", error.toString());

                        downloadQuizButton.setEnabled(true);
                        editQuizDataUrl.setEnabled(true);

                        Toast.makeText(context, getString(R.string.error_failed_to_download_data), Toast.LENGTH_SHORT).show();
                    }
                }
                )
        );
    }

}
