package com.turboturnip.turnipmusic.model;

import com.turboturnip.turnipmusic.utils.LogHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

public class MusicFilter {
	public static final String TAG = LogHelper.makeLogTag(MusicFilter.class);

	private static final String TYPE_VALUE_SEPARATOR = "/";

	private boolean isValid = false;

	public MusicFilterType filterType;
	public String filterValue = "";

	public MusicFilter() {
		filterType = MusicFilterType.Empty;
		filterValue = "";
		isValid = true;
	}

	public MusicFilter(String encodedString) {
		String[] filterParts = encodedString.split(TYPE_VALUE_SEPARATOR);
		if (filterParts.length > 2) {
			StringBuilder sb = new StringBuilder().append("[");
			for (String s : filterParts) {
				sb.append(s).append(",");
			}
			LogHelper.e(TAG, "Invalid Filter [Parts > 2]: ", sb.append("]").toString());
			return;
		}
		this.filterType = MusicFilterType.valueFor(filterParts[0]);
		if (filterParts.length > 1) {
			try {
				this.filterValue = URLDecoder.decode(filterParts[1], "UTF-8");
			}catch (UnsupportedEncodingException e){
				LogHelper.e("What the fuck? UTF-8 not supported as a URL encoding.");
				this.filterValue = filterParts[1];
			}
		}
		isValid = this.filterType != null;
	}
	public MusicFilter(MusicFilterType filterType, String filterValue) {
		this.filterType = filterType;
		this.filterValue = filterValue;
		isValid = true;
	}

	public boolean isValid(){ return isValid; }

	public static MusicFilter emptyFilter(){
		return new MusicFilter();
	}
	public static MusicFilter rootFilter(){
		return new MusicFilter(MusicFilterType.Root, "");
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
		return filterType.toString() + TYPE_VALUE_SEPARATOR + encodedFilterValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (o.getClass() != MusicFilter.class) return false;
		MusicFilter castedO = (MusicFilter) o;
		return castedO.filterType.equals(filterType) && castedO.filterValue.equals(filterValue);
	}
	@Override
	public int hashCode(){
		return 37 * filterType.hashCode() + filterValue.hashCode();
	}

}
