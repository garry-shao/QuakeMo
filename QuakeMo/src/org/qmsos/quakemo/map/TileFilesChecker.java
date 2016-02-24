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
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Utility class use to checker if the offline map tiles used in MapView class
 * are available on storage device.
 * 
 *
 */
public class TileFilesChecker {

	private static final String MAP_SOURCE = "Mapnik";
	private static final String MAP_TILE_FILE = MAP_SOURCE + ".zip";
	private static final String MAP_TILE_HASH = MAP_SOURCE + ".sha";

	// Zoom levels of the offline map-tile source.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;
	
	// Max retry count before abort.
	private static final int MAX_RETRY = 3;

	/**
	 * Create offline map-tile source.
	 * 
	 * @return The offline map-tile source.
	 */
	public static ITileSource offlineTileSource() {
		return new XYTileSource(MAP_SOURCE, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {});
	}

	/**
	 * Check whether offline map tiles are available, create new ones if not.
	 * 
	 * @param context
	 *            The associated context.
	 */
	public static void checkMapTileFiles(Context context) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
				!= PackageManager.PERMISSION_GRANTED) {
			return;
		}

		boolean flag = false;
		for (int i = 0; i < MAX_RETRY && !flag; i++) {
			File mapTileFile = new File(OpenStreetMapTileProviderConstants.getBasePath(), MAP_TILE_FILE);
			if (mapTileFile.exists() || copyFiles(context, MAP_TILE_FILE, mapTileFile)) {
				flag = hashFiles(context, MAP_TILE_HASH, mapTileFile);
			}
		}
	}

	/**
	 * Copy file from assets in APK to specific path.
	 * 
	 * @param context
	 *            The associated context.
	 * @param assetsFilename
	 *            The name of the file to be copied in assets of APK. 
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
	 *            The name of file containing hash info in assets of APK.
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
