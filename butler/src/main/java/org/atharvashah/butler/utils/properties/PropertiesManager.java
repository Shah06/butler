package org.atharvashah.butler.utils.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesManager {
	
	private static final String PROPERTIES_FILEPATH = "butler/target/classes/butler.properties";
	private static final Logger logger = LogManager.getLogger(PropertiesManager.class.getName());
	private static Object lockObject = new Object();
	private static Properties properties = null;

	private PropertiesManager() {
		
	}

	private static void checkInit() {
		if (null == properties) {
			synchronized(lockObject) {
				if (null == properties) {
				
					// initialize properties
					File f = new File(PropertiesManager.PROPERTIES_FILEPATH);
					logger.info("Loading properties from " + f.getAbsolutePath());
					properties = new Properties();
					try {
						FileInputStream in = new FileInputStream(f);
						properties.load(in);
					} catch (Exception e) {
						logger.error("Failed to property load properties file", e);	
					}

				}
			}	
		}
	}

	public static String get(String key) {
		checkInit();
		return properties.getProperty(key);
	}

	public static void set(String key, String value) {
		checkInit();
		properties.setProperty(key, value);
	}

}