
package br.com.aluguelcarros.config;

import br.com.aluguelcarros.dto.CategoriaVeiculoResponse;
import br.com.aluguelcarros.dto.VeiculoResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        RedisCacheWriter cacheWriter = RedisCacheWriter.create(
                connectionFactory,
                RedisCacheWriter.RedisCacheWriterConfigurer::immediateWrites
        );

        JsonMapper jsonMapper = JsonMapper.builder().build();

        JavaType categoriasType = jsonMapper.getTypeFactory()
                .constructCollectionType(
                        List.class,
                        CategoriaVeiculoResponse.class
                );

        JavaType veiculosType = jsonMapper.getTypeFactory()
                .constructCollectionType(
                        List.class,
                        VeiculoResponse.class
                );

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues();

        RedisCacheConfiguration categoriasConfig = defaultConfig
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(
                                jsonMapper,
                                categoriasType
                        )
                ));

        RedisCacheConfiguration veiculosConfig = defaultConfig
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(
                                jsonMapper,
                                veiculosType
                        )
                ));

        RedisCacheConfiguration veiculoConfig = defaultConfig
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(
                                jsonMapper,
                                VeiculoResponse.class
                        )
                ));

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        "categorias", categoriasConfig,
                        "veiculos", veiculosConfig,
                        "veiculo", veiculoConfig
                ))
                .build();
    }
}
