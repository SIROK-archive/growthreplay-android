package com.growthreplay.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Build;

public class IOUtils {

	public static String toString(InputStream inputStream) throws IOException {

		InputStreamReader objReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(objReader);
		StringBuilder stringBuilder = new StringBuilder();

		try {

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}

			return stringBuilder.toString();

		} catch (IOException e) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
				throw new IOException("Failed to convert InputStream to String.", e);
			else
				throw new IOException("Failed to convert InputStream to String.");
		} finally {

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
						throw new IOException("Failed to close InputStream.", e);
					else
						throw new IOException("Failed to close InputStream.");
				}
			}

		}

	}

	public static byte[] readBytesFromFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists())
			return null;

		byte[] data = new byte[(int) file.length()];
		try {
			BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(file));
			buffer.read(data, 0, data.length);
			buffer.close();
			file.delete();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return data;
	}

}
