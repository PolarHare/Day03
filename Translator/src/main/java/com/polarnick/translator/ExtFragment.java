package com.polarnick.translator;

import android.app.Fragment;
import android.view.View;

/**
 * @author Никита
 */
@SuppressWarnings("unchecked")
public abstract class ExtFragment extends Fragment {
    public <T extends View> T findViewById(int id) {
        return (T) getActivity().findViewById(id);
    }

    public <T extends Fragment> T findFragmentById(int id) {
        return (T) getFragmentManager().findFragmentById(id);
    }
}
