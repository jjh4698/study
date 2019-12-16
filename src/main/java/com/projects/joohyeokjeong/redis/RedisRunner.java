package com.projects.joohyeokjeong.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.projects.joohyeokjeong.domain.Product;
import com.projects.joohyeokjeong.domain.ProductRepository;

/**
 * @author 정주혁 (joohyeok.jeong@navercorp.com)
 */
@Component
public class RedisRunner implements ApplicationRunner {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    private AtomicLong atomicLong = new AtomicLong();

    private Consumer<Runnable> rejectedExecutionHandler = Runnable::run;


    private static final String KEY = "1";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for(int i=0; i<10; i++){
            execute(KEY, makeRunnableThread());
        }
    }

    private Runnable makeRunnableThread(){
        return () -> {
            makeProduct();
            System.out.println("product save" + atomicLong.addAndGet(1));
        };
    }

    private Product makeProduct(){
        Product product = new Product();
        product.setName("신상품반짝반짝");
        product.setPrice(2000L);
        product.setQuantity(2L);
        return product;
    }

    public void execute(String lockKey, Runnable process) throws InterruptedException {
        if (lockKey == null) {
            return;
        }

        if (makeRedisLock(lockKey)) {
            try {
                process.run();
            } finally {
                release(lockKey);
            }
        } else {
            System.out.println("get lock Failed. start retry.");
            rejectedExecutionHandler.accept(process);
        }

    }


    // 10개의 쓰레드가 대기한다.
    private boolean makeRedisLock(String lockName) throws InterruptedException {
        try {
            while (true) {
                Boolean acquire = redisTemplate.opsForValue().setIfAbsent(lockName, String.valueOf(System.currentTimeMillis()));

                if (acquire) {
                    System.out.println("3 second later expire!");
                    redisTemplate.expire(lockName, 3000, TimeUnit.MILLISECONDS);
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }
    }

    private void release(String lockName){
        redisTemplate.delete(lockName);
    }

}
