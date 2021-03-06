package org.example.filter;

import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

@Slf4j
public class CognitoIdentityFilter implements Filter {

    public static final String COGNITO_IDENTITY_ATTRIBUTE = "com.amazonaws.serverless.cognitoId";


    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
        // nothing to do in init
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {
        Object apiGwContext =
                servletRequest.getAttribute(RequestReader.API_GATEWAY_CONTEXT_PROPERTY);
        if (apiGwContext == null) {
            log.warn("API Gateway context is null");
            filterChain.doFilter(servletRequest, servletResponse);
        }
        if (!AwsProxyRequestContext.class.isAssignableFrom(apiGwContext.getClass())) {
            log.warn("API Gateway context object is not of valid type");
            filterChain.doFilter(servletRequest, servletResponse);
        }

        AwsProxyRequestContext ctx = (AwsProxyRequestContext) apiGwContext;
        if (ctx.getIdentity() == null) {
            log.warn("Identity context is null");
            filterChain.doFilter(servletRequest, servletResponse);
        }
        String cognitoIdentityId = ctx.getIdentity().getCognitoIdentityId();
        if (cognitoIdentityId == null || "".equals(cognitoIdentityId.trim())) {
            log.warn("Cognito identity id in request is null");
        }
        servletRequest.setAttribute(COGNITO_IDENTITY_ATTRIBUTE, cognitoIdentityId);
        filterChain.doFilter(servletRequest, servletResponse);
    }


    @Override
    public void destroy() {
        // nothing to do in destroy
    }

}
