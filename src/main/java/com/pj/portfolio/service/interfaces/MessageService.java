package com.pj.portfolio.service.interfaces;

import com.pj.portfolio.entity.Message;

import java.util.List;

public interface MessageService {
    List<Message> getAllMessages();

    Message findByEmail(String theEmail);

    Message save(Message message);

    void deleteById(Integer id);
}
