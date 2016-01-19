package org.qmsos.quakemo.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

/**
 * Utility class use to checker if the offline map tiles used in MapView class
 * are available on storage device.
 * 
 *
 */
public class UtilMapTileChecker {

	/**
	 * Check whether offline map tiles are available, fix if not(WARING: this
	 * fix may fail, one may have to delete incomplete map tiles manually if
	 * grey area shown on MapView or unusually high frequency of GC happened.
	 * 
	 * @param context
	 *            The context used to access assets.
	 */
	public static void checkMapTiles(Context context) {
		File osmdroidBasePath = new File(Environment.getExternalStorageDirectory(), "osmdroid");
		if (!osmdroidBasePath.exists()) {
			boolean flag = false;
			for (int i = 0; i < 3 && !flag; i++) {
				flag = unzip(context, osmdroidBasePath);
			}
		}
	}

	/**
	 * Unzip map tiles to specific path.
	 * 
	 * @param context
	 * @param unzippedRootPath
	 * @return
	 */
	private static boolean unzip(Context context, File unzippedRootPath) {
		if (unzippedRootPath == null) {
			return false;
		}

		boolean flag = false;

		ZipInputStream zin = null;
		try {
			zin = new ZipInputStream(context.getAssets().open("Mapnik.zip"));

			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				File entryFile = new File(unzippedRootPath, "tiles/" + entry.getName());

				if (entry.isDirectory()) {
					flag = entryFile.mkdirs();
				} else {
					BufferedOutputStream bout = null;
					try {
						byte[] buffer = new byte[2048];

						bout = new BufferedOutputStream(new FileOutputStream(entryFile), buffer.length);

						int content;
						while ((content = zin.read(buffer)) != -1) {
							bout.write(buffer, 0, content);
						}
						flag = true;
					} catch (IOException e) {
						flag = false;
					} finally {
						if (bout != null) {
							try {
								bout.flush();
								bout.close();
							} catch (IOException e) {
								flag = false;
							}
						}
					}
				}
				entry = zin.getNextEntry();
			}

		} catch (IOException e) {
			flag = false;
		} finally {
			try {
				zin.close();
			} catch (IOException e) {
				flag = false;
			}
		}

		return flag;
	}

}
