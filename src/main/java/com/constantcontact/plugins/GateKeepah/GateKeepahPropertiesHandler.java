package com.constantcontact.plugins.GateKeepah;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jenkinsci.remoting.RoleChecker;

import com.constantcontact.plugins.GateKeepah.exceptionHandling.GateKeepahException;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

public class GateKeepahPropertiesHandler implements FilePath.FileCallable<Map<String, String>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String propertiesFilePath;
	private String propertiesContent;

	public GateKeepahPropertiesHandler(final String propertiesFilePath, final String propertiesContent) {
		this.propertiesFilePath = propertiesFilePath;
		this.propertiesContent = propertiesContent;
	}

	public Map<String, String> invoke(File base, VirtualChannel channel) throws IOException, InterruptedException {
		Properties props;

		try {
			if (null == getPropertiesFilePath()) {
				throw new Exception("No file name found, creating our own");
			}
			File propsFile = new File(getPropertiesFilePath());
			InputStream input = new FileInputStream(propsFile);
			props = new Properties();
			props.load(input);

			if (null != getPropertiesContent() && !getPropertiesContent().isEmpty()) {
				for (String line : getPropertiesContent().split(System.getProperty("line.separator"))) {
					String[] newProperty = line.split("=");
					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						throw new GateKeepahException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(getPropertiesFilePath());
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					throw new GateKeepahException(fnfe.getMessage());
				} catch (IOException ioe) {
					throw new GateKeepahException(ioe.getMessage());
				}
			}
		} catch (Exception e) {
			if (e instanceof GateKeepahException) {
				throw new GateKeepahException(e.getMessage());
			}

			props = new Properties();
			if (null != getPropertiesContent() && !getPropertiesContent().isEmpty()) {
				for (String line : getPropertiesContent().split(System.getProperty("line.separator"))) {
					if (line.length() < 1) {
						throw new GateKeepahException(
								"A properties file must be in the right place or properties added to the text area");
					}
					String[] newProperty = line.split("=");

					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						throw new GateKeepahException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(getPropertiesFilePath() + File.separator + "gatekeepah.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					throw new GateKeepahException(fnfe.getMessage());
				} catch (IOException ioe) {
					throw new GateKeepahException(ioe.getMessage());
				}
			}

		}
		Map<String, String> map = new HashMap<String,String>();
		for (final String name : props.stringPropertyNames()) {
			map.put(name, props.getProperty(name));
		}
		return map;

	}

	@Override
	public void checkRoles(RoleChecker arg0) throws SecurityException {
		// TODO Auto-generated method stub

	}

	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}

	public void setPropertiesFilePath(String propertiesFilePath) {
		this.propertiesFilePath = propertiesFilePath;
	}

	public String getPropertiesContent() {
		return propertiesContent;
	}

	public void setPropertiesContent(String propertiesContent) {
		this.propertiesContent = propertiesContent;
	}

}
