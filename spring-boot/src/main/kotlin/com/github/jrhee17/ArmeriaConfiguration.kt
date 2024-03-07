package com.github.jrhee17

import com.linecorp.armeria.server.healthcheck.HealthChecker
import com.linecorp.armeria.server.tomcat.TomcatService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ArmeriaConfiguration {
    /**
     * Returns a new [HealthChecker] that marks the server as unhealthy when Tomcat becomes unavailable.
     */
    @Bean
    open fun tomcatConnectorHealthChecker(applicationContext: ServletWebServerApplicationContext): HealthChecker {
        val connector = getConnector(applicationContext)
        return HealthChecker { connector.state.isAvailable }
    }

    /**
     * Returns a new [TomcatService] that redirects the incoming requests to the Tomcat instance
     * provided by Spring Boot.
     */
    @Bean
    open fun tomcatService(applicationContext: ServletWebServerApplicationContext): TomcatService {
        return TomcatService.of(getConnector(applicationContext))
    }

    /**
     * Returns a new [ArmeriaServerConfigurator] that is responsible for configuring a [Server]
     * using the given [ServerBuilder].
     */
    @Bean
    open fun armeriaServiceInitializer(tomcatService: TomcatService): ArmeriaServerConfigurator {
        return ArmeriaServerConfigurator { sb -> sb.serviceUnder("/", tomcatService) }
    }

    companion object {
        /**
         * Extracts a Tomcat [Connector] from Spring webapp context.
         */
        fun getConnector(applicationContext: ServletWebServerApplicationContext): Connector {
            val container = applicationContext.webServer as TomcatWebServer

            // Start the container to make sure all connectors are available.
            container.start()
            return container.tomcat.getConnector()
        }
    }
}