package io.github.landuo.l4d2.entity;

import lombok.Data;

import java.util.List;

/**
 * @author accidia
 */
@Data
public class A2sPlayers {
    private Integer total;

    private List<Player> players;

}
