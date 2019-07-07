package com.ristana.ringtone_app.ui;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ristana.ringtone_app.R;
import com.ristana.ringtone_app.adapter.RingtoneAdapter;
import com.ristana.ringtone_app.api.apiClient;
import com.ristana.ringtone_app.api.apiRest;
import com.ristana.ringtone_app.entity.Ringtone;
import com.ristana.ringtone_app.entity.Slide;
import com.ristana.ringtone_app.manager.PrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {

    private Integer page = 0;
    private Boolean loaded=false;

    private RelativeLayout relative_layout_search_activity;
    private SwipeRefreshLayout swipe_refreshl_search_activity;
    private ImageView image_view_empty;
    private RecyclerView recycle_view_search_activity;
    private RelativeLayout relative_layout_load_more;
    private LinearLayout linear_layout_page_error;
    private Button button_try_again;
    private GridLayoutManager gridLayoutManager;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private RingtoneAdapter ringtoneAdapter;
    private List<Ringtone> ringtoneList =new ArrayList<>();
    private List<Slide> slideList=new ArrayList<>();

    private String query;


    private Integer playeditem = -1;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;
    private TrackSelection.Factory trackSelectionFactory;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private Handler mainHandler;
    private RenderersFactory renderersFactory;
    private BandwidthMeter bandwidthMeter;
    private LoadControl loadControl;
    private TrackSelector trackSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        renderersFactory = new DefaultRenderersFactory(this);
        bandwidthMeter = new DefaultBandwidthMeter();
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);

        dataSourceFactory = new DefaultDataSourceFactory(this, "ExoplayerDemo");
        extractorsFactory = new DefaultExtractorsFactory();
        mainHandler = new Handler();




        setContentView(R.layout.activity_search);
        showAdsBanner();
        Bundle bundle = getIntent().getExtras() ;
        this.query =  bundle.getString("query");
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(query);

        initView();
        initAction();
        loadWallpapers();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        this.relative_layout_search_activity=(RelativeLayout) findViewById(R.id.relative_layout_search_activity);
        this.swipe_refreshl_search_activity=(SwipeRefreshLayout) findViewById(R.id.swipe_refreshl_search_activity);
        this.image_view_empty=(ImageView) findViewById(R.id.image_view_empty);
        this.recycle_view_search_activity=(RecyclerView) findViewById(R.id.recycle_view_search_activity);
        this.relative_layout_load_more=(RelativeLayout) findViewById(R.id.relative_layout_load_more);
        this.linear_layout_page_error=(LinearLayout) findViewById(R.id.linear_layout_page_error);
        this.button_try_again=(Button) findViewById(R.id.button_try_again);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            this.gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, GridLayoutManager.VERTICAL, false);
        }else{
            this.gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.VERTICAL, false);
        }
        this.ringtoneAdapter =new RingtoneAdapter(ringtoneList,slideList,this,player,mediaSource,trackSelectionFactory,dataSourceFactory,extractorsFactory,mainHandler,renderersFactory,bandwidthMeter,loadControl,trackSelector,playeditem,false);
        recycle_view_search_activity.setHasFixedSize(true);
        recycle_view_search_activity.setAdapter(ringtoneAdapter);
        recycle_view_search_activity.setLayoutManager(gridLayoutManager);
    }

    private void initAction() {
        recycle_view_search_activity.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {

                    visibleItemCount    = gridLayoutManager.getChildCount();
                    totalItemCount      = gridLayoutManager.getItemCount();
                    pastVisiblesItems   = gridLayoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            loadNextWallpapers();
                        }
                    }
                }else{

                }
            }
        });
        this.swipe_refreshl_search_activity.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 0;
                loading = true;
                slideList.clear();
                ringtoneList.clear();
                loadWallpapers();
            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page = 0;
                loading = true;
                slideList.clear();
                ringtoneList.clear();
                loadWallpapers();
            }
        });
    }

    private void loadNextWallpapers() {
        relative_layout_load_more.setVisibility(View.VISIBLE);
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Ringtone>> call = service.ringtoneBysearch(page,query);
        call.enqueue(new Callback<List<Ringtone>>() {
            @Override
            public void onResponse(Call<List<Ringtone>> call, Response<List<Ringtone>> response) {
                if(response.isSuccessful()){
                    if (response.body().size()!=0){

                        for (int i=0;i<response.body().size();i++){
                            ringtoneList.add(response.body().get(i));
                        }
                        ringtoneAdapter.notifyDataSetChanged();
                        page++;
                        loading=true;
                    }
                }
                relative_layout_load_more.setVisibility(View.GONE);

            }
            @Override
            public void onFailure(Call<List<Ringtone>> call, Throwable t) {
                relative_layout_load_more.setVisibility(View.GONE);
            }
        });
    }

    private void loadWallpapers() {
        recycle_view_search_activity.setVisibility(View.GONE);
        image_view_empty.setVisibility(View.GONE);
        linear_layout_page_error.setVisibility(View.GONE);
        swipe_refreshl_search_activity.setRefreshing(true);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Ringtone>> call = service.ringtoneBysearch(page,query);
        call.enqueue(new Callback<List<Ringtone>>() {
            @Override
            public void onResponse(Call<List<Ringtone>> call, Response<List<Ringtone>> response) {
                apiClient.FormatData(SearchActivity.this,response);
                if(response.isSuccessful()){
                    if (response.body().size()!=0){

                        for (int i=0;i<response.body().size();i++){
                            ringtoneList.add(response.body().get(i));
                        }
                        ringtoneAdapter.notifyDataSetChanged();
                        page++;
                        loaded=true;

                        recycle_view_search_activity.setVisibility(View.VISIBLE);
                        image_view_empty.setVisibility(View.GONE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }else {
                        recycle_view_search_activity.setVisibility(View.GONE);
                        image_view_empty.setVisibility(View.VISIBLE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }
                }else{
                    recycle_view_search_activity.setVisibility(View.GONE);
                    image_view_empty.setVisibility(View.GONE);
                    linear_layout_page_error.setVisibility(View.VISIBLE);
                }
                swipe_refreshl_search_activity.setRefreshing(false);

            }
            @Override
            public void onFailure(Call<List<Ringtone>> call, Throwable t) {
                recycle_view_search_activity.setVisibility(View.GONE);
                image_view_empty.setVisibility(View.GONE);
                linear_layout_page_error.setVisibility(View.VISIBLE);
                swipe_refreshl_search_activity.setRefreshing(false);

            }
        });
    }
    private void showAdsBanner() {

        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (getString(R.string.AD_MOB_ENABLED_BANNER).equals("true")) {
            if (prefManager.getString("SUBSCRIBED").equals("FALSE")) {
                final AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                mAdView.loadAd(adRequest);

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mAdView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
