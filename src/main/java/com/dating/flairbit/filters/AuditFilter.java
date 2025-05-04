package com.dating.flairbit.filters;

import com.dating.flairbit.models.Audit;
import com.dating.flairbit.repo.AuditRepository;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import com.dating.flairbit.utils.encryption.EncryptionUtil;
import com.dating.flairbit.utils.basic.HeadersUtility;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

//@Component
//@Order(2)

public class AuditFilter implements Filter {

    @Value("${app.security.secret-key}")
    private String secretKey;

    private final AuditRepository auditRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuditFilter.class);

    /**
     * Constructor to initialize the filter with the required dependencies.
     *
     * @param auditRepository the repository to save audit data
     */
    public AuditFilter(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * This method intercepts the HTTP request and response, wraps them to cache the content,
     * and then processes the filter chain. After the request is processed, it saves the audit data
     * and ensures that the response body is written back to the client.
     *
     * @param servletRequest  the incoming HTTP request
     * @param servletResponse the outgoing HTTP response
     * @param filterChain     the filter chain to pass the request and response to the next filter
     * @throws ServletException if an error occurs during request processing
     * @throws IOException      if an error occurs during request/response handling
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (ServletException | IOException e) {
            logger.error(e.getMessage());
        } finally {
            saveAuditData(requestWrapper, responseWrapper);
            try {
                responseWrapper.copyBodyToResponse();
                response.flushBuffer();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Saves the audit data to the database by extracting the necessary information from the request
     * and response wrappers, encrypting the data where appropriate, and saving it via the repository.
     *
     * @param requestWrapper  the wrapped HTTP request containing cached content
     * @param responseWrapper the wrapped HTTP response containing cached content
     */
    private void saveAuditData(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) {
        Audit audit = Audit.builder().build();

        try {
            int statusCode = responseWrapper.getStatus();
            String responseHeaders = HeadersUtility.extractResponseHeaders(responseWrapper);
            String responseBody = new String(responseWrapper.getContentAsByteArray());
            String requestBody = requestWrapper.getContentAsString();
            String requestHeaders = HeadersUtility.extractRequestHeaders(requestWrapper);

            audit.setTimestamp(DefaultValuesPopulator.getCurrentTimestamp());
            audit.setMethodName(requestWrapper.getMethod());
            audit.setRequest(EncryptionUtil.encrypt(requestHeaders + "\n" + "Request Body: " + requestBody, secretKey));
            audit.setResponse(responseHeaders + "\n" + "Response Body: " + responseBody);
            audit.setStatus(String.valueOf(statusCode));
            audit.setUri(requestWrapper.getRequestURI());

        } catch (Exception e) {
            logger.error("Error saving audit data: {}", e.getMessage());
        }

        auditRepository.save(audit);
    }
}
