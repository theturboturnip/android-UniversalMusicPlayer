package com.turboturnip.turnipmusic;

import com.turboturnip.turnipmusic.model.MusicProvider;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicFilter {
	public static final String TAG = LogHelper.makeLogTag(MusicFilter.class);

	public static final String TYPE_VALUE_SEPARATOR = "/";
	public static final String FILTER_END = ";";

	public static final String FILTER_EMPTY = "__EMPTY__";
	public static final String FILTER_ROOT = "__ROOT__";
	public static final String FILTER_BY_SEARCH = "__SEARCH__";
	public static final String FILTER_BY_GENRE = "__GENRE__";
	public static final String FILTER_BY_ID = "__ID__";

	public static final String EMPTY_FILTER_VALUE = "__NONE__";

	public ArrayList<SubFilter> subFilters;
	private boolean isEmpty, isRoot;

	public static class SubFilter {
		public String filterType, filterValue;

		public SubFilter() {
			filterType = FILTER_EMPTY;
			filterValue = EMPTY_FILTER_VALUE;
		}

		public static SubFilter fromSong(String songID) {
			return new SubFilter(FILTER_BY_ID, songID);
		}
		public SubFilter(String _filterType){
			filterType = _filterType;
			filterValue = EMPTY_FILTER_VALUE;
		}
		public SubFilter(String _filterType, String _filterValue) {
			filterType = _filterType;
			filterValue = _filterValue;
		}

		@Override
		public String toString(){
			return filterType + TYPE_VALUE_SEPARATOR + filterValue + FILTER_END;
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
		for (int i = 0; i < filterStrings.length; i++){
			String[] filterParts = filterStrings[i].split(TYPE_VALUE_SEPARATOR);
			if (filterParts.length > 2){
				StringBuilder sb = new StringBuilder().append("[");
				for (String s : filterParts){
					sb.append(s).append(",");
				}
				LogHelper.e(TAG, "Invalid Filter [Parts > 2]: ", sb.append("]").toString());
			}else if (filterParts.length == 1)
				subFilters.add(new SubFilter(filterParts[0], ""));
			else
				subFilters.add(new SubFilter(filterParts[0], filterParts[1]));
		}
		initialize();
	}
	public MusicFilter (SubFilter filter){
		subFilters = new ArrayList<>();
		subFilters.add(filter);
		initialize();
	}
	public MusicFilter (ArrayList<SubFilter> filters){
		subFilters = filters;
		initialize();
	}
	public MusicFilter (MusicFilter base, String songID){
		subFilters = base.subFilters;
		for (SubFilter sf : subFilters) {
			if (sf.filterType.equals(FILTER_BY_ID)){
				sf.filterValue = songID;
				return;
			}
		}
		subFilters.add(new SubFilter(FILTER_BY_ID, songID));
		initialize();
	}
	private void initialize(){
		isEmpty = subFilters.size() == 0 || subFilters.get(0).filterType.equals(FILTER_EMPTY);
		isRoot = subFilters.size() == 1 && subFilters.get(0).filterType.equals(FILTER_ROOT);
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

	// TODO: Make this actually do something
	public boolean accepts(Song song){
		return true;
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
