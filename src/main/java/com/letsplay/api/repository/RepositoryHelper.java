package com.letsplay.api.repository;

import com.letsplay.api.exception.RepositoryException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class RepositoryHelper {

    /**
     * Execute repository operation with exception handling
     * Converts DataAccessException to RepositoryException
     */
    public <T> T executeWithExceptionHandling(Supplier<T> operation, String errorMessage) {
        try {
            return operation.get();
        } catch (DataAccessException e) {
            throw new RepositoryException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    /**
     * Find entity by ID or throw exception if not found
     */
    public <T> T findByIdOrThrow(Optional<T> optional, String entityName, String id) {
        return optional.orElseThrow(() ->
            new RepositoryException(entityName + " not found with id: " + id)
        );
    }
}
