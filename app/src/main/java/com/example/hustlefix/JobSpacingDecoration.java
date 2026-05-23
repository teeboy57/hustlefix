package com.example.hustlefix;

import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JobSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public JobSpacingDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = spacing;
        outRect.right = spacing;
        outRect.bottom = spacing;

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = spacing;
        }

        // Apply fade + slide animation when item is attached
        Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up);

        view.startAnimation(fadeIn);
        view.startAnimation(slideUp);
    }
}
