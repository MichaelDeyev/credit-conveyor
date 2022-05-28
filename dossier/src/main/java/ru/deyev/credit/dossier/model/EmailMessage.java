package ru.deyev.credit.dossier.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailMessage {
    private String address;
    private String subject;
    private String text;
}
