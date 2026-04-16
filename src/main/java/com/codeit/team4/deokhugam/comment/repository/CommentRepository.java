package com.codeit.team4.deokhugam.comment.repository;

import com.codeit.team4.deokhugam.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
