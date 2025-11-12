package com.prm392.g5.labverse.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private int parentWidth;
    private int parentHeight;
    private float dX, dY;
    private boolean isDragging = false;
    private static final float CLICK_DRAG_TOLERANCE = 10f;
    private float downX, downY;

    public DraggableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parentWidth = parent.getWidth();
            parentHeight = parent.getHeight();
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                downX = event.getRawX();
                downY = event.getRawY();
                isDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float movedX = Math.abs(event.getRawX() - downX);
                float movedY = Math.abs(event.getRawY() - downY);

                if (movedX > CLICK_DRAG_TOLERANCE || movedY > CLICK_DRAG_TOLERANCE) {
                    isDragging = true;
                }

                if (isDragging) {
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    // Prevent FAB from going outside the screen
                    if (newX < 0) newX = 0;
                    if (newY < 0) newY = 0;
                    if (newX + view.getWidth() > parentWidth) {
                        newX = parentWidth - view.getWidth();
                    }
                    if (newY + view.getHeight() > parentHeight) {
                        newY = parentHeight - view.getHeight();
                    }

                    view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    // If not dragging, treat as click
                    return performClick();
                }
                isDragging = false;
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
