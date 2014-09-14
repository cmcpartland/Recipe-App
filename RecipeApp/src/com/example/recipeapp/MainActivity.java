package com.example.recipeapp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class MainActivity extends Activity implements OnInitListener {
	public final static String EXTRA_MESSAGE = "com.example.recipeapp.MESSAGE";
	private static final int RESULT_SPEECH = 1;
	private TextToSpeech tts;
//	private TextView tv;
	private ImageView iv;
	private Button listenButton;
	private String[] phrases;
	private Recipe recipe;
	private ProgressDialog dialog;
	
	@SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(this);
        iv = (ImageView) findViewById(R.id.iv);
       
		Intent intent = getIntent();
		if (!(intent == null)) {
			Log.i("intent action", intent.getAction());
			if (intent.getAction().equals("android.intent.action.SEND")) {
				Bundle extras = intent.getExtras();
				if (extras!=null) {
					loadRecipe(extras.getString("android.intent.extra.TEXT"));
				}
	        }
	    }
        initializeButtons();
        tts = new TextToSpeech(this, this);
        recipe = new Recipe();
        checkVoiceRecognition();
    }
	
	void dialogShow(String message) {
	    dialog.setMessage(message);
	    dialog.show();
	}

	void dialogHide() {
	    dialog.dismiss();
	}


