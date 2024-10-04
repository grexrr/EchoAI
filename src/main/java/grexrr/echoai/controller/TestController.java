package grexrr.echoai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController{

    // Test Open Endpoint
    @GetMapping("/public/hello")
    public String publicHello(){
        return "Hello Hello Hello from public endpoint";
    }

    // Test Protected Endpoint
    @GetMapping("/private/hello")
    public String privateHello(){
        return "Hello from private endpoint";
    }
}