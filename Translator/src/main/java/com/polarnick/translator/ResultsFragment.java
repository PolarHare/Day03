package com.polarnick.translator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.polarnick.polaris.services.YandexTranslateService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Никита
 */
public class ResultsFragment extends ExtFragment {
    public static final String QUERY_INDEX = "query";

    private static final String ACCOUNT_KEY = "trnsl.1.1.20130921T184657Z.2b7426257ae60b23.5a54cb565a7471b7462b5f3bef1895e281bfc9d8";
    private static final List<String> EN_RU = Arrays.asList("en", "ru");
    private static final YandexTranslateService translatorEnRu = new YandexTranslateService(ACCOUNT_KEY, EN_RU);

    private final Runnable postEnableButton = new Runnable() {
        @Override
        public void run() {
            moreButton.setEnabled(true);
        }
    };

    private final Runnable allImagesLoaded = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(postEnableButton);
        }
    };

    private TextView translation;
    private ImageAdapter imageAdapter;
    private Button moreButton;

    public static ResultsFragment newInstance(String query) {
        ResultsFragment fragment = new ResultsFragment();

        Bundle bundle = new Bundle();
        bundle.putString(QUERY_INDEX, query);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        translation = findViewById(R.id.translation);

        moreButton = new Button(getActivity());
        moreButton.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        moreButton.setText(getString(R.string.more));
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNext();
            }
        });
        moreButton.setEnabled(false);

        String query = getCurrentQuery();
        if (query != null) {
            new TranslateTask().execute(query);
            ListView imagesView = this.findViewById(R.id.images);
            imageAdapter = new ImageAdapter(getActivity(), query);
            imagesView.addFooterView(moreButton);
            imagesView.setAdapter(imageAdapter);
            loadNext();
        }
    }

    private void loadNext() {
        moreButton.setEnabled(false);
        imageAdapter.loadMore(allImagesLoaded);
    }

    public String getCurrentQuery() {
        Bundle args = getArguments();
        return args != null ? args.getString(QUERY_INDEX) : null;
    }

    private class TranslateTask extends AsyncTask<String, Void, String> {

        IOException exception = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                return translatorEnRu.translate(strings[0]);
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (exception != null) {
                final Activity activity = getActivity();
                Log.e(this.getClass().getName(), "Exception caught while translating: " + exception.getMessage());
                new AlertDialog.Builder(activity)
                        .setMessage(exception.getMessage())
                        .setPositiveButton(R.string.on_error_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (activity instanceof ReturnableActivity) {
                                            ((ReturnableActivity) activity).returnBack();
                                        }
                                    }
                                }).create().show();
            }
            if (s != null) {
                translation.setText(s);
            }
        }
    }
}
