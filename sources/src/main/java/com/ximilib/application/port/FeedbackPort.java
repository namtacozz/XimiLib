package com.ximilib.application.port;

import java.util.List;

public interface FeedbackPort {

    void submitFeedback(String userId, String title, String content);

    List<Object[]> getAllFeedbacks();

    void markAsProcessed(int id);
}
