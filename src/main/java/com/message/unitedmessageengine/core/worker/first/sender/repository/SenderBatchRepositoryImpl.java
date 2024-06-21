package com.message.unitedmessageengine.core.worker.first.sender.repository;

import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class SenderBatchRepositoryImpl implements SenderBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdate(List<MessageEntity> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            UPDATE MESSAGE SET STATUS_CODE = ? where MESSAGE_ID=?
                        """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        setStatementUpdate(ps, batchList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchList.size();
                    }
                }
        );

    }

    private void setStatementUpdate(PreparedStatement ps, MessageEntity messageEntity) throws SQLException {
        ps.setString(1, "P");
        ps.setString(2, messageEntity.getMessageId());
    }

}
