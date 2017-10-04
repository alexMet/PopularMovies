package com.example.android.popularmovies.utils;

import android.util.Log;

import com.example.android.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions to handle MovieDB JSON data about movies.
 */
public final class MovieJsonUtils {

    public static ArrayList<Movie> getMovieStringsFromJson(String movieJsonStr) throws JSONException {
        final String MOVIE_LIST = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);

        ArrayList<Movie> parsedMovieData = new ArrayList<>();

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);

            String title = movie.getString(Movie.TITLE);
            String image = movie.getString(Movie.IMAGE);
            String plot = movie.getString(Movie.PLOT);
            String rating = String.valueOf(movie.getDouble(Movie.RATING));
            String date = movie.getString(Movie.DATE);
            String movieId = movie.getString(Movie.MOVIE_ID);

            parsedMovieData.add(new Movie(title, image, plot, rating, date, movieId));
        }

        return parsedMovieData;
    }

    //  "author":"anythingbutfifi","content":"..."
    public static String getReviewStringsFromJson(String reviewJsonStr) throws JSONException {
        final String REVIEWS_OBJ = "reviews";
        final String RESULTS_LIST = "results";
        final String AUTHOR = "author";
        final String CONTENT = "content";

        JSONObject reviewJson = new JSONObject(reviewJsonStr);;
        JSONArray reviewArray = reviewJson.getJSONObject(REVIEWS_OBJ).getJSONArray(RESULTS_LIST);

        String parsedReviewData = "";

        for (int i = 0; i < reviewArray.length(); i++) {
            JSONObject movie = reviewArray.getJSONObject(i);

            String author = movie.getString(AUTHOR);
            String content = movie.getString(CONTENT);

            parsedReviewData += author + "\n\n" + content + "\n\n\n";
        }

        return parsedReviewData;
    }

    // "key":"guztEQ7DkaE","name":"Official Trailer #2","site":"YouTube","size":1080
    // https://www.youtube.com/watch?v=SUXWAEX2jlg
    public static ArrayList<String> getTrailerStringsFromJson(String trailerJsonStr) throws JSONException {
        final String TRAILERS_OBJ = "videos";
        final String RESULTS_LIST = "results";
        final String TRAILER_KEY = "key";
        final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=";

        JSONObject trailerJson = new JSONObject(trailerJsonStr);
        JSONArray trailerArray = trailerJson.getJSONObject(TRAILERS_OBJ).getJSONArray(RESULTS_LIST);

        ArrayList<String> parsedTrailerData = new ArrayList<>();

        for (int i = 0; i < trailerArray.length(); i++) {
            JSONObject movie = trailerArray.getJSONObject(i);

            String key = movie.getString(TRAILER_KEY);

            parsedTrailerData.add(YOUTUBE_LINK + key);
        }

        return parsedTrailerData;
    }
}