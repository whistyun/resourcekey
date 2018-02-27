package jp.gr.java_conf.tyun.resourcekey;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.maven.plugin.logging.Log;

import jp.co.java_conf.tyun.flowsc.out.IOUtil;
import jp.co.java_conf.tyun.flowsc.out.ScriptWriter;

public class PropertyClassMaker {
	private Log logger;

	private String outputJavaVersion;
	private Charset encoding;
	private File sourceOutputDirectory;
	private String sourcePackage;
	private String outputClassName;
	private ResourceBundle targetBundle;

	private Grouping root;

	public PropertyClassMaker(MyMojo mojo, ResourceBundle bundle) {
		this.logger = mojo.getLog();
		this.outputJavaVersion = mojo.getVersion();
		this.encoding = Charset.forName(mojo.getEncoding());
		this.sourceOutputDirectory = mojo.getSourceOutputDirectory();
		this.sourcePackage = mojo.getPackageStr();
		this.targetBundle = bundle;

		String bundleBaseName = bundle.getBaseBundleName();
		int li = bundleBaseName.lastIndexOf('.');
		this.outputClassName = propertyFileToClassName(li == -1 ? bundleBaseName : bundleBaseName.substring(li + 1));

		this.root = new Grouping(this.outputClassName);
		for (String key : targetBundle.keySet()) {
			String[] names = key.split("\\.");
			Grouping grp = this.root;
			for (int i = 0; i < names.length - 1; ++i) {
				grp = grp.child(names[i]);
			}
			grp.put(key, names[names.length - 1], targetBundle.getString(key));
		}
	}

	public void write() throws IOException {
		File outputFile = new File(sourceOutputDirectory, outputClassName + ".java");

		logger.info(String.format("output class '%s' to %s", outputClassName, outputFile.getAbsolutePath()));

		ScriptWriter writer = null;
		try {
			writer = new ScriptWriter(outputFile, encoding);

			// ソースの開始部分
			Map<String, String> parameter = new HashMap<String, String>();
			parameter.put("sourcePackage", sourcePackage);
			parameter.put("className", root.className);
			writer.namedTemplateClasspath("/resourcekey/classhead-parent.txt", parameter);
			writer.indend();

			// 決まりきったプロパティ
			writeForField(writer, root.className, root);

			// コンストラクタ
			parameter.clear();
			parameter.put("className", root.className);
			writer.namedTemplateClasspath("/resourcekey/constructor-parent.txt", parameter);
			writer.ln();

			// 内部クラス
			writeForDerived(writer, root.className, root);

			// 固定ロジック
			parameter.clear();
			parameter.put("bundleName", targetBundle.getBaseBundleName());

			if (isElderJavaVersion(this.outputJavaVersion)) {
				// ~java1.7
				writer.namedTemplateClasspath("/resourcekey/resource-access7.txt", parameter);
			} else {
				// java1.8~
				writer.namedTemplateClasspath("/resourcekey/resource-access8.txt", parameter);
			}
			writer.ln();

			writer.dedend();
			writer.w("}");

		} finally {
			IOUtil.closeChain(writer);
		}
	}

	private boolean isElderJavaVersion(String versionLabel) {
		try {
			double version = Double.parseDouble(versionLabel);
			return version < 1.8;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void writeForField(ScriptWriter writer, String className, Grouping group) throws IOException {
		for (Map.Entry<String, GrpuingEntry> nkv : group.name2Val.entrySet()) {
			Map<String, String> parameter = new HashMap<String, String>();
			parameter.put("className", className);
			parameter.put("name", nkv.getValue().getName());
			parameter.put("key", nkv.getValue().getKey());
			parameter.put("value", nkv.getValue().getValue());
			writer.namedTemplateClasspath("/resourcekey/keyfields.txt", parameter);
		}
		writer.ln();
	}

	private void writeForDerived(ScriptWriter writer, String className, Grouping parentGroup) throws IOException {
		String parentClass = parentGroup.className;

		for (Map.Entry<String, Grouping> groupEntry : parentGroup.keyAndGrouping.entrySet()) {
			Grouping group = groupEntry.getValue();

			// クラス名(始まり)
			logger.debug(String.format("create derived class '%s'", group.className));
			writer.w("public static class %s {", group.className);
			writer.indend();

			// 決まりきったプロパティ
			writeForField(writer, className, group);

			// サブクラス
			writeForDerived(writer, className, group);

			// クラス名(終わり)
			writer.dedend();
			writer.w("}");
		}

	}

	private static class Grouping {
		String className;
		TreeMap<String, Grouping> keyAndGrouping = new TreeMap<String, Grouping>();
		TreeMap<String, GrpuingEntry> name2Val = new TreeMap<String, GrpuingEntry>();

		public Grouping(String className) {
			this.className = className;
		}

		public void put(String key, String name, String value) {
			String name2 = propertyFileToClassName(name);

			name2Val.put(name, new GrpuingEntry(key, name2, value));
		}

		public Grouping child(String name) {
			String name2 = propertyFileToClassName(name);

			if (keyAndGrouping.containsKey(name)) {
				return keyAndGrouping.get(name);
			} else {
				Grouping grouping = new Grouping(name2);
				keyAndGrouping.put(name, grouping);
				return grouping;
			}
		}
	}

	private static class GrpuingEntry {
		String name;
		String key;
		String value;

		public GrpuingEntry(String key, String name, String value) {
			super();
			this.key = key;
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

	}

	private static String propertyFileToClassName(String propName) {

		StringBuilder builder = new StringBuilder();
		for (int idx = 0; idx < propName.length();) {
			int codePoint = propName.codePointAt(idx);
			idx += ((codePoint & 0xFFFF0000) == 0) ? 1 : 2;

			boolean accept;
			if (builder.length() == 0) {
				accept = Character.isJavaIdentifierStart(codePoint);
			} else {
				accept = Character.isJavaIdentifierPart(codePoint);
			}

			if (accept) {
				builder.appendCodePoint(codePoint);
			} else if (codePoint == '-') {
				builder.append('_');
			}
		}

		return builder.toString();
	}
}