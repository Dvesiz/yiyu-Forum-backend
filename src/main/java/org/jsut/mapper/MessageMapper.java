package org.jsut.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jsut.pojo.Message;
import java.util.List;

@Mapper
public interface MessageMapper {
    // 查询所有消息，按时间倒序
    @Select("select * from message order by create_time desc")
    List<Message> list();
}