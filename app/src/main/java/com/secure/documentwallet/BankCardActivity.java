package com.secure.documentwallet;

import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankCardActivity extends FragmentActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private VaultRepository repo;
    private JSONObject document;
    private Bitmap front;
    private Bitmap back;
    private FrameLayout card;
    private TextView sideLabel;
    private boolean showingBack;
    private boolean ready;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.safeScreen(this);
        Ui.lightBars(this);
        if (!Session.valid()) { finish(); return; }
        repo = new VaultRepository(this);
        buildPage();
        load();
    }

    private void buildPage() {
        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(0xfffbfaf6);
        page.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 28));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView backButton = Ui.text(this, "‹", 40, 0xff061f3b);
        backButton.setGravity(Gravity.CENTER);
        backButton.setContentDescription("Back To Bank Cards");
        backButton.setOnClickListener(v -> finish());
        bar.addView(backButton, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 54)));
        LinearLayout titles = Ui.column(this);
        TextView title = Ui.title(this, "Bank Card", 25);
        title.setTextColor(0xff061f3b);
        titles.addView(title);
        titles.addView(Ui.text(this, "Encrypted Front And Back", 13, 0xff727a87));
        bar.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(bar);

        TextView instruction = Ui.text(this, "Tap The Card To Flip It", 15, 0xff0e6f69);
        instruction.setTypeface(null, android.graphics.Typeface.BOLD);
        instruction.setGravity(Gravity.CENTER);
        instruction.setPadding(0, Ui.dp(this, 23), 0, Ui.dp(this, 15));
        page.addView(instruction);

        card = new FrameLayout(this);
        card.setBackground(Ui.bg(Color.WHITE, Ui.dp(this, 24)));
        card.setElevation(Ui.dp(this, 10));
        card.setCameraDistance(Ui.dp(this, 9000));
        card.setOnClickListener(v -> flip());
        ProgressBar loading = new ProgressBar(this);
        card.addView(loading, new FrameLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48), Gravity.CENTER));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 238));
        cardParams.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), Ui.dp(this, 18));
        page.addView(card, cardParams);

        sideLabel = Ui.text(this, "Loading Secure Card…", 13, 0xff727a87);
        sideLabel.setGravity(Gravity.CENTER);
        page.addView(sideLabel, new LinearLayout.LayoutParams(-1, Ui.dp(this, 28)));

        TextView privacy = Ui.text(this, "The card is decrypted only while this private screen is open. Tap anywhere on the card to switch sides.", 13, Ui.MUTED);
        privacy.setGravity(Gravity.CENTER);
        privacy.setLineSpacing(0, 1.12f);
        privacy.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 22));
        page.addView(privacy);

        TextView details = Ui.pill(this, "Open Full Document And Export Options", 0xff087f78);
        details.setOnClickListener(v -> { if (document != null) startActivity(new Intent(this, DocumentActivity.class).putExtra("id", document.optString("id"))); });
        page.addView(details, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));
        setContentView(page);
    }

    private void load() {
        String id = getIntent().getStringExtra("id");
        io.execute(() -> {
            try {
                document = repo.get(id);
                if (document == null) throw new IllegalStateException("Card Not Found");
                JSONArray pages = document.optJSONArray("pages");
                if (pages == null || pages.length() == 0) throw new IllegalStateException("No Card Image Was Saved");
                front = decode(repo.page(document, 0));
                if (pages.length() > 1) back = decode(repo.page(document, 1));
                runOnUiThread(() -> { ready = true; showSide(false); });
            } catch (Exception error) {
                runOnUiThread(() -> showError(error.getMessage()));
            }
        });
    }

    private Bitmap decode(byte[] data) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sample > 1800) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private void flip() {
        if (!ready || card.getAnimation() != null) return;
        card.setEnabled(false);
        card.animate().rotationY(90f).setDuration(170).withEndAction(() -> {
            showingBack = !showingBack;
            showSide(showingBack);
            card.setRotationY(-90f);
            card.animate().rotationY(0f).setDuration(190).withEndAction(() -> card.setEnabled(true)).start();
        }).start();
    }

    private void showSide(boolean backSide) {
        card.removeAllViews();
        Bitmap image = backSide ? back : front;
        if (image != null) {
            ImageView view = new ImageView(this);
            view.setImageBitmap(image);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
            card.addView(view, new FrameLayout.LayoutParams(-1, -1));
        } else {
            LinearLayout missing = Ui.column(this);
            missing.setGravity(Gravity.CENTER);
            missing.setBackground(Ui.bg(0xff0e6f69, Ui.dp(this, 24)));
            TextView stripe = new TextView(this);
            stripe.setBackgroundColor(0xff203f49);
            missing.addView(stripe, new LinearLayout.LayoutParams(-1, Ui.dp(this, 46)));
            TextView message = Ui.text(this, "No Back Image Was Saved", 17, Color.WHITE);
            message.setGravity(Gravity.CENTER);
            message.setTypeface(null, android.graphics.Typeface.BOLD);
            missing.addView(message, new LinearLayout.LayoutParams(-1, 0, 1));
            card.addView(missing, new FrameLayout.LayoutParams(-1, -1));
        }
        TextView badge = Ui.text(this, backSide ? "BACK" : "FRONT", 11, Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        badge.setBackground(Ui.bg(0xff071f3a, Ui.dp(this, 12)));
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(Ui.dp(this, 68), Ui.dp(this, 26), Gravity.TOP | Gravity.END);
        badgeParams.setMargins(0, Ui.dp(this, 13), Ui.dp(this, 13), 0);
        card.addView(badge, badgeParams);
        sideLabel.setText((backSide ? "Back Of " : "Front Of ") + document.optString("title", "Bank Card"));
    }

    private void showError(String message) {
        card.removeAllViews();
        TextView error = Ui.text(this, message == null ? "Could Not Open This Card" : message, 15, 0xffb3261e);
        error.setGravity(Gravity.CENTER);
        card.addView(error, new FrameLayout.LayoutParams(-1, -1));
        sideLabel.setText("Card Unavailable");
    }

    @Override public boolean dispatchTouchEvent(android.view.MotionEvent event) {
        if (Session.valid()) Session.touch();
        return super.dispatchTouchEvent(event);
    }

    @Override protected void onResume() { super.onResume(); if (!Session.valid()) finish(); }

    @Override protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
        if (front != null && !front.isRecycled()) front.recycle();
        if (back != null && !back.isRecycled()) back.recycle();
    }
}
