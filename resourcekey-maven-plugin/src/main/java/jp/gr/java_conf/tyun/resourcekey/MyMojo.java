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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "resourcekey", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "resourcekey", phase = LifecyclePhase.GENERATE_SOURCES)
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

	@Parameter(defaultValue = "${maven.compiler.target}", readonly = true, required = true)
	public String version;

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		try {
			// compute parameter from maven's
			computePackageStr();
			computeSourceOutputDirectory();
			computeResourceDirectoriesClassLoader();

			// source generate
			ClassLoader propertyDistDir = getResourceDirectoriesClassLoader();
			for (String resource : getProperties()) {
				ResourceBundle bundle = ResourceBundle.getBundle(resource, Locale.getDefault(), propertyDistDir);
				if (bundle == null) {
					getLog().warn(String.format("property (%s) is not found", resource));
				}

				PropertyClassMaker classMaker = new PropertyClassMaker(this, bundle);
				try {
					classMaker.write();
				} catch (IOException e) {
					throw new MojoExecutionException("output error", e);
				}
			}
		} catch (RuntimeException e) {
			throw new MojoExecutionException("unexpected exxception", e);
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

		if (!sourceOutputDirectory.isDirectory()) {
			sourceOutputDirectory.mkdirs();
		}
	}

	public void computeResourceDirectoriesClassLoader() {
		List<URL> list = new ArrayList<URL>();

		Log logger = getLog();

		for (Resource res : getResources()) {
			File resourceFile = new File(res.getDirectory());
			logger.debug("load " + resourceFile.getAbsolutePath());
			try {
				list.add(resourceFile.toURI().toURL());
			} catch (MalformedURLException e) {
				getLog().warn("failed to load " + resourceFile.getAbsolutePath());
			}
		}

		File sourceDir = getSourceDirectory();
		logger.debug("load " + sourceDir.getAbsolutePath());
		try {
			list.add(sourceDir.toURI().toURL());
		} catch (MalformedURLException e) {
			getLog().warn("failed to load " + sourceDir.getAbsolutePath());
		}

		resourceDirectoriesClassLoader = new URLClassLoader(list.toArray(new URL[list.size()]));
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
