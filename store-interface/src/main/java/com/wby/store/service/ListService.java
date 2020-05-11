package com.wby.store.service;

import com.wby.store.bean.SkuLsInfo;
import com.wby.store.bean.SkuLsParams;
import com.wby.store.bean.SkuLsResult;

public interface ListService {
    public void  saveSkuListInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams  );
}
