package net.astigan.impetus.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.astigan.impetus.R;
import net.astigan.impetus.main.MainActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A fragment used for navigation when travelling. The distance travelled and time elapsed are
 * shown, and there is an option to end the journey.
 */
public class NavigatorFragment extends Fragment {

    public interface NavigatorFragmentListener {
        public void onJourneyEnd();
    }

	private NavigatorFragmentListener listener;

    public static NavigatorFragment newInstance() {
        NavigatorFragment fragment = new NavigatorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.end_journey_button) void endJourney() {
        listener.onJourneyEnd();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navigator, container, false);
        ButterKnife.inject(this, view);

		if (getActivity() instanceof MainActivity) {
			listener = (NavigatorFragmentListener) getActivity();
		} else {
            throw new RuntimeException("Activity must implement NavigatorFragmentListener!");
        }
		return view;
	}



}