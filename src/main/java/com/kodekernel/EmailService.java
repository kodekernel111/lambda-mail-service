package com.kodekernel;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;

@Slf4j
public class EmailService implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final String FROM = System.getenv("FROM_EMAIL");
    private final String TO = System.getenv("TO_EMAIL");
    private final String REGION = System.getenv("AWS_REGION_SES") != null
            ? System.getenv("AWS_REGION_SES")
            : "eu-north-1";

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        Map<String, String> corsHeaders = new HashMap<>();
        corsHeaders.put("Access-Control-Allow-Origin", "http://localhost:5173");
        corsHeaders.put("Access-Control-Allow-Headers", "*");
        corsHeaders.put("Access-Control-Allow-Methods", "OPTIONS,POST,GET");

        if ("OPTIONS".equalsIgnoreCase(event.getRequestContext().getHttp().getMethod())) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(corsHeaders)
                    .withBody("")
                    .build();
        }

        try {
            AmazonSimpleEmailService ses = AmazonSimpleEmailServiceClientBuilder
                    .standard()
                    .withRegion(REGION)
                    .build();

            String body = event.getBody();
            if(body == null) {
                throw new RuntimeException("Request body is null");
            }

            EmailRequest req = new Gson().fromJson(body, EmailRequest.class);
            log.info("Received request: Name: {}, Email: {}, Service: {}, Message: {}",req.getName(), req.getEmail(), req.getService(), req.getMessage());

            SendEmailRequest request = new SendEmailRequest()
                    .withSource(FROM)
                    .withDestination(new Destination().withToAddresses(TO))
                    .withMessage(
                            new Message()
                                    .withSubject(new Content("Contact Form: " + req.getService()))
                                    .withBody(new Body().withText(new Content(
                                            "Name: " + req.getName() + "\n" +
                                                    "Email: " + req.getEmail() + "\n" +
                                                    "Message: " + req.getMessage()
                                    )))
                    );

            ses.sendEmail(request);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody("{\"message\": \"Email sent successfully!\"}")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("{\"message\": \"Failed to send email\"}")
                    .build();
        }
    }
}
