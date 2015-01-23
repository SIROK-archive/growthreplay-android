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
		GrowthReplay.getInstance().initialize(getApplicationContext(), "OyVa3zboPjHVjsDC", "3EKydeJ0imxJ5WqS22FJfdVamFLgu7XA");
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
