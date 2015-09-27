package com.clicky.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clicky.popularmovies.DetailsActivity;
import com.clicky.popularmovies.R;
import com.clicky.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by fabianrodriguez on 9/25/15.
 *
 */
public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_MOVIE = 1;
    public static final int ITEM_LOAD = 0;

    private List<Movie> mObjects;
    private boolean isFooterEnabled = true;

    public MoviesAdapter(List<Movie> objects){
        mObjects = objects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View mView;
        public TextView mTtitle;
        public ImageView mPoster;

        public ViewHolder(final View view) {
            super(view);

            mView = view;
            mPoster = (ImageView)view.findViewById(R.id.img_poster);
            mTtitle = (TextView)view.findViewById(R.id.label_title);

        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if(viewType == ITEM_MOVIE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movie_card, parent, false);

            vh = new ViewHolder(v);
        }else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder mHolder, final int pos) {
        if(mHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder)mHolder;
            final Movie movie = mObjects.get(pos);

            Uri uri = Uri.parse(movie.getPosterUrl());
            final Context mContext = holder.mPoster.getContext();

            holder.mTtitle.setText(movie.getTitle());

            Picasso.with(mContext)
                    .load(uri)
                    .into(holder.mPoster);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailsActivity.class);
                    intent.putExtra(DetailsActivity.EXTRA_MOVIE, movie);

                    mContext.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return  (isFooterEnabled) ? mObjects.size() + 1 : mObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (isFooterEnabled && position >= mObjects.size() ) ? ITEM_LOAD : ITEM_MOVIE;
    }

    /**
     * Enable or disable footer (Default is true)
     *
     * @param isEnabled boolean to turn on or off footer.
     */
    public void enableFooter(boolean isEnabled){
        this.isFooterEnabled = isEnabled;
    }

    public void addItem(Movie item){
        mObjects.add(item);
        notifyItemInserted(mObjects.size());
    }

    public void clearAll(){
        mObjects.clear();
        notifyDataSetChanged();
    }

    public void addList(ArrayList<Movie> list){
        mObjects = list;
        notifyDataSetChanged();
    }

}
