package io.telereso.annotations.client.api;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
// Main class
public class HelloWorldController {

    @RequestMapping("")
    @ResponseBody
    public String getString() {

        return "Hello world1";
    }

}