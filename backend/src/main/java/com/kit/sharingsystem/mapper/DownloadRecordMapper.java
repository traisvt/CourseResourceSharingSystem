package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.DownloadRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface DownloadRecordMapper {
    @Insert("INSERT INTO download_records (resource_id, user_id, ip_address, downloaded_at) " +
            "VALUES (#{resource.id}, #{user.id}, #{ipAddress}, #{downloadedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DownloadRecord record);

    @Delete("DELETE FROM download_records WHERE resource_id = #{resourceId}")
    void deleteByResourceId(Long resourceId);
}
