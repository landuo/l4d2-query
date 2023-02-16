package io.github.landuo.l4d2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author accidia
 */
@AllArgsConstructor
public enum PacketTypeEnum {
    SERVERDATA_AUTH(3), SERVERDATA_AUTH_RESPONSE(2), SERVERDATA_EXECCOMMAND(2), SERVERDATA_RESPONSE_VALUE(0);

    @Getter
    private Integer value;

}
