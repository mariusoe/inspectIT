package info.novatec.inspectit.agent.sensor.method.http;

import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.util.StringConstraint;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpRequestParameterExtractorTest extends AbstractLogSupport {

	private HttpRequestParameterExtractor extractor;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@Mock
	private HttpServletRequest httpServletRequest;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		extractor = new HttpRequestParameterExtractor(new StringConstraint(new HashMap<String, Object>() {
			{
				put("stringLength", "20");
			}
		}));
	}

	@Test
	public void readParameters() {
		final String param1 = "p1";
		final String param2 = "p2";
		final String param1VReal = "I am a really long string that should be cropped in an meaningful way.";
		final String param2VReal1 = "value5";
		final String param2VReal2 = "value6";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>() {
			{
				put(param1, param1V);
				put(param2, param2V);
			}
		};

		when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Map<String, String[]> result = extractor.getParameterMap(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result.size(), parameterMap.size());
		Assert.assertTrue(result.containsKey(param1));
		Assert.assertTrue(result.containsKey(param2));
		Assert.assertTrue(result.get(param1) != (param1V), "Value should be cropped!");
		Assert.assertTrue(result.get(param2) == param2V);
	}

	@Test
	public void readParametersNull() {
		Map<String, String[]> result = extractor.getParameterMap(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, null);
	}

	@Test
	public void readHeaders() {
		final String h1 = "h1";
		final String h2 = "h2";
		final String h1Value = "hValue1";
		final String h2Value = "hValue2";
		final Vector<String> headersList = new Vector<String>() {
			{
				add(h1);
				add(h2);
			}
		};
		final Enumeration<String> headers = headersList.elements();

		when(httpServletRequest.getHeaderNames()).thenReturn(headers);
		when(httpServletRequest.getHeader(h1)).thenReturn(h1Value);
		when(httpServletRequest.getHeader(h2)).thenReturn(h2Value);

		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, new HashMap<String, String>() {
			{
				put(h1, h1Value);
				put(h2, h2Value);
			}
		});

		// We only create a new instance of the element if we need to change it (e.g. crop)
		Assert.assertTrue(result.get(h1) == h1Value);
		Assert.assertTrue(result.get(h2) == h2Value);
	}

	@Test
	public void readHeadersCrop() {
		final String h1 = "h1";
		final String h2 = "h2";
		// this will be cropped!
		final String h1Value = "hValue1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		final String h2Value = "hValue2";
		final Vector<String> headersList = new Vector<String>() {
			{
				add(h1);
				add(h2);
			}
		};
		final Enumeration<String> headers = headersList.elements();

		when(httpServletRequest.getHeaderNames()).thenReturn(headers);
		when(httpServletRequest.getHeader(h1)).thenReturn(h1Value);
		when(httpServletRequest.getHeader(h2)).thenReturn(h2Value);

		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertTrue(result.size() == 2);

		// We only create a new instance of the element if we need to change it (e.g. crop)
		Assert.assertTrue(result.get(h1) != h1Value);
		Assert.assertTrue(result.get(h2) == h2Value);
	}

	@Test
	public void readHeadersNull() {
		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, null);
	}

	@Test
	public void readAttributes() {
		final String att1 = "a1";
		final String att2 = "a2";
		final String att1Value = "aValue1";
		final String att2Value = "aValue2";
		final Vector<String> attributesList = new Vector<String>() {
			{
				add(att1);
				add(att2);
			}
		};
		final Enumeration<String> attributes = attributesList.elements();

		when(httpServletRequest.getAttributeNames()).thenReturn(attributes);
		when(httpServletRequest.getAttribute(att1)).thenReturn(att1Value);
		when(httpServletRequest.getAttribute(att2)).thenReturn(att2Value);

		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, new HashMap<String, String>() {
			{
				put(att1, att1Value);
				put(att2, att2Value);
			}
		});

		// We only create a new instance of the element if we need to change it (e.g. crop)
		Assert.assertTrue(result.get(att1) == att1Value);
		Assert.assertTrue(result.get(att2) == att2Value);
	}

	@Test
	public void readAttributesNull() {
		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, null);
	}

	@Test
	public void readRequestUri() {
		final String uri = "URI";
		when(httpServletRequest.getRequestURI()).thenReturn(uri);

		String result = extractor.getRequestUri(httpServletRequest.getClass(), httpServletRequest);
		Assert.assertEquals(result, uri);
		Assert.assertTrue(uri == result);
	}

	@Test
	public void readRequestUriNull() {
		String result = extractor.getRequestUri(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, HttpTimerData.UNDEFINED);
	}

	@Test
	public void readRequestMethod() {
		final String method = "GET";
		when(httpServletRequest.getMethod()).thenReturn(method);

		String result = extractor.getRequestMethod(httpServletRequest.getClass(), httpServletRequest);
		Assert.assertEquals(result, method);
		Assert.assertTrue(method == result);
	}

	@Test
	public void readRequestMethodNull() {
		String result = extractor.getRequestMethod(httpServletRequest.getClass(), httpServletRequest);

		Assert.assertEquals(result, HttpTimerData.UNDEFINED);
	}

	@Test
	public void sessionAttributesWithNoSession() {
		when(httpServletRequest.getSession(false)).thenReturn(null);

		Map<String, String> result = extractor.getSessionAttributes(httpServletRequest.getClass(), httpServletRequest);
		Assert.assertEquals(result, null);
	}

	@Test
	public void sessionAttributesWithSession() {

		final String sa1 = "sa1";
		final String sa2 = "sa2";
		final String sa1Value = "saValue1";
		final String sa2Value = "saValue2";
		final Vector<String> sessionAttributesList = new Vector<String>() {
			{
				add(sa1);
				add(sa2);
			}
		};
		final Enumeration<String> sessionAttributes = sessionAttributesList.elements();

		HttpSession session = Mockito.mock(HttpSession.class);
		when(session.getAttributeNames()).thenReturn(sessionAttributes);
		when(session.getAttribute(sa1)).thenReturn(sa1Value);
		when(session.getAttribute(sa2)).thenReturn(sa2Value);
		when(httpServletRequest.getSession(false)).thenReturn(session);

		Map<String, String> result = extractor.getSessionAttributes(httpServletRequest.getClass(), httpServletRequest);
		Assert.assertEquals(result, new HashMap<String, String>() {
			{
				put(sa1, sa1Value);
				put(sa2, sa2Value);
			}
		});
	}
}
