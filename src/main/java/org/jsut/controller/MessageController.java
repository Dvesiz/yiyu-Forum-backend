package org.jsut.controller;

import org.jsut.mapper.MessageMapper;
import org.jsut.pojo.Message;
import org.jsut.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;

    @GetMapping("/list")
    public Result<List<Message>> list() {
        List<Message> list = messageMapper.list();
        return Result.success(list);
    }
}