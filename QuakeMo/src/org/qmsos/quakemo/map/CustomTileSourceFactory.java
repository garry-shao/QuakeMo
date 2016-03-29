package org.qmsos.quakemo.map;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
 * Customized factory class that creates offline tile source.
 *
 */
public class CustomTileSourceFactory {

	/**
	 * Name of tile source, should be the same with the folder name in map-tile zip file.
	 */
	protected static final String MAP_SOURCE = "Mapnik";

	// Zoom levels of the offline map-tile source.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;

	/**
	 * Create offline map-tile source.
	 * 
	 * @return The offline map-tile source.
	 */
	public static ITileSource offlineTileSource() {
		return new XYTileSource(MAP_SOURCE, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {});
	}

}
