package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/getByUserName")
    public User getUserByUserName(@RequestParam("userName") String userName) throws Exception{
        return userService.getUserByUserName(userName);
    }

    @PostMapping("/add")
    public String createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }
}
