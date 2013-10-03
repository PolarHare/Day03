package com.polarnick.translator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.polarnick.polaris.services.BingSearchService;
import com.polarnick.polaris.services.YandexTranslateService;
import com.polarnick.translator.net.AsyncCallbackOnUiThread;
import com.polarnick.translator.ui.ArrayListAdapter;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Никита
 */
public class ResultsFragment extends ExtFragment {
    public static final String QUERY_INDEX = "query";

    private static final String YANDEX_ACCOUNT_KEY = "trnsl.1.1.20130921T184657Z.2b7426257ae60b23.5a54cb565a7471b7462b5f3bef1895e281bfc9d8";
    private static final List<String> EN_RU = Arrays.asList("en", "ru");
    private static final YandexTranslateService translatorEnRu = new YandexTranslateService(YANDEX_ACCOUNT_KEY, EN_RU);

    private static final String BING_ACCOUNT_KEY = "cEbtMs0M2NMFC9DQg4TogGtkuorbFQhjaW3dzhQjEVg=";
    private static final int IMAGE_LIMIT = 10;
    private static BingSearchService bingSearchServiceStrict;


    private final AsyncCallbackOnUiThread<List<BingSearchService.ResultImage>,Exception> allImagesLoaded = new AsyncCallbackOnUiThread<List<BingSearchService.ResultImage>, Exception>() {
        @Override
        public void fail(Exception reason) {
            moreButton.setEnabled(true);
        }

        @Override
        public void success(List<BingSearchService.ResultImage> resultImages) {
            int size = resultImages.size();
            List<ImageItem> items = new ArrayList<ImageItem>(size);
            String[] urls = new String[size];
            for (int i = 0; i < resultImages.size(); ++i) {
                urls[i] = resultImages.get(i).getImageURL();
                items.add(new ImageItem(null, imageSize, getActivity().getLayoutInflater()));
            }
            adapter.addAll(items);
            new ImagesDownloadTask(items).execute(urls);

            if (resultImages.size() != IMAGE_LIMIT) {
                moreButton.setVisibility(View.GONE);
            }
        }
    };

    private ProgressBar progressBar;
    private TextView translation;
    private ArrayListAdapter<Bitmap> adapter;
    private Button moreButton;
    private int imageSize;
    private boolean dual;

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

        if (savedInstanceState != null) return;

        imageSize = (int) getResources().getDimension(R.dimen.image_size);
        bingSearchServiceStrict = new BingSearchService(
                BING_ACCOUNT_KEY, imageSize, imageSize, IMAGE_LIMIT, BingSearchService.FilterStrictness.STRICT);

        View second = findViewById(R.id.search_fragment);
        dual = second != null && second.getVisibility() == View.VISIBLE;

        translation = findViewById(R.id.translation);
        progressBar = findViewById(R.id.translation_progress);

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
        }
    }

    private void loadNext() {
        moreButton.setEnabled(false);
        bingSearchServiceStrict.searchAsync(getCurrentQuery(), adapter.getCount(), allImagesLoaded, null);
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
            progressBar.setVisibility(View.GONE);
            if (exception != null) {
                final Activity activity = getActivity();
                Log.e(this.getClass().getName(), "Exception caught while translating: " + exception.getMessage());
                new AlertDialog.Builder(activity)
                        .setMessage(exception.getMessage())
                        .setPositiveButton(R.string.on_error_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dual) {
                                            getFragmentManager().beginTransaction()
                                                    .remove(ResultsFragment.this)
                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                                    .commit();
                                        } else {
                                            getActivity().finish();
                                        }
                                    }
                                }).create().show();
            }

            if (s != null) {
                translation.setText(s);

                ListView imagesView = findViewById(R.id.images);
                adapter = new ArrayListAdapter<Bitmap>();
                imagesView.addFooterView(moreButton);
                imagesView.setAdapter(adapter);
                loadNext();
            }
        }
    }

    private static class ImageItem extends ArrayListAdapter.Item<Bitmap> {
        private final LayoutInflater inflater;
        private final int height;
        private ImageView imageView;

        private ImageItem(Bitmap bitmapUrl, int height, LayoutInflater inflater) {
            super(bitmapUrl);
            this.inflater = inflater;
            this.height = height;
        }

        @Override
        protected View composeView(View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.image_item, parent, false);
                view.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }

            imageView = (ImageView)view.findViewById(R.id.image);
            imageView.setImageBitmap(value);
            imageView.setTag(this);

            return view;
        }

        protected void setBitmap(Bitmap bitmap) {
            this.value = bitmap;
            if (imageView != null && imageView.getTag() == this) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private class ImagesDownloadTask extends AsyncTask<String, Integer, Void> {
        private List<ImageItem> items;
        private List<Bitmap> bitmaps = new ArrayList<Bitmap>();

        private ImagesDownloadTask(List<ImageItem> items) {
            this.items = items;
        }

        @Override
        protected Void doInBackground(String... strings) {
            for (String url : strings) {
                try {
                    URLConnection connection = new URL(url).openConnection();
                    bitmaps.add(BitmapFactory.decodeStream(connection.getInputStream()));
                } catch (Exception e) {
                    Log.w(ResultsFragment.class.getName(), e);
                    bitmaps.add(null);
                }
                publishProgress(bitmaps.size() - 1);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            items.get(values[0]).setBitmap(bitmaps.get(values[0]));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            moreButton.setEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("DST", "destrooooy");

    }
}
