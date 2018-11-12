package tododiary2.ejvindh.com.tododiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

public class JumbleActivity extends AppCompatActivity {
	private String jumblecontent;
	private EditText jumbletext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jumble);
		// Show the Up button in the action bar.
		setupActionBar();
		jumbletext = findViewById(R.id.jumbletext);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			jumblecontent = bundle.getString("jumbletext");
		}
		jumbletext.setText(jumblecontent);
		jumbletext.setFocusable(true);
	}
	
	
	@Override
	public void onBackPressed() {
		returncontent();
    }

	private void returncontent() {
    	String newjumblecontent = jumbletext.getText().toString();
    	if (newjumblecontent.compareTo(jumblecontent) == 0) {
    		Intent returnIntent = new Intent();
    		setResult(RESULT_CANCELED, returnIntent);
        	finish();
    	} else {
    		Intent returnIntent = new Intent();
            returnIntent.putExtra("returningresult", newjumblecontent);
            setResult(RESULT_OK,returnIntent);     
            finish();
    	}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            returncontent();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
	}

	private void setupActionBar() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}
}
