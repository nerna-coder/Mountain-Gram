package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.mountaingram.MountainGramConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class MountainGramSettingsActivity extends BaseFragment {

    // Позиции строк
    private static final int ROW_GHOST_HEADER   = 0;
    private static final int ROW_NO_READ        = 1;
    private static final int ROW_NO_TYPING      = 2;
    private static final int ROW_NO_ONLINE      = 3;
    private static final int ROW_GHOST_INFO     = 4;
    private static final int ROW_HISTORY_HEADER = 5;
    private static final int ROW_SAVE_DELETED   = 6;
    private static final int ROW_SAVE_EDITS     = 7;
    private static final int ROW_HISTORY_INFO   = 8;
    private static final int ROW_COUNT          = 9;

    // Типы ячеек
    private static final int TYPE_HEADER  = 0;
    private static final int TYPE_CHECK   = 1;
    private static final int TYPE_INFO    = 2;
    private static final int TYPE_SHADOW  = 3;

    private RecyclerListView listView;
    private SettingsAdapter  adapter;
    private MountainGramConfig cfg;

    @Override
    public boolean onFragmentCreate() {
        cfg = MountainGramConfig.getInstance();
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Mountain Gram");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frame = (FrameLayout) fragmentView;
        frame.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        adapter = new SettingsAdapter(context);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((view, position) -> {
            if (view instanceof TextCheckCell) {
                TextCheckCell cell = (TextCheckCell) view;
                switch (position) {
                    case ROW_NO_READ:
                        cfg.set(MountainGramConfig.KEY_GHOST_NO_READ,   !cfg.isGhostNoRead());
                        cell.setChecked(cfg.isGhostNoRead());
                        break;
                    case ROW_NO_TYPING:
                        cfg.set(MountainGramConfig.KEY_GHOST_NO_TYPING, !cfg.isGhostNoTyping());
                        cell.setChecked(cfg.isGhostNoTyping());
                        break;
                    case ROW_NO_ONLINE:
                        cfg.set(MountainGramConfig.KEY_GHOST_NO_ONLINE, !cfg.isGhostNoOnline());
                        cell.setChecked(cfg.isGhostNoOnline());
                        break;
                    case ROW_SAVE_DELETED:
                        cfg.set(MountainGramConfig.KEY_SAVE_DELETED,    !cfg.isSaveDeleted());
                        cell.setChecked(cfg.isSaveDeleted());
                        break;
                    case ROW_SAVE_EDITS:
                        cfg.set(MountainGramConfig.KEY_SAVE_EDITS,      !cfg.isSaveEdits());
                        cell.setChecked(cfg.isSaveEdits());
                        break;
                }
            }
        });

        frame.addView(listView, LayoutHelper.createFrame(
            LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT
        ));
        return fragmentView;
    }

    // ─── Адаптер ─────────────────────────────────────────────────────────────

    private class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context ctx;

        SettingsAdapter(Context ctx) { this.ctx = ctx; }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case ROW_GHOST_HEADER:
                case ROW_HISTORY_HEADER: return TYPE_HEADER;
                case ROW_NO_READ:
                case ROW_NO_TYPING:
                case ROW_NO_ONLINE:
                case ROW_SAVE_DELETED:
                case ROW_SAVE_EDITS:   return TYPE_CHECK;
                case ROW_GHOST_INFO:
                case ROW_HISTORY_INFO: return TYPE_INFO;
                default:               return TYPE_SHADOW;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            switch (viewType) {
                case TYPE_HEADER: v = new HeaderCell(ctx);         break;
                case TYPE_CHECK:  v = new TextCheckCell(ctx);      break;
                case TYPE_INFO:   v = new TextInfoPrivacyCell(ctx); break;
                default:          v = new ShadowSectionCell(ctx);  break;
            }
            v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            boolean ru = LocaleController.isRTL || isRussian();
            switch (position) {
                case ROW_GHOST_HEADER:
                    ((HeaderCell) holder.itemView).setText(
                        ru ? "👻 Режим призрака" : "👻 Ghost Mode"
                    );
                    break;
                case ROW_NO_READ:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(
                        ru ? "Не отправлять «Прочитано»" : "Don't send read receipts",
                        cfg.isGhostNoRead(), true
                    );
                    break;
                case ROW_NO_TYPING:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(
                        ru ? "Не отправлять «Печатает…»" : "Don't send typing status",
                        cfg.isGhostNoTyping(), true
                    );
                    break;
                case ROW_NO_ONLINE:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(
                        ru ? "Скрыть статус «Онлайн»" : "Hide online status",
                        cfg.isGhostNoOnline(), false
                    );
                    break;
                case ROW_GHOST_INFO:
                    ((TextInfoPrivacyCell) holder.itemView).setText(
                        ru ? "Режим призрака позволяет читать сообщения, не оставляя следов присутствия."
                           : "Ghost mode lets you read messages without leaving any trace of presence."
                    );
                    break;
                case ROW_HISTORY_HEADER:
                    ((HeaderCell) holder.itemView).setText(
                        ru ? "🗂 История сообщений" : "🗂 Message History"
                    );
                    break;
                case ROW_SAVE_DELETED:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(
                        ru ? "Сохранять удалённые сообщения" : "Save deleted messages",
                        cfg.isSaveDeleted(), true
                    );
                    break;
                case ROW_SAVE_EDITS:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(
                        ru ? "Хранить историю правок" : "Keep edit history",
                        cfg.isSaveEdits(), false
                    );
                    break;
                case ROW_HISTORY_INFO:
                    ((TextInfoPrivacyCell) holder.itemView).setText(
                        ru ? "Удалённые сообщения и все версии отредактированных сообщений сохраняются локально на устройстве."
                           : "Deleted messages and all versions of edited messages are saved locally on your device."
                    );
                    break;
            }
        }

        @Override
        public int getItemCount() { return ROW_COUNT; }

        private boolean isRussian() {
            String lang = LocaleController.getInstance().getCurrentLocaleInfo() != null
                ? LocaleController.getInstance().getCurrentLocaleInfo().shortName
                : "en";
            return lang.startsWith("ru");
        }
    }
}
