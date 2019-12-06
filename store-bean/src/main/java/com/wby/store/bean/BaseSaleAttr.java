package com.wby.store.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 基本销售属性表
 */
@Data
@NoArgsConstructor
public class BaseSaleAttr implements Serializable {

    @Id
    @Column
    String id ;

    @Column
    String name;

}
