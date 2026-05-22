package com.eulerity.taskmanager.ai;

public interface AiTaskModelClient {

    SuggestedTask suggestTask(String naturalLanguageText);
}