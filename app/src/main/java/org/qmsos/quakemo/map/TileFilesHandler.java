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

import android.content.Context;

/**
 * Utility class that used to handle the offline map-tile files.
 *
 */
public class TileFilesHandler {

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
	public static final boolean copyFiles(Context context, String assetsFilename, File targetFilePath) {
		boolean isCopySucceeded = false;

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
	public static final boolean hashFiles(Context context, String assetsHashFilename, File targetFilePath) {
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
			valueOfHash = bigInt.toString(16);
			
			isHashSucceeded = true;
		} catch (IOException e) {
			isHashSucceeded = false;
		} catch (NoSuchAlgorithmException e) {
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
		
		if (isChecksumSucceeded && isHashSucceeded && 
				valueOfChecksum != null && valueOfHash != null && valueOfChecksum.equals(valueOfHash)) {
			
			return true;
		} else {
			return false;
		}
	}

}
