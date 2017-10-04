package com.example.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class MovieContract {
    public static final String AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "movie_title";
        public static final String COLUMN_IMAGE = "movie_poster";
        public static final String COLUMN_PLOT = "movie_overview";
        public static final String COLUMN_RATING = "movie_vote_average";
        public static final String COLUMN_DATE = "movie_release_date";
    }
}
