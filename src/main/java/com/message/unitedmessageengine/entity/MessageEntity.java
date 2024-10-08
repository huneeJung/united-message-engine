package com.message.unitedmessageengine.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MESSAGE", indexes = {
        @Index(name = "idx_messageId", columnList = "MESSAGE_ID"),
        @Index(name = "idx_statusCode", columnList = "STATUS_CODE,SERVICE_DIVISION")
})
public class MessageEntity {

    @Id
    @Column(name = "SEQ")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(name = "MESSAGE_ID", nullable = false)
    private String messageId;

    // SLM, KKO
    @Column(name = "SERVICE_DIVISION", nullable = false)
    private String serviceDivision;

    // SMS, LMS, MMS, TKA ...
    @Column(name = "SERVICE_TYPE", nullable = false)
    private String serviceType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    // W:대기중 , P:진행중 , C:완료
    @Column(name = "STATUS_CODE", nullable = false)
    private String statusCode;

    @Column(name = "TO_NUMBER", nullable = false)
    private String toNumber;

    @Column(name = "FROM_NUMBER")
    private String fromNumber;

    @CreatedDate
    @Column(name = "REG_DTT")
    private LocalDateTime regDtt;

    @Column(name = "SEND_DTT")
    private LocalDateTime sendDtt;

    @Column(name = "RESULT_CODE")
    private String resultCode;

    @Column(name = "RESULT_MESSAGE")
    private String resultMessage;

    @JsonManagedReference
    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageImageEntity> imageList;

}
