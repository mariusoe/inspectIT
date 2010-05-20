package info.novatec.inspectit.rcp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * FolderZiper provide a static method to zip a folder.
 * 
 * @author Patrice Bouillet
 */
public class ZipUtil {

	private static final byte[] buffer = new byte[0xFFFF];

	/**
	 * Zip the srcFolder into the destFileZipFile. All the folder subtree of the
	 * src folder is added to the destZipFile archive.
	 * 
	 * @param srcFolder
	 *            the path of the srcFolder
	 * @param destZipFile
	 *            the path of the destination zipFile. This file will be created
	 *            or erased.
	 */
	public static void zipFolder(String srcFolder, String destZipFile) {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		try {
			fileWriter = new FileOutputStream(destZipFile);
			zip = new ZipOutputStream(fileWriter);
			addFolderToZip("", srcFolder, zip, false);
			zip.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (null != zip) {
					zip.close();
				}
			} catch (IOException e) {
				// do not care
			}
		}
	}

	/**
	 * Unzips a file into the target folder.
	 * 
	 * @param zipFile
	 *            the zipped file.
	 * @param targetFolder
	 *            the target folder.
	 */
	public static void unzipFile(String zipFile, String targetFolder) {
		File zip = new File(zipFile);
		if (!zip.exists() || zip.isDirectory()) {
			throw new RuntimeException("Target does not exist / is a directory: " + zip.getAbsolutePath());
		}

		try {
			ZipFile file = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> zipEntryEnum = file.entries();

			while (zipEntryEnum.hasMoreElements()) {
				ZipEntry zipEntry = zipEntryEnum.nextElement();
				extractEntry(file, zipEntry, targetFolder);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void extractEntry(ZipFile zf, ZipEntry entry, String destDir) throws IOException {
		File file = new File(destDir, entry.getName());

		if (entry.isDirectory()) {
			file.mkdirs();
		} else {
			new File(file.getParent()).mkdirs();

			InputStream is = null;
			OutputStream os = null;

			try {
				is = zf.getInputStream(entry);
				os = new FileOutputStream(file);

				for (int len; (len = is.read(buffer)) != -1;) {
					os.write(buffer, 0, len);
				}
			} finally {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			}
		}
	}

	/**
	 * Write the content of srcFile in a new ZipEntry, named path+srcFile, of
	 * the zip stream. The result is that the srcFile will be in the path folder
	 * in the generated archive.
	 * 
	 * @param path
	 *            the relative path with the root archive.
	 * @param srcFile
	 *            the absolute path of the file to add
	 * @param zip
	 *            the stream to use to write the given file.
	 * @param includeDir
	 */
	private static void addToZip(String path, String srcFile, ZipOutputStream zip, boolean includeDir) {
		File file = new File(srcFile);
		if (file.isDirectory()) {
			addFolderToZip(path, srcFile, zip, true);
		} else {
			// Transfer bytes from in to out
			int len;
			try {
				FileInputStream in = new FileInputStream(srcFile);
				if (includeDir) {
					zip.putNextEntry(new ZipEntry(path + "/" + file.getName()));
				} else {
					zip.putNextEntry(new ZipEntry(file.getName()));
				}
				while ((len = in.read(buffer)) > 0) {
					zip.write(buffer, 0, len);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * add the srcFolder to the zip stream.
	 * 
	 * @param path
	 *            the relative path with the root archive.
	 * @param srcFile
	 *            the absolute path of the file to add
	 * @param zip
	 *            the stream to use to write the given file.
	 */
	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, boolean includeDir) {
		File file = new File(srcFolder);
		String[] files = file.list();
		for (int i = 0; i < files.length; i++) {
			addToZip(path + "/" + file.getName(), srcFolder + "/" + files[i], zip, includeDir);
		}
	}

}