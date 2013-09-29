package com.polarnick.translator;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.List;

/**
 * @author Никита
 */
public class RecentQueriesAdapter extends BaseAdapter {
    private static final String PREFS_NAME = "recent";
    private static final String QUERIES_INDEX = "queries";
    private Context context;

    private List<String> queries;
    private SharedPreferences settings;

    public RecentQueriesAdapter(Context context) {
        this.context = context;

        settings = context.getSharedPreferences(PREFS_NAME, 0);
        queries = Lists.newArrayList(new Gson().<String[]>fromJson(settings.getString(QUERIES_INDEX, "[]"), String[].class));
    }

    public void addQuery(String query) {
        queries.add(query);
        settings.edit().putString(QUERIES_INDEX, new Gson().toJson(queries.toArray()));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return queries.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = new TextView(context);
            textView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(queries.get(queries.size() - position - 1));

        return textView;
    }
}
