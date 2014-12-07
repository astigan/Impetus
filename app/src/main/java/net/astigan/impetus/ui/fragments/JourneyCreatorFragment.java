package net.astigan.impetus.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import net.astigan.impetus.R;
import net.astigan.impetus.main.MainActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A fragment used to create a journey, with options to change the journey distance,
 * generate a new destination, and start travelling.
 */
public class JourneyCreatorFragment extends Fragment {

    private static final int MAX_JOURNEY_DISTANCE = 50;
    private static final int DEFAULT_JOURNEY_DISTANCE = 1;

    public interface JourneyCreatorFragmentListener {
        public void onJourneyStart();
        public void onRandomiseJourney(int distanceKm);
        public void onSeekbarChanged(int distanceKm);
    }

    @InjectView(R.id.distance_seekbar) SeekBar seekBar;
    @InjectView(R.id.distance_choice) TextView distanceChoice;
    @InjectView(R.id.randomise_journey_button) Button randomiseJourneyButton;
    @InjectView(R.id.create_journey_button) Button getLostBtn;

    @OnClick(R.id.create_journey_button) void startJourney() {
        listener.onJourneyStart();
    }

	private JourneyCreatorFragmentListener listener;

	public static JourneyCreatorFragment newInstance() {
		JourneyCreatorFragment fragment = new JourneyCreatorFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_journey_creator, container, false);
        ButterKnife.inject(this, view);

        getLostBtn.setEnabled(false);
        randomiseJourneyButton.setOnClickListener(new RandomiseJourneyListener(getLostBtn, seekBar));

        seekBar.setMax(MAX_JOURNEY_DISTANCE);
        seekBar.setProgress(DEFAULT_JOURNEY_DISTANCE);
        seekBar.setOnSeekBarChangeListener(new DistanceSeekBarListener(getLostBtn));

        setDistanceLabel(DEFAULT_JOURNEY_DISTANCE);
		
		if (getActivity() instanceof MainActivity) {
			listener = (JourneyCreatorFragmentListener) getActivity();
		} else {
            throw new RuntimeException("Activity must implement JourneyCreatorFragmentListener!");
        }
		
		return view;
	}

    public void informNoJourneyAvailable() {
        getLostBtn.setEnabled(false);
    }

    private class RandomiseJourneyListener implements OnClickListener {

        private final View getLostBtn;
        private final SeekBar seekBar;

        public RandomiseJourneyListener(View getLostBtn, SeekBar seekBar) {
            this.getLostBtn = getLostBtn;
            this.seekBar = seekBar;
        }

        @Override
        public void onClick(View v) {
            listener.onRandomiseJourney(getSeekbarDistanceInMetres());
            getLostBtn.setEnabled(true);
        }

        private int getSeekbarDistanceInMetres() {
            return seekBar.getProgress() * 1000;
        }
    }

    private class DistanceSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        private final View btnGetLost;
        private boolean swiping = false;

        public DistanceSeekBarListener(View btnGetLost) {
            this.btnGetLost = btnGetLost;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (i == 0) { // workaround to set min value on seekbar to 1
                i = 1;
                seekBar.setProgress(i);
            }
            btnGetLost.setEnabled(false);

            if (!swiping) {
                listener.onSeekbarChanged(seekBar.getProgress());
            }
            setDistanceLabel(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            swiping = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            swiping = false;
            listener.onSeekbarChanged(seekBar.getProgress());
        }
    }

    private void setDistanceLabel(int distanceKm) {
        String distanceText = Integer.toString(distanceKm);
        String labelText = getString(R.string.distance_label_wildcard, distanceText);

        int spanStart = labelText.indexOf(distanceText.charAt(0));
        int spanEnd = labelText.length();

        if (spanStart != -1 && spanEnd != -1) {
            final SpannableStringBuilder sb = new SpannableStringBuilder(labelText);
            final StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
            sb.setSpan(styleSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            distanceChoice.setText(sb);
        }
        else {
            Crashlytics.log("Distance label error: " + distanceText + labelText + spanStart + spanEnd);
        }
    }

}