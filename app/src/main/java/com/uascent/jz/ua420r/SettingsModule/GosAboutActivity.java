package com.uascent.jz.ua420r.SettingsModule;

import android.os.Bundle;
import android.view.MenuItem;

import com.uascent.jz.ua420r.CommonModule.GosBaseActivity;
import com.uascent.jz.ua420r.R;

public class GosAboutActivity extends GosBaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gos_about);
		// 设置ActionBar
		setActionBar(true, true, R.string.about);

	}


	@Override
	public void onResume() {
		super.onResume();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
