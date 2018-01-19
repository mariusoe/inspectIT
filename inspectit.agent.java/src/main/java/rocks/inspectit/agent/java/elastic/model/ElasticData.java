package rocks.inspectit.agent.java.elastic.model;

import java.util.List;

/**
 *
 * APM Server data model.
 *
 * @author Marius Oehler
 *
 */
public class ElasticData {
	public App app;
	public List<Transaction> transactions;

	public static class App {
		public String name;
		public Agent agent;
	}

	public static class Agent {
		public String name;
		public String version;
	}

	public static class Transaction {
		public String id;
		public String name;
		public String type;
		public double duration;
		public String timestamp;
		public String result;
		public List<Trace> traces;
		public Context context;
	}

	public static class Trace {
		public Integer id;
		public Integer parent;
		public String name;
		public String type;
		public double start;
		public double duration;
	}

	public static class Context {
		public Request request;
	}

	public static class Request {
		public String method;
		public Url url;
	}

	public static class Url {
		public String raw;
	}
}
