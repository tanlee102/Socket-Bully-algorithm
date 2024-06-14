package com.example.SocketServer.Repo;

import com.example.SocketServer.User.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    @Query("{ 'name' : ?0, 'password' : ?1 }")
    User findByNameAndPassword(String name, String password);
}
