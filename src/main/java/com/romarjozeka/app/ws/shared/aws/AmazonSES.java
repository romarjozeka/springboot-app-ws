package com.romarjozeka.app.ws.shared.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.romarjozeka.app.ws.shared.dto.UserDto;

public class AmazonSES {

    static final String FROM = "romarjozeka@gmail.com";

    static final String SUBJECT = "Verify your email to complete registration";

    static final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering. To complete registration process and be able to log in,"
            + " click on the following link: "
            + "<a href='http://localhost:8080/users/email-verification?token=$tokenValue'>"
            + "Final step to complete your registration" + "</a><br/><br/>"
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
}