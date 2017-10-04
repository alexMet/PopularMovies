package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract.*;
import com.example.android.popularmovies.utils.MovieJsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<ArrayList<Movie>> {
    private final String SORT_BY_FAVORITES = "favorites";
    private final String SORT_BY_POPULAR = "popular";
    private final String SORT_BY_TOP_RATED = "top_rated";
    private final String SORT_CRITERIA = "criteria";
    private String mSortBy;

    private static final int MOVIES_SEARCH_LOADER = 4242;

    private MovieAdapter mMovieAdapter;
    private GridView mGridView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private Button mTryAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Movie> lst = new ArrayList<>();

        mMovieAdapter = new MovieAdapter(MainActivity.this, this, lst);
        mGridView = (GridView) findViewById(R.id.grid_view);
        mGridView.setAdapter(mMovieAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mTryAgainButton = (Button) findViewById(R.id.button_try_again);

        mSortBy = SORT_BY_POPULAR;
        if (savedInstanceState != null)
            if (savedInstanceState.containsKey(SORT_CRITERIA))
                mSortBy = savedInstanceState.getString(SORT_CRITERIA);

        loadMovieData(mSortBy);
    }

    /**
     * This method will get the user's sorting choise, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData(String sortBy) {
        if (isOnline() || sortBy.equals(SORT_BY_FAVORITES)) {
            showMovieDataView();

            Bundle queryBundle = new Bundle();
            queryBundle.putString(SORT_CRITERIA, sortBy);

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<Movie> moviesSearchLoader = loaderManager.getLoader(MOVIES_SEARCH_LOADER);

            if (moviesSearchLoader == null)
                loaderManager.initLoader(MOVIES_SEARCH_LOADER, queryBundle, this);
            else
                loaderManager.restartLoader(MOVIES_SEARCH_LOADER, queryBundle, this);
        } else
            showErrorMessage();
    }

    /**
     * This method is overridden by our MainActivity class in order to handle ArrayAdapter item
     * clicks.
     *
     * @param selectedMovie The details for the movie that was clicked
     */
    @Override
    public void onClick(Movie selectedMovie) {
        Intent intentToStartDetailActivity = new Intent(this, MovieDetailsActivity.class);
        selectedMovie.packageIntent(intentToStartDetailActivity);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     */
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mTryAgainButton.setVisibility(View.INVISIBLE);
        mGridView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movies view.
     */
    private void showErrorMessage() {
        mGridView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setText(R.string.loading_error);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mTryAgainButton.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the no favorites error message visible and hide the movies view.
     */
    private void showNoFavoritesError() {
        mGridView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setText(R.string.no_favorites);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mTryAgainButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<Movie>>(this) {
            ArrayList<Movie> mMovieDetailsJson;
            String mLoaderSortBy;

            @Override
            protected void onStartLoading() {
                mLoadingIndicator.setVisibility(View.VISIBLE);
                String sortBy = args.getString(SORT_CRITERIA);

                if (mMovieDetailsJson != null && mLoaderSortBy.equals(sortBy))
                    deliverResult(mMovieDetailsJson);
                else {
                    mLoaderSortBy = sortBy;
                    forceLoad();
                }
            }

            @Override
            public ArrayList<Movie> loadInBackground() {
                if (mLoaderSortBy.equals(SORT_BY_FAVORITES)) {
                    Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null, null);
                    ArrayList<Movie> movieArrayList = null;

                    if (cursor != null) {
                        if (cursor.moveToNext()) {
                            movieArrayList = new ArrayList<Movie>();

                            do {
                                Movie favMovie = new Movie(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)),
                                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_IMAGE)),
                                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_PLOT)),
                                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RATING)),
                                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DATE)),
                                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID)));

                                movieArrayList.add(favMovie);
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }

                    return movieArrayList;
                }
                else {
                    URL movieRequestUrl = NetworkUtils.buildUrl(mLoaderSortBy);

                    try {
                        String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                        return MovieJsonUtils.getMovieStringsFromJson(jsonMovieResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

            @Override
            public void deliverResult(ArrayList<Movie> movieDetailsJson) {
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mMovieDetailsJson = movieDetailsJson;
                super.deliverResult(movieDetailsJson);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movieData) {
        if (movieData != null) {
            showMovieDataView();
            mMovieAdapter.setMovieData(movieData);
        } else {
            if (mSortBy.equals(SORT_BY_FAVORITES))
                showNoFavoritesError();
            else
                showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
        mMovieAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_popular) {
            mMovieAdapter.clear();
            mSortBy = SORT_BY_POPULAR;
            loadMovieData(mSortBy);
            return true;
        }

        if (id == R.id.action_top_rated) {
            mMovieAdapter.clear();
            mSortBy = SORT_BY_TOP_RATED;
            loadMovieData(mSortBy);
            return true;
        }

        if (id == R.id.action_favorite) {
            mMovieAdapter.clear();
            mSortBy = SORT_BY_FAVORITES;
            loadMovieData(mSortBy);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onTryAgain(View view) {
        loadMovieData(mSortBy);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SORT_CRITERIA, mSortBy);
    }
}