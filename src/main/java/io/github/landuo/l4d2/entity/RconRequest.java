package io.github.landuo.l4d2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author accidia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RconRequest {
    /**
     * 服务器IP
     */
    private String ip;
    /**
     * 服务器端口
     */
    private Integer port;
    /**
     * rcon密码
     */
    private String passwd;
    /**
     * 执行的命令
     */
    private String cmd;

}
