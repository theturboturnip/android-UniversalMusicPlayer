/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.turboturnip.turnipmusic.backend;

import android.content.Context;

import com.turboturnip.turnipmusic.model.Album;
import com.turboturnip.turnipmusic.model.Song;
import com.turboturnip.turnipmusic.model.db.SongDatabase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MusicProviderSource {
    Collection<Album> albums(Context context);
    Collection<Song> songs(Context context, Map<String, Album> albums, SongDatabase db);
    List<String> songMediaIdsForAlbumLibraryId(Context context, String albumLibraryId);
}
