package com.example.android.popularmovies.utils;

import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the MovieDB servers.
 */
public final class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MOVIE_URL = "http://api.themoviedb.org/3/movie/";

    private final static String API_KEY_QUERY_PARAM = "api_key";
    private final static String APPEND_TO_RESPONSE_QUERY_PARAM = "append_to_response";
    private final static String VIDEOS_AND_REVIEWS = "videos,reviews";
    /**
     * Builds the URL used to talk to the movie server using a string. To get either the
     * most popular or the top rated ones.
     *
     * @param sortBy The location that will be queried for.
     * @return The URL to use to query the the movie server.
     */
    public static URL buildUrl(String sortBy) {
        String baseUrl = MOVIE_URL + sortBy;

        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_QUERY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Movies sort by " + url);

        return url;
    }

    /**
     * Builds the URL used to talk to the movie server using a string. To get the
     * videos and revies of a movie by its id.
     *
     * @param movieId The movie id that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildDetailsUrl(String movieId) {
        String baseUrl = MOVIE_URL + movieId;

        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_QUERY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .appendQueryParameter(APPEND_TO_RESPONSE_QUERY_PARAM, VIDEOS_AND_REVIEWS)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Videos and revies " + url);

        return url;
    }
    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
