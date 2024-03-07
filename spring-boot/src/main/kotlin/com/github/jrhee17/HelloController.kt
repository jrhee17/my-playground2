package com.github.jrhee17

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
open class HelloController {

    @RequestMapping(method = [RequestMethod.GET], path = ["/"])
    fun index(): String {
        return "index"
    }
}