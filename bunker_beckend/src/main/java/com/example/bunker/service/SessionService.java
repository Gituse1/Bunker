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

    private String redisTemplate(Long roomId,String userName){
        return "session"+roomId+":"+userName;
    }

    public void updateSession(Long roomId, String userName, Consumer<ProductDTO> update){

        String key = redisTemplate(roomId,userName);
        ProductDTO dto = redisTemplate.opsForValue().get(key);

        if(dto == null){
            dto = new ProductDTO();
        }
        update.accept(dto);
        redisTemplate.opsForValue().set(key,dto);

    }

    public void saveSession(Long roomId, String userName, ProductDTO dto) {
        String key = redisTemplate(roomId, userName);
        redisTemplate.opsForValue().set(key, dto);
    }
    public ProductDTO getSession(Long roomId,String userName){
        return redisTemplate.opsForValue().get(redisTemplate(roomId,userName));
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

    public void deleteSession(Long roomId,String userName){
        redisTemplate.delete(redisTemplate(roomId,userName));
    }
}
