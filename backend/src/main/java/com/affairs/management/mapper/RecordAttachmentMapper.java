package com.affairs.management.mapper;

import com.affairs.management.entity.RecordAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordAttachmentMapper {

    int insert(RecordAttachment ra);

    int batchInsert(@Param("recordId") String recordId, @Param("attachmentIds") List<String> attachmentIds);

    List<String> selectAttachmentIdsByRecordId(@Param("recordId") String recordId);
}
