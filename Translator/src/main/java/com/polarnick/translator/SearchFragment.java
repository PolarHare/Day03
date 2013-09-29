package com.polarnick.translator;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Никита
 */
public class SearchFragment extends ExtFragment {
    private boolean dual;
    private EditText searchInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private final View.OnClickListener SEARCH_BUTTON_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String query = searchInput.getText().toString();
            if (dual) {
                ResultsFragment resultsFragment = findFragmentById(R.id.results_fragment);
                if (resultsFragment == null || !query.equalsIgnoreCase(resultsFragment.getCurrentQuery())) {
                    resultsFragment = ResultsFragment.newInstance(query);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.results_fragment, resultsFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                }
            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ResultsActivity.class);
                intent.putExtra(ResultsFragment.QUERY_INDEX, query);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View second = findViewById(R.id.results_fragment);
        dual = second != null && second.getVisibility() == View.VISIBLE;

        searchInput = findViewById(R.id.search_input);
        this.<Button>findViewById(R.id.search_button).setOnClickListener(SEARCH_BUTTON_LISTENER);
    }
}
