package com.zpj.fragmentation.dialog.base;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import android.view.View;

import com.zpj.fragmentation.dialog.R;

public abstract class CardDialogFragment<T extends CardDialogFragment<T>> extends ContainerDialogFragment<T> {

    private boolean transparent = false;

    @Override
    protected final boolean isDragDialog() {
        return false;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);
        if (transparent) {
            CardView cardView = findViewById(R.id._centerPopupContainer);
            cardView.setCardElevation(0);
            cardView.setUseCompatPadding(false);
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    public T setTransparentBackground(boolean transparent) {
        this.transparent = transparent;
        return self();
    }
}
