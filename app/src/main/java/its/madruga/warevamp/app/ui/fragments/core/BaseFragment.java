package its.madruga.warevamp.app.ui.fragments.core;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    public ActionBar getSupportActionBar() {
        if (getActivity() == null) return null;
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
}
