package com.message.unitedmessageengine.core.worker.result.repository;

import com.message.unitedmessageengine.core.worker.result.dto.AckDto;
import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class ResultBatchRepositoryImpl implements ResultBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public void batchUpdateResult(List<ResultDto> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            UPDATE MESSAGE SET STATUS_CODE=?, RESULT_CODE=?, RESULT_MESSAGE=? 
                            where MESSAGE_ID=?
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

    private void setStatement(PreparedStatement ps, ResultDto resultDto) throws SQLException {
        ps.setString(1, "C");
        ps.setString(2, resultDto.getResultCode());
        ps.setString(3, resultDto.getResultMessage());
        ps.setString(4, resultDto.getMessageId());
    }

    @Override
    public void batchUpdateAck(List<AckDto> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            UPDATE MESSAGE SET STATUS_CODE=?, RESULT_CODE=?, RESULT_MESSAGE=? 
                            where MESSAGE_ID=? AND (RESULT_CODE IS NULL OR RESULT_CODE = ?)
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

    private void setStatement(PreparedStatement ps, AckDto resultDto) throws SQLException {
        ps.setString(1, resultDto.getResultCode() == null || resultDto.getResultCode().equals("100") ? "P" : "C");
        ps.setString(2, resultDto.getResultCode());
        ps.setString(3, resultDto.getResultMessage());
        ps.setString(4, resultDto.getMessageId());
        ps.setString(5, "100");
    }
}
