package jp.co.java_conf.tyun.flowsc.out;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class IOUtil {
	public static Charset UTF8 = Charset.forName("UTF-8");

	public static String loadString(String classpath) throws IOException {
		return loadString(classpath, UTF8);
	}

	public static String loadString(String classpath, Charset charset) throws IOException {
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[1024];

		InputStream is = null;
		InputStreamReader isr = null;
		try {
			is = IOUtil.class.getResourceAsStream(classpath);
			isr = new InputStreamReader(is, charset);

			int len;
			while ((len = isr.read(buffer)) != -1)
				builder.append(buffer, 0, len);

			return builder.toString();

		} finally {
			if (isr != null) {
				isr.close();
			}
			if (is != null) {
				is.close();
			}
		}
	}

	public static void closeChain(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static BufferedWriter openWriter(File to, Charset charset) throws IOException {

		boolean success = false;

		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try {
			fos = new FileOutputStream(to);
			osw = new OutputStreamWriter(fos, charset);
			writer = new BufferedWriter(osw);
			success = true;

			return writer;
		} finally {
			if (!success) {
				closeChain(writer, osw, fos);
			}
		}
	}
}
