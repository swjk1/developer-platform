package com.leon.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.leon.common.RequestIdFilter;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.Mockito;

/** Section 37: pure logic, no Spring context, runs in milliseconds. */
class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void generatesAnIdWhenTheCallerSuppliedNone() throws Exception {
        MockHttpServletResponse response = invokeWith(null);

        assertThat(response.getHeader(RequestIdFilter.HEADER)).isNotBlank();
    }

    @Test
    void honoursAValidCallerSuppliedId() throws Exception {
        String supplied = UUID.randomUUID().toString();

        MockHttpServletResponse response = invokeWith(supplied);

        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo(supplied);
    }

    @Test
    void replacesAnIdThatIsNotAUuid() throws Exception {
        // Echoing arbitrary client input into log lines is log injection.
        MockHttpServletResponse response = invokeWith("not-a-uuid\nINFO fake log line");

        assertThat(response.getHeader(RequestIdFilter.HEADER))
                .isNotEqualTo("not-a-uuid\nINFO fake log line")
                .matches("[0-9a-f-]{36}");
    }

    private MockHttpServletResponse invokeWith(String header) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
        if (header != null) {
            request.addHeader(RequestIdFilter.HEADER, header);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, Mockito.mock(FilterChain.class));
        return response;
    }
}
