package com.message.unitedmessageengine.core.worker.sender.repository;

import com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto;
import com.message.unitedmessageengine.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
public class SenderBatchRepositoryImpl implements SenderBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdate(List<ExternalMessageDto> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            UPDATE MESSAGE SET STATUS_CODE = ? where MESSAGE_ID=?
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        setStatement(ps, batchList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchList.size();
                    }
                }
        );

    }

    private void setStatement(PreparedStatement ps, ExternalMessageDto messageDto) throws SQLException {
        ps.setString(1, "P");
        ps.setString(2, messageDto.getKEY());
    }

    public void batchInsert(List<Message> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            INSERT INTO MESSAGE(
                                MESSAGE_ID, CONTENT, FROM_NUMBER, REG_DTT, SEND_DTT, 
                                SERVICE_DIVISION, SERVICE_TYPE, STATUS_CODE, TO_NUMBER
                            ) 
                            VALUES (?,?,?,?,?,?,?,?,?)
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        setStatement(ps, batchList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchList.size();
                    }
                }
        );

    }

    private void setStatement(PreparedStatement ps, Message messageDto) throws SQLException {
        ps.setString(1, messageDto.getMessageId());
        ps.setString(2, messageDto.getContent());
        ps.setString(3, messageDto.getFromNumber());
        ps.setTimestamp(4, Timestamp.valueOf(messageDto.getRegDtt()));
        ps.setTimestamp(5, Timestamp.valueOf(messageDto.getSendDtt()));
        ps.setString(6, messageDto.getServiceDivision());
        ps.setString(7, messageDto.getServiceType());
        ps.setString(8, messageDto.getStatusCode());
        ps.setString(9, messageDto.getToNumber());
    }
}
