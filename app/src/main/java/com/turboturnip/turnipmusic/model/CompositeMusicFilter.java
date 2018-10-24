package com.turboturnip.turnipmusic.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CompositeMusicFilter {
	public static final String FILTER_SEPARATOR = ";";

	public List<MusicFilter> filters;

	public CompositeMusicFilter(MusicFilter... filters){
		this.filters = new LinkedList<>(Arrays.asList(filters));
	}
	public CompositeMusicFilter(String source){
		String[] filterStrings = source.split(FILTER_SEPARATOR);
		this.filters = new LinkedList<>();
		for (int i = 0; i < filterStrings.length; i++) this.filters.add(new MusicFilter(filterStrings[i]));
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < filters.size(); i++){
			result.append(filters.get(i).toString());
			if (i > 0) result.append(FILTER_SEPARATOR);
		}
		return result.toString();
	}
}
