package com.message.unitedmessageengine.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "IMAGE")
public class ImageEntity {

    @Id
    @Column(name = "IMAGE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageName;

    @Column(name = "IMAGE_PATH", nullable = false)
    private String imagePath;

    @ManyToOne
    @JsonIgnoreProperties
    @JoinColumn(name = "ID")
    private MessageEntity message;

}
