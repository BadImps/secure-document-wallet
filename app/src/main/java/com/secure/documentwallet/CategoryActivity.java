package com.secure.documentwallet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryActivity extends FragmentActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private VaultRepository repo;
    private LinearLayout list;
    private TextView count;
    private String group;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.safeScreen(this);
        Ui.lightBars(this);
        if (!Session.valid()) { finish(); return; }
        group = getIntent().getStringExtra("group");
        if (group == null) group = DocumentGroups.DOCUMENTS;
        repo = new VaultRepository(this);
        buildPage();
    }

    private void buildPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(0xfffbfaf6);
        page.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 30));
        scroll.addView(page);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = Ui.text(this, "‹", 40, 0xff061f3b);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription("Back To My Vault");
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 54)));

        ImageView categoryIcon = new ImageView(this);
        categoryIcon.setImageResource(DocumentGroups.icon(group));
        categoryIcon.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        categoryIcon.setBackground(Ui.bg(DocumentGroups.color(group), Ui.dp(this, 14)));
        bar.addView(categoryIcon, new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 56)));

        LinearLayout titles = Ui.column(this);
        titles.setPadding(Ui.dp(this, 14), 0, 0, 0);
        TextView title = Ui.title(this, group, 25);
        title.setTextColor(0xff061f3b);
        titles.addView(title);
        count = Ui.text(this, "Opening Secure Documents…", 13, 0xff727a87);
        titles.addView(count);
        bar.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(bar);

        TextView hint = Ui.text(this, DocumentGroups.CARDS.equals(group) ? "Choose a saved card to view it, then tap the card to flip between its front and back." : "Your encrypted files in this category are listed below.", 14, Ui.MUTED);
        hint.setLineSpacing(0, 1.12f);
        hint.setPadding(Ui.dp(this, 6), Ui.dp(this, 18), Ui.dp(this, 6), Ui.dp(this, 18));
        page.addView(hint);

        list = Ui.column(this);
        page.addView(list);
        setContentView(scroll);
    }

    private void load() {
        list.removeAllViews();
        ProgressBar progress = new ProgressBar(this);
        list.addView(progress, new LinearLayout.LayoutParams(-1, Ui.dp(this, 70)));
        io.execute(() -> {
            try {
                JSONArray all = repo.all();
                runOnUiThread(() -> render(all));
            } catch (Exception error) {
                runOnUiThread(() -> { list.removeAllViews(); Ui.toast(this, "Could Not Open This Category"); });
            }
        });
    }

    private void render(JSONArray all) {
        list.removeAllViews();
        int total = 0;
        for (int i = 0; i < all.length(); i++) {
            JSONObject document = all.optJSONObject(i);
            if (document != null && group.equals(DocumentGroups.fromCategory(document.optString("category")))) total++;
        }
        count.setText(total + " Saved " + (total == 1 ? "Item" : "Items"));
        if (total == 0) { showEmpty(); return; }

        for (int i = all.length() - 1; i >= 0; i--) {
            JSONObject document = all.optJSONObject(i);
            if (document == null || !group.equals(DocumentGroups.fromCategory(document.optString("category")))) continue;
            list.addView(documentRow(document), rowParams());
        }
    }

    private View documentRow(JSONObject document) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
        card.setBackground(Ui.bg(Color.WHITE, Ui.dp(this, 19)));
        card.setElevation(Ui.dp(this, 5));

        FrameLayout iconBox = new FrameLayout(this);
        iconBox.setBackground(Ui.bg(DocumentGroups.color(group), Ui.dp(this, 14)));
        ImageView icon = new ImageView(this);
        icon.setImageResource(DocumentGroups.icon(group));
        icon.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        iconBox.addView(icon, new FrameLayout.LayoutParams(-1, -1));
        card.addView(iconBox, new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64)));

        LinearLayout words = Ui.column(this);
        words.setPadding(Ui.dp(this, 15), 0, Ui.dp(this, 7), 0);
        TextView name = Ui.title(this, document.optString("title", "Document"), 18);
        name.setTextColor(0xff061f3b);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        words.addView(name);
        JSONArray pages = document.optJSONArray("pages");
        int pageCount = pages == null ? 0 : pages.length();
        String detail = DocumentGroups.CARDS.equals(group) ? "Tap To View And Flip" : pageCount + " Page" + (pageCount == 1 ? "" : "s");
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(document.optLong("createdAt")));
        words.addView(Ui.text(this, detail + "  ·  " + date, 13, 0xff7b838e));
        card.addView(words, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = Ui.text(this, "›", 32, 0xff71807d);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow, new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 54)));
        card.setOnClickListener(v -> open(document));
        return card;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, Ui.dp(this, 92));
        params.bottomMargin = Ui.dp(this, 13);
        return params;
    }

    private void open(JSONObject document) {
        Class<?> destination = DocumentGroups.CARDS.equals(group) ? BankCardActivity.class : DocumentActivity.class;
        startActivity(new Intent(this, destination).putExtra("id", document.optString("id")));
    }

    private void showEmpty() {
        LinearLayout empty = Ui.column(this);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(Ui.dp(this, 20), Ui.dp(this, 42), Ui.dp(this, 20), Ui.dp(this, 42));
        empty.setBackground(Ui.stroke(Color.WHITE, Ui.dp(this, 1), 0xffe0e6e3, Ui.dp(this, 20)));
        ImageView icon = new ImageView(this);
        icon.setImageResource(DocumentGroups.icon(group));
        icon.setAlpha(.82f);
        empty.addView(icon, new LinearLayout.LayoutParams(-1, Ui.dp(this, 78)));
        TextView title = Ui.title(this, "No " + group + " Saved Yet", 19);
        title.setTextColor(0xff061f3b);
        title.setGravity(Gravity.CENTER);
        empty.addView(title);
        TextView message = Ui.text(this, "Return to My Vault and scan your first item for this category.", 14, Ui.MUTED);
        message.setGravity(Gravity.CENTER);
        message.setPadding(Ui.dp(this, 18), Ui.dp(this, 9), Ui.dp(this, 18), 0);
        empty.addView(message);
        list.addView(empty);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!Session.valid()) { finish(); return; }
        if (list != null) load();
    }

    @Override public boolean dispatchTouchEvent(android.view.MotionEvent event) {
        if (Session.valid()) Session.touch();
        return super.dispatchTouchEvent(event);
    }

    @Override protected void onDestroy() { super.onDestroy(); io.shutdownNow(); }
}
