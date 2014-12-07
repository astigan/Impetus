package net.astigan.impetus.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.astigan.impetus.R;

public class PrivacyDialogFragment extends BaseDialogFragment {

    public static PrivacyDialogFragment newInstance() {
        PrivacyDialogFragment fragment = new PrivacyDialogFragment();
        Bundle extras = new Bundle();
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_privacy, container, false);
    }
}
