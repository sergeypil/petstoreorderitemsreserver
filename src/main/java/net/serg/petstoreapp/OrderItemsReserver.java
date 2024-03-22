package net.serg.petstoreapp;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OrderItemsReserver {
    private static final String connectionString = "<Add when need>";
    private static final String blobContainerName = "order-items-reserver";

    @FunctionName("reserveOrderItems")
    public HttpResponseMessage run(
        @HttpTrigger(
                name = "req", methods = {HttpMethod.POST}, 
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<SessionData>> request,
        final ExecutionContext context) {

        context.getLogger().info("reserveOrderItems Function triggered.");
        context.getLogger().info("Request body: " + request.getBody().orElse(null));
        SessionData sessionData = request.getBody().orElse(null);
        if (sessionData == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing sessionData data.").build();
        }

        try {
            String orderJson = sessionData.getOrderJson();

            BlobContainerClientBuilder blobContainerClientBuilder = new BlobContainerClientBuilder();
            BlobContainerClient blobContainerClient = blobContainerClientBuilder.connectionString(connectionString).containerName(blobContainerName).buildClient();
            BlobClient blobClient = blobContainerClient.getBlobClient(sessionData.getSessionId() + ".json");

            if (blobClient.exists()) {
                blobClient.delete();  // Delete blob if it already exists
            }

            // Create new blob and upload JSON data.
            BlobClient newBlobClient = blobContainerClient.getBlobClient(sessionData.getSessionId() + ".json");
            newBlobClient.upload(new ByteArrayInputStream(orderJson.getBytes(StandardCharsets.UTF_8)), orderJson.length());
            return request.createResponseBuilder(HttpStatus.OK).body("Order item reservation completed successfully.").build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while reserving sessionData items.").build();
        }
    }
}