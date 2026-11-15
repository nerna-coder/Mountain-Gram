package org.telegram.messenger.mountaingram;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

public class DeletedMessagesManager extends SQLiteOpenHelper {

    private static final String DB_NAME    = "mg_deleted.db";
    private static final int    DB_VERSION = 2;

    private static final String TABLE_DELETED = "deleted_messages";
    private static final String TABLE_EDITS   = "edit_history";

    private static volatile DeletedMessagesManager instance;

    public static DeletedMessagesManager getInstance() {
        if (instance == null) {
            synchronized (DeletedMessagesManager.class) {
                if (instance == null)
                    instance = new DeletedMessagesManager(ApplicationLoader.applicationContext);
            }
        }
        return instance;
    }

    private DeletedMessagesManager(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_DELETED + " (" +
            "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "msg_id      INTEGER NOT NULL," +
            "dialog_id   INTEGER NOT NULL," +
            "sender_id   INTEGER NOT NULL," +
            "sender_name TEXT," +
            "text        TEXT," +
            "date        INTEGER NOT NULL," +
            "deleted_at  INTEGER NOT NULL," +
            "media_path  TEXT" +
            ");"
        );
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_EDITS + " (" +
            "id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "msg_id      INTEGER NOT NULL," +
            "dialog_id   INTEGER NOT NULL," +
            "old_text    TEXT," +
            "new_text    TEXT," +
            "edit_date   INTEGER NOT NULL" +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EDITS);
        onCreate(db);
    }

    // ─── Сохранить удалённое сообщение ───────────────────────────────────────

    public void saveDeletedMessage(TLRPC.Message msg, long dialogId) {
        if (!MountainGramConfig.getInstance().isSaveDeleted()) return;
        if (msg == null) return;

        String senderName = "";
        if (msg.from_id != null) {
            // имя подтянется через MessagesController при отображении — здесь храним id
        }

        ContentValues cv = new ContentValues();
        cv.put("msg_id",      msg.id);
        cv.put("dialog_id",   dialogId);
        cv.put("sender_id",   msg.from_id != null ? msg.from_id.user_id : 0);
        cv.put("text",        msg.message != null ? msg.message : "");
        cv.put("date",        msg.date);
        cv.put("deleted_at",  System.currentTimeMillis() / 1000L);

        getWritableDatabase().insertWithOnConflict(
            TABLE_DELETED, null, cv, SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    // ─── Сохранить версию до редактирования ──────────────────────────────────

    public void saveEditHistory(TLRPC.Message oldMsg, TLRPC.Message newMsg, long dialogId) {
        if (!MountainGramConfig.getInstance().isSaveEdits()) return;
        if (oldMsg == null || newMsg == null) return;

        ContentValues cv = new ContentValues();
        cv.put("msg_id",    oldMsg.id);
        cv.put("dialog_id", dialogId);
        cv.put("old_text",  oldMsg.message != null ? oldMsg.message : "");
        cv.put("new_text",  newMsg.message != null ? newMsg.message : "");
        cv.put("edit_date", newMsg.edit_date);

        getWritableDatabase().insert(TABLE_EDITS, null, cv);
    }

    // ─── Получить удалённые по диалогу ───────────────────────────────────────

    public List<DeletedEntry> getDeletedForDialog(long dialogId) {
        List<DeletedEntry> result = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
            TABLE_DELETED, null,
            "dialog_id=?", new String[]{String.valueOf(dialogId)},
            null, null, "deleted_at DESC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                DeletedEntry e = new DeletedEntry();
                e.msgId      = c.getInt(c.getColumnIndexOrThrow("msg_id"));
                e.senderId   = c.getLong(c.getColumnIndexOrThrow("sender_id"));
                e.text       = c.getString(c.getColumnIndexOrThrow("text"));
                e.date       = c.getLong(c.getColumnIndexOrThrow("date"));
                e.deletedAt  = c.getLong(c.getColumnIndexOrThrow("deleted_at"));
                result.add(e);
            }
            c.close();
        }
        return result;
    }

    // ─── Получить историю правок ──────────────────────────────────────────────

    public List<EditEntry> getEditsForMessage(int msgId, long dialogId) {
        List<EditEntry> result = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
            TABLE_EDITS, null,
            "msg_id=? AND dialog_id=?",
            new String[]{String.valueOf(msgId), String.valueOf(dialogId)},
            null, null, "edit_date ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                EditEntry e = new EditEntry();
                e.oldText  = c.getString(c.getColumnIndexOrThrow("old_text"));
                e.newText  = c.getString(c.getColumnIndexOrThrow("new_text"));
                e.editDate = c.getLong(c.getColumnIndexOrThrow("edit_date"));
                result.add(e);
            }
            c.close();
        }
        return result;
    }

    public static class DeletedEntry {
        public int    msgId;
        public long   senderId;
        public String text;
        public long   date;
        public long   deletedAt;
    }

    public static class EditEntry {
        public String oldText;
        public String newText;
        public long   editDate;
    }
}
