package org.qmsos.quakemo.map;

import android.content.Context;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Customized factory class that creates offline map-tile source.
 */
public class OfflineTileSourceFactory {

	// Zoom levels of the offline map-tile source, should be consistent with the map-tile file.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;

	// Max retry count before abort.
	private static final int MAX_RETRY_COUNT = 3;

    // Name of tile source, should be the same with the folder name in map-tile file.
	private static final String MAP_SOURCE = "Mapnik";
	// Map tile file name, copied to.
	private static final String MAP_TILE_FILE = MAP_SOURCE + ".zip";

	// Asset file name, copied from.
	private static final String ASSET_TILE_NAME = "Mapnik.zip";

	// Asset hash name, whether the map-tile file is corrupted.
	private static final String ASSET_HASH_NAME = "Mapnik.sha";

	/**
	 * Create offline map-tile source.
	 * 
	 * @return The offline map-tile source.
	 */
	public static ITileSource offlineTileSource() {
		return new XYTileSource(MAP_SOURCE,
				ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {});
	}

	/**
	 * Initialize offline map tiles, should be called before map being drawed.
	 * 
	 * @param context
	 *            The context of the map resides in.
	 */
	public static void initiateOfflineMapTiles(Context context) {
        // Valid up until osmdroid@5.5, changing coufiguration of this library.
        //
        // Known issues:
        // OSMDROID_PATH is static final fields, the tiles-base path created
        // before configuring, so it still created under old configuration, but
        // since we are using offline map-tiles, blocking this file creation
        // by revoking the permission does no harm except an error line on log.
        //
        // Since osmdroid@5.6, there are drastic change in configuration of this
        // library, setting offline map tiles may be impossible.
        //
        // see configuration package in the library source for more information.

        String cachePath = context.getCacheDir().getAbsolutePath();
        String filePath = context.getFilesDir().getAbsolutePath();

        OpenStreetMapTileProviderConstants.setCachePath(cachePath);
        OpenStreetMapTileProviderConstants.setOfflineMapsPath(filePath);

        File mapTileFile =
				new File(OpenStreetMapTileProviderConstants.getBasePath(), MAP_TILE_FILE);

        tileCheckup(context, mapTileFile, MAX_RETRY_COUNT, ASSET_TILE_NAME, ASSET_HASH_NAME);
	}

    /**
     * Check whether offline map tiles are valid, create new valid ones if not.
     *
     * @param context
     *            The context of this library resides in.
     * @param tileFile
     *            The offline tile file.
     * @param maxRetryCount
     *            Maximum retry count of checking map tiles before failure.
     * @param TileAssetsName
     *            The name of the file to be copied in assets folder of apk.
     * @param HashAssetsName
     *            The name of file containing hash info in assets folder of apk.
     * @return TRUE if the offline map tiles are valid, FALSE otherwise.
     */
    private static boolean tileCheckup(Context context,
                                       File tileFile,
                                       int maxRetryCount,
                                       String TileAssetsName,
                                       String HashAssetsName) {

        boolean isTileValid = false;

        if (tileFile.exists()) {
            isTileValid = hashFiles(context, HashAssetsName, tileFile);
        }

        int count = 0;
        while ((!isTileValid) && (count <= maxRetryCount)) {
            boolean isCopySucceed = copyFiles(context, TileAssetsName, tileFile);
            count++;

            if (isCopySucceed) {
                isTileValid = hashFiles(context, HashAssetsName, tileFile);
            }
        }

        return isTileValid;
    }

    /**
     * Copy file from assets folder in apk to specific path.
     *
     * @param context
     *            The associated context.
     * @param assetsFilename
     *            The name of the file to be copied in assets folder of apk.
     * @param targetFilePath
     *            Targeted file path.
     * @return TRUE if copying succeeded, FALSE otherwise.
     */
    private static boolean copyFiles(Context context,
                                     String assetsFilename,
                                     File targetFilePath) {

        boolean isCopySucceeded = false;

        InputStream in = null;
        BufferedOutputStream bout = null;
        try {
            in = context.getAssets().open(assetsFilename);

            byte[] buffer = new byte[2048];

            bout = new BufferedOutputStream(
                    new FileOutputStream(targetFilePath), buffer.length);

            int content;
            while ((content = in.read(buffer)) != -1) {
                bout.write(buffer, 0, content);
            }
            isCopySucceeded = true;
        } catch (IOException e) {
            isCopySucceeded = false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    isCopySucceeded = false;
                }
            }
            if (bout != null) {
                try {
                    bout.flush();
                    bout.close();
                } catch (IOException e) {
                    isCopySucceeded = false;
                }
            }
        }

        return isCopySucceeded;
    }

    /**
     * Hash file to decide whether it is authentic.
     *
     * @param context
     *            The associated context.
     * @param assetsHashFilename
     *            The name of file containing hash info in assets folder of apk.
     * @param targetFilePath
     *            Targeted file path to be hashed.
     * @return TRUE if the comparison succeeded, FALSE otherwise.
     */
    private static boolean hashFiles(Context context,
                                     String assetsHashFilename,
                                     File targetFilePath) {

        String valueOfChecksum = null;
        boolean isChecksumSucceeded = false;

        InputStream in = null;
        try {
            in = context.getAssets().open(assetsHashFilename);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            valueOfChecksum = reader.readLine();

            isChecksumSucceeded = true;
        } catch (IOException e) {
            isChecksumSucceeded = false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    isChecksumSucceeded = false;
                }
            }
        }

        String valueOfHash = null;
        boolean isHashSucceeded = false;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            in = new FileInputStream(targetFilePath);

            byte[] buffer = new byte[2048];

            int content;
            while ((content = in.read(buffer)) != -1) {
                digest.update(buffer, 0, content);
            }

            byte[] sha1 = digest.digest();

            BigInteger bigInt = new BigInteger(1, sha1);
            valueOfHash = bigInt.toString(16);

            isHashSucceeded = true;
        } catch (IOException | NoSuchAlgorithmException e) {
            isHashSucceeded = false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    isHashSucceeded = false;
                }
            }
        }

        return isChecksumSucceeded && isHashSucceeded &&
                valueOfChecksum != null && valueOfHash != null &&
                valueOfChecksum.equals(valueOfHash);
    }
}
