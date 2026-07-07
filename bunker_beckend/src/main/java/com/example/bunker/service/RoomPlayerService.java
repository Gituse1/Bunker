package com.example.bunker.service;

import com.example.bunker.dto.ProductDTO;
import com.example.bunker.dto.Room.RoomDataResponse;
import com.example.bunker.model.*;
import com.example.bunker.projection.PlayerProjection;
import com.example.bunker.repository.EffectRepository;
import com.example.bunker.repository.PlayerRepository;
import com.example.bunker.repository.RoomPlayerRepository;
import com.example.bunker.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository  playerRepository;
    private final EffectRepository effectRepository;

    private final PlayerService  playerService;
    private final SessionService  sessionService;

    private final AuthService authService;

    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public RoomDataResponse connectToGame(String codeToConnect) {

        String userName =authService.getCurrentUserName();

        log.info("user : "+userName+"connectToGame by code : "+codeToConnect);


        Room room =roomRepository.findRoomByCodeToConnect(codeToConnect).orElseThrow(
                ()->new EntityNotFoundException("Code is not valid or something went wrong"));


        //Шукаємо чи вже існує створювався герой до цієї кімнати.
        RoomPlayer roomPlayer =roomPlayerRepository.findPlayerByRoomPlayerId(room.getId(),userName)
                .orElseGet(() ->{           //Якщо не знайшли то створюємо нового героя
                    RoomPlayer newRoomPlayer = createRoomPlayer(room);
                    newRoomPlayer.setPlayer(playerService.createPlayer(room.getId()));
                    return newRoomPlayer;
                });

        // Якщо витягнули вже існуючого
        if(roomPlayer.getPlayer().getStatus() == StatusInGame.WAS_LEFT_EARLIER ){
            roomPlayer.getPlayer().setStatus(StatusInGame.PREPARATION_FOR_THE_GAME);
        }

        //Шукаємо інших гравців з кімнати
        List<PlayerProjection> projections = getUsersNameByRoomId(room.getId());

        List<String> names = new ArrayList<>();
        List<Long> idUser = new ArrayList<>();

        for(PlayerProjection projection:projections){
            names.add(projection.getName());
            idUser.add(projection.getPlayerId());
        }

        //Зберігаємо зміни.
        roomPlayerRepository.save(roomPlayer);

        sessionService.updateSession(roomPlayer.getRoom().getId(),userName,
                preparingRequest(roomPlayer));

        return  RoomDataResponse.builder()
                .names(names)
                .roomId(room.getId())
                .player(roomPlayer.getPlayer())
                .ids(idUser)
                .build();
    }

    @Transactional
    public void leaveGame(Long roomPlayerId) {
        String name =authService.getCurrentUserName();

        log.info("user : "+name+"leaveGame");
       RoomPlayer roomPlayer= roomPlayerRepository.findByIdCurrentRoomPlayer(roomPlayerId,name).orElseThrow(
               ()->new IllegalArgumentException("Player is not exist"));

       if(roomPlayer.getPlayer().getStatus() == StatusInGame.PREPARATION_FOR_THE_GAME ){
           sessionService.deleteSession(roomPlayer.getRoom().getId(),name);

           playerRepository.deleteById(roomPlayer.getPlayer().getId());
           roomPlayerRepository.deleteById(roomPlayerId);
           return;
       }
       roomPlayer.getPlayer().setStatus(StatusInGame.WAS_LEFT_EARLIER);
       roomPlayer.setJoined(false);

       roomPlayerRepository.save(roomPlayer);
    }

    @Transactional
    public RoomPlayer createRoomPlayer(Room room){

        RoomPlayer roomPlayer = RoomPlayer.builder()
                .room(room)
                .build();
       return roomPlayerRepository.save(roomPlayer);
    }

    public void playerExpulsion(Long roomId,String targetUserName){
        String code = roomRepository.findCodeToConnectById(roomId).orElseThrow(
                ()->new EntityNotFoundException("Code is not valid or something went wrong")
        );
        String name =authService.getCurrentUserName();
        log.info("user : "+name+"playerExpulsion to player : "+targetUserName);

        ProductDTO productDTO = sessionService.getSession(roomId,name);
        productDTO.setVoteSelectedName(targetUserName);

        sessionService.saveSession(roomId,name,productDTO);

        messagingTemplate.convertAndSend("/topic/expulsion/"+code,name);

    }

    @Transactional
    public void nextMove(Long roomId){
        String name =authService.getCurrentUserName();
        String roomCode= roomRepository.findCodeToConnectById(roomId).orElseThrow(
                ()->new IllegalArgumentException("Room is not exist"));

        log.info("user : "+name+"nextMove by room with id : "+roomId);

        Room room =roomRepository.findById(roomId).orElseThrow(
                ()->new EntityNotFoundException("Room is not exist in function nextMove"));
        if(!room.getUser().getUsername().equals(name)){
            throw new IllegalArgumentException("User is not a creator");
        }

        // Виправлена конкатенація та додана крапка
        messagingTemplate.convertAndSend("/topic/next_move" + roomCode, roomId);

        List<ProductDTO> productDTOS= sessionService.getAllSessionByRoomId(roomId);

        if(productDTOS.isEmpty()){
            throw new IllegalArgumentException("Room by id "+roomId+"is not exist");
        }
        productDTOS =productDTOS.stream()
                .filter(o ->o.getTimeOfProtection()>0||o.getTimeOfStunned()>0)
                .toList();

        for(ProductDTO productDTO:productDTOS){
            if(productDTO.getTimeOfStunned()==1){
                productDTO.setTimeOfStunned(0);
                messagingTemplate.convertAndSendToUser(
                        productDTO.getUserName(),
                        "/topic/stun" + roomCode,
                        false
                );
            }
            if(productDTO.getTimeOfProtection()==1){
                productDTO.setTimeOfProtection(0);
                messagingTemplate.convertAndSendToUser(
                        productDTO.getUserName(),
                        "/topic/protect" + roomCode,
                        false
                );
            }
            if(productDTO.getTimeOfStunned()>1){
                productDTO.setTimeOfStunned(productDTO.getTimeOfStunned()-1);
            }
            if(productDTO.getTimeOfProtection()>1){
                productDTO.setTimeOfProtection(productDTO.getTimeOfProtection()-1);
            }
            sessionService.saveSession(roomId, productDTO.getUserName(), productDTO);
        }

    }

    public void votingResults(Long roomId){
        String name =authService.getCurrentUserName();
        ProductDTO userDto =sessionService.getSession(roomId,name);

        Room room =roomRepository.findRoomAndUserBy(roomId).orElseThrow(
                ()->new EntityNotFoundException("Room is not exist"));


        if(!userDto.getUserName().equals(room.getUser().getUsername())){
            throw new IllegalArgumentException("User is not a creator");
        }


        String roomCode= roomRepository.findCodeToConnectById(roomId).orElseThrow(
                ()->new EntityNotFoundException("Room is not exist"));
        List<ProductDTO> productDTOS= sessionService.getAllSessionByRoomId(roomId);


        if(productDTOS.isEmpty()){
            throw new IllegalArgumentException("Room by id "+roomId+"is not exist");
        }
        boolean b = productDTOS.stream()
                .noneMatch(o -> o.getVoteSelectedName() == null);

        if(b){

            Map<String, Long> usersPoints = productDTOS.stream()
                    .collect(Collectors.groupingBy(
                            ProductDTO::getVoteSelectedName,
                            Collectors.counting()
                    ));
            long maxValue = usersPoints.values().stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElseThrow();

            List<String> keys = usersPoints.entrySet().stream()
                    .filter(e -> e.getValue() == maxValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            messagingTemplate.convertAndSend("/topic/voting_results/"+roomCode,keys);
        }
        else {
            throw new NoResultException("Not all participants voted");
        }
    }


    List<PlayerProjection> getUsersNameByRoomId(Long roomId){
        List<PlayerProjection> projection= roomPlayerRepository.findUserNameByRoomId(roomId);
        if(projection.isEmpty()){
            throw new IllegalArgumentException("Players is not found but code is valid");
        };
        return projection;
    }

    Consumer<ProductDTO> preparingRequest(RoomPlayer roomPlayer){
        Player player = roomPlayer.getPlayer();
        return dto -> {
            dto.setPlayerId(player.getId());

            if(player.getHero()!=null){
                dto.setHeroId(player.getHero().getId());
            }

            if(player.getCharacter()!=null){
                dto.setCharacterId(player.getCharacter().getId());
            }

            if(player.getArtifactHeroCatalog()!=null){
                dto.setArtifactHeroId(player.getArtifactHeroCatalog().getId());
            }

            if(player.getFirstArtifactRandomCatalog()!=null){
                dto.setArtifactRand1Id(player.getFirstArtifactRandomCatalog().getId());
            }
            if(player.getSecondArtifactRandomCatalog()!=null){
                dto.setArtifactRand2Id(player.getSecondArtifactRandomCatalog().getId());
            }
            if(player.getEffect()!=null){
                dto.setEffectId(player.getEffect().getId());
            }
            else {
                Effect effect =effectRepository.save(new Effect());
                dto.setEffectId(effect.getId());
            }
            dto.setUserName(authService.getCurrentUserName());
        };
    }
}
