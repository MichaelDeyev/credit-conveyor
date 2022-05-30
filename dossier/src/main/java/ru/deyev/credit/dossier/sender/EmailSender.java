package ru.deyev.credit.dossier.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ru.deyev.credit.dossier.model.EmailMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender javaMailSender;

    public void sendMessage(String address, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("credit.conveyor@yandex.ru");
        message.setTo(address);
        message.setSubject(subject);
        message.setText(text);

        log.info("Sending email: \n{}", message);
        javaMailSender.send(message);
    }

    public void sendMessage(EmailMessage emailMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("credit.conveyor@yandex.ru");
        message.setTo(emailMessage.getAddress());
        message.setSubject(emailMessage.getSubject());
        message.setText(emailMessage.getText());

        log.info("Sending email: \n{}", message);

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("Exception while sending email: {}. Please, use no-email flow instead.", e.getMessage());
            throw new RuntimeException("Exception while sending email" + e.getMessage() + ". Please, use no-email flow instead.");
        }
    }

    public void sendMessageWithAttachment(String address,
                                          String subject,
                                          String text,
                                          Map<String, File> attachmentsWithNames) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("credit.conveyor@yandex.ru");
            helper.setTo(address);
            helper.setSubject(subject);
            helper.setText(text);

            for (Map.Entry<String, File> entry: attachmentsWithNames.entrySet()) {
                helper.addAttachment(entry.getKey(), entry.getValue());
            }

            javaMailSender.send(message);

        } catch (MessagingException e) {
            log.warn("Some error during attaching files: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
