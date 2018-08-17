package com.mykha.opendo.interfaces;

import com.mykha.opendo.objects.ToDoList;

public interface MainSendDataInterface{
    void sendList(ToDoList list);
    void sendListId(String listId);
}