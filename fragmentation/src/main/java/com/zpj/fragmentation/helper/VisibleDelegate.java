package com.zpj.fragmentation.helper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentationMagician;

import com.zpj.fragmentation.ISupportFragment;

import java.util.List;

/**
 * Created by YoKey on 17/4/4.
 */

public class VisibleDelegate {

    private static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave";
    private static final String FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace";

    // SupportVisible相关
    private boolean mIsSupportVisible;
    private boolean mNeedDispatch = true;
    private boolean mInvisibleWhenLeave;
    private boolean mIsFirstVisible = true;
    private boolean  mIsLazyInit;
    private boolean mFirstCreateViewCompatReplace = true;

    private Handler mHandler;
    private Bundle mSaveInstanceState;

    private final ISupportFragment mSupportF;
    private final Fragment mFragment;

    public VisibleDelegate(ISupportFragment fragment) {
        this.mSupportF = fragment;
        this.mFragment = (Fragment) fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState;
            // setUserVisibleHint() may be called before onCreate()
            mInvisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE);
            mFirstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mInvisibleWhenLeave);
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, mFirstCreateViewCompatReplace);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (!mFirstCreateViewCompatReplace && mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:")) {
            return;
        }

        if (mFirstCreateViewCompatReplace) {
            mFirstCreateViewCompatReplace = false;
        }

        if (!mInvisibleWhenLeave && !mFragment.isHidden() && mFragment.getUserVisibleHint()) {
            if ((mFragment.getParentFragment() != null && isFragmentVisible(mFragment.getParentFragment()))
                    || mFragment.getParentFragment() == null) {
                mNeedDispatch = false;
                safeDispatchUserVisibleHint(true);
            }
        }
    }

    public void onResume() {
        if (!mIsFirstVisible) {
            if (!mIsSupportVisible && !mInvisibleWhenLeave && isFragmentVisible(mFragment)) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        }
    }

    public void onPause() {
        if (mIsSupportVisible && isFragmentVisible(mFragment)) {
            mNeedDispatch = false;
            mInvisibleWhenLeave = false;
            dispatchSupportVisible(false);
        } else {
            mInvisibleWhenLeave = true;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (!hidden && !mFragment.isResumed()) {
            //if fragment is shown but not resumed, ignore...
            mInvisibleWhenLeave = false;
            return;
        }
        if (hidden) {
            safeDispatchUserVisibleHint(false);
        } else {
            enqueueDispatchVisible();
        }
    }

    public void onDestroyView() {
        mIsFirstVisible = true;
        mIsLazyInit = false;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mFragment.isResumed() || (!mFragment.isAdded() && isVisibleToUser)) {
            if (!mIsSupportVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true);
            } else if (mIsSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false);
            }
        }
    }

    private void safeDispatchUserVisibleHint(boolean visible) {
        if (mIsFirstVisible) {
            if (!visible) return;
            enqueueDispatchVisible();
        } else {
            dispatchSupportVisible(visible);
        }
    }

    private void enqueueDispatchVisible() {
        getHandler().post(() -> dispatchSupportVisible(true));
//        RxHandler.post(() -> {
//            dispatchSupportVisible(true);
//        });
    }

    private void dispatchSupportVisible(boolean visible) {
        if (visible && isParentInvisible()) return;

        if (mIsSupportVisible == visible) {
            mNeedDispatch = true;
            return;
        }

        mIsSupportVisible = visible;

        if (visible) {
            if (checkAddState()) return;
            mSupportF.onSupportVisible();

            if (mIsFirstVisible) {
                mIsFirstVisible = false;
                mSupportF.onLazyInitView(mSaveInstanceState);
                mIsLazyInit = true;
            }
            dispatchChild(true);
        } else {
            dispatchChild(false);
            mSupportF.onSupportInvisible();
        }
    }

    private void dispatchChild(boolean visible) {
        if (!mNeedDispatch) {
            mNeedDispatch = true;
        } else {
            if (checkAddState()) return;
            FragmentManager fragmentManager = mFragment.getChildFragmentManager();
            List<Fragment> childFragments = FragmentationMagician.getActiveFragments(fragmentManager);
            if (childFragments != null) {
                for (Fragment child : childFragments) {
                    if (child instanceof ISupportFragment && !child.isHidden() && child.getUserVisibleHint()) {
                        ((ISupportFragment) child).getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible);
                    }
                }
            }
        }
    }

    private boolean isParentInvisible() {
        Fragment parentFragment = mFragment.getParentFragment();

        if (parentFragment instanceof ISupportFragment) {
            return !((ISupportFragment) parentFragment).isSupportVisible();
        }

        return parentFragment != null && !parentFragment.isVisible();
    }

    private boolean checkAddState() {
        if (!mFragment.isAdded()) {
            mIsSupportVisible = !mIsSupportVisible;
            return true;
        }
        return false;
    }

    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.getUserVisibleHint();
    }

    public boolean isSupportVisible() {
        return mIsSupportVisible;
    }

    public boolean isLazyInit() {
        return mIsLazyInit;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}
