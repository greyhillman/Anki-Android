package com.ichi2.anki;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AnkiDb {
	
	static public SQLiteDatabase mDb;
	static public final int DB_OPEN_OPTS =
		SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS;
	
	static public void openDatabase(String filename) throws SQLException {
		if (mDb != null) {
			mDb.close();
		}
        mDb = SQLiteDatabase.openDatabase(filename, null, DB_OPEN_OPTS);		
	}
	
	static public abstract class AnkiModel {

		//abstract public AnkiModel instanceFromCursor(Cursor cursor);
	}
	
	static public class Card extends AnkiModel {
		
		public Integer id;
		public String question, answer;
		public double interval;
		
		static final public String TABLE = "cards";
		static final public String COLUMNS = "id,interval,question,answer";
		
		static public Card smallestIntervalCard() throws SQLException {
			Card card = oneFromCursor(
					AnkiDb.mDb.rawQuery("select " + COLUMNS + " from " + TABLE + " order by /*interval*/ random() limit 1", null)
					);
			Log.d("db", "Selected card id " + card.id + " with interval " + card.interval);
			return card;
		}
		
		static private Card oneFromCursor(Cursor cursor) {
			if (cursor.isClosed()) {
				throw new SQLException();
			}
			cursor.moveToFirst();
			return instanceFromCursor(cursor);
		}
		
		static Card instanceFromCursor(Cursor cursor) {
			Card card = new Card();
			card.id = cursor.getInt(0);
			card.interval = cursor.getDouble(1);
			card.question = cursor.getString(2);
			card.answer = cursor.getString(3);
			Log.d("db", "id=" + card.id + ", interval=" + card.interval);
			return card;
		}
		
		// Space this card because it has been successfully remembered.
		public void space() {
			double newInterval = 2*interval; // Very basic spaced repetition.
			String query = "update " + TABLE + " set interval=" + newInterval + " where id=" + id;
			Log.d("db", query);
			Cursor cursor = AnkiDb.mDb.rawQuery(query, null);
			Log.d("db", cursor.toString());
		}
		
		// Reset this card because it has not been successfully remembered.
		public void reset() {
			String query = "update " + TABLE + " set interval=0.1 where id=" + id;
			Log.d("db", query);
			AnkiDb.mDb.rawQuery(query, null);
		}
	}
	
	static public class FactModel extends AnkiModel {
		
		public String tableName() { return "facts"; }
		public String columnMatch() { return "*"; }
	}
	
	static public class DeckModel extends AnkiModel {
		
		public String tableName() { return "decks"; }
		public String columnMatch() { return "*"; }
	}
}
