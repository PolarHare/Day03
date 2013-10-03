package com.polarnick.translator.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Никита
 */
public class ArrayListAdapter<T> extends BaseAdapter {
    private List<Item<? extends T>> items = new ArrayList<Item<? extends T>>();
    private int typeCount = 1;

    public void add(Item<? extends T> item) {
        items.add(item);
        typeCount = Math.max(typeCount, item.type + 1);
        notifyDataSetChanged();
    }

    public void add(int position, Item<? extends T> item) {
        items.add(position, item);
        typeCount = Math.max(typeCount, item.type + 1);
        notifyDataSetChanged();
    }

    public boolean remove(Item<?> item) {
        if (items.remove(item)) {
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void addAll(Collection<? extends Item<? extends T>> collection) {
        items.addAll(collection);
        for (Item<? extends T> item : collection) {
            typeCount = Math.max(typeCount, item.type);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item<? extends T> getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public int getViewTypeCount() {
        return typeCount;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return items.get(position).composeView(convertView, parent);
    }

    public abstract static class Item<T> {
        protected T value;
        protected final int type;

        protected Item(T value, int type) {
            this.value = value;
            this.type = type;
        }

        protected Item(T value) {
            this(value, 0);
        }

        protected int getId() {
            return 0;
        }

        protected abstract View composeView(View convertView, ViewGroup parent);
    }
}
