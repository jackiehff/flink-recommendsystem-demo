package com.datafly.recommend.top;

import com.datafly.recommend.domain.TopProductEntity;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Date;

public class WindowFunction extends ProcessAllWindowFunction<TopProductEntity, TopProductEntity, TimeWindow> {

    @Override
    public void process(Context context, Iterable<TopProductEntity> iterable, Collector<TopProductEntity> collector) {

        String now = new Date().toString();

        for (TopProductEntity topProductEntity : iterable) {
            topProductEntity.setRankName(now);
            collector.collect(topProductEntity);
        }
    }
}