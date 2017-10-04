package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.popularmovies.data.MovieContract.*;

public class MovieContentProvider extends ContentProvider {
    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    // CDeclare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Member variable for a MovieDbHelper that's initialized in the onCreate() method
    private MovieDbHelper mMoveDbHelper;

    // Define a static buildUriMatcher method that associates URI's with their int match
    /**
     Initialize a new matcher object without any matches,
     then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the movie directory and a single item by ID.
         */
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        mMoveDbHelper = new MovieDbHelper(getContext());
        return true;
    }


    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the movie database (to write new data to)
        final SQLiteDatabase db = mMoveDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the movies directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case MOVIES:
                // Insert new values into the database
                // Inserting values into movies table
                long row_id = db.insert(MovieEntry.TABLE_NAME, null, values);

                if (row_id > 0)
                    returnUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, row_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }


    // Implement query to handle requests for data by URI
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mMoveDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the movies directory and write a default case
        switch (match) {
            // Query for the movies directory
            case MOVIES:
                retCursor =  db.query(MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }


    // Implement delete to delete a single row of data
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mMoveDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted movies
        int moviesDeleted; // starts as 0

        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case MOVIE_WITH_ID:
                // Get the movie row ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                moviesDeleted = db.delete(MovieEntry.TABLE_NAME, "_id = ?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (moviesDeleted != 0)
            // A movie was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of movies deleted
        return moviesDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
