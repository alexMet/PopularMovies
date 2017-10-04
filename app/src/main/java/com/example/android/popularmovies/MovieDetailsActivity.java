package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract.*;
import com.example.android.popularmovies.utils.MovieJsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final int MOVIE_SEARCH_LOADER = 42;
    private ArrayList<String> mTrailersList;
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mMovie = null;
        if (savedInstanceState != null)
            mMovie = new Movie(savedInstanceState);
        else {
            Intent movieIntent = getIntent();
            if (movieIntent != null)
                mMovie = new Movie(movieIntent);
        }

        if (mMovie != null) {
            initializeViews();
            setFavorite();
        }
    }

    private void setFavorite() {
        Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                null,
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[] {mMovie.getMovieId()},
                null,
                null);

        if (cursor != null && cursor.moveToNext()) {
            mMovie.setRowId(cursor.getLong(cursor.getColumnIndex(MovieEntry._ID)));
            ((Button) findViewById(R.id.bt_favorite)).setBackgroundResource(android.R.drawable.star_big_on);
            cursor.close();
        }
    }

    private void initializeViews() {
        TextView movieTitle = (TextView) findViewById(R.id.tv_movie_title);
        TextView movieDate = (TextView) findViewById(R.id.tv_movie_date);
        TextView movieRating = (TextView) findViewById(R.id.tv_movie_rating);
        TextView moviePlot = (TextView) findViewById(R.id.tv_movie_plot);
        ImageView moviePoster = (ImageView) findViewById(R.id.iv_movie_poster);

        movieTitle.setText(mMovie.getOriginal_title());
        movieDate.setText(getMovieYear(mMovie.getRelease_date()));
        String ratingSlashTen = mMovie.getRating() + "/10";
        movieRating.setText(ratingSlashTen);
        moviePlot.setText(mMovie.getPlot());
        Picasso.with(this).load("http://image.tmdb.org/t/p/w185/" + mMovie.getImage()).into(moviePoster);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> movieSearchLoader = loaderManager.getLoader(MOVIE_SEARCH_LOADER);

        if (movieSearchLoader == null)
            loaderManager.initLoader(MOVIE_SEARCH_LOADER, null, this);
        else
            loaderManager.restartLoader(MOVIE_SEARCH_LOADER, null, this);
    }

    private String getMovieYear(String release_date) {
        String[] year = release_date.split("-");
        return (year.length > 0) ? year[0] : release_date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share_trailer) {
            if (mTrailersList != null && mTrailersList.size() > 0)
                ShareCompat.IntentBuilder
                    .from(this)
                    .setType("text/plain")
                    .setChooserTitle(getString(R.string.share_trailer_title))
                    .setText(mTrailersList.get(0))
                    .startChooser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void markAsFavorite(View view) {
        if (mMovie.isFavorite()) {
            // delete the movie from favorites
            String stringId = Long.toString(mMovie.getRowId());
            Uri uri = MovieEntry.CONTENT_URI.buildUpon().appendPath(stringId).build();
            getContentResolver().delete(uri, null, null);

            // after delete set favorite button to off
            mMovie.setRowId(-1);
            view.setBackgroundResource(android.R.drawable.star_big_off);
        }
        else {
            // insert the new movie to favorites
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieEntry.COLUMN_MOVIE_ID, mMovie.getMovieId());
            contentValues.put(MovieEntry.COLUMN_TITLE, mMovie.getOriginal_title());
            contentValues.put(MovieEntry.COLUMN_IMAGE, mMovie.getImage());
            contentValues.put(MovieEntry.COLUMN_PLOT, mMovie.getPlot());
            contentValues.put(MovieEntry.COLUMN_RATING, mMovie.getRating());
            contentValues.put(MovieEntry.COLUMN_DATE, mMovie.getRelease_date());
            Uri uri = getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);

            // after successful insert, set favorite button to on
            if (uri != null) {
                mMovie.setRowId(Long.valueOf(uri.getPathSegments().get(1)));
                view.setBackgroundResource(android.R.drawable.star_big_on);
            }
        }
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            String mMovieDetailsJson;

            @Override
            protected void onStartLoading() {
                if (mMovieDetailsJson != null)
                    deliverResult(mMovieDetailsJson);
                else
                    forceLoad();
            }

            @Override
            public String loadInBackground() {
                try {
                    URL movieRequestUrl = NetworkUtils.buildDetailsUrl(mMovie.getMovieId());
                    return NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String movieDetailsJson) {
                mMovieDetailsJson = movieDetailsJson;
                super.deliverResult(movieDetailsJson);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        try {
            // Get trailers from JSON response an show them accordingly
            mTrailersList = MovieJsonUtils.getTrailerStringsFromJson(data);

            if (mTrailersList.size() == 1) {
                ((TextView) findViewById(R.id.tv_trailers_error)).setVisibility(View.GONE);
                ((Button) findViewById(R.id.bt_play_trailer1)).setVisibility(View.VISIBLE);
            }
            else if (mTrailersList.size() > 1) {
                ((TextView) findViewById(R.id.tv_trailers_error)).setVisibility(View.GONE);
                ((Button) findViewById(R.id.bt_play_trailer1)).setVisibility(View.VISIBLE);
                ((View) findViewById(R.id.line2)).setVisibility(View.VISIBLE);
                ((Button) findViewById(R.id.bt_play_trailer2)).setVisibility(View.VISIBLE);
            }

            // Get reviews from JSON response an show them if they aren't empty
            String reviews = MovieJsonUtils.getReviewStringsFromJson(data);
            TextView reviews_tv = (TextView) findViewById(R.id.tv_reviews);

            if (reviews.isEmpty())
                reviews_tv.setText(getString(R.string.no_reviews));
            else
                reviews_tv.setText(reviews);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        ((TextView) findViewById(R.id.tv_trailers_error)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.bt_play_trailer1)).setVisibility(View.GONE);
        ((View) findViewById(R.id.line2)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.bt_play_trailer2)).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.tv_reviews)).setText(getString(R.string.no_reviews));
    }

    public void showTrailerOne(View view) {
        Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mTrailersList.get(0)));
        startActivity(Intent.createChooser(trailerIntent, getString(R.string.open_with)));
    }

    public void showTrailerTwo(View view) {
        Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mTrailersList.get(1)));
        if (trailerIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(trailerIntent);
        }
        //startActivity(Intent.createChooser(trailerIntent, getString(R.string.open_with)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMovie.packageBundle(outState);
    }
}