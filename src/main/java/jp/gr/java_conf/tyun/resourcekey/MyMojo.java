package jp.gr.java_conf.tyun.resourcekey;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "propertykey", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MyMojo extends AbstractMojo {

	////////////////////////////////////////////////////////////////////////////////
	//
	// Maven's parameters
	// https://maven.apache.org/guides/mini/guide-configuring-plugins.html#Configuring_Parameters
	//
	////////////////////////////////////////////////////////////////////////////////

	@Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
	private File baseDir;

	@Parameter(defaultValue = "${project.resources}", required = true, readonly = true)
	private List<Resource> resources;

	@Parameter(defaultValue = "${project.build.sourceDirectory}", required = true, readonly = true)
	private File sourceDirectory;

	@Parameter(defaultValue = "${project.groupId}.${project.artifactId}", readonly = true, required = true)
	private String defaultOutputPackage;

	/** the package for organizing generated class */
	@Parameter(property = "outputPackage")
	private String outputPackage;

	/** charset for generated source */
	@Parameter(defaultValue = "${project.build.sourceEncoding}", property = "encoding")
	private String encoding;

	/** input properties files */
	@Parameter(property = "properties", required = true)
	private List<String> properties;

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getDefaultOutputPackage() {
		return defaultOutputPackage;
	}

	public void setDefaultOutputPackage(String defaultOutputPackage) {
		this.defaultOutputPackage = defaultOutputPackage;
	}

	public String getOutputPackage() {
		return outputPackage;
	}

	public void setOutputPackage(String outputPackage) {
		this.outputPackage = outputPackage;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	//////////////////////////////////////////////////
	//
	// little net logic
	//
	//////////////////////////////////////////////////

	private String packageStr;
	private File sourceOutputDirectory;
	private ClassLoader resourceDirectoriesClassLoader;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// compute parameter from maven's
		computePackageStr();
		computeSourceOutputDirectory();
		computeResourceDirectoriesClassLoader();

		// source generate
		ClassLoader propertyDistDir = getResourceDirectoriesClassLoader();
		for (String resource : getProperties()) {
			ResourceBundle bundle = ResourceBundle.getBundle(resource, Locale.getDefault(), propertyDistDir);
			String className = propertyFileToClassName(bundle.getBaseBundleName());
			if (bundle == null) {
				getLog().warn(String.format("property (%s) is not found", resource));
			}

			PropertyClassMaker classMaker = new PropertyClassMaker(this, bundle, className);
			try {
				classMaker.write();
			} catch (IOException e) {
				throw new MojoExecutionException("output error", e);
			}
		}
	}

	private void computePackageStr() {
		String defaultOutputPackage = getDefaultOutputPackage();
		String outputPackage = getOutputPackage();

		if (outputPackage != null && !outputPackage.isEmpty()) {
			packageStr = outputPackage;
		} else {
			packageStr = defaultOutputPackage.replace("-", "_").replaceAll("\\.+", ".");
		}
	}

	private void computeSourceOutputDirectory() {
		File f = getSourceDirectory();
		for (String pack : getPackageStr().split("\\.")) {
			f = new File(f, pack);
		}
		sourceOutputDirectory = f;
	}

	public void computeResourceDirectoriesClassLoader() {
		List<URL> list = new ArrayList<URL>();

		for (Resource res : getResources()) {
			File resourceFile = new File(getBaseDir(), res.getDirectory());
			try {
				list.add(resourceFile.toURI().toURL());
			} catch (MalformedURLException e) {
				getLog().warn("failed to load " + resourceFile.getAbsolutePath());
			}
		}

		File sourceDir = getSourceDirectory();
		try {
			list.add(sourceDir.toURI().toURL());
		} catch (MalformedURLException e) {
			getLog().warn("failed to load " + sourceDir.getAbsolutePath());
		}

		resourceDirectoriesClassLoader = new URLClassLoader(list.toArray(new URL[list.size()]));
	}

	private String propertyFileToClassName(String bundleBasename) {
		String propName;
		if (bundleBasename.lastIndexOf('.') != -1) {
			propName = bundleBasename.substring(0, bundleBasename.lastIndexOf('.'));
		} else {
			propName = bundleBasename;
		}

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

		if (builder.length() == 0) {
			getLog().warn(String.format("property file (%s) can not convert to java class name", propName));
		}

		return builder.toString();
	}

	public File getSourceOutputDirectory() {
		return sourceOutputDirectory;
	}

	public String getPackageStr() {
		return packageStr;
	}

	public ClassLoader getResourceDirectoriesClassLoader() {
		return resourceDirectoriesClassLoader;
	}
}
