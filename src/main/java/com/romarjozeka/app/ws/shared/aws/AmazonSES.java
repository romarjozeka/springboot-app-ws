package com.romarjozeka.app.ws.shared.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.romarjozeka.app.ws.shared.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class AmazonSES {

    private static final String FROM = "romarjozeka@gmail.com";

    private static final String SUBJECT = "Verify your email to complete registration";

    private static final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering. To complete registration process and be able to log in,"
            + " click on the following link: "
            + "<a href='http://localhost:8080/springboot-app-ws/users/email-verification?token=$tokenValue'>"
            + "Final step to complete your registration" + "</a><br/><br/>"
            + "Thank you!";

    private final String PASSWORD_RESET_SUBJECT = "Password reset request";

    private final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Someone has requested to reset your password. If it were not you, please ignore it."
            + " Otherwise please click on the link below to set a new password: "
            + "<a href='http://localhost:8080/springboot-app-ws/users/password-reset-request?token=$tokenValue'>"
            + " Click this link to Reset Password"
            + "</a><br/><br/>"
            + "Thank you!";

    public void verifyEmail(UserDto userDto) {
        try {

            StringBuilder htmlBodyWithToken = new StringBuilder();

            htmlBodyWithToken.append(HTMLBODY.replace("$tokenValue", userDto.getEmailVerificationToken()));


            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(userDto.getEmail()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(htmlBodyWithToken.toString())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);
            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
    }


    public boolean sendPasswordResetRequest(String firstName, String email, String token) {

        boolean returnValue = false;
        try {

            StringBuilder htmlBodyWithToken = new StringBuilder();

            htmlBodyWithToken.append(PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token));


            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(email))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(htmlBodyWithToken.toString())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
                    .withSource(FROM);
            SendEmailResult result = client.sendEmail(request);
            if (result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
                returnValue = true;
            }
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }

        return returnValue;
    }
}