package com.kiof.hymne;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Setting extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.savesetting) {
			setResult(RESULT_OK);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
//		if (getParent() == null) startActivity(new Intent(this, HymneActivity.class));
		setResult(RESULT_OK);
		super.onDestroy();
	}
	
//	public void onBackPressed() {
//		setResult(RESULT_OK);
//		super.onBackPressed();
//	}
	
}
