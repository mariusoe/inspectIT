package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.opentracing.References;
import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.service.rest.error.JsonError;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Marius Oehler
 *
 */
@Controller
@RequestMapping(value = "/spans")
public class RestSpanService {

	@Autowired
	private DefaultDataDao defaultDataDao;

	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public String index() {
		return "{\"msg\": \"hello\"}";
	}

	@RequestMapping(method = POST, value = "")
	@ResponseBody
	public String postSpan(@RequestBody JsonSpan[] jsonSpans) {
		List<AbstractSpan> spans = new ArrayList<>();

		for (JsonSpan jsonSpan : jsonSpans) {
			SpanIdent span = new SpanIdent(jsonSpan.getId(), jsonSpan.getTraceId());

			ServerSpan serverSpan = new ServerSpan();
			serverSpan.setSpanIdent(span);
			serverSpan.setParentSpanId(jsonSpan.getParentId());
			serverSpan.setReferenceType(References.CHILD_OF);
			serverSpan.setTimeStamp(new Timestamp(jsonSpan.getTimestamp()));
			serverSpan.setPlatformIdent(jsonSpan.getAgentId());
			serverSpan.setDuration(jsonSpan.getDuration());

			for (Entry<String, String> entry : jsonSpan.getTags().entrySet()) {
				serverSpan.addTag(entry.getKey(), entry.getValue());
			}

			spans.add(serverSpan);
		}

		defaultDataDao.saveAll(spans);

		return "{\"Added spans count\": " + spans.size() + "}";
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception exception) {
		return new JsonError(exception).asModelAndView();
	}

	public static class JsonSpan {

		@JsonProperty("agentId")
		private long agentId;

		@JsonProperty("traceId")
		private long traceId;

		@JsonProperty("id")
		private long id;

		@JsonProperty("parentId")
		private long parentId;

		@JsonProperty("tags")
		private Map<String, String> tags;

		@JsonProperty("duration")
		double duration;

		@JsonProperty("timestamp")
		long timestamp;

		/**
		 * Gets {@link #duration}.
		 *
		 * @return {@link #duration}
		 */
		public double getDuration() {
			return this.duration;
		}

		/**
		 * Sets {@link #duration}.
		 *
		 * @param duration
		 *            New value for {@link #duration}
		 */
		public void setDuration(double duration) {
			this.duration = duration;
		}

		/**
		 * Gets {@link #timestamp}.
		 *
		 * @return {@link #timestamp}
		 */
		public long getTimestamp() {
			return this.timestamp;
		}

		/**
		 * Sets {@link #timestamp}.
		 *
		 * @param timestamp
		 *            New value for {@link #timestamp}
		 */
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		/**
		 * Gets {@link #tags}.
		 *
		 * @return {@link #tags}
		 */
		public Map<String, String> getTags() {
			return this.tags;
		}

		/**
		 * Sets {@link #tags}.
		 *
		 * @param tags
		 *            New value for {@link #tags}
		 */
		public void setTags(Map<String, String> tags) {
			this.tags = tags;
		}

		/**
		 * Gets {@link #agentId}.
		 *
		 * @return {@link #agentId}
		 */
		public long getAgentId() {
			return this.agentId;
		}

		/**
		 * Sets {@link #agentId}.
		 *
		 * @param agentId
		 *            New value for {@link #agentId}
		 */
		public void setAgentId(long agentId) {
			this.agentId = agentId;
		}

		/**
		 * Gets {@link #traceId}.
		 *
		 * @return {@link #traceId}
		 */
		public long getTraceId() {
			return this.traceId;
		}

		/**
		 * Sets {@link #traceId}.
		 *
		 * @param traceId
		 *            New value for {@link #traceId}
		 */
		public void setTraceId(long traceId) {
			this.traceId = traceId;
		}

		/**
		 * Gets {@link #id}.
		 *
		 * @return {@link #id}
		 */
		public long getId() {
			return this.id;
		}

		/**
		 * Sets {@link #id}.
		 *
		 * @param id
		 *            New value for {@link #id}
		 */
		public void setId(long id) {
			this.id = id;
		}

		/**
		 * Gets {@link #parentId}.
		 *
		 * @return {@link #parentId}
		 */
		public long getParentId() {
			return this.parentId;
		}

		/**
		 * Sets {@link #parentId}.
		 *
		 * @param parentId
		 *            New value for {@link #parentId}
		 */
		public void setParentId(long parentId) {
			this.parentId = parentId;
		}

	}
}
