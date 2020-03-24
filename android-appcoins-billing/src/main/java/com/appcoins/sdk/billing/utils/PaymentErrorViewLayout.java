package com.appcoins.sdk.billing.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.appcoins.sdk.billing.utils.LayoutUtils.dpToPx;
import static com.appcoins.sdk.billing.utils.LayoutUtils.generateRandomId;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setConstraint;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setMargins;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setPadding;

public class PaymentErrorViewLayout {

  private static int ERROR_TITLE_ID;
  private final Activity activity;
  private final int orientation;
  private Button errorPositiveButton;
  private TextView errorMessage;

  public PaymentErrorViewLayout(Activity activity, int orientation) {

    this.activity = activity;
    this.orientation = orientation;
  }

  public ViewGroup buildErrorView() {
    ViewGroup relativeLayout = new RelativeLayout(activity);
    setPadding(relativeLayout, 16, 16, 16, 16);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(160));

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(Color.WHITE);
    gradientDrawable.setCornerRadius(dpToPx(8));
    relativeLayout.setBackground(gradientDrawable);

    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

    int start, end;

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      start = 16;
      end = 16;
    } else {
      start = 64;
      end = 64;
    }
    setMargins(layoutParams, start, 0, end, 0);

    relativeLayout.setLayoutParams(layoutParams);

    TextView errorTitle = buildErrorTitle();
    errorMessage = buildErrorMessage();
    errorPositiveButton = buildErrorPositiveButton();

    relativeLayout.addView(errorTitle);
    relativeLayout.addView(errorMessage);
    relativeLayout.addView(errorPositiveButton);

    return relativeLayout;
  }

  private Button buildErrorPositiveButton() {
    Button button = new Button(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36));
    setPadding(button, 0, 0, 4, 0);
    setConstraint(layoutParams, RelativeLayout.ALIGN_PARENT_RIGHT);
    setConstraint(layoutParams, RelativeLayout.ALIGN_PARENT_BOTTOM);
    setMargins(layoutParams, 0, 56, 0, 0);
    int[] gradientColors = { Color.parseColor("#FC9D48"), Color.parseColor("#FF578C") };
    GradientDrawable enableBackground =
        new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
    enableBackground.setShape(GradientDrawable.RECTANGLE);
    enableBackground.setStroke(dpToPx(1), Color.WHITE);
    enableBackground.setCornerRadius(dpToPx(16));
    button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    button.setBackground(enableBackground);

    button.setMaxWidth(dpToPx(126));
    button.setMinWidth(dpToPx(80));

    button.setTextColor(Color.WHITE);
    button.setTextSize(14);
    button.setText("OK".toUpperCase());
    button.setLayoutParams(layoutParams);

    return button;
  }

  private TextView buildErrorMessage() {
    TextView textView = new TextView(activity);
    textView.setId(generateRandomId());
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, ERROR_TITLE_ID);
    setMargins(layoutParams, 0, 8, 0, 0);

    textView.setLayoutParams(layoutParams);
    textView.setMaxLines(3);
    textView.setText("An error as ocurred");
    textView.setTextColor(Color.parseColor("#8a8a8a"));
    textView.setTextSize(12);
    return textView;
  }

  private TextView buildErrorTitle() {
    TextView textView = new TextView(activity);
    ERROR_TITLE_ID = generateRandomId();
    textView.setId(ERROR_TITLE_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    textView.setLayoutParams(layoutParams);
    textView.setText("Error");
    textView.setTextColor(Color.BLACK);
    textView.setTextSize(16);

    return textView;
  }

  public Button getErrorPositiveButton() {
    return errorPositiveButton;
  }

  public void setMessage(String message) {
    errorMessage.setText(message);
  }
}