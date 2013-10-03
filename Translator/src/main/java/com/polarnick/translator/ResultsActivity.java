package com.polarnick.translator;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * @author Никита
 */
public class ResultsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            ResultsFragment resultsFragment = new ResultsFragment();
            resultsFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, resultsFragment)
                    .commit();
        }
    }
}