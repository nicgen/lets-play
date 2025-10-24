package com.letsplay.api.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.letsplay.api.repository")
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    CommandLineRunner initIndexes(MongoClient mongoClient) {
        return args -> {
            // Create unique index on user email
            MongoCollection<Document> users = mongoClient
                    .getDatabase("letsplay")
                    .getCollection("users");

            users.createIndex(
                Indexes.ascending("email"),
                new IndexOptions().unique(true)
            );

            // Create index on product userId for efficient queries
            MongoCollection<Document> products = mongoClient
                    .getDatabase("letsplay")
                    .getCollection("products");

            products.createIndex(Indexes.ascending("user_id"));
        };
    }
}
