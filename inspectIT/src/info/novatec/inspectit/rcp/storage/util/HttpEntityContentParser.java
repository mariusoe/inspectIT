package info.novatec.inspectit.rcp.storage.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

/**
 * {@link HttpEntityContentParser} can extract the byte content of the {@link HttpEntity}. This
 * class uses {@link EntityUtils} class to extract the bytes if the HTTP response is not
 * multipart/byterange response. However of the response has multipart/byterange status it manually
 * extract the bytes from each part. Extracting of response is done by HTTP1.1 RFC 2068 standard
 * specifications (http://www.freesoft.org/CIE/RFC/2068/225.htm).
 * <p>
 * Note that this class is only tested with Jetty 6.x server and responses given by its
 * DefaultServlet class. This class maybe not work with other servers or responses given by any
 * other servlet on Jetty server. Thus, this class should not be generally used.
 * 
 * @author Ivan Senic
 * 
 */
public final class HttpEntityContentParser {

	/**
	 * Key to find the boundary in the Content-Type of the HTTP response.
	 */
	private static final String BOUNDARY_KEY = "boundary=";

	/**
	 * Sequence of chars that in combination with boundary word separate the parts in the
	 * multipart/byterange response.
	 */
	private static final String BOUNDARY_SEPARATOR = "--";

	/**
	 * New line in the HTTP response.
	 */
	private static final String NEW_LINE = "\r\n";

	/**
	 * Private constructor.
	 */
	private HttpEntityContentParser() {
	}

	/**
	 * Returns the byte content for a given {@link HttpEntity}. See
	 * {@link #HttpEntityContentParser()} documentation for more information about this method.
	 * 
	 * @param httpEntity
	 *            Http entity that contains the data gotten from a Http request.
	 * @return {@link ByteBuffer} containing only data bytes.
	 * @throws IOException
	 *             If {@link IOException} is thrown.
	 */
	public static ByteBuffer getByteContent(HttpEntity httpEntity) throws IOException {
		// check if it is multipart response
		if (isMultipart(httpEntity)) {

			// extract the boundary word from the Content-Type
			String boundary = BOUNDARY_SEPARATOR + getBoundary(httpEntity);

			// read the complete response byte content
			ByteArrayBuffer contentBuffer = readContent(httpEntity, boundary);

			byte[] boundaryBytes = boundary.getBytes();
			int index = 0;

			byte[] newLineBytes = NEW_LINE.getBytes();
			List<StartEndMarker> startEndMarkerList = new ArrayList<StartEndMarker>();
			int totalBytes = 0;
			while (index < contentBuffer.length()) {
				// find part start
				int partStart = indexOf(contentBuffer.buffer(), boundaryBytes, index) + boundaryBytes.length;
				// find part end
				int partEnd = indexOf(contentBuffer.buffer(), boundaryBytes, partStart);
				if (partEnd == -1) {
					break;
				}

				// where bytes end in the part
				int partBytesEnd = partEnd - newLineBytes.length;

				// find where bytes start in the part
				// they start after the last \r\n in the part
				int partBytesStart = partStart;
				while (true) {
					int newLineEnd = indexOf(contentBuffer.buffer(), newLineBytes, partBytesStart);
					if (newLineEnd < partBytesEnd) {
						partBytesStart = newLineEnd + newLineBytes.length;
					} else {
						break;
					}
				}

				// create marker and add to list
				StartEndMarker startEndMarker = new StartEndMarker(partBytesStart, partBytesEnd);
				startEndMarkerList.add(startEndMarker);
				// add to total bytes count
				totalBytes += partBytesEnd - partBytesStart;

				// move index to the next part start
				index = partEnd;
			}

			// copy only bytes to new array
			byte[] allBytes = new byte[totalBytes];
			int nextCopyPosition = 0;
			for (StartEndMarker marker : startEndMarkerList) {
				System.arraycopy(contentBuffer.buffer(), marker.getStart(), allBytes, nextCopyPosition, marker.getEnd() - marker.getStart());
				nextCopyPosition += marker.getEnd() - marker.getStart();
			}

			// wrap with ByteBuffer
			ByteBuffer buffer = ByteBuffer.wrap(allBytes);
			return buffer;
		} else {
			byte[] content = EntityUtils.toByteArray(httpEntity);
			ByteBuffer buffer = ByteBuffer.wrap(content);
			return buffer;
		}

	}

	/**
	 * Checks if the {@link HttpEntity} holds the multipart/byterange HTTP response.
	 * 
	 * @param httpEntity
	 *            {@link HttpEntity} that holds a response.
	 * @return True if it has the "multipart" marker in the response Content-Type header.
	 */
	private static boolean isMultipart(HttpEntity httpEntity) {
		return httpEntity.getContentType().getValue().indexOf("multipart") != -1;
	}

