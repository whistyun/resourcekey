package jp.co.java_conf.tyun.flowsc.out;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptWriter implements ScriptWriterIF<ScriptWriter>, Closeable {
	private int indendDep = 0;
	private String theTab;
	private String indendSpaceTxt = "";
	private Writer writer;

	public ScriptWriter(File file, Charset charset) throws IOException {
		this(file, charset, "    ");
	}

	public ScriptWriter(File file, Charset charset, String theTab) throws IOException {
		this(IOUtil.openWriter(file, charset), theTab);
	}

	public ScriptWriter(Writer writer) {
		this(writer, "    ");
	}

	public ScriptWriter(Writer writer, String theTab) {
		this.theTab = theTab;
		this.writer = writer;
	}

	@Override
	public ScriptWriter w(String text) throws IOException {
		writer.write(indendSpaceTxt);
		writer.write(text);
		ln();
		return this;
	}

	@Override
	public ScriptWriter w(String format, Object... objects) throws IOException {
		writer.write(indendSpaceTxt);
		writer.write(String.format(format, objects));
		ln();
		return this;
	}

	@Override
	public ScriptWriter ln() throws IOException {
		writer.write("\r\n");
		return this;
	}

	@Override
	public ScriptWriterIF<?> tab() {
		return new Tab(indendText(indendSpaceTxt), writer);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * 複数行分の出力します。
	 * 
	 * 文字列先頭に空白文字がある場合、コンストラクタで指定されたインデントに自動的に変換されます。
	 * <strong>空白文字は1文字が1インデントではなく、複数文字をまとめて1インデントとします</strong>
	 * 次の行で、空白文字が増えた場合は1インデント増え、減った場合は1インデント減らします
	 * 
	 * @param format
	 *            出力する書式文字列
	 * 
	 * @param objects
	 *            パラメータ
	 */
	public ScriptWriter block(String sentence) throws IOException {
		return block(Arrays.asList(sentence.split("\r\n?")));
	}

	public ScriptWriter block(List<String> lines) throws IOException {

		int indentCnt = 0;

		try {
			int oldI = 0;
			for (String line : lines) {
				// counting whitespace
				int i;
				for (i = 0; i < line.length(); ++i) {
					if (!Character.isWhitespace(line.charAt(i))) {
						break;
					}
				}

				if (i < oldI) {
					indentCnt--;
					dedend();
					oldI = i;
				} else if (i > oldI) {
					indentCnt++;
					indend();
					oldI = i;
				}
				w(line.replaceAll("^[ \t\f]+", ""));
			}
		} finally {
			while (indentCnt != 0) {
				if (indentCnt < 0) {
					++indentCnt;
					indend();
				} else {
					--indentCnt;
					dedend();
				}
			}
		}
		return this;
	}

	public ScriptWriter namedTemplateClasspath(String classpath, Map<String, String> param) throws IOException {
		String format = IOUtil.loadString(classpath);
		return namedTemplate(format, param);
	}

	public ScriptWriter namedTemplate(String namedFormats, Map<String, String> param) throws IOException {
		Pattern variableHolder = Pattern.compile("#\\{([^\\}]+)\\}");

		StringBuilder builder = new StringBuilder();
		List<String> buildTxt = new ArrayList<String>();
		for (String namedFormat : namedFormats.split("\r\n?")) {
			int bgn = 0;
			Matcher matcher = variableHolder.matcher(namedFormat);
			while (matcher.find(bgn)) {
				builder.append(namedFormat.substring(bgn, matcher.start()));
				builder.append(param.get(matcher.group(1)));
				bgn = matcher.end();
			}
			if (bgn < namedFormat.length()) {
				builder.append(namedFormat.substring(bgn));
			}

			buildTxt.add(builder.toString());
			builder.setLength(0);
		}

		block(buildTxt);

		return this;
	}

	public void indend() {
		++indendDep;
		indendSpaceTxt = indendText(indendSpaceTxt);
	}

	public void dedend() {
		if (indendDep == 0) {
			throw new IllegalStateException("no indend");
		}

		--indendDep;
		indendSpaceTxt = dedendText(indendSpaceTxt);
	}

	private String indendText(String text) {
		return text + theTab;
	}

	private String dedendText(String text) {
		return text.substring(0, text.length() - theTab.length());
	}

	private class Tab implements ScriptWriterIF<Tab> {
		private String indend;
		private Writer out;

		public Tab(String indend, Writer out) {
			this.indend = indend;
			this.out = out;
		}

		public Tab w(String text) throws IOException {
			out.write(indend);
			out.write(text);
			ln();
			return this;
		}

		public Tab w(String format, Object... objects) throws IOException {
			out.write(indend);
			out.write(String.format(format, objects));
			ln();
			return this;
		}

		public Tab ln() throws IOException {
			out.write("\r\n");
			return this;
		}

		public ScriptWriterIF<?> tab() {
			return new Tab(ScriptWriter.this.indendText(indend), out);
		}
	}
}
