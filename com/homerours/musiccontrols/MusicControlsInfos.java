package com.homerours.musiccontrols;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicControlsInfos {
    public String artist;
    public String cover;
    public boolean dismissable;
    public boolean hasClose;
    public boolean hasNext;
    public boolean hasPrev;
    public boolean isPlaying;
    public String ticker;
    public String track;

    public MusicControlsInfos(JSONArray args) throws JSONException {
        JSONObject params = args.getJSONObject(0);
        this.track = params.getString("track");
        this.artist = params.getString("artist");
        this.ticker = params.getString("ticker");
        this.cover = params.getString("cover");
        this.isPlaying = params.getBoolean("isPlaying");
        this.hasPrev = params.getBoolean("hasPrev");
        this.hasNext = params.getBoolean("hasNext");
        this.hasClose = params.getBoolean("hasClose");
        this.dismissable = params.getBoolean("dismissable");
    }
}
