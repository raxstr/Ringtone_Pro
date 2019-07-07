package com.ristana.ringtone_app.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ristana.ringtone_app.R;
import com.ristana.ringtone_app.entity.Ringtone;
import com.ristana.ringtone_app.entity.Slide;
import com.ristana.ringtone_app.manager.FavoritesStorage;
import com.ristana.ringtone_app.ui.RingtoneActivity;
import com.ristana.ringtone_app.ui.view.ClickableViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by hsn on 27/11/2017.
 */

public class RingtoneAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private  Runnable runnable;
    private boolean favorites = false;
    private List<Ringtone> ringtoneList;
    private Activity activity;
    private List<Slide> slideList= new ArrayList<>();
    private SlideAdapter slide_adapter;
    private Handler mainHandler;
    private RenderersFactory renderersFactory;
    private BandwidthMeter bandwidthMeter;
    private LoadControl loadControl;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private MediaSource mediaSource;
    private TrackSelection.Factory trackSelectionFactory;
    private SimpleExoPlayer player;
    private TrackSelector trackSelector;
    private Integer playeditem = -1;
   // private Timer mTimer;


    public RingtoneAdapter(
            List<Ringtone> ringtoneList,
            List<Slide> slideList,
            Activity activity,
            SimpleExoPlayer player,
            MediaSource mediaSource,
            TrackSelection.Factory trackSelectionFactory,
            DataSource.Factory dataSourceFactory,
            ExtractorsFactory extractorsFactory,
            Handler mainHandler,
            RenderersFactory renderersFactory,
            BandwidthMeter bandwidthMeter,
            LoadControl loadControl,
            TrackSelector trackSelector,
            Integer playeditem,
            Boolean favorites
    ) {
        this.ringtoneList=ringtoneList;
        this.activity=activity;
        this.slideList = slideList;
        this.favorites=favorites;

        this.renderersFactory = renderersFactory;
        this.bandwidthMeter =bandwidthMeter;
        this.trackSelectionFactory =trackSelectionFactory;
        this.trackSelector = trackSelector;
        this.loadControl = loadControl;
        this.player = player;

        this.dataSourceFactory = dataSourceFactory;
        this.extractorsFactory = extractorsFactory;
        this.mainHandler = mainHandler;
        this.playeditem = playeditem;
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v1 = inflater.inflate(R.layout.item_ringtone, parent, false);
                viewHolder = new RingtoneHolder(v1);
               break;
            }
            case 2: {
                View v2 = inflater.inflate(R.layout.item_slide, parent, false);
                viewHolder = new SlideHolder(v2);
                break;
            }
        }
        return viewHolder;
    }
    @Override
    public int getItemViewType(int position) {
        if (ringtoneList.get(position) == null){
            return  1;
        }else{
            return ringtoneList.get(position).getViewType();
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_parent, final int position) {

        switch (ringtoneList.get(position).getViewType()) {
            case 1:{

                final RingtoneHolder holder = (RingtoneHolder) holder_parent;

                final FavoritesStorage storageFavorites = new FavoritesStorage(activity.getApplicationContext());
                List<Ringtone> ringtones = storageFavorites.loadFavorites();
                Boolean exist = false;
                if (ringtones == null) {
                    ringtones = new ArrayList<>();
                }
                for (int i = 0; i < ringtones.size(); i++) {
                    if (ringtones.get(i).getId().equals(ringtoneList.get(position).getId())) {
                        exist = true;
                    }
                }
                if (exist == false) {
                    holder.like_button_fav_item_ringtone.setLiked(false);
                } else {
                    holder.like_button_fav_item_ringtone.setLiked(true);
                }
                holder.like_button_fav_item_ringtone.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        try {
                            List<Ringtone> favorites_list = storageFavorites.loadFavorites();
                            Boolean exist = false;
                            if (favorites_list == null) {
                                favorites_list = new ArrayList<>();
                            }

                            for (int i = 0; i < favorites_list.size(); i++) {
                                if (favorites_list.get(i).getId().equals(ringtoneList.get(position).getId())) {
                                    exist = true;
                                }
                            }

                            if (exist == false) {
                                ArrayList<Ringtone> audios = new ArrayList<Ringtone>();

                                for (int i = 0; i < favorites_list.size(); i++) {
                                    audios.add(favorites_list.get(i));
                                }
                                audios.add(ringtoneList.get(position));
                                storageFavorites.storeAudio(audios);
                                holder.like_button_fav_item_ringtone.setLiked(true);

                            } else {
                                ArrayList<Ringtone> new_favorites = new ArrayList<Ringtone>();
                                for (int i = 0; i < favorites_list.size(); i++) {
                                    if (!favorites_list.get(i).getId().equals(ringtoneList.get(position).getId())) {
                                        new_favorites.add(favorites_list.get(i));

                                    }
                                }
                                if (favorites) {

                                    ringtoneList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();

                                }
                                storageFavorites.storeAudio(new_favorites);
                                holder.like_button_fav_item_ringtone.setLiked(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            try {
                                ringtoneList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        try {
                            List<Ringtone> favorites_list = storageFavorites.loadFavorites();
                            Boolean exist = false;
                            if (favorites_list == null) {
                                favorites_list = new ArrayList<>();
                            }

                            for (int i = 0; i < favorites_list.size(); i++) {
                                if (favorites_list.get(i).getId().equals(ringtoneList.get(position).getId())) {
                                    exist = true;
                                }
                            }

                            if (exist == false) {
                                ArrayList<Ringtone> audios = new ArrayList<Ringtone>();

                                for (int i = 0; i < favorites_list.size(); i++) {
                                    audios.add(favorites_list.get(i));
                                }
                                audios.add(ringtoneList.get(position));
                                storageFavorites.storeAudio(audios);
                                holder.like_button_fav_item_ringtone.setLiked(true);

                            } else {
                                ArrayList<Ringtone> new_favorites = new ArrayList<Ringtone>();
                                for (int i = 0; i < favorites_list.size(); i++) {
                                    if (!favorites_list.get(i).getId().equals(ringtoneList.get(position).getId())) {
                                        new_favorites.add(favorites_list.get(i));

                                    }
                                }
                                if (favorites) {

                                    ringtoneList.get(position).setPlaying(false);
                                    player.stop();
                                    player.seekToDefaultPosition();
                                    mainHandler.removeCallbacksAndMessages(null);
                                    notifyDataSetChanged();
                                    notifyDataSetChanged();


                                    ringtoneList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();

                                }
                                storageFavorites.storeAudio(new_favorites);
                                holder.like_button_fav_item_ringtone.setLiked(false);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            try {
                                ringtoneList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }
                    }
                });
                int step = 1;
                int final_step = 1;
                for (int i = 1; i < position + 1; i++) {
                    if (i == position + 1) {
                        final_step = step;
                    }
                    step++;
                    if (step > 7) {
                        step = 1;
                    }
                }
                holder.progress_bar_item_ringtone_play.getIndeterminateDrawable().setColorFilter(activity.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                holder.text_view_item_ringtone_title.setText(ringtoneList.get(position).getTitle());
                holder.text_view_item_ringtone_author.setText(ringtoneList.get(position).getUser());
                holder.text_view_item_ringtone_downloads.setText(format(ringtoneList.get(position).getDownloads()));
                holder.text_view_item_ringtone_duration.setText(secToTime(ringtoneList.get(position).getDuration()));
                if (ringtoneList.get(position).getPreparing()) {
                    holder.progress_bar_item_ringtone_play.setVisibility(View.VISIBLE);
                    holder.image_view_item_ringtone_play.setVisibility(View.GONE);
                    holder.image_view_item_ringtone_pause.setVisibility(View.GONE);
                } else {
                    if (ringtoneList.get(position).getPlaying()) {
                        holder.progress_bar_item_ringtone_play.setVisibility(View.GONE);
                        holder.image_view_item_ringtone_play.setVisibility(View.GONE);
                        holder.image_view_item_ringtone_pause.setVisibility(View.VISIBLE);
                    } else {
                        Log.v("PAUSE", "YES");
                        holder.progress_bar_item_ringtone_play.setVisibility(View.GONE);
                        holder.image_view_item_ringtone_play.setVisibility(View.VISIBLE);
                        holder.image_view_item_ringtone_pause.setVisibility(View.GONE);
                    }
                }
                if (position == playeditem) {
                    if (ringtoneList.get(position).getPlaying()) {
                        Log.v("v", "I4MHERE");
                        Integer initial = (int) ((player.getCurrentPosition() * 1000) / player.getDuration());
                        Log.v("v", "I4MHERE" + initial + "-" + player.getCurrentPosition());
                        ProgressBarAnimation anim = new ProgressBarAnimation(holder.progress_bar_item_ringtone_background, initial, 1000);
                        anim.setDuration(player.getDuration() - player.getCurrentPosition());
                        holder.progress_bar_item_ringtone_background.startAnimation(anim);
                    } else {
                        holder.progress_bar_item_ringtone_background.setProgress(0);
                    }
                } else {
                    holder.progress_bar_item_ringtone_background.setProgress(0);
                }

                holder.image_view_item_ringtone_play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        for (int i = 0; i < ringtoneList.size(); i++) {
                            ringtoneList.get(i).setPlaying(false);
                            ringtoneList.get(i).setPreparing(false);
                        }
                        ringtoneList.get(position).setPreparing(true);
                        notifyDataSetChanged();
                        playeditem = position;
                        Log.v("url", ringtoneList.get(position).getRingtone());
                        mediaSource = new ExtractorMediaSource(Uri.parse(ringtoneList.get(position).getRingtone()),
                                dataSourceFactory,
                                extractorsFactory,
                                mainHandler,
                                null);
                        player.seekTo(0);
                        player.addListener(new Player.EventListener() {
                            @Override
                            public void onTimelineChanged(Timeline timeline, Object manifest) {
                                Log.v("v", "onTimelineChanged");
                            }

                            @Override
                            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                                Log.v("v", "onTracksChanged");

                            }

                            @Override
                            public void onLoadingChanged(boolean isLoading) {
                                Log.v("v", "onLoadingChanged");

                            }

                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                                if (playbackState == ExoPlayer.STATE_READY) {
                                    if (playeditem == position) {
                                        ringtoneList.get(position).setPlaying(true);
                                        ringtoneList.get(position).setPreparing(false);
                                        Log.v("getDuration", "=" + player.getDuration());
                                    } else {
                                        ringtoneList.get(position).setPlaying(false);
                                        ringtoneList.get(position).setPreparing(false);
                                    }
                                    notifyDataSetChanged();
                                }
                                if (playbackState == ExoPlayer.STATE_ENDED) {
                                    ringtoneList.get(position).setPlaying(false);
                                    player.stop();
                                    player.seekToDefaultPosition();
                                    mainHandler.removeCallbacksAndMessages(null);
                                    notifyDataSetChanged();
                                    notifyDataSetChanged();

                                }

                                Log.v("v", "onRepeatModeChanged" + playbackState + "-" + ExoPlayer.STATE_READY);


                            }

                            @Override
                            public void onRepeatModeChanged(int repeatMode) {
                                Log.v("v", "onRepeatModeChanged");

                            }

                            @Override
                            public void onPlayerError(ExoPlaybackException error) {
                                Log.v("v", "onPlayerError");

                            }

                            @Override
                            public void onPositionDiscontinuity() {
                                Log.v("v", "onPositionDiscontinuity");

                            }

                            @Override
                            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                                Log.v("v", "onPlaybackParametersChanged");

                            }

                        });
                        player.setPlayWhenReady(true);
                        player.prepare(mediaSource);

                    }
                });
                holder.image_view_item_ringtone_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ringtoneList.get(position).setPlaying(false);
                        player.stop();
                        player.seekToDefaultPosition();
                        mainHandler.removeCallbacksAndMessages(null);
                        notifyDataSetChanged();
                        notifyDataSetChanged();
                    }
                });
                holder.progress_bar_item_ringtone_background.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, RingtoneActivity.class);
                        intent.putExtra("id", ringtoneList.get(position).getId());
                        intent.putExtra("title", ringtoneList.get(position).getTitle());
                        intent.putExtra("userid", ringtoneList.get(position).getUserid());
                        intent.putExtra("size", ringtoneList.get(position).getSize());
                        intent.putExtra("user", ringtoneList.get(position).getUser());
                        intent.putExtra("userimage", ringtoneList.get(position).getUserimage());
                        intent.putExtra("type", ringtoneList.get(position).getType());
                        intent.putExtra("duration", ringtoneList.get(position).getDuration());
                        intent.putExtra("ringtone", ringtoneList.get(position).getRingtone());
                        intent.putExtra("extension", ringtoneList.get(position).getExtension());
                        intent.putExtra("downloads", ringtoneList.get(position).getDownloads());
                        intent.putExtra("created", ringtoneList.get(position).getCreated());
                        intent.putExtra("tags", ringtoneList.get(position).getTags());
                        intent.putExtra("description", ringtoneList.get(position).getDescription());
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                });
                switch (step) {
                    case 1:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_1));

                        break;
                    case 2:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_2));
                        break;
                    case 3:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_3));
                        break;
                    case 4:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_4));
                        break;
                    case 5:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_5));
                        break;
                    case 6:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_6));
                        break;
                    case 7:
                        holder.progress_bar_item_ringtone_background.setProgressDrawable(activity.getResources().getDrawable(R.drawable.bg_progress_item_7));
                        break;
                }
            }
            break;
            case 2: {
                final SlideHolder holder = (SlideHolder) holder_parent;

                slide_adapter = new SlideAdapter(activity, slideList);
                holder.view_pager_slide.setAdapter(this.slide_adapter);
                holder.view_pager_slide.setOffscreenPageLimit(1);

                holder.view_pager_slide.setClipToPadding(false);
                holder.view_pager_slide.setPageMargin(0);
                holder.view_pager_indicator.setupWithViewPager(holder.view_pager_slide);

                holder.view_pager_slide.setCurrentItem(slideList.size() / 2);
            }
            break;
        }
    }
    private class SlideHolder extends RecyclerView.ViewHolder {
        private final ViewPagerIndicator view_pager_indicator;
        private final ClickableViewPager view_pager_slide;
        public SlideHolder(View itemView) {
            super(itemView);
            this.view_pager_indicator=(ViewPagerIndicator) itemView.findViewById(R.id.view_pager_indicator);
            this.view_pager_slide=(ClickableViewPager) itemView.findViewById(R.id.view_pager_slide);
        }

    }
    public static class RingtoneHolder extends RecyclerView.ViewHolder {

        public final ProgressBar progress_bar_item_ringtone_play;
        public final ProgressBar progress_bar_item_ringtone_background;
        public final ImageView image_view_item_ringtone_play;
        public final ImageView image_view_item_ringtone_pause;
        private final TextView text_view_item_ringtone_author;
        private final TextView text_view_item_ringtone_downloads;
        private final TextView text_view_item_ringtone_duration;
        private final TextView text_view_item_ringtone_title;
        private final LikeButton like_button_fav_item_ringtone;

        public RingtoneHolder(View itemView) {
            super(itemView);
            progress_bar_item_ringtone_background=(ProgressBar) itemView.findViewById(R.id.progress_bar_item_ringtone_background);
            progress_bar_item_ringtone_play=(ProgressBar) itemView.findViewById(R.id.progress_bar_item_ringtone_play);
            image_view_item_ringtone_play=(ImageView) itemView.findViewById(R.id.image_view_item_ringtone_play);
            image_view_item_ringtone_pause=(ImageView) itemView.findViewById(R.id.image_view_item_ringtone_pause);
            text_view_item_ringtone_author=(TextView) itemView.findViewById(R.id.text_view_item_ringtone_author);
            text_view_item_ringtone_downloads=(TextView) itemView.findViewById(R.id.text_view_item_ringtone_downloads);
            text_view_item_ringtone_duration=(TextView) itemView.findViewById(R.id.text_view_item_ringtone_duration);
            text_view_item_ringtone_title=(TextView) itemView.findViewById(R.id.text_view_item_ringtone_title);
            like_button_fav_item_ringtone=(LikeButton) itemView.findViewById(R.id.like_button_fav_item_ringtone);

         }
    }
    @Override
    public int getItemCount() {
        return ringtoneList.size();
    }






    public class ProgressBarAnimation extends Animation{
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
             progressBar.setProgress((int) value);
        }

    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String secToTime(int sec) {
        int seconds = sec % 60;
        int minutes = sec / 60;

        if (minutes>0){
            return String.format("%01d m %02d s", minutes, seconds);
        }else{
            return String.format("%01d s", seconds);
        }
    }
}
