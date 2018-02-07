package com.turboturnip.turnipmusic;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MusicFilter {
	public static final String TAG = LogHelper.makeLogTag(MusicFilter.class);

	public static final String TYPE_VALUE_SEPARATOR = "/";
	public static final String FILTER_END = ";";

	public static final String FILTER_EMPTY = "__EMPTY__";
	public static final String FILTER_ROOT = "__ROOT__";
	public static final String FILTER_BY_SEARCH = "__SEARCH__";

	public static final String EXPLORE_FILTER = "__EXPLORE__";
	public static final String FILTER_BY_ALBUM = "__ALBUM__";
	public static final String FILTER_BY_ARTIST = "__ARTIST__";
	public static final String FILTER_BY_TAG = "__TAG__";
	public static final String FILTER_BY_GENRE = "__GENRE__";

	public List<SubFilter> subFilters;
	private boolean isEmpty, isRoot, isValid;

	public static class SubFilter {
		public String filterType, filterValue;

		public SubFilter() {
			filterType = FILTER_EMPTY;
			filterValue = "";
		}

		public SubFilter(String _filterType){
			filterType = EXPLORE_FILTER;
			filterValue = _filterType;
		}
		public SubFilter(String _filterType, String _filterValue) {
			filterType = _filterType;
			filterValue = _filterValue;
		}

		// This returns a weight to use when filtering songs. -ve is doesn't fit, 0 is best, more +ve are worse
		public int songStrength(Song s){
			if (filterType == null) return -1;
			if (filterType.equals(FILTER_BY_SEARCH)){
				int searchStrength;
				MediaMetadataCompat metadata = s.getMetadata();
				String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
				String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
				int titleStrength = title.indexOf(filterValue);
				int albumStrength = album.indexOf(filterValue);

				if (titleStrength == -1 && albumStrength == -1)
					searchStrength = -1;
				else
					searchStrength = ((titleStrength < 0) ? 0 : titleStrength) + ((albumStrength < 0) ? 0 : albumStrength);
				return searchStrength;
			}else if (filterType.equals(FILTER_BY_ALBUM)){
				MediaMetadataCompat metadata = s.getMetadata();
				return filterValue.equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)) ? (int)metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) : -1;
			}
			return 0;
		}

		public boolean isValid(){
			if (filterType == null) return false;
			/*
			public static final String FILTER_EMPTY = "__EMPTY__";
			public static final String FILTER_ROOT = "__ROOT__";
			public static final String FILTER_BY_SEARCH = "__SEARCH__";

			public static final String EXPLORE_FILTER = "__EXPLORE__";
			public static final String FILTER_BY_ALBUM = "__ALBUM__";
			public static final String FILTER_BY_ARTIST = "__ARTIST__";
			public static final String FILTER_BY_TAG = "__TAG__";
			public static final String FILTER_BY_GENRE = "__GENRE__";
			*/

			if (filterType.equals(FILTER_EMPTY)) return true;
			if (filterType.equals(FILTER_ROOT)) return true;
			if (filterType.equals(FILTER_BY_SEARCH)) return true;

			if (filterType.equals(EXPLORE_FILTER)) return true;
			if (filterType.equals(FILTER_BY_ALBUM)) return true;
			if (filterType.equals(FILTER_BY_ARTIST)) return true;
			if (filterType.equals(FILTER_BY_TAG)) return true;
			if (filterType.equals(FILTER_BY_GENRE)) return true;

			return false;
		}

		@Override
		public String toString(){
			String encodedFilterValue;
			try {
				encodedFilterValue = URLEncoder.encode(filterValue, "UTF-8");
			}catch (UnsupportedEncodingException e){
				LogHelper.e("What the fuck? UTF-8 not supported as a URL encoding.");
				encodedFilterValue = filterValue;
			}
			return filterType + TYPE_VALUE_SEPARATOR + encodedFilterValue + FILTER_END;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;
			if (o.getClass() != SubFilter.class) return false;
			SubFilter castedO = (SubFilter) o;
			return castedO.filterType.equals(filterType) && castedO.filterValue.equals(filterValue);
		}
	}

	public MusicFilter (String source){
		String[] filterStrings = source.split(FILTER_END);
		subFilters = new ArrayList<>();
		for (String filterString : filterStrings){
			String[] filterParts = filterString.split(TYPE_VALUE_SEPARATOR);
			if (filterParts.length > 2){
				StringBuilder sb = new StringBuilder().append("[");
				for (String s : filterParts){
					sb.append(s).append(",");
				}
				LogHelper.e(TAG, "Invalid Filter [Parts > 2]: ", sb.append("]").toString());
			}else if (filterParts.length == 1)
				subFilters.add(new SubFilter(filterParts[0], ""));
			else {
				String filterValue;
				try {
					filterValue = URLDecoder.decode(filterParts[1], "UTF-8");
				}catch (UnsupportedEncodingException e){
					LogHelper.e("What the fuck? UTF-8 not supported as a URL encoding.");
					filterValue = filterParts[1];
				}
				subFilters.add(new SubFilter(filterParts[0], filterValue));
			}
		}
		initialize();
	}
	public MusicFilter (SubFilter filter){
		subFilters = new ArrayList<>();
		subFilters.add(filter);
		initialize();
	}
	public MusicFilter (SubFilter... filters){
		subFilters = Arrays.asList(filters);
		initialize();
	}
	private void initialize(){
		isEmpty = subFilters.size() == 0 || subFilters.get(0).filterType.equals(FILTER_EMPTY);
		isRoot = subFilters.size() == 1 && subFilters.get(0).filterType.equals(FILTER_ROOT);

		isValid = subFilters.size() > 0;
		for (SubFilter sf : subFilters){
			if (sf.filterType == null || !sf.isValid()){
				isValid = false;
				break;
			}
		}
	}

	public boolean isEmpty(){
		return isEmpty;
	}
	public static MusicFilter emptyFilter(){
		return new MusicFilter(new SubFilter(FILTER_EMPTY, ""));
	}
	public boolean isRoot(){
		return isRoot;
	}
	public static MusicFilter rootFilter(){
		return new MusicFilter(new SubFilter(FILTER_ROOT, ""));
	}
	public boolean isValid() { return isValid; }

	public String getGenreFilter(){
		for (SubFilter sf : subFilters){
			if (sf.filterType.equals(FILTER_BY_GENRE))
				return sf.filterValue;
		}
		return null;
	}
	public String getSearchFilter(){
		for (SubFilter sf : subFilters){
			if (sf.filterType.equals(FILTER_BY_SEARCH))
				return sf.filterValue;
		}
		return null;
	}
	public String getExploreFilter(){
		for (SubFilter sf : subFilters){
			if (sf.filterType.equals(EXPLORE_FILTER))
				return sf.filterValue;
		}
		return null;
	}

	public int songStrength(Song song){
		if (isRoot) return 0;
		if (isEmpty) return 0;
		int songStrength = 0;
		for (SubFilter sf : subFilters) {
			int subFilterStrength = sf.songStrength(song);
			if (subFilterStrength == -1) return -1;
			songStrength += subFilterStrength;
		}
		return songStrength;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (SubFilter sf : subFilters) {
			sb.append(sf.toString());
		}
		return sb.toString();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (o.getClass() != MusicFilter.class) return false;
		MusicFilter castedO = (MusicFilter) o;
		if (castedO.subFilters.size() != subFilters.size()) return false;
		// TODO: This expects them in the same order, which it shouldn't.
		for (int i = 0; i < subFilters.size(); i++)
			if (!subFilters.get(i).equals(castedO.subFilters.get(i))) return false;
		return true;
	}
}
