package net.serg.petstoreapp;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class OrderItemsReserver {
    private static final String connectionString = "***";
    private static final String blobContainerName = "order-items-reserver";

    @FunctionName("reserveOrderItems")
    public void run(
        @ServiceBusQueueTrigger(
            name = "sessionData",
            queueName = "PetStoreQueue",
            connection = "SERVICE_BUS_CONNECTION_STRING") // env variable
        SessionData sessionData,
        final ExecutionContext context) {

        context.getLogger().info("reserveOrderItems Function triggered.");
        context.getLogger().info("Received sessionData: " + sessionData);

        try {
            String orderJson = sessionData.getOrderJson();

            ExponentialBackoffOptions exponentialOptions = new ExponentialBackoffOptions()
                .setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(1))
                .setMaxDelay(Duration.ofSeconds(10));

            RetryOptions retryOptions = new RetryOptions(exponentialOptions);

            BlobContainerClientBuilder blobContainerClientBuilder = new BlobContainerClientBuilder();
            BlobContainerClient blobContainerClient = blobContainerClientBuilder
                .connectionString(connectionString)
                .containerName(blobContainerName)
                .retryOptions(retryOptions)
                .buildClient();

            BlobClient blobClient = blobContainerClient.getBlobClient(sessionData.getSessionId() + ".json");
            if (blobClient.exists()) {
                blobClient.delete();  // Delete blob if it already exists
            }

            // Create new blob and upload JSON data
            BlobClient newBlobClient = blobContainerClient.getBlobClient(sessionData.getSessionId() + ".json");
            newBlobClient.upload(new ByteArrayInputStream(orderJson.getBytes(StandardCharsets.UTF_8)), orderJson.length());
            context.getLogger().info("Order item reservation completed successfully.");

        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
        }
    }
}