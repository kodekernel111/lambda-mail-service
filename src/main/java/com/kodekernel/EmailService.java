package com.kodekernel;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;

public class EmailService implements RequestHandler<EmailRequest, String> {

    private final String FROM = System.getenv("FROM_EMAIL");
    private final String TO = System.getenv("TO_EMAIL");
    private final String REGION = System.getenv("AWS_REGION_SES") != null
            ? System.getenv("AWS_REGION_SES")
            : "ap-south-1";

    @Override
    public String handleRequest(EmailRequest input, Context context) {

        try {
            AmazonSimpleEmailService ses = AmazonSimpleEmailServiceClientBuilder
                    .standard()
                    .withRegion(REGION)
                    .build();

            SendEmailRequest request = new SendEmailRequest()
                    .withSource(FROM)
                    .withDestination(new Destination().withToAddresses(TO))
                    .withMessage(
                            new Message()
                                    .withSubject(new Content("Contact Form: " + input.getService()))
                                    .withBody(new Body().withText(new Content(
                                            "Name: " + input.getName() + "\n" +
                                                    "Email: " + input.getEmail() + "\n" +
                                                    "Message: " + input.getMessage()
                                    )))
                    );

            ses.sendEmail(request);

            return "Email sent successfully!";
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