//	public void createRecipe() {
//        LinkedList<String> ingredients = new LinkedList<String>(Arrays.asList("1 lb onion, chopped","1 lb carrot, diced", "1 cup broth, whole", "1 lb chicken, whole"));
//        LinkedList<String> steps = new LinkedList<String>(Arrays.asList("Step 1. Saute the onions and carrots in olive oil. Season with salt and pepper.",
//        									"Step 2. Bring chicken broth to a boil. Add chicken once broth is boiling. After one minute, bring the boil down to a simmer. Let cook for 30 minutes.",
//        									"Step 3. Add onions and carrots to chicken broth. Let cook together for 10 min.",
//        									"Step 4. Remove chicken. Let cool, tear apart, and put back in soup. Serve."));
//        recipe = new Recipe(ingredients, steps);
//	}
	
	public boolean loadRecipe(String url) {
		if (url.equals("")) {
			phrases = new String[] {"No URL selected."};
			return false;
		}
		else {
			LoadRecipeTask lrt = new LoadRecipeTask();
			lrt.execute(new String[] {url});
			return true;
		}
	}
	
    public void onInit(int status) {
        // TODO Auto-generated method stub
          //TTS is successfully initialized
    	
//    	tv.setText(tts.isLanguageAvailable(Locale.US));
        if (status == TextToSpeech.SUCCESS) {
                       //Setting speech language
            int result = tts.setLanguage(Locale.US);
           //If your device doesn't support language you set above
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                           //Cook simple toast message with message
                Toast.makeText(this, "Language not supported", Toast.LENGTH_LONG).show();
                Log.e("TTS", "Language is not supported");
            } 
                 //Enable the button - It was disabled in main.xml (Go back and Check it)
            //TTS is not initialized properly
        } else {
                    Toast.makeText(this, "TTS Initilization Failed", Toast.LENGTH_LONG).show();
            Log.e("TTS", "Initilization Failed");
        }
    }
    
    public void initializeButtons() {
//    	tv = (TextView) findViewById(R.id.tv);
    	listenButton = (Button) findViewById(R.id.btListen);
    	listenButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
 
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
 
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
//                    tv.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
    }
    
    private void speakOut() {
        //Get the text typed
//       String text = tv.getText().toString();
//       et.setText(text);
        //If no text is typed, tts will read out 'You haven't typed text'
        //else it reads out the text you typed
        if (phrases == null) {
        	tts.speak("Command not recognized", TextToSpeech.QUEUE_FLUSH, null);
        } 
        else {
        	for (String phrase : phrases)
        		tts.speak(phrase, TextToSpeech.QUEUE_ADD, null);
        }
   }
    
    public void checkVoiceRecognition() {
    	  // Check if voice recognition is present
    	  PackageManager pm = getPackageManager();
    	  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
    	    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    	  if (activities.size() == 0) {
    		  Toast.makeText(this, "Voice recognizer not present",
    		  Toast.LENGTH_SHORT).show();
    	  }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
	        case RESULT_SPEECH: {
	            if (resultCode == RESULT_OK && null != data) {
	 
	            	ArrayList<String> results = data
	                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            	AnalyzeVoiceRecTask avrt = new AnalyzeVoiceRecTask();
	            	avrt.execute(new ArrayList[] {results});
	            }
	            break;
	        }
        }
    }
   
    class AnalyzeVoiceRecTask extends AsyncTask<ArrayList, Void, String[]> {
    	protected void onPreExecute() {
    		dialogShow("Analyzing Command...");
    	}
    	
    	protected String[] doInBackground(ArrayList... arrayLists) {
    		ArrayList<String> results = arrayLists[0];
    		String[] responses = null;
    		String cmd = "";
        	List<String> commands = Arrays.asList("list ingredients", "begin", "next step", "previous step", "load recipe");
        	for (String command : commands) {
        		if (results.contains(command)) {
        			cmd = command;
        			break;
        		}
        	}
        	if (cmd.equals("list ingredients")) 
        		responses = recipe.getIngredients();
        	else if (cmd.equals("begin")) 
        		responses = recipe.begin();
        	else if	(cmd.equals("next step"))
        		responses = recipe.nextStep();
			else if (cmd.equals("previous step"))
				responses = recipe.previousStep();
        	return responses;
    	}
//    	
    	protected void onPostExecute(String[] responses) {
    		dialogHide();
    		phrases = responses;
    		speakOut();
    	}

    }
    
    class LoadRecipeTask extends AsyncTask<String, Void, LinkedList[]> {
    	Bitmap bm = null;
		protected void onPreExecute() {
			dialogShow("Loading Recipe...");
		}
		
		protected LinkedList[] doInBackground(String...  url) {
			LinkedList<String> string_ings = new LinkedList<String>();
			LinkedList<String> string_insts = new LinkedList<String>();
//			LinkedList<Bitmap> bms = new LinkedList<Bitmap>();

			Document doc = null;
			Connection conn = null;
			conn = Jsoup.connect(url[0]).timeout(5000);
			try { 
				doc = conn.get();
				Elements ings = doc.select("ul[class=kv-ingred-list1]").select("li[itemprop=ingredients]");
				if (ings != null) {
					for (Element ing : ings) 
						string_ings.add(ing.text());
				}
				else
					Log.i("ings", "ings is null");
				Elements insts = doc.select("div[itemprop=recipeInstructions]").select("p");
				if (insts != null)  {
					for (Element inst : insts)
						string_insts.add(inst.text());
				}
				else
					Log.i("insts", "insts is null");
				Elements pics = doc.select("meta[property=og:image]");
				if (pics != null) {
					Log.i("url", "pics is not null");
					Log.i("url",""+pics.size());
					Log.i("url", pics.get(0).attr("content"));
					
					URL temp = new URL(pics.get(0).attr("content"));
					URLConnection url_conn = temp.openConnection();
			        url_conn.connect();
			        InputStream is = url_conn.getInputStream();
			        BufferedInputStream bis = new BufferedInputStream(is);
			        bm = BitmapFactory.decodeStream(bis);
//			        bms.add(BitmapFactory.decodeStream(bis));
			        bis.close();
			        is.close();
					
					
				}
				else
					Log.i("pics", "pics is null");
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("ASYNC", "IOException");;
			}
			catch (NullPointerException e) {
				Log.i("ASYNC", "NULLPOINTEREXCEPTION");
				e.printStackTrace();
			}
			finally {
				return new LinkedList[] {string_ings, string_insts};
			}
		}
		@Override
	    protected void onPostExecute(LinkedList[] result) {
	    	if (result.length == 2) {
	    		LinkedList<String> ingredients = result[0];
		    	LinkedList<String> steps = result[1];
//		    	Bitmap bm = (Bitmap) result[2].get(0);
		    	if (!ingredients.isEmpty() && !steps.isEmpty()) {
		    		recipe = new Recipe(ingredients, steps);
		    		phrases = new String[] {"Recipe loaded."};
		    	}
		    	else {
		    		phrases = new String[] {"Recipe failed to load."};
		    	}
		    	dialogHide();
		    	speakOut();
		    	iv.setImageBitmap(bm);
//		    	setImage();
//		    	iv.postInvalidate();
	    	}
	    }
	}
    
//    public void setImage() {
//    	iv.setImageBitmap(recipe.bm);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
        
    }
    
}
