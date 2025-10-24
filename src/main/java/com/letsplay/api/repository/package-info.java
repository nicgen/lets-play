/**
 * Repository layer for MongoDB data access.
 *
 * <p>Spring Data MongoDB Query Methods:</p>
 * <ul>
 *   <li>findBy[PropertyName] - Find by exact match</li>
 *   <li>findBy[PropertyName]Containing - Find by partial match</li>
 *   <li>findBy[PropertyName]Between - Find within range</li>
 *   <li>findBy[PropertyName]GreaterThan - Find greater than value</li>
 *   <li>findBy[PropertyName]LessThan - Find less than value</li>
 *   <li>existsBy[PropertyName] - Check existence</li>
 *   <li>countBy[PropertyName] - Count matching records</li>
 *   <li>deleteBy[PropertyName] - Delete by property</li>
 * </ul>
 *
 * <p>Custom queries use @Query annotation with MongoDB query syntax.</p>
 *
 * @see com.letsplay.api.repository.UserRepository
 * @see com.letsplay.api.repository.ProductRepository
 */
package com.letsplay.api.repository;
