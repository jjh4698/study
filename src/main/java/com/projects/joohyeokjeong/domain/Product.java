package com.projects.joohyeokjeong.domain;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 정주혁 (joohyeok.jeong@navercorp.com)
 */
@RedisHash("products")
@Setter
@ToString
public class Product {
    @Id
    private String id;

    private String name;

    private Long price;

    private Long quantity;

}
