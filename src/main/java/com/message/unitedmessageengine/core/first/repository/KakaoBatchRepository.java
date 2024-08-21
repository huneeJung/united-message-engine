package com.message.unitedmessageengine.core.first.repository;

import com.message.unitedmessageengine.entity.KakaoEntity;

import java.util.List;

public interface KakaoBatchRepository {

    void batchUpdate(List<KakaoEntity> batchList);

}
