package com.github.jrhee17;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.logging.RequestLog;
import com.linecorp.armeria.server.logging.AccessLogWriter;

public class CustomAccessLogWriter implements AccessLogWriter {

    private static final Logger ACCESS_LOGGER = LogManager.getLogger("AccessLogger");
    private static final String ACCESS_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String HEALTH_CHECK_PATH = "/actuator/healthcheck/";

    @Override
    public void log(RequestLog log) {
        String clientIp =
                Optional.ofNullable(log.context().remoteAddress())
                        .map(InetSocketAddress::getAddress)
                        .map(InetAddress::getHostAddress)
                        .orElse("-");
        String method = log.requestHeaders().method().name();
        String path = log.requestHeaders().path();
        String protocol = log.sessionProtocol().uriText();
        int statusCode = log.responseHeaders().status().code();
        long contentLength = log.responseHeaders().contentLength();
        String userAgent = log.requestHeaders().get("user-agent", "-");
        String referer = log.requestHeaders().get("referer", "-");
        String clientAuthInfo = getClientAuthInfo(log);
        String userId = getUserId(log.context());
        String logMessage =
                String.format(
                        "%s %s %s [%s] \"%s %s %s\" %d %d \"%s\" \"%s\"",
                        clientIp,
                        clientAuthInfo,
                        userId,
                        "",
                        method,
                        path,
                        protocol,
                        statusCode,
                        contentLength,
                        referer,
                        userAgent);

        if (ACCESS_LOGGER.isDebugEnabled()) {
            ACCESS_LOGGER.debug(logMessage);
        } else if (!path.contains(HEALTH_CHECK_PATH)) {
            ACCESS_LOGGER.info(logMessage);
        }
    }

    private String getClientAuthInfo(RequestLog log) {
        return "-";
    }

    private String getUserId(RequestContext ctx) {
        return "-";
    }
}

