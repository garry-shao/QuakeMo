package org.qmsos.quakemo.map;

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

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.content.Context;

/**
 * Utility class that used to handle the offline map-tiles.
 *
 */
public class MapTilesHandler {

	// Map tile file name, copied to.
	private static final String MAP_TILE_FILE = CustomTileSourceFactory.MAP_SOURCE + ".zip";

	// Asset file names, copied from.
	private static final String ASSET_TILE_NAME = "Mapnik.zip";
	private static final String ASSET_HASH_NAME = "Mapnik.sha";

	// Max retry count before abort.
	private static final int MAX_RETRY_COUNT = 3;

	/**
	 * Check whether offline map tiles are valid, create new valid ones if not.
	 * 
	 * @param context
	 *            The context of this library resides in.
	 */
	public static void initiateMapTiles(Context context) {
		initiateLibraryPaths(context);
		
		boolean tileFileIsValid = false;
		
		File mapTileFile = new File(OpenStreetMapTileProviderConstants.getBasePath(), MAP_TILE_FILE);
		if (mapTileFile.exists()) {
			tileFileIsValid = hashFiles(context, ASSET_HASH_NAME, mapTileFile);
		}
		
		int count = 0;
		while ((!tileFileIsValid) && (count <= MAX_RETRY_COUNT)) {
			boolean copySucceed = false;
			
			copySucceed = copyFiles(context, ASSET_TILE_NAME, mapTileFile);
			count++;
			
			if (copySucceed) {
				tileFileIsValid = hashFiles(context, ASSET_HASH_NAME, mapTileFile);
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
	private static boolean copyFiles(Context context, String assetsFilename, File targetFilePath) {
		boolean flag = false;

		InputStream in = null;
		BufferedOutputStream bout = null;
		try {
			in = context.getAssets().open(assetsFilename);
			
			byte[] buffer = new byte[2048];
			
			bout = new BufferedOutputStream(new FileOutputStream(targetFilePath), buffer.length);
			
			int content;
			while ((content = in.read(buffer)) != -1) {
				bout.write(buffer, 0, content);
			}
			flag = true;
		} catch (IOException e) {
			flag = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flag = false;
				}
			}
			if (bout != null) {
				try {
					bout.flush();
					bout.close();
				} catch (IOException e) {
					flag = false;
				}
			}
		}
		
		return flag;
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
	private static boolean hashFiles(Context context, String assetsHashFilename, File targetFilePath) {
		boolean flagChecksum = false;
		String fileChecksum = null;
		InputStream in = null;
		try {
			in = context.getAssets().open(assetsHashFilename);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			fileChecksum = reader.readLine();
			flagChecksum = true;
		} catch (IOException e) {
			flagChecksum = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flagChecksum = false;
				}
			}
		}

		boolean flagHash = false;
		String fileHash = null;
		MessageDigest digest = null;
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
			fileHash = bigInt.toString(16);
			flagHash = true;
		} catch (IOException e) {
			flagHash = false;
		} catch (NoSuchAlgorithmException e) {
			flagHash = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					flagHash = false;
				}
			}
		}
		
		if (flagChecksum && flagHash && 
				fileChecksum != null && fileHash != null && fileChecksum.equals(fileHash)) {
			
			return true;
		} else {
			return false;
		}
	}

}
