package com.hdsoft.models;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hdsoft.common.Common_Utils;

public class ApiProcessor {
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // Controls concurrency level

    public void processRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle request failure
                System.out.println("Request to " + url + " failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle request success
            	
            	Common_Utils util = new Common_Utils();
            	
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println("Response time " + url + ": " + util.get_oracle_Timestamp());
                    System.out.println("Response from " + url + ": " + responseData);
                    // Process the response data here
                } else {
                    System.out.println("Server error for " + url + ": " + response.code());
                }
            }
        });
    }

    public void addToQueue(String url) {
        executor.submit(() -> processRequest(url));
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown if tasks are not finished
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // Force shutdown if interrupted
        }
    }

    public static void main(String[] args) {
        ApiProcessor apiProcessor = new ApiProcessor();

        // Example of URLs to process
        String[] urls = {
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://jsonplaceholder.typicode.com/posts/2",
            "https://jsonplaceholder.typicode.com/posts/3",
            "https://jsonplaceholder.typicode.com/posts/4",
            "https://jsonplaceholder.typicode.com/posts/5",
            "https://jsonplaceholder.typicode.com/posts/6",
            "https://jsonplaceholder.typicode.com/posts/7",
            "https://jsonplaceholder.typicode.com/posts/8",
            "https://jsonplaceholder.typicode.com/posts/9",
            "https://jsonplaceholder.typicode.com/posts/10",
            // Add more URLs as needed
        };

        // Queue each request
        for (String url : urls) {
            apiProcessor.addToQueue(url);
        }

        // Shutdown the executor after processing all requests
        apiProcessor.shutdown();
    }
}
