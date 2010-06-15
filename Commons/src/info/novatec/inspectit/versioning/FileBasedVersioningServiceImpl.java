package info.novatec.inspectit.versioning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This utility searches for the versioning file and provides the current
 * version of this release for display.
 * 
 * The version is provided by the release process and is stored as a file 
 * in the classpath.
 * 
 * @author Stefan Siegl
 */
public class FileBasedVersioningServiceImpl implements IVersioningService {
	
	public static final String VERSION_LOG_NAME = "version.log";

	/**
	 * {@inheritDoc}
	 */
	public String getVersion () throws IOException {
		
		// Get a classloader to find the version file
		ClassLoader classLoader = FileBasedVersioningServiceImpl.class.getClassLoader();
		if (null == classLoader) {
			// this means inspectIT was started using the XBootclasspath option and thus all classes are in fact
			// loaded by the system classloader, so we need to use the system classloader
			classLoader = ClassLoader.getSystemClassLoader();
		}
		
		InputStream s = classLoader.getResourceAsStream(VERSION_LOG_NAME);
		
		if (null == s){
			throw new FileNotFoundException("The version information file \""+VERSION_LOG_NAME+"\" cannot be found in the classpath");
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(s));
					
			String versionString = reader.readLine();
			String version = "n/a";
			if (null != versionString && ! "".equals(versionString.trim())) {
				version = versionString;
			}
	
			return version;
			
		} finally {
			try {
				if (null != reader) {
					reader.close();
					reader = null;
					s.close();
					s = null;
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		System.out.println(VersionUtil.getVersion());
//	}
}
