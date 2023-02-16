package io.github.landuo.l4d2.entity;

import lombok.Data;

/**
 * @author accidia
 */
@Data
public class Player {
    /**
     * Index of player chunk starting from 0.
     */
    private Integer index;
    /**
     * Name of the player.
     */
    private String name;
    /**
     * Player's score (usually "frags" or "kills".)
     */
    private Long score;
    /**
     * Time (in seconds) player has been connected to the server.
     */
    private String duration;
}
