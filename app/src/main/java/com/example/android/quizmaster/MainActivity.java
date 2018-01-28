package com.example.android.quizmaster;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    private LinkedHashMap<Integer, Answer> correctAnswers;

    private Context context;

    private LinearLayout mainLayout;
    private LinearLayout.LayoutParams visualVerticalDivider;

    private EditText editQuizDataUrl;

    private Button downloadQuizButton;
    private Button submitAnswersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main_layout);

        requestQueue = Volley.newRequestQueue(this);

        context = getApplicationContext();

        editQuizDataUrl = findViewById(R.id.quiz_data_url);

        downloadQuizButton = findViewById(R.id.download_quiz_button);
        submitAnswersButton = findViewById(R.id.submit_answers_button);

        visualVerticalDivider = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        visualVerticalDivider.setMargins(0, getResources().getDimensionPixelSize(R.dimen.standard_space), 0, 0);
    }

    private void buildUIForOneQuestion(ArrayDeque<View> viewQueue, int questionNumber) {
        View v;

        LinearLayout encapsulatingLayout = null;
        RadioGroup radioGroup = null;

        if (viewQueue.size() > 1) {
            // instanceof doesn't seem to always work
            if (viewQueue.peek().getClass().getName().equalsIgnoreCase("android.widget.RadioButton")) {
                radioGroup = new RadioGroup(context);
            } else {
                encapsulatingLayout = new LinearLayout(context);

                encapsulatingLayout.setOrientation(LinearLayout.VERTICAL);
            }
        }

        while ((v = viewQueue.poll()) != null) {
            if (v.getId() > -1) {
                correctAnswers.get(v.getId()).questionNumber = questionNumber;
            }

            if (encapsulatingLayout != null) {
                encapsulatingLayout.addView(v);
            } else if (radioGroup != null) {
                radioGroup.addView(v, -1, ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                v.setLayoutParams(visualVerticalDivider);

                mainLayout.addView(v);
            }
        }

        if (encapsulatingLayout != null) {
            encapsulatingLayout.setLayoutParams(visualVerticalDivider);

            mainLayout.addView(encapsulatingLayout);
        } else if (radioGroup != null) {
            mainLayout.addView(radioGroup);
        }
    }

    private void buildQuizUI(String response) {
        int questionNumber = 0;

        mainLayout.removeAllViews();

        correctAnswers = new LinkedHashMap<Integer, Answer>();

        response = response.trim();

        BufferedReader responseReader = new BufferedReader(new StringReader(response));

        String line;

        ArrayDeque<View> viewQueue = new ArrayDeque<View>();

        int newId = -1;

        try {
            while ((line = responseReader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    if (newId > -1) {
                        newId = -1;

                        questionNumber++;
                    }

                    buildUIForOneQuestion(viewQueue, questionNumber);
                } else {
                    String elementType = line.substring(0, 2);
                    String elementText = line.substring(2);

                    switch (elementType) {
                        case "l:":
                            TextView newLabel = new TextView(context);

                            newLabel.setText(elementText);

                            viewQueue.add(newLabel);

                            break;
                        case "e:":
                            EditText newEditText = new EditText(context);

                            newId = View.generateViewId();

                            newEditText.setId(newId);
                            correctAnswers.put(newId, new Answer(elementText));

                            viewQueue.add(newEditText);

                            break;
                        case "r:":
                        case "r>":
                            RadioButton newRadioButton = new RadioButton(context);

                            newRadioButton.setText(elementText);

                            if (elementType.endsWith(">")) {
                                newId = View.generateViewId();

                                newRadioButton.setId(newId);

                                correctAnswers.put(newId, new Answer("1"));
                            }

                            viewQueue.add(newRadioButton);

                            break;
                        case "c:":
                        case "c>":
                            CheckBox newCheckBox = new CheckBox(context);

                            newId = View.generateViewId();

                            newCheckBox.setId(newId);
                            newCheckBox.setText(elementText);

                            if (elementType.endsWith(">")) {
                                correctAnswers.put(newId, new Answer("1"));
                            } else {
                                correctAnswers.put(newId, new Answer("0"));
                            }

                            viewQueue.add(newCheckBox);

                            break;
                    }
                }
            }

            if (newId > -1) {
                questionNumber++;
            }

            buildUIForOneQuestion(viewQueue, questionNumber);
        } catch (IOException error) {
            Log.v("MainActivity", error.toString());

            Toast.makeText(context, getString(R.string.error_while_parsing_quiz_data), Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadQuizClick(View view) {
        downloadQuizButton.setEnabled(false);
        submitAnswersButton.setEnabled(false);
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
                                buildQuizUI(response);

                                editQuizDataUrl.setEnabled(true);
                                downloadQuizButton.setEnabled(true);
                                submitAnswersButton.setEnabled(true);
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

    public void submitAnswersClick(View view) {
        int mainQuestionNumber = 1;

        boolean wrongMultipleChoiceAnswerMarked = false;

        int correctlyAnsweredQuestionsForMainQuestion = 0;
        int totalQuestionsOfMainQuestion = 0;

        int correctlyAnsweredQuestions = 0;
        int totalQuestions = 0;

        Map.Entry<Integer, Answer> entry = null;

        StringBuilder results = new StringBuilder();

        for (Map.Entry<Integer, Answer> entry_ : correctAnswers.entrySet()) {
            entry = entry_;

            if (entry.getValue().questionNumber > mainQuestionNumber) {
                if (wrongMultipleChoiceAnswerMarked) {
                    wrongMultipleChoiceAnswerMarked = false;

                    results.append(getString(R.string.wrong_multiple_choice_answer, mainQuestionNumber));
                } else {
                    results.append(getString(R.string.main_answer_no_and_statistics,
                            mainQuestionNumber, correctlyAnsweredQuestionsForMainQuestion,
                            totalQuestionsOfMainQuestion));
                }

                mainQuestionNumber++;

                correctlyAnsweredQuestions += correctlyAnsweredQuestionsForMainQuestion;
                totalQuestions += totalQuestionsOfMainQuestion;

                correctlyAnsweredQuestionsForMainQuestion = 0;
                totalQuestionsOfMainQuestion = 0;
            }

            View entryView = findViewById(entry.getKey());

            switch (entryView.getClass().getName()) {
                case "android.widget.EditText":
                    if (((EditText) entryView).getText().toString().equals(entry.getValue().answer)) {
                        correctlyAnsweredQuestionsForMainQuestion++;
                    }

                    totalQuestionsOfMainQuestion++;

                    break;
                case "android.widget.RadioButton":
                    if (((RadioButton) entryView).isChecked()) {
                        correctlyAnsweredQuestionsForMainQuestion = 1;
                    }

                    totalQuestionsOfMainQuestion = 1;

                    break;
                case "android.widget.CheckBox":
                    // correct answer: 0, given answer: 0 -> do nothing

                    // correct answer: 0, given answer: 1
                    if (entry.getValue().answer.equals("0") && ((CheckBox) entryView).isChecked()) {
                        wrongMultipleChoiceAnswerMarked = true;

                        correctlyAnsweredQuestionsForMainQuestion = 0;
                    }

                    // correct answer: 1, given answer: 0 -> do nothing

                    // correct answer: 1, given answer: 1 -> correctlyAnsweredQuestionsForMainQuestion++;
                    if (!wrongMultipleChoiceAnswerMarked && entry.getValue().answer.equals("1") &&
                            ((CheckBox) entryView).isChecked()) {
                        correctlyAnsweredQuestionsForMainQuestion++;
                    }

                    if (entry.getValue().answer.equals("1")) {
                        totalQuestionsOfMainQuestion++;
                    }

                    break;
            }
        }

        if (entry == null) {
            Log.v("Results", getString(R.string.error_missing_model_answers));
        } else {
            totalQuestions += totalQuestionsOfMainQuestion;

            if (wrongMultipleChoiceAnswerMarked) {
                results.append(getString(R.string.wrong_multiple_choice_answer, mainQuestionNumber));
            } else {
                correctlyAnsweredQuestions += correctlyAnsweredQuestionsForMainQuestion;

                results.append(getString(R.string.main_answer_no_and_statistics,
                        mainQuestionNumber, correctlyAnsweredQuestionsForMainQuestion,
                        totalQuestionsOfMainQuestion));
            }

            results.append(getString(R.string.final_score, correctlyAnsweredQuestions, totalQuestions));

            /*
             *  The duration for the toast is too short. Unfortunately showing the results
             *  using a Toast is in the Quiz App's project specification. However I think that
             *  it's better to use a dialogue in this case. Even though people tried to find
             *  various workarounds
             *  https://stackoverflow.com/questions/2220560/can-an-android-toast-be-longer-than-toast-length-long
             *  But workarounds should only be used when it's absolutely necessary.
             */

            // Toast.makeText(context, results, Toast.LENGTH_LONG).show();

            /*
             *  1. Instantiate an AlertDialog.Builder with its constructor
             *
             *  2. Chain together various setter methods to set the dialog characteristics
             *
             *  3. Get the AlertDialog from create()
             */

            (new AlertDialog.Builder(this)).setMessage(results)
                    .setTitle(R.string.results)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create().show();
        }
    }

    private class Answer {
        int questionNumber;

        private String answer;

        private Answer(String answer) {
            this.answer = answer;
        }
    }

}
