package ru.deyev.credit.dossier.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageFromKafka {

    private String address;

    private MessageType theme;

    private Long applicationId;
}