	/**
	 * Extracts the string that denotes the boundary of the multipart response from Content-Type
	 * headrer.
	 * 
	 * @param httpEntity
	 *            {@link HttpEntity} that holds a response.
	 * @return Boundary word, or null if the Content-Type header does not define it.
	 */
	private static String getBoundary(HttpEntity httpEntity) {
		Header contentTypeHeader = httpEntity.getContentType();
		if (contentTypeHeader == null) {
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(contentTypeHeader.getValue(), ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			int boundaryIndex = token.indexOf(BOUNDARY_KEY);
			if (boundaryIndex != -1) {
				String boundaryString = token.substring(boundaryIndex + BOUNDARY_KEY.length());
				return boundaryString;
			}
		}
		return null;
	}

	/**
	 * Reads the byte content of {@link HttpEntity}. This method is only working if the response is
	 * multipart. This method closes the {@link InputStream} of {@link HttpEntity}.
	 * 
	 * @param httpEntity
	 *            {@link HttpEntity} that holds a response.
	 * @param boundary
	 *            Boundary word that seperates the part. This is needed for recognition of the
	 *            stream end. Otherwise read is blocked until server closes the connection.
	 * @return {@link ByteArrayBuffer} with all read bytes.
	 * @throws IOException
	 *             If {@link IOException} is thrown.
	 */
	private static ByteArrayBuffer readContent(HttpEntity httpEntity, String boundary) throws IOException {
		// endMark bytes should appear at the end of the stream, only stop reading when this is
		// reached
		String endMark = boundary + BOUNDARY_SEPARATOR + NEW_LINE;
		byte[] endBytes = endMark.getBytes();

		// capacity is actually default Jetty buffer size
		int capacity = (int) httpEntity.getContentLength();
		if (capacity == -1) {
			capacity = 8192;
		}
		ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(capacity);

		InputStream inputStream = httpEntity.getContent();
		byte[] content = new byte[capacity];
		try {
			while (true) {
				int read = inputStream.read(content);
				if (read == -1) {
					break;
				}
				byteArrayBuffer.append(content, 0, read);
				if (read <= capacity) {
					if (areBytesAtEnd(byteArrayBuffer, endBytes)) {
						break;
					}
				}
			}
		} finally {
			inputStream.close();
		}

		return byteArrayBuffer;
	}

	/**
	 * Checks if the supplied byte array is at the end of the {@link ByteArrayBuffer}.
	 * 
	 * @param byteArrayBuffer
	 *            {@link ByteArrayBuffer}.
	 * @param endBytes
	 *            Bytes that are serached.
	 * @return True if the supplied bytes are really at the end of the {@link ByteArrayBuffer}.
	 */
	private static boolean areBytesAtEnd(ByteArrayBuffer byteArrayBuffer, byte[] endBytes) {
		int start = byteArrayBuffer.length() - endBytes.length;
		if (start < 0) {
			return false;
		}
		for (int i = 0; i < endBytes.length; i++) {
			if (byteArrayBuffer.byteAt(start + i) != endBytes[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find the next index of the search byte sequence in the byte array source.
	 * 
	 * @param source
	 *            Array to look in.
	 * @param searchSequence
	 *            Searching byte array sequence.
	 * @param startIndex
	 *            Starting from position.
	 * @return Index position, or -1 if it is not found.
	 */
	private static int indexOf(byte[] source, byte[] searchSequence, int startIndex) {
		if (startIndex > source.length) {
			return -1;
		}

		if (startIndex + searchSequence.length > source.length) {
			return -1;
		}

		for (int i = startIndex; i < source.length - searchSequence.length; i++) {
			if (source[i] == searchSequence[0]) {
				boolean found = true;
				int j;
				for (j = 1; j < searchSequence.length; j++) {
					if (source[i + j] != searchSequence[j]) {
						found = false;
						break;
					}
				}
				if (found) {
					return i;
				} else {
					i = i + j - 1;
				}
			}
		}

		return -1;
	}
	
	/**
	 * Simple class to mark the start and end of array.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class StartEndMarker {

		/**
		 * Start.
		 */
		private int start;

		/**
		 * End.
		 */
		private int end;

		/**
		 * Default constructor.
		 * @param start Start value
		 * @param end End value
		 */
		public StartEndMarker(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}

		/**
		 * @return the start
		 */
		public int getStart() {
			return start;
		}

		/**
		 * @return the end
		 */
		public int getEnd() {
			return end;
		}

	}

}
