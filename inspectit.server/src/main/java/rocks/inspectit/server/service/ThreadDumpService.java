package rocks.inspectit.server.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

import rocks.inspectit.server.messaging.AgentMessageProvider;
import rocks.inspectit.shared.all.communication.message.ThreadDumpRequestMessage;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IThreadDumpService;

/**
 * @author Marius Oehler
 *
 */
@Service
public class ThreadDumpService implements IThreadDumpService {

	private static final String THREAD_DUMP_DIRECTORY = "thread-dumps";

	@Log
	private Logger log;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	@Autowired
	private AgentMessageProvider messageProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Date, String> getThreadDumps(long platformId) {
		Map<Date, String> threadDumps = new HashMap<>();

		File dumpDirectory = Paths.get(getThreadDumpDirectory().getAbsolutePath(), String.valueOf(platformId)).toFile();
		File[] files = dumpDirectory.listFiles();

		if (files != null) {
			for (File file : files) {
				try {
					Date date = DATE_FORMAT.parse(file.getName());
					String threadDump = Files.toString(file, Charset.forName("UTF-8"));

					threadDumps.put(date, threadDump);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return threadDumps;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestThreadDump(long platformId) {
		log.info("Requesting thread-dump for agent {}", platformId);

		messageProvider.provideMessage(platformId, new ThreadDumpRequestMessage());
	}

	@Override
	public void storeThreadDump(long platformId, String threadDump) {
		File dumpDirectory = getOrCreateThreadDumpDirectory(platformId);

		String fileName = getFileName();

		File outFile = new File(dumpDirectory, fileName);

		try {
			Files.write(threadDump, outFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized String getFileName() {
		String fileName = DATE_FORMAT.format(new Date());
		return fileName + "." + "dump";
	}

	private File getOrCreateThreadDumpDirectory(long platformId) {
		File dumDirectory = Paths.get(getThreadDumpDirectory().getAbsolutePath(), String.valueOf(platformId)).toFile();

		if (!dumDirectory.exists()) {
			dumDirectory.mkdirs();
		}

		return dumDirectory;
	}

	private File getThreadDumpDirectory() {
		return Paths.get(THREAD_DUMP_DIRECTORY).toFile();
	}
}
