package com.learn.elasticsearch.demo.mapping;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * description......
 *
 * @author yeeee
 * @since 2022/4/26 16:01
 */
@Data
public class BaseIndex {
    @Id
    private String id;
}