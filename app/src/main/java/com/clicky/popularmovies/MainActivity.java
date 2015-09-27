package com.clicky.popularmovies;

import android.app.ActionBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.clicky.popularmovies.adapters.MoviesAdapter;
import com.clicky.popularmovies.adapters.SpinnerAdapter;
import com.clicky.popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String SCROLL_STATE = "scrolled";
    private static final String LIST_STATE = "listItems";
    private static final String SORT_STATE = "sort";

    private Toolbar toolbar;
    private RecyclerView list;
    private SwipeRefreshLayout swipeLayout;
    private GridLayoutManager mLayoutManager;
    private MoviesAdapter adapter;
    private String sortTypes[] = {"popularity.desc","vote_average.desc"};
    private ArrayList<Movie> mMovies;
    private int page = 1;
    private int sortSelected = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (RecyclerView)findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.setOnRefreshListener(this);

        setSupportActionBar(toolbar);

        mMovies = new ArrayList<>();

        adapter = new MoviesAdapter(mMovies);
        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_columns));
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case MoviesAdapter.ITEM_MOVIE:
                        return 1;
                    case MoviesAdapter.ITEM_LOAD:
                        return 2;
                    default:
                        return -1;
                }
            }
        });
        list.setHasFixedSize(true);
        list.addItemDecoration(new MoviesDecoration(getResources().getDimensionPixelSize(R.dimen.grid_spacing),
                getResources().getInteger(R.integer.grid_columns)));
        list.setLayoutManager(mLayoutManager);
        list.setAdapter(adapter);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = list.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {

                    page++;
                    getMovies(sortTypes[sortSelected]);
                    loading = true;
                }
            }
        });

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        spinner.setAdapter(new SpinnerAdapter(this));

        if (savedInstanceState != null) {
            sortSelected = savedInstanceState.getInt(SORT_STATE);
            mMovies = savedInstanceState.getParcelableArrayList(LIST_STATE);
            adapter.addList(mMovies);
            mLayoutManager.scrollToPosition(savedInstanceState.getInt(SCROLL_STATE));

            spinner.setSelection(sortSelected);
        }
        if(mMovies.size() == 0)
            getMovies(sortTypes[sortSelected]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != sortSelected) {
                    adapter.clearAll();
                    sortSelected = position;
                    page = 1;
                    getMovies(sortTypes[sortSelected]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelableArrayList(LIST_STATE, mMovies);
        savedInstanceState.putInt(SCROLL_STATE, mLayoutManager.findFirstVisibleItemPosition());
        savedInstanceState.putInt(SORT_STATE, sortSelected);
    }

    @Override
    public void onRefresh() {
        adapter.clearAll();
        page = 1;
        getMovies(sortTypes[sortSelected]);
    }

    private void getMovies(String sort){

        adapter.enableFooter(true);

        String MOVIE_URL =
                "http://api.themoviedb.org/3/discover/movie?page=" + page +"&sort_by="
                        + sort + "&api_key=" + getResources().getString(R.string.movieDBAPI);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                MOVIE_URL, (String)null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                final String JSON_RESULTS = "results";
                final String JSON_BACKGROUND = "backdrop_path";
                final String JSON_TITLE = "original_title";
                final String JSON_SYNOPSIS = "overview";
                final String JSON_DATE = "release_date";
                final String JSON_POSTER = "poster_path";
                final String JSON_RATE = "vote_average";

                try {
                    // Parsing json object response
                    // response will be a json object
                    JSONArray moviesArray = response.getJSONArray(JSON_RESULTS);

                    for(int i = 0; i < moviesArray.length(); i++) {
                        String title;
                        String synopsis;
                        String date;
                        String posterUrl;
                        String backgroundUrl;
                        double rate;

                        // Get the JSON object representing the day
                        JSONObject movieJSON = moviesArray.getJSONObject(i);

                        title = movieJSON.getString(JSON_TITLE);
                        synopsis = movieJSON.getString(JSON_SYNOPSIS);
                        date = movieJSON.getString(JSON_DATE);
                        posterUrl = movieJSON.getString(JSON_POSTER);
                        backgroundUrl = movieJSON.getString(JSON_BACKGROUND);
                        rate = movieJSON.getDouble(JSON_RATE);

                        Movie movie = new Movie(title, "http://image.tmdb.org/t/p/w780" + backgroundUrl, synopsis,
                                "http://image.tmdb.org/t/p/w185" + posterUrl, date, rate);

                        adapter.addItem(movie);
                    }


                    //txtResponse.setText(jsonResponse);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                adapter.enableFooter(false);
                swipeLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.enableFooter(false);
                swipeLayout.setRefreshing(false);
            }
        });

        MoviesApplication.getInstance().addToRequestQueue(jsonObjReq);
    }
}
