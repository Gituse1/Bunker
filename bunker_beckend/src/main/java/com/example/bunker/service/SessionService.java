package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisTemplate<String, ProductDTO> redisTemplate;

    private String redisTemplate(Long roomId,String userEmail){
        return "session"+roomId+":"+userEmail;
    }

    public void updateSession(Long roomId, String userEmail, Consumer<ProductDTO> update){

        String key = redisTemplate(roomId,userEmail);
        ProductDTO dto = redisTemplate.opsForValue().get(key);

        if(dto == null){
            dto = new ProductDTO();
        }
        update.accept(dto);
        redisTemplate.opsForValue().set(key,dto);

    }
    public ProductDTO getSession(Long roomId,String userEmail){
        return redisTemplate.opsForValue().get(redisTemplate(roomId,userEmail));
    }
    public List<ProductDTO> getAllSessionByRoomId( Long roomId){
        String keyPattern = "session"+roomId + ":*";

        Set<String > keys =redisTemplate.keys(keyPattern);

        if(keys.isEmpty()){
            return Collections.emptyList();
        }
        List<ProductDTO> dto = redisTemplate.opsForValue().multiGet(keys);

        if(dto.isEmpty()){
            return Collections.emptyList();
        }
        return dto.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void deleteSession(Long roomId,String userEmail){
        redisTemplate.delete(redisTemplate(roomId,userEmail));
    }
}
