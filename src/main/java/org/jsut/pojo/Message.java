package org.jsut.pojo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createTime;
}