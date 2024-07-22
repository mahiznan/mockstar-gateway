package com.span.mockstar_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GlobalTargetFilter implements GlobalFilter, Ordered {

    final Logger logger = LoggerFactory.getLogger(GlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering global pre filter");
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(originalRequest -> {
                    HttpRequest request = exchange.getRequest();
                    String targetPath = Objects.requireNonNull(request.getHeaders().getFirst("X-Content-Target-Path"));
                    originalRequest.uri(
                            UriComponentsBuilder.fromUri(request
                                            .getURI())
                                    .path(targetPath)
                                    .build()
                                    .toUri());
                })
                .build();
        return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> logger.debug("Entering global post filter")));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
