package com.zpj.fragmentation.demo.demo_flow.ui.fragment_swipe_back;

import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.zpj.fragmentation.demo.R;
import com.zpj.fragmentation.swipeback.SwipeBackFragment;

/**
 * Created by YoKeyword on 16/4/21.
 */
public class BaseSwipeBackFragment extends SwipeBackFragment {

    void _initToolbar(Toolbar toolbar) {
        toolbar.setTitle("SwipeBackActivity's Fragment");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mActivity.onBackPressed();
            }
        });
    }
}
