package com.example.domain;

import lombok.Data;


import java.io.Serializable;

/**
 * 数据返回封装顶层抽象类
 * <p>顶层抽象类, 可以使用我已经写好的Result<T>, 也可以选择自己实现AbstractResult<T></p>
 */
@Data
public abstract class AbstractResult<T> implements Serializable {
    private static final long serialVersionUID = -7804054241710088L;
    /**
     * MDC链路踪迹id
     */
    private String traceId;
}
