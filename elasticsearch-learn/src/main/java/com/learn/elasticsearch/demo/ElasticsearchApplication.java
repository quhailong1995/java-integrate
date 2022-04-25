package com.learn.elasticsearch.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * description......
 *
 * @author yeeee
 * @since 2022/4/25 22:38
 */
@SpringBootApplication
public class ElasticsearchApplication {

    public static void main(String[] args) {
        new SpringApplication(ElasticsearchApplication.class).run(args);
    }

}
