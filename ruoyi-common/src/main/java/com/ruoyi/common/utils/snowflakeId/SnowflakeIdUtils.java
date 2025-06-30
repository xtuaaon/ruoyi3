package com.ruoyi.common.utils.snowflakeId;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * 雪花ID生成工具类
 *
 * @author ruoyi
 */
@Component
public class SnowflakeIdUtils {

    /**
     * 机器ID (0-31)
     */
    private static final long WORKER_ID = 1;

    /**
     * 数据中心ID (0-31)
     */
    private static final long DATACENTER_ID = 1;

    /**
     * 雪花算法对象
     */
    private static final Snowflake snowflake = IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID);

    /**
     * 获取雪花ID（Long类型）
     *
     * @return 雪花ID
     */
    public static long getSnowflakeId() {
        return snowflake.nextId();
    }

    /**
     * 获取雪花ID（String类型）
     *
     * @return 雪花ID字符串
     */
    public static String getSnowflakeIdStr() {
        return snowflake.nextIdStr();
    }

    /**
     * 获取雪花算法对象
     *
     * @return Snowflake对象
     */
    public static Snowflake getSnowflake() {
        return snowflake;
    }

    /**
     * 手动创建雪花ID生成器
     *
     * @param workerId 机器ID
     * @param datacenterId 数据中心ID
     * @return 雪花ID
     */
    public static long getSnowflakeId(long workerId, long datacenterId) {
        return IdUtil.getSnowflake(workerId, datacenterId).nextId();
    }
}