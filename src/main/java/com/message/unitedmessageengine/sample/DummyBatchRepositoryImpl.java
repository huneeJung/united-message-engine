package com.message.unitedmessageengine.sample;

import com.message.unitedmessageengine.entity.MessageEntity;
import com.message.unitedmessageengine.entity.MessageImageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
public class DummyBatchRepositoryImpl implements DummyBatchRepository {

    private final JdbcTemplate jdbcTemplate;


    // Dummy TEST Message Insert Batch
    public void batchInsertMessage(List<MessageEntity> batchList) {
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
                        setStatementInsert(ps, batchList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchList.size();
                    }
                }
        );

    }

    private void setStatementInsert(PreparedStatement ps, MessageEntity messageEntity) throws SQLException {
        ps.setString(1, messageEntity.getMessageId());
        ps.setString(2, messageEntity.getContent());
        ps.setString(3, messageEntity.getFromNumber());
        ps.setTimestamp(4, Timestamp.valueOf(messageEntity.getRegDtt()));
        ps.setTimestamp(5, Timestamp.valueOf(messageEntity.getSendDtt()));
        ps.setString(6, messageEntity.getServiceDivision());
        ps.setString(7, messageEntity.getServiceType());
        ps.setString(8, messageEntity.getStatusCode());
        ps.setString(9, messageEntity.getToNumber());
    }

    // Dummy TEST Image Insert Batch
    public void batchInsertImage(List<MessageImageEntity> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            INSERT INTO IMAGE(
                                IMAGE_NAME, IMAGE_PATH, ID
                            ) 
                            VALUES (?,?,?)
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        setStatementInsert(ps, batchList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchList.size();
                    }
                }
        );

    }

    private void setStatementInsert(PreparedStatement ps, MessageImageEntity messageImageEntity) throws SQLException {
        ps.setString(1, messageImageEntity.getImageName());
        ps.setString(2, messageImageEntity.getImagePath());
        ps.setLong(3, messageImageEntity.getMessage().getSeq());
    }

}
