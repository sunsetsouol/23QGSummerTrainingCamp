package com.yinjunbiao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yinjunbiao.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static ObjectMapper mapper= new ObjectMapper();

    @Test
    void contextLoads() throws JsonProcessingException {
        User user = new User("lee",17);
        String jsonObject = mapper.writeValueAsString(user);
        redisTemplate.opsForValue().set("key",jsonObject);
        String value = redisTemplate.opsForValue().get("key");
        User jsonUser = mapper.readValue(value, User.class);
        System.out.println(jsonUser);
    }

}
