package com.scottlindley.sudokuapp;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scott Lindley on 12/12/2016.
 */

public class DBHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = DBAssetHelper.DATA_BASE_NAME;
    public static final int VERSION_NUMBER = 1;

    public static final String PUZZLE_TABLE = "Puzzle";
    public static final String STATS_TABLE = "Stats";

    public static final String COL_KEY = "key";
    public static final String COL_DIFFICULTY = "difficulty";
    public static final String COL_HIGHSCORE = "highscore";
    public static final String COL_BEST_TIME = "besttime";
    public static final String COL_RACES_WON = "raceswon";
    public static final String COL_RACES_LOST = "raceslost";

    public static final String CREATE_PUZZLE_TABLE =
            "CREATE TABLE "+PUZZLE_TABLE+" ("+
                    COL_KEY+" TEXT, "+
                    COL_DIFFICULTY+" TEXT)";
    public static final String CREATE_STATS_TABLE =
            "CREATE TABLE "+STATS_TABLE+" ("+
                    COL_HIGHSCORE+" INTEGER, "+
                    COL_BEST_TIME+" TEXT, "+
                    COL_RACES_WON+" INTEGER, "+
                    COL_RACES_LOST+" INTEGER)";

    private static DBHelper sInstance;
    private Context mContext;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        mContext = context;
    }

    public static DBHelper getInstance(Context context){
        if (sInstance == null){
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PUZZLE_TABLE);
        sqLiteDatabase.execSQL(CREATE_STATS_TABLE);
        setUpBroadcastReceiver();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+PUZZLE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+STATS_TABLE);
        this.onCreate(sqLiteDatabase);
    }

    /**
     * Listens for a broadcasted intent from the Puzzle Refresh Service
     * Once received, it first look to see how many puzzles have been sent.
     * The method then loops through the puzzles and creates a new puzzle object
     * for each iteration of the loop.
     * The puzzles are then sent off to the {@link #replacePuzzles(List)} method.
     */
    private void setUpBroadcastReceiver(){
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Puzzle> puzzles = new ArrayList<>();
                int numberOfPuzzles =
                        intent.getIntExtra(PuzzleRefreshService.NUMBER_OF_PUZZLES_INTENT_KEY, -1);
                if (numberOfPuzzles != -1){
                    for (int i=0; i<numberOfPuzzles; i++){
                        try {
                            JSONArray jArr = new JSONArray(
                                    intent.getStringArrayExtra(PuzzleRefreshService.KEYS_INTENT_KEY+i));
                            String difficulty =
                                    intent.getStringExtra(PuzzleRefreshService.DIFFICULTIES_INTENT_KEY+i);
                            puzzles.add(new Puzzle(jArr, difficulty));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                replacePuzzles(puzzles);
            }
        };

        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(receiver, new IntentFilter(PuzzleRefreshService.PUZZLE_REFRESH_SERVICE));
    }

    /**
     * @return returns a list of all puzzles stored locally
     */
    public List<Puzzle> getAllPuzzles(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null, null, null, null, null, null);
        List<Puzzle> puzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        return puzzles;
    }

    /**
     * @return returns all easy puzzles
     */
    public List<Puzzle> getAllEasyPuzzles(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"easy"}, null, null, null);
        List<Puzzle> easyPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        return easyPuzzles;
    }

    /**
     * @return randomly returns one easy puzzle
     */
    public Puzzle getEasyPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"easy"}, null, null, null);
        List<Puzzle> easyPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        int randomIndex = (int) (Math.random()*(easyPuzzles.size()));
        return easyPuzzles.get(randomIndex);
    }

    /**
     * @return returns all medium puzzles
     */
    public List<Puzzle> getAllMediumPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"medium"}, null, null, null);
        List<Puzzle> mediumPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        return mediumPuzzles;
    }

    /**
     * @return randomly returns one medium puzzle
     */
    public Puzzle getMediumPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"medium"}, null, null, null);
        List<Puzzle> mediumPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        int randomIndex = (int) (Math.random()*(mediumPuzzles.size()));
        return mediumPuzzles.get(randomIndex);
    }

    /**
     * @return returns all hard puzzles
     */
    public List<Puzzle> getAllHardPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"hard"}, null, null, null);
        List<Puzzle> hardPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        return hardPuzzles;
    }

    /**
     * @return randomly returns one hard puzzle
     */
    public Puzzle getHardPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"hard"}, null, null, null);
        List<Puzzle> hardPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        int randomIndex = (int) (Math.random()*(hardPuzzles.size()));
        return hardPuzzles.get(randomIndex);
    }

    /**
     * @return returns all expert puzzles
     */
    public List<Puzzle> getALLExpertPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"expert"}, null, null, null);
        List<Puzzle> expertPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        return expertPuzzles;
    }

    /**
     * @return randomly returns one expert puzzle
     */
    public Puzzle getExpertPuzzle(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                PUZZLE_TABLE, null,
                "WHERE "+COL_DIFFICULTY+" = ?", new String[]{"expert"}, null, null, null);
        List<Puzzle> expertPuzzles = getPuzzlesOutOfCursor(cursor);
        cursor.close();
        int randomIndex = (int) (Math.random()*(expertPuzzles.size()));
        return expertPuzzles.get(randomIndex);
    }

    /**
     * @return returns the single row of stats
     */
    public Stats getStats(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                STATS_TABLE, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            Stats stats = new Stats(
                cursor.getInt(cursor.getColumnIndex(COL_HIGHSCORE)),
                cursor.getInt(cursor.getColumnIndex(COL_RACES_WON)),
                cursor.getInt(cursor.getColumnIndex(COL_RACES_LOST)),
                cursor.getString(cursor.getColumnIndex(COL_BEST_TIME)));
            cursor.close();
            return stats;
        }
        cursor.close();
        return null;
    }



    public void replacePuzzles(List<Puzzle> puzzles) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PUZZLE_TABLE, null, null);
        for (Puzzle puzzle: puzzles) {
            ContentValues values = new ContentValues();
            values.put(COL_DIFFICULTY, puzzle.getDifficulty());
            JSONArray keyArr = puzzle.getKey();
            values.put(COL_KEY, keyArr.toString());
            db.insert(PUZZLE_TABLE, null, values);
        }
        db.close();
    }

    public void updateHighScore(int highscore){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_HIGHSCORE, highscore);
        db.update(
                STATS_TABLE, values, null, null);
        db.close();
    }

    public void updateRacesWon(int racesWon){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RACES_WON, racesWon);
        db.update(
                STATS_TABLE, values, null, null);
        db.close();
    }

    public void updateRacesLost(int racesLost){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RACES_LOST, racesLost);
        db.update(
                STATS_TABLE, values, null, null);
        db.close();
    }

    public void updateBestTime(String bestTime){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BEST_TIME, bestTime);
        db.update(
                STATS_TABLE, values, null, null);
        db.close();
    }

    /**
     * A helper method to consolidate code in the above 'get' methods
     *
     * @param cursor
     * @return a list of puzzles with the data stored in the given cursor
     */

    public List<Puzzle> getPuzzlesOutOfCursor(Cursor cursor){
        List<Puzzle> puzzles = new ArrayList<>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                try {
                    JSONArray keyArr =
                            new JSONArray(cursor.getString(cursor.getColumnIndex(COL_KEY)));
                    puzzles.add(new Puzzle(
                            keyArr,
                            cursor.getString(cursor.getColumnIndex(COL_DIFFICULTY))
                    ));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        return puzzles;
    }
}
