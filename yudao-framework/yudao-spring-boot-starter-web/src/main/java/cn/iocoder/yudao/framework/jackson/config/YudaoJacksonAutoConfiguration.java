package cn.iocoder.yudao.framework.jackson.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.json.databind.NumberSerializer;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeDeserializer;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@AutoConfiguration
@Slf4j
public class YudaoJacksonAutoConfiguration {

    // 配置SpringBoot默认的jackson配置, 也可以通过springboot的配置文件去配置(spring.jackson.*)
    // 这个地方有个巧思, 本来Utils类是不用实例化到容器中的, 当中的方法都是静态方法, 但是这里想将两件事合在一起做
    // 1. 配置springboot中jackson的序列化和反序列化
    // 2. 让自己的jsonUtils工具类中序列化和反序列化与spring中的保持一致
    // 技术上这里通过将自己注册到容器中, 由于自己依赖List<ObjectMapper>实例, 所以spring容器会按照类型注入实例
    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public JsonUtils jsonUtils(List<ObjectMapper> objectMappers) {
        // 1.1 创建 SimpleModule 对象
        SimpleModule simpleModule = new SimpleModule();
        simpleModule
                // 新增 Long 类型序列化规则，数值超过 2^53-1，在 JS 会出现精度丢失问题，因此 Long 自动序列化为字符串类型
                .addSerializer(Long.class, NumberSerializer.INSTANCE)
                .addSerializer(Long.TYPE, NumberSerializer.INSTANCE)
                .addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE)
                .addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE)
                .addSerializer(LocalTime.class, LocalTimeSerializer.INSTANCE)
                .addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE)
                // 新增 LocalDateTime 序列化、反序列化规则，使用 Long 时间戳
                .addSerializer(LocalDateTime.class, TimestampLocalDateTimeSerializer.INSTANCE)
                .addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        // 1.2 注册到 objectMapper
        objectMappers.forEach(objectMapper -> objectMapper.registerModule(simpleModule));

        // 2. 设置 objectMapper 到 JsonUtils
        JsonUtils.init(CollUtil.getFirst(objectMappers));
        log.info("[init][初始化 JsonUtils 成功]");
        return new JsonUtils();
    }

}
