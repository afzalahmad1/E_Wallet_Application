package com.example.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisTemplate<String,User> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    //for redis
    private final String REDIS_PREFIX_USER = "user::";

    //for kafka(create topic)
    //private final String CREATE_WALLET_TOPIC = "create_wallet";
    public String createUser(UserRequest userRequest) {

        User user = User.builder()
                .age(userRequest.getAge())
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .userName(userRequest.getUserName())
                .build();

        userRepository.save(user);

        //function call  to save in redis
        saveInCache(user);

        // kafka
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",user.getUserName());

        // convert this objet to string to save in kafka template
        String message = jsonObject.toString();

        //for single message (attribute) we can send msg like this
        //send an update to the wallet module/wallet service
        //create a new wallet for this userName
        kafkaTemplate.send("create_wallet",message);

        return "user added successfully";

    }

    //in redis data stored in map format so wee need to convert object to map first
    public void saveInCache(User user){
        Map map = objectMapper.convertValue(user,Map.class);
        String key = "REDIS_PREFIX_USER"+user.getUserName();
        System.out.println("User key is"+key);
        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key, Duration.ofHours(12));
    }
    public User getUserByUserName(String userName) throws Exception{

        Map map = redisTemplate.opsForHash().entries(REDIS_PREFIX_USER+userName);

        if(map==null || map.size()==0){
            // cache miss -> search in DB
            User user = userRepository.findByUserName(userName);

            if(user!=null){
                saveInCache(user);
            }
            else { //Throw an error
                throw new UserNotFoundException();
            }
            return user;
        }
        else{
            return objectMapper.convertValue(map,User.class);
        }
    }
}

