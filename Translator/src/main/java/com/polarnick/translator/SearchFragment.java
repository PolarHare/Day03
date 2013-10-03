package com.polarnick.translator;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * @author Никита
 */
public class SearchFragment extends ExtFragment {
    private RecentQueriesAdapter adapter;
    private boolean dual;
    private EditText searchInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private final View.OnClickListener searchButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
         search(searchInput.getText().toString());
        }
    };

    private final TextView.OnEditorActionListener onEnterKeyListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                search(v.getText().toString());
                return true;
            }
            return false;
        }
    };

    private final ListView.OnItemClickListener recentQueryClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String query = adapter.getQuery(position);
            searchInput.setText(query);
            search(query);
        }
    };

    private void search(String query) {
        query = query.trim();
        if (query.isEmpty()) return;

        adapter.addQuery(query);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new RecentQueriesAdapter(getActivity());

        View second = findViewById(R.id.results_fragment);
        dual = second != null && second.getVisibility() == View.VISIBLE;

        searchInput = findViewById(R.id.search_input);
        searchInput.setOnEditorActionListener(onEnterKeyListener);
        this.<Button>findViewById(R.id.search_button).setOnClickListener(searchButtonListener);
        ListView listView = this.findViewById(R.id.recent_queries);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(recentQueryClickListener);
    }
}
