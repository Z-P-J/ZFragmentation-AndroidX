package com.zpj.fragmentation.dialog.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.zpj.fragmentation.ISupportFragment;
import com.zpj.fragmentation.SupportActivity;
import com.zpj.fragmentation.SupportFragment;
import com.zpj.fragmentation.dialog.AbstractDialogFragment;
import com.zpj.fragmentation.dialog.IDialog;
import com.zpj.fragmentation.dialog.R;
import com.zpj.fragmentation.dialog.animator.DialogAnimator;
import com.zpj.fragmentation.dialog.animator.ShadowMaskAnimator;
import com.zpj.utils.ContextUtils;
import com.zpj.utils.ScreenUtils;

import java.lang.ref.WeakReference;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class BaseDialogFragment<T extends BaseDialogFragment<T>> extends AbstractDialogFragment {

    protected DialogAnimator mDialogAnimator;
    protected DialogAnimator mShadowAnimator;

    private FrameLayout rootView;
    private ViewGroup implView;

    protected boolean isDismissing;

    protected boolean interceptTouch = true;
    protected boolean cancelable = true;
    protected boolean cancelableInTouchOutside = true;

    private int gravity = Gravity.CENTER;

    protected int maxWidth = WRAP_CONTENT;
    protected int maxHeight = WRAP_CONTENT;
    protected int marginStart, marginTop, marginEnd, marginBottom;

    protected IDialog.OnDismissListener onDismissListener;

    private WeakReference<ISupportFragment> preFragment;

    protected Drawable bgDrawable;

    @Override
    protected final int getLayoutId() {
        return R.layout._dialog_layout_dialog_view;
    }

    protected abstract int getImplLayoutId();

    protected abstract DialogAnimator onCreateDialogAnimator(ViewGroup contentView);

    protected DialogAnimator onCreateShadowAnimator(FrameLayout flContainer) {
        return new ShadowMaskAnimator(flContainer);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        preFragment = new WeakReference<>(getPreFragment());
        FrameLayout flContainer = findViewById(R.id._dialog_fl_container);
        this.rootView = flContainer;

        if (interceptTouch) {
            interceptTouch();
        }


        implView = (ViewGroup) getLayoutInflater().inflate(getImplLayoutId(), flContainer, false);
        flContainer.addView(implView);

        initLayoutParams(implView);

    }

    protected void initLayoutParams(ViewGroup view) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = getGravity();
        params.leftMargin = getMarginStart();
        params.topMargin = getMarginTop();
        params.rightMargin = getMarginEnd();
        params.bottomMargin = getMarginBottom();
        params.height = getMaxHeight();
        params.width = getMaxWidth();
        view.setFocusableInTouchMode(true);
        view.setFocusable(true);
        view.setClickable(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        getRootView().getViewTreeObserver()
//                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        doShowAnimation();
//                    }
//                });


        getRootView()
                .getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getRootView().getViewTreeObserver().removeOnPreDrawListener(this);
                        mShadowAnimator = onCreateShadowAnimator(rootView);
                        if (mShadowAnimator != null) {
                            mShadowAnimator.setShowDuration(getShowAnimDuration());
                            mShadowAnimator.setDismissDuration(getDismissAnimDuration());
                        }
                        mDialogAnimator = onCreateDialogAnimator(implView);
                        if (mDialogAnimator != null) {
                            mDialogAnimator.setShowDuration(getShowAnimDuration());
                            mDialogAnimator.setDismissDuration(getDismissAnimDuration());
                        }
                        doShowAnimation();
                        return false;
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (preFragment != null) {
            ISupportFragment fragment = preFragment.get();
            if (fragment != null && fragment == getTopFragment()) {
                fragment.onSupportVisible();
                preFragment.clear();
            }
        }
        preFragment = null;
        this.isDismissing = false;
        super.onDestroy();
    }

    @Override
    protected boolean onBackPressed() {
        return !cancelable;
    }

    @Override
    public void pop() {
        if (!cancelable) {
            return;
        }
        dismiss();
    }

    protected T self() {
        return (T) this;
    }

    public T show(SupportFragment fragment) {
        onBeforeShow();
        fragment.start(this);
        return self();
    }

    public T show(Context context) {
        onBeforeShow();
        Activity activity = ContextUtils.getActivity(context);
        if (activity instanceof SupportActivity) {
            ((SupportActivity) activity).start(this);
        } else if (activity instanceof FragmentActivity) {
            FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, "tag");
            ft.commit();
        } else {
            throw new RuntimeException("the context is not a FragmentActivity object!");
        }
        return self();
    }

    public T show(SupportActivity activity) {
        onBeforeShow();
        activity.start(this);
        return self();
    }

    public void doShowAnimation() {
        if (mDialogAnimator != null) {
            mDialogAnimator.setShowDuration(getShowAnimDuration());
            mDialogAnimator.animateToShow();
        }

        if (mShadowAnimator != null) {
            mShadowAnimator.setShowDuration(getShowAnimDuration());
            mShadowAnimator.animateToShow();
        }
    }

    public void doDismissAnimation() {
        if (mDialogAnimator != null) {
            mDialogAnimator.setDismissDuration(getDismissAnimDuration());
            mDialogAnimator.animateToDismiss();
        }
        if (mShadowAnimator != null) {
            mShadowAnimator.setDismissDuration(getDismissAnimDuration());
            mShadowAnimator.animateToDismiss();
        }
    }

    public void dismiss() {
        postOnEnterAnimationEnd(() -> {
            if (!isDismissing) {
                isDismissing = true;
                doDismissAnimation();

                onDismiss();
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BaseDialogFragment.super.popThis();
                        isDismissing = false;
                        onAfterDismissed();
                    }
                }, getDismissAnimDuration());
            }
        });
    }

    protected void onDismiss() {
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    protected void onAfterDismissed() {
    }

    protected void onBeforeShow() {
        isDismissing = false;
    }

    protected void onHide() {

    }

    protected int getGravity() {
        return gravity;
    }

    protected int getMaxWidth() {
        return maxWidth;
    }

    protected int getMaxHeight() {
        return maxHeight;
    }

    public int getMarginStart() {
        return marginStart;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginEnd() {
        return marginEnd;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    protected FrameLayout getRootView() {
        return rootView;
    }

    protected ViewGroup getImplView() {
        return implView;
    }

    public T setShowAnimDuration(long duration) {
        this.showAnimDuration = duration;
        duration = getShowAnimDuration();
        if (mDialogAnimator != null) {
            mDialogAnimator.setShowDuration(duration);
        }
        if (mShadowAnimator != null) {
            mShadowAnimator.setShowDuration(duration);
        }
        return self();
    }

    public T setDismissAnimDuration(long duration) {
        this.dismissAnimDuration = duration;
        duration = getDismissAnimDuration();
        if (mDialogAnimator != null) {
            mDialogAnimator.setDismissDuration(duration);
        }
        if (mShadowAnimator != null) {
            mShadowAnimator.setDismissDuration(duration);
        }
        return self();
    }

    public T setDialogBackground(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return self();
    }

    public T setGravity(int gravity) {
        this.gravity = gravity;
        return self();
    }

    public T setMaxWidth(int maxWidth) {
        if (maxWidth == WRAP_CONTENT || maxWidth == MATCH_PARENT) {
            this.maxWidth = maxWidth;
        } else if (maxWidth >= 0) {
            this.maxWidth = maxWidth;
            int margin = ScreenUtils.getScreenWidth() - maxWidth;
            if (margin > 0) {
                setMarginStart(margin);
                setMarginEnd(margin);
            }
        }
        return self();
    }

    public T setMaxHeight(int maxHeight) {
        if (maxHeight == WRAP_CONTENT || maxHeight == MATCH_PARENT) {
            this.maxHeight = maxHeight;
        } else if (maxHeight >= 0) {
            this.maxHeight = maxHeight;
            int margin = ScreenUtils.getScreenHeight() - maxHeight;
            if (margin > 0) {
                setMarginTop(margin);
                setMarginBottom(margin);
            }
        }
        return self();
    }

    public T setMarginStart(int marginStart) {
        this.marginStart = marginStart;
        return self();
    }

    public T setMarginTop(int marginTop) {
        this.marginTop = marginTop;
        return self();
    }

    public T setMarginEnd(int marginEnd) {
        this.marginEnd = marginEnd;
        return self();
    }

    public T setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
        return self();
    }

    public T setMarginHorizontal(int margin) {
        setMarginStart(margin);
        setMarginEnd(margin);
        return self();
    }

    public T setMarginVertical(int margin) {
        setMarginTop(margin);
        setMarginBottom(margin);
        return self();
    }

    public T setInterceptTouch(boolean interceptTouch) {
        this.interceptTouch = interceptTouch;
        if (rootView != null) {
            if (interceptTouch) {
                interceptTouch();
            } else {
                rootView.setOnClickListener(null);
                rootView.setOnLongClickListener(null);
                rootView.setClickable(false);
                rootView.setLongClickable(false);
            }
        }
        return self();
    }

    private void interceptTouch() {
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cancelable || !cancelableInTouchOutside) {
                    return;
                }
                dismiss();
            }
        });

        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    public T setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return self();
    }

    public T setCancelableInTouchOutside(boolean cancelableInTouchOutside) {
        this.cancelableInTouchOutside = cancelableInTouchOutside;
        return self();
    }

    public T setOnDismissListener(IDialog.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return self();
    }

}
