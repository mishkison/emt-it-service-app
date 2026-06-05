package com.emtit.service;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.emtit.service.model.Ticket;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

public class EmailSender {
    private static final String FROM = "emishkis@gmail.com";
    private static final String PASS = "hyaujtqhklzrdxwp";
    private static final String TO = "eliran@mishkis.com";

    public interface Callback { void onSuccess(); void onFailure(Exception e); }

    public static void send(Ticket t, Bitmap bmp, Callback cb) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... v) {
                try {
                    Properties p = new Properties();
                    p.put("mail.smtp.auth", "true");
                    p.put("mail.smtp.starttls.enable", "true");
                    p.put("mail.smtp.host", "smtp.gmail.com");
                    p.put("mail.smtp.port", "587");
                    p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                    Session ses = Session.getInstance(p, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(FROM, PASS);
                        }
                    });
                    MimeMessage msg = new MimeMessage(ses);
                    msg.setFrom(new InternetAddress(FROM));
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
                    msg.setSubject("קריאת שירות חדשה #" + t.getId() + " - " + t.getTitle(), "UTF-8");
                    MimeMultipart mp = new MimeMultipart();
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(buildHtml(t), "text/html; charset=UTF-8");
                    mp.addBodyPart(htmlPart);
                    if (bmp != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        MimeBodyPart imgPart = new MimeBodyPart();
                        DataSource ds = new ByteArrayDataSource(baos.toByteArray(), "image/jpeg");
                        imgPart.setDataHandler(new DataHandler(ds));
                        imgPart.setFileName("screenshot.jpg");
                        mp.addBodyPart(imgPart);
                    }
                    msg.setContent(mp);
                    Transport.send(msg);
                    return null;
                } catch (Exception e) { return e; }
            }
            @Override
            protected void onPostExecute(Exception e) {
                if (e == null) cb.onSuccess(); else cb.onFailure(e);
            }
        }.execute();
    }

    private static String h(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private static String buildHtml(Ticket t) {
        return "<!DOCTYPE html><html dir='rtl'><head><meta charset='UTF-8'><style>"
            + "body{font-family:Arial;background:#f0f4f8;padding:20px;}"
            + ".card{background:#fff;border-radius:12px;max-width:600px;margin:0 auto;overflow:hidden;}"
            + ".hdr{background:#0D1B2A;padding:24px;text-align:center;}"
            + ".logo{background:#00C9A7;color:#0D1B2A;font-weight:bold;font-size:22px;padding:10px 20px;border-radius:6px;display:inline-block;}"
            + ".hdr h2{color:#fff;margin:12px 0 4px;}.body{padding:24px;}"
            + "table{width:100%;border-collapse:collapse;}"
            + "td{padding:10px 12px;border-bottom:1px solid #eee;font-size:14px;}"
            + "td:first-child{color:#666;width:30%;}td:last-child{font-weight:bold;}"
            + ".desc{background:#f8f9fa;border-right:4px solid #00C9A7;padding:12px;margin-top:16px;}"
            + ".ftr{background:#0D1B2A;color:#fff;text-align:center;padding:16px;font-size:13px;}"
            + ".ftr a{color:#00C9A7;}</style></head><body><div class='card'>"
            + "<div class='hdr'><div class='logo'>EMT</div><h2>קריאת שירות חדשה</h2></div>"
            + "<div class='body'><table>"
            + "<tr><td>מספר</td><td>#" + String.valueOf(t.getId()) + "</td></tr>"
            + "<tr><td>נושא</td><td>" + h(t.getTitle()) + "</td></tr>"
            + "<tr><td>שם</td><td>" + h(t.getReporterName()) + "</td></tr>"
            + "<tr><td>טלפון</td><td>" + h(t.getReporterPhone()) + "</td></tr>"
            + "<tr><td>חברה</td><td>" + h(t.getCompany()) + "</td></tr>"
            + "<tr><td>קטגוריה</td><td>" + h(t.getCategory()) + "</td></tr>"
            + "<tr><td>עדיפות</td><td style='color:" + t.getPriorityHex() + ";'>" + h(t.getPriorityLabel()) + "</td></tr>"
            + "<tr><td>תאריך</td><td>" + h(t.getCreatedAt()) + "</td></tr>"
            + "</table><div class='desc'><b>תיאור:</b><br><br>" + h(t.getDescription()) + "</div>"
            + "<div class='ftr'>EMT-IT Solution | <a href='tel:0509166011'>050-9166011</a></div>"
            + "</div></div></body></html>";
    }
}
