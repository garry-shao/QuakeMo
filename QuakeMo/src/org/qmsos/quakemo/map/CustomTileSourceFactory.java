package org.qmsos.quakemo.map;

import java.io.File;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import android.content.Context;

/**
 * Customized factory class that creates offline map-tile source.
 *
 */
public class CustomTileSourceFactory {

	/**
	 * Name of tile source, should be the same with the folder name in map-tile file.
	 */
	protected static final String MAP_SOURCE = "Mapnik";

	// Zoom levels of the offline map-tile source, should be consistent with the map-tile file.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;

	// Map tile file name, copied to.
	private static final String MAP_TILE_FILE = MAP_SOURCE + ".zip";

	// Asset file name, copied from.
	private static final String ASSET_TILE_NAME = "Mapnik.zip";
	
	// Asset hash name, whether the map-tile file is corrupted.
	private static final String ASSET_HASH_NAME = "Mapnik.sha";
	
	// Max retry count before abort.
	private static final int MAX_RETRY_COUNT = 3;

	/**
	 * Create offline map-tile source.
	 * 
	 * @return The offline map-tile source.
	 */
	public static ITileSource offlineTileSource() {
		return new XYTileSource(MAP_SOURCE, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {});
	}

	/**
	 * Check whether offline map tiles are valid, create new valid ones if not.
	 * 
	 * @param context
	 *            The context of this library resides in.
	 */
	public static void initiateOfflineMapTiles(Context context) {
		initiateLibraryPaths(context);
		
		boolean tileFileIsValid = false;
		
		File mapTileFile = new File(OpenStreetMapTileProviderConstants.getBasePath(), MAP_TILE_FILE);
		if (mapTileFile.exists()) {
			tileFileIsValid = TileFilesHandler.hashFiles(context, ASSET_HASH_NAME, mapTileFile);
		}
		
		int count = 0;
		while ((!tileFileIsValid) && (count <= MAX_RETRY_COUNT)) {
			boolean copySucceed = false;
			
			copySucceed = TileFilesHandler.copyFiles(context, ASSET_TILE_NAME, mapTileFile);
			count++;
			
			if (copySucceed) {
				tileFileIsValid = TileFilesHandler.hashFiles(context, ASSET_HASH_NAME, mapTileFile);
			}
		}
	}

	/**
	 * Initiate paths of Osmdroid library.
	 * 
	 * @param context
	 *            The context of this library resides in.
	 */
	private static void initiateLibraryPaths(Context context) {
		// Change osmdroid's path, but there are still bugs: since the paths are 
		// static final fields, the tiles-base path created before the change, so
		// that still created on old configuration, but since we are using offline
		// map-tiles, blocking this file creation by revoking the permission does
		// no harm except an error line on log.
		String cachePath = context.getCacheDir().getAbsolutePath();
		String filePath = context.getFilesDir().getAbsolutePath();
		
		OpenStreetMapTileProviderConstants.setCachePath(cachePath);
		OpenStreetMapTileProviderConstants.setOfflineMapsPath(filePath);
	}

}
