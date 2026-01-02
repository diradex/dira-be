package com.driveflow.backend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String clientUrl;

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setText(buildEmail(name, clientUrl + "/verify?token=" + token), true);
            helper.setTo(to);
            helper.setSubject("Erősítsd meg az e-mail címedet - DriveFlow");
            helper.setFrom("noreply@driveflow.com");
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    private String buildEmail(String name, String link) {
        return String.format("""
                <div style="font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c">
                <span style="display:none;font-size:1px;color:#fff;max-height:0"></span>
                  <table role="presentation" width="100%%" style="border-collapse:collapse;min-width:100%%;width:100%%!important" cellpadding="0" cellspacing="0" border="0">
                    <tbody><tr>
                      <td width="100%%" height="53" bgcolor="#0b0c0c">
                        <table role="presentation" width="100%%" style="border-collapse:collapse;max-width:580px" cellpadding="0" cellspacing="0" border="0" align="center">
                          <tbody><tr>
                            <td width="70" bgcolor="#0b0c0c" valign="middle">
                                <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse">
                                  <tbody><tr>
                                    <td style="padding-left:10px">
                                    </td>
                                    <td style="font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px">
                                      <span style="font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block">Erősítsd meg az e-mail címedet</span>
                                    </td>
                                  </tr>
                                </tbody></table>
                              </a>
                            </td>
                          </tr>
                        </tbody></table>
                      </td>
                    </tr>
                  </tbody></table>
                  <table role="presentation" class="m_-6186904992287805515content" align="center" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse;max-width:580px;width:100%%!important" width="100%%">
                    <tbody><tr>
                      <td width="10" height="10" valign="middle"></td>
                      <td>
                                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse">
                                  <tbody><tr>
                                    <td bgcolor="#1D70B8" width="100%%" height="10"></td>
                                  </tr>
                                </tbody></table>
                      </td>
                      <td width="10" valign="middle" height="10"></td>
                    </tr>
                  </tbody></table>
                  <table role="presentation" class="m_-6186904992287805515content" align="center" cellpadding="0" cellspacing="0" border="0" style="border-collapse:collapse;max-width:580px;width:100%%!important" width="100%%">
                    <tbody><tr>
                      <td height="30"><br></td>
                    </tr>
                    <tr>
                      <td width="10" valign="middle"><br></td>
                      <td style="font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px">
                          <p style="Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c">Szia %s,</p><p style="Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c"> Köszönjük, hogy regisztráltál. Kérlek kattints az alábbi linkre a fiókod aktiválásához: </p><blockquote style="Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px"><p style="Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href="%s">Aktiválás most</a> </p></blockquote>
                      </td>
                      <td width="10" valign="middle"><br></td>
                    </tr>
                    <tr>
                      <td height="30"><br></td>
                    </tr>
                  </tbody></table><div class="yj6qo"></div><div class="adL">
                </div></div>
                """, name, link);
    }
}
