package br.com.tarefa.config.cache;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

@Configuration
public class CacheConfig {

	@Value("${applcation.cache.tokenblacklist.expiration}")
	private long tokenJwtExpiration;

	@Bean
	RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder.withCacheConfiguration("blacklistedTokens",
				RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMillis(tokenJwtExpiration)));
	}

}
