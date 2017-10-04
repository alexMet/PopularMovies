package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;

public class Movie {
    public final static String TITLE = "original_title";
    public final static String IMAGE = "poster_path";
    public final static String PLOT = "overview";
    public final static String RATING = "vote_average";
    public final static String DATE = "release_date";
    public final static String MOVIE_ID = "id";

    private String original_title;
    private String image;
    private String plot;
    private String rating;
    private String release_date;
    private String movie_id;
    private long row_id = -1;

    public Movie(String original_title, String image, String plot, String rating, String release_date, String movie_id) {
        this.original_title = original_title;
        this.image = image;
        this.plot = plot;
        this.rating = rating;
        this.release_date = release_date;
        this.movie_id = movie_id;
    }

    public Movie(Intent intent) {
        if (intent.hasExtra(Movie.TITLE))
            this.original_title = intent.getStringExtra(Movie.TITLE);
        if (intent.hasExtra(Movie.IMAGE))
            this.image = intent.getStringExtra(Movie.IMAGE);
        if (intent.hasExtra(Movie.PLOT))
            this.plot = intent.getStringExtra(Movie.PLOT);
        if (intent.hasExtra(Movie.RATING))
            this.rating = intent.getStringExtra(Movie.RATING);
        if (intent.hasExtra(Movie.DATE))
            this.release_date = intent.getStringExtra(Movie.DATE);
        if (intent.hasExtra(Movie.MOVIE_ID))
            this.movie_id = intent.getStringExtra(Movie.MOVIE_ID);
    }

    public Movie(Bundle bundle) {
        if (bundle.containsKey(Movie.TITLE))
            this.original_title = bundle.getString(Movie.TITLE);
        if (bundle.containsKey(Movie.IMAGE))
            this.image = bundle.getString(Movie.IMAGE);
        if (bundle.containsKey(Movie.PLOT))
            this.plot = bundle.getString(Movie.PLOT);
        if (bundle.containsKey(Movie.RATING))
            this.rating = bundle.getString(Movie.RATING);
        if (bundle.containsKey(Movie.DATE))
            this.release_date = bundle.getString(Movie.DATE);
        if (bundle.containsKey(Movie.MOVIE_ID))
            this.movie_id = bundle.getString(Movie.MOVIE_ID);
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getImage() {
        return image;
    }

    public String getPlot() {
        return plot;
    }

    public String getRating() {
        return rating;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getMovieId() {
        return movie_id;
    }

    public long getRowId() { return row_id; }

    public void setRowId(long row_id) { this.row_id = row_id; }

    public boolean isFavorite() { return row_id != -1; }

    public void packageIntent(Intent intent) {
        intent.putExtra(Movie.TITLE, this.original_title);
        intent.putExtra(Movie.IMAGE, this.image);
        intent.putExtra(Movie.PLOT, this.plot);
        intent.putExtra(Movie.RATING, this.rating);
        intent.putExtra(Movie.DATE, this.release_date);
        intent.putExtra(Movie.MOVIE_ID, this.movie_id);
    }

    public void packageBundle(Bundle bundle) {
        bundle.putString(Movie.TITLE, this.original_title);
        bundle.putString(Movie.IMAGE, this.image);
        bundle.putString(Movie.PLOT, this.plot);
        bundle.putString(Movie.RATING, this.rating);
        bundle.putString(Movie.DATE, this.release_date);
        bundle.putString(Movie.MOVIE_ID, this.movie_id);
    }
}
