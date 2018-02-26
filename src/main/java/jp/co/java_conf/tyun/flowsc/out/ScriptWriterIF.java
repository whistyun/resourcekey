package jp.co.java_conf.tyun.flowsc.out;

import java.io.IOException;

public interface ScriptWriterIF<E extends ScriptWriterIF<E>> {
	/**
	 * 1行分の出力します。
	 * 
	 * 文字列は自動的にインデントされます
	 * 
	 * @param text
	 *            出力する文字列
	 */
	public E w(String t) throws IOException;

	/**
	 * 1行分の出力します。
	 * 
	 * 文字列は{@link String#format(String, Object...)}によりフォーマットした後、自動的にインデントされます
	 * 
	 * @param format
	 *            出力する書式文字列
	 * 
	 * @param objects
	 *            パラメータ
	 */
	public E w(String format, Object... objects) throws IOException;

	/** 空行を出力します */
	public E ln() throws IOException;

	public ScriptWriterIF<?> tab();
}
