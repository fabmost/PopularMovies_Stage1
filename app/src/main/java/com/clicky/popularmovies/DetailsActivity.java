package com.clicky.popularmovies;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.clicky.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

/**
 *
 * Created by fabianrodriguez on 9/25/15.
 *
 */
public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "movie";
    private Toolbar toolbar;

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView img_back = (ImageView)findViewById(R.id.backdrop);
        ImageView imgPoster = (ImageView)findViewById(R.id.img_poster);
        TextView labelTitle = (TextView)findViewById(R.id.label_title);
        TextView labelRelease = (TextView)findViewById(R.id.label_release);
        TextView labelRate = (TextView)findViewById(R.id.label_rate);
        TextView labelSynopsis = (TextView)findViewById(R.id.label_synopsis);

        setSupportActionBar(toolbar);
        setTitle("");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovie = getIntent().getExtras().getParcelable(EXTRA_MOVIE);

        if(mMovie != null) {

            labelTitle.setText(mMovie.getTitle());
            labelRelease.setText(mMovie.getReleaseDate());
            labelRate.setText(String.valueOf(mMovie.getRating()));

            if(mMovie.getSynopsis() != null && !mMovie.getSynopsis().equals("null"))
                labelSynopsis.setText(mMovie.getSynopsis());
            else
                labelSynopsis.setText(R.string.no_synopsis);

            Picasso.with(this)
                    .load(Uri.parse(mMovie.getBackgroundUrl()))
                    .into(img_back);

            Picasso.with(this)
                    .load(Uri.parse(mMovie.getPosterUrl()))
                    .into(imgPoster);
        }else{
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
