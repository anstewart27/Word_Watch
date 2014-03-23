package com.example.wordwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button submitButton;
	EditText targetTime;
	EditText badWord;
	EditText numberOfTimes;


	ListView lvVoiceReturn;
	static final int check = 1111;
	int replacement_counter = 0;
	String[] badWordsStrings;

	int timeLength;
	ArrayList<String> words = new ArrayList<String>();
	int wordCount;

	//how many times you've said it
	int speechCounter=0;

	int num_times_clicked = 0;
	boolean time_up = false;

	boolean counterStarted = false;

	public String formattedWordList(ArrayList<String> words){
		String rtn = " ";
		int number = words.size();
		int i = 0;
		for (String word : words) {
			if (number == 1) {
				return word;
			}
			if (i < number-1) {
				if (number == 2) {
					rtn = rtn + word + " ";
				} else {
					rtn = rtn + word + ", ";
				}
			} else {
				rtn = rtn + "& " + word;
			}
			i++;
		}
		return rtn;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		submitButton = (Button)findViewById(R.id.submitButton);
		targetTime   = (EditText)findViewById(R.id.targetTime);
		badWord   = (EditText)findViewById(R.id.badWord);
		numberOfTimes   = (EditText)findViewById(R.id.numberOfTimes);
		lvVoiceReturn = (ListView) findViewById(R.id.lvVoiceReturn);


		submitButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						if (num_times_clicked == 0) {
							timeLength = Integer.parseInt(targetTime.getText().toString());

							new CountDownTimer(timeLength*60000, 10000) {

								public void onTick(long millisUntilFinished) {
//									if((millisUntilFinished/1000) %15 == 0) {
//										num_times_clicked++;
//
//										((Button)(findViewById(R.id.submitButton))).performClick();
//										//((Button)(findViewById(R.id.submitButton))).setEnabled(true);
//									}

								}

								public void onFinish() {
									//Stop Recording

									//									if (((Button)(findViewById(R.id.submitButton))).isEnabled()) {
									//										((Button)(findViewById(R.id.submitButton))).performClick();
									//										((Button)(findViewById(R.id.submitButton))).setEnabled(false);
									//									}

									time_up = true;
									sendAlertToPebble("" ," ");
									sendAlertToPebble("" ," ");
									sendAlertToPebble("Time's up!", timeLength + " minutes have passed."
											+ " \nStats: You said" + formattedWordList(words) + " " + speechCounter + " times." );

								}
							}.start();

							words = new ArrayList<String>();
							badWordsStrings = badWord.getText().toString().split(" ");
							for (String word : badWordsStrings) {
								words.add(word);
							}
							wordCount = Integer.parseInt(numberOfTimes.getText().toString());
						}

						//Start recording
						if (time_up == false) {
							num_times_clicked++;
							Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
							i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
							i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
							i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Starting to listen!");
							startActivityForResult(i, check);
						}
					}



				}
				);




		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && check == requestCode) {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			lvVoiceReturn.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results));


			String s = results.get(0);
			String [] eachWord = s.split(" ");
			for (String s1: eachWord) {
				for (String check: badWordsStrings) {
					if (s1.compareTo(check) == 0) {
						Toast.makeText(getApplicationContext(), "YOU SAID " + check, Toast.LENGTH_LONG).show();
						speechCounter++;
					}
				}
			}

			if(speechCounter >= wordCount) {
				sendAlertToPebble("Oops! Poor word choice!", "Stop saying: \n" + formattedWordList(words) + ".");
			}


			if (replacement_counter <= (timeLength*60000/1000)) {
				(((Button)findViewById(R.id.submitButton))).performClick();

				(((Button)findViewById(R.id.submitButton))).performClick();
				replacement_counter++;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	public void sendAlertToPebble(String title, String body) {
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

		final Map data = new HashMap();
		data.put("title", title);
		data.put("body", body);
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData).toString();

		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "MyAndroidApp");
		i.putExtra("notificationData", notificationData);

		//  Log.d("TAG", "About to send a modal alert to Pebble: " %2B notificationData);
		sendBroadcast(i);
	}

	public void sendTimeUpToPebble() {

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
