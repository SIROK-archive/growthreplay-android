package sample.com.growthreplay;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.growthreplay.GrowthReplay;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GrowthReplay.getInstance().initialize(getApplicationContext(), "1RE2wSOp6V5qpq5O", "j4Gnk3tWQMV31QPnZ1GwYuJRkyPDkDcK");
	}

	@Override
	protected void onResume() {
		super.onResume();
		GrowthReplay.getInstance().setActivity(this);
		GrowthReplay.getInstance().start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GrowthReplay.getInstance().stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
