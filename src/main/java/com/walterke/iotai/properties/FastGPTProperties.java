package com.walterke.iotai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fastgpt",ignoreInvalidFields = true)
public class FastGPTProperties {
    private String url;
    private String apikey;
}
