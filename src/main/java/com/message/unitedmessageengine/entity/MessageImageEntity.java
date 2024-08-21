package com.message.unitedmessageengine.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MESSAGE_IMAGE")
public class MessageImageEntity {

    @Id
    @Column(name = "SEQ")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageName;

    @Column(name = "IMAGE_PATH", nullable = false)
    private String imagePath;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "SEQ")
    private MessageEntity message;

}
