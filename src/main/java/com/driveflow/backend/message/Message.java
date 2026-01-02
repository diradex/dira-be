package com.driveflow.backend.message;

import com.driveflow.backend.common.BaseEntity;
import com.driveflow.backend.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Message extends BaseEntity {

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    private String content;
    private boolean read;
}
