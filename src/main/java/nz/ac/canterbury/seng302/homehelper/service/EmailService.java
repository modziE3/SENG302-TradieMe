package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public EmailService(JavaMailSender emailSender, UserRepository userRepository, UserService userService) {
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public void sendEmail(
            String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendResetPasswordLinkEmail(String to, String url) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Reset Password Home Helper");
        message.setText("Here is the link to reset your password. If you didn't intend to reset your password then ignore this email\n\n"+url);
        emailSender.send(message);
    }

    public void sendResetPasswordSuccessEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("If you made this change, no further action is required \n\nIf you didn't intend to change your password, " +
                "please reset your password as soon as possible");
        emailSender.send(message);
    }

    public void sendConfirmRegistrationEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Home helper verification code");
        message.setText("Here is your sign up code: \n\n" + verificationCode + "\n\nIf you didn’t register, ignore this email, and your account will be deleted in 10 minutes.");
        emailSender.send(message);
    }

    public void sendConfirmUpdatedPasswordEmail(String to, User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Password updated");
        String emailUpdate = "Hey "+user.getFirstName()+"\nYour password has just been updated!\n\nTeam 1000";
        message.setText(emailUpdate);
        emailSender.send(message);
    }

    public void sendQuoteReceivedEmail(Job job) {
        RenovationRecord renovationRecord = job.getRenovationRecord();
        String email = renovationRecord.getUserEmail();
        User user = userService.getUser(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Quote received");
        message.setText("Hey " +user.getFirstName() + "\nYou have received a quote for your job " + job.getName());
        emailSender.send(message);
    }

    public void sendQuoteRejectedEmail(String to, Job job) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Quote rejected");
        String emailMessage = "Your quote on the job " + job.getName() + " has been rejected!\n\nTeam 1000";
        message.setText(emailMessage);
        emailSender.send(message);
    }

    public void sendQuoteAcceptedEmail(String to, Job job) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(to);
        message.setSubject("Quote Accepted");
        String emailMessage = "Your quote on the job " + job.getName() + " has been accepted";
        message.setText(emailMessage);
        emailSender.send(message);
    }

    public void sendQuoteRetractedEmail(Job job, User retractor) {
        RenovationRecord renovationRecord = job.getRenovationRecord();
        String retractorEmail = retractor.getEmail();
        String ownerEmail = renovationRecord.getUserEmail();
        User jobOwner = userService.getUser(ownerEmail);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("seng301team1000@gmail.com");
        message.setTo(jobOwner.getEmail());
        message.setSubject("Quote retracted");
        message.setText("Hey " + jobOwner.getFirstName() + "\nA quote for your job " + job.getName() + " has been retracted by " + retractorEmail);
        emailSender.send(message);
    }


}