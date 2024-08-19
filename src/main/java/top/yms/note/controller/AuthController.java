package top.yms.note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yms.note.utils.JwtUtil;

/**
 * Created by yangmingsen on 2024/8/19.
 */


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/authenticate")
    public String generateToken(@RequestParam("username") String username) {
        return jwtUtil.generateToken(username);
    }

    @GetMapping("/validate")
    public Boolean validateToken(@RequestParam String token, @RequestParam String username) {
        return jwtUtil.validateToken(token, username);
    }
}

