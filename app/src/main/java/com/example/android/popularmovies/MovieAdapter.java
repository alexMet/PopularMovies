package com.example.android.popularmovies;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieAdapter extends ArrayAdapter<Movie> {
    private final MovieAdapterOnClickHandler mClickHandler;
    public ArrayList<Movie> mMovies;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context       The current context. Used to inflate the layout file.
     * @param clickHandler  In order to pass the movie item to the details activity.
     */
    public MovieAdapter(Activity context, MovieAdapterOnClickHandler clickHandler, ArrayList<Movie> lst) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, lst);
        mClickHandler = clickHandler;
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movieClicked);
    }

    private static class ViewHolderItem {
        ImageView imageView;
    }
    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Gets the Movie object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);
        ViewHolderItem viewHolder;

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_item, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_movie_poster);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolderItem) convertView.getTag();


        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        double dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        viewHolder.imageView.getLayoutParams().height = (int) dpHeight;
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185/" + movie.getImage()).into(viewHolder.imageView);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Movie selectedMovie = mMovies.get(position);
                mClickHandler.onClick(selectedMovie);
            }
        });

        return convertView;
    }

    /**
     * This method is used to set the movie posters on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param movies The new movie data to be displayed.
     */
    public void setMovieData(ArrayList<Movie> movies) {
        mMovies = movies;
        addAll(movies);
        notifyDataSetChanged();
    }
}