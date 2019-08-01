package solutions.trsoftware.commons.server.servlet.gwt;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.InputStreamTooLongException;
import solutions.trsoftware.commons.server.io.StringInputStream;
import solutions.trsoftware.commons.server.servlet.testutil.*;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Unit-tests {@link BaseRpcServlet} without loading it into a servlet container.
 * <p>
 * For the "full-stack" client-side test, see {@link solutions.trsoftware.commons.client.server.BaseRpcServletTest}
 *
 * @author Alex
 * @since 7/29/2019
 */
public class BaseRpcServletTest extends TestCase {

  public void testReadContent() throws Exception {
    // 1) test an instance with no init-params given (its readContent method should not throw an exception)
    {
      BaseRpcServlet servlet = newInstance(Collections.emptyMap());
      assertEquals(0L, servlet.getMaxContentLength());
      String payload = RandomUtils.randString(1000);  // random dummy string (NOTE: the servlet won't be deserialize it as a GWT RPCRequest)
      String readContent = servlet.readContent(createMockRequest(payload));
      assertEquals(readContent, payload);
    }
    // 2) test an instance with a "maxContentLength" init-param (its readContent method should throw an exception if POST body exceeds this length)
    {
      long maxContentLength = 1024;
      BaseRpcServlet servlet = newInstance(MapUtils.stringMap(BaseRpcServlet.INIT_PARAM_MAX_CONTENT_LENGTH, String.valueOf(maxContentLength)));
      assertEquals(maxContentLength, servlet.getMaxContentLength());
      // a) test a request with payload size <= the limit (should succeed)
      {
        String payload = RandomUtils.randString((int)maxContentLength);  // random dummy string (NOTE: the servlet won't be deserialize it as a GWT RPCRequest)
        String readContent = servlet.readContent(createMockRequest(payload));
        assertEquals(readContent, payload);
      }
      // b) test a request with payload size > the limit (should throw exception)
      {
        String payload = RandomUtils.randString((int)maxContentLength+1);  // random dummy string (NOTE: the servlet won't be deserialize it as a GWT RPCRequest)
        ServletException ex = AssertUtils.assertThrows(ServletException.class, (Function0_t<Throwable>)()
            -> servlet.readContent(createMockRequest(payload)));
        // verify the cause of the exception
        InputStreamTooLongException cause = (InputStreamTooLongException)ex.getRootCause();
        assertEquals(maxContentLength, cause.getInputLengthLimit());
      }
    }
  }

  /**
   * @return a new instance initialized with the given init-params.
   */
  private BaseRpcServlet newInstance(Map<String, String> initParams) throws ServletException {
    BaseRpcServlet servlet = new BaseRpcServlet();
    servlet.init(new DummyServletConfig(initParams, new DummyServletContext()));
    return servlet;
  }

  private DummyHttpServletRequest createMockRequest(String postBody) {
    return new DummyHttpServletRequest() {
      private ServletInputStreamAdapter inputStream = new ServletInputStreamAdapter(
          new StringInputStream(postBody, RPCServletUtils.CHARSET_UTF8));

      @Override
      public ServletInputStream getInputStream() throws IOException {
        return inputStream;
      }

      @Override
      public String getContentType() {
        return RPCServletUtils.GWT_RPC_CONTENT_TYPE;
      }

      @Override
      public String getCharacterEncoding() {
        return RPCServletUtils.CHARSET_UTF8_NAME;
      }
    };
  }

  /**
   * Tests that {@link BaseRpcServlet#doUnexpectedFailure(Throwable)} properly sets the response status
   * code if the exception was caused by {@link InputStreamTooLongException}.
   */
  public void testDoUnexpectedFailure() throws Exception {

    long maxContentLength = 1024;
    BaseRpcServlet servlet = newInstance(MapUtils.stringMap(BaseRpcServlet.INIT_PARAM_MAX_CONTENT_LENGTH, String.valueOf(maxContentLength)));
    assertEquals(maxContentLength, servlet.getMaxContentLength());

    // 1) normal exception that would be caused by GWT attempting to deserialize a random string that doesn't represent a valid RPC request
    {
      String payload = RandomUtils.randString((int)maxContentLength);  // random dummy string (NOTE: the servlet won't be deserialize it as a GWT RPCRequest)
      DummyHttpServletRequest request = createMockRequest(payload);
      DummyHttpServletResponse response = new DummyHttpServletResponse();
      servlet.doPost(request, response);
      // expected response status: 500
      verifyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "text/plain");
    }

    // 2) InputStreamTooLongException thrown by BaseRpcServlet.readConent()
    {
      String payload = RandomUtils.randString((int)maxContentLength+1);  // random dummy string (NOTE: the servlet won't be deserialize it as a GWT RPCRequest)
      DummyHttpServletRequest request = createMockRequest(payload);
      DummyHttpServletResponse response = new DummyHttpServletResponse();
      servlet.doPost(request, response);
      // expected response status: 413
      verifyResponse(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "text/plain");
    }
  }

  private void verifyResponse(DummyHttpServletResponse response, int expectedStatus, String expectedContentType) {
    assertEquals(expectedStatus, response.getStatus());
    assertEquals(expectedContentType, response.getContentType());
    System.out.printf("Received a %d response with %s content:%n%s%n", response.getStatus(), response.getContentType(), response.getOutputAsString());
  }

}