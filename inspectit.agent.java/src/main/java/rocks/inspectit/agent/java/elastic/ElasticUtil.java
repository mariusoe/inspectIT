package rocks.inspectit.agent.java.elastic;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import rocks.inspectit.agent.java.config.impl.ConfigurationStorage;
import rocks.inspectit.agent.java.elastic.model.ElasticData;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Agent;
import rocks.inspectit.agent.java.elastic.model.ElasticData.App;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Context;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Request;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Trace;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Transaction;
import rocks.inspectit.agent.java.elastic.model.ElasticData.Url;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData.MutableInt;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Utility class for creating Json which is compatible to the APM Server.
 *
 * @author Marius Oehler
 *
 */
@Component
public class ElasticUtil {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The agents configuration.
	 */
	@Autowired
	private ConfigurationStorage configuration;

	private final Gson gson = new Gson();

	private String elasticHost;

	// not used now
	private String elasticToken;

	@PostConstruct
	public void postConstruct() {
		try {
			elasticHost = System.getProperty("elastic-apm.host");
		} catch (Exception e) {
			elasticHost = null;
		}
		try {
			elasticToken = System.getProperty("elastic-apm.token");
		} catch (Exception e) {
			elasticToken = null;
		}

		log.info("|- Using following Elastic APM Server: {}", elasticHost);
	}

	public boolean send(ElasticData data) {
		String json = gson.toJson(data);

		if (elasticHost == null) {
			log.info(json);
		} else {
			try {
				URL url = new URL("http://" + elasticHost + "/v1/transactions");
				URLConnection con = url.openConnection();
				HttpURLConnection http = (HttpURLConnection) con;
				http.setRequestMethod("POST");
				http.setDoOutput(true);

				if (elasticToken != null) {
					http.setRequestProperty("Authorization", "Bearer " + elasticToken);
				}

				byte[] out = json.getBytes(Charset.forName("UTF-8"));
				int length = out.length;

				http.setFixedLengthStreamingMode(length);
				http.setRequestProperty("Content-Type", "application/json");
				http.connect();

				http.getOutputStream().write(out);

				http.getOutputStream().close();

				if (http.getResponseCode() != 202) {
					log.info("Elastic-APM response: {}", http.getResponseCode());
				} else {
					// successful
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			TimeZone tz = TimeZone.getTimeZone("CET");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			tz.setRawOffset(0); // fix
			df.setTimeZone(tz);

			return df;
		};
	};

	public ElasticData createElasticData() {
		ElasticData data = new ElasticData();

		data.app = new App();
		data.app.name = configuration.getAgentName();
		data.app.agent = new Agent();
		data.app.agent.name = "inspectit-java";
		data.app.agent.version = "0.0.1";

		data.transactions = new LinkedList<Transaction>();

		return data;
	}

	public void add(ElasticData elasticData, InvocationSequenceData isData) {
		Transaction transaction = new Transaction();
		elasticData.transactions.add(transaction);

		HttpTimerData httpData = (HttpTimerData) isData.getTimerData();
		transaction.id = UUID.randomUUID().toString();

		transaction.name = httpData.getHttpInfo().getRequestMethod() + " " + httpData.getHttpInfo().getUri();

		transaction.type = "Request";
		transaction.duration = isData.getDuration();
		transaction.timestamp = dateFormat.get().format(isData.getTimeStamp());
		transaction.result = String.valueOf(httpData.getHttpResponseStatus());

		transaction.context = new Context();
		transaction.context.request = new Request();
		transaction.context.request.method = httpData.getHttpInfo().getRequestMethod();
		transaction.context.request.url = new Url();
		transaction.context.request.url.raw = httpData.getHttpInfo().getUrl();

		transaction.traces = new LinkedList<Trace>();

		MutableInt counter = new MutableInt(0);

		findTraces(transaction.traces, isData, -1, counter, isData.getStart());
	}

	private void findTraces(List<Trace> traces, InvocationSequenceData isData, int parentId, MutableInt counter, double timeOffset) {
		Trace trace = new Trace();

		counter.add(1);
		trace.id = counter.getValue();
		trace.parent = parentId < 0 ? null : parentId;

		trace.name = MethodNameMapper.resolve(isData.getMethodIdent());
		trace.type = "method-call";
		trace.duration = isData.getDuration();
		trace.start = isData.getStart() - timeOffset;

		for (InvocationSequenceData nestedData : isData.getNestedSequences()) {
			findTraces(traces, nestedData, trace.id, counter, timeOffset);
		}

		traces.add(trace);
	}

}
