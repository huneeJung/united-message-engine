package com.message.unitedmessageengine.core.first.repository;

import com.message.unitedmessageengine.entity.KakaoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class KakaoBatchRepositoryImpl implements KakaoBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdate(List<KakaoEntity> batchList) {
        jdbcTemplate.batchUpdate(
                """
                            UPDATE KAKAO SET STATUS_CODE = ? where KAKAO_ID=?
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

    private void setStatementUpdate(PreparedStatement ps, KakaoEntity kakaoEntity) throws SQLException {
        ps.setString(1, "P");
        ps.setString(2, kakaoEntity.getKakaoId());
    }

}
