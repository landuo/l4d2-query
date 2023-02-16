package io.github.landuo.l4d2.entity;

import lombok.Data;

/**
 * @author accidia
 */
@Data
public class SourceServerInfo {
    /**
     * Steam Application ID of game.
     */
    private Integer id;
    /**
     * Name of the server.
     */
    private String name;
    /**
     * Map the server has currently loaded.
     */
    private String map;
    /**
     * Name of the folder containing the game files.
     */
    private String folder;
    /**
     * Full name of the game.
     */
    private String game;
    /**
     * Number of players on the server.
     */
    private Integer players;
    /**
     * Maximum number of players the server reports it can hold.
     */
    private Integer maxPlayers;
    /**
     * Number of bots on the server.
     */
    private Integer bots;
    /**
     * Indicates the type of server:
     * 'd' for a dedicated server,
     * 'l' for a non-dedicated server,
     * 'p' for a SourceTV relay (proxy)
     */
    private String serverType;
    /**
     * Indicates the operating system of the server:
     * 'l' for Linux,
     * 'w' for Windows,
     * 'm' or 'o' for Mac (the code changed after L4D1)
     */
    private String environment;
    /**
     * Indicates whether the server requires a password:
     * 0 for public,
     * 1 for private
     */
    private String visibility;
    /**
     * Specifies whether the server uses VAC:
     * 0 for unsecured,
     * 1 for secured
     */
    private String vac;
    /**
     * Version of the game installed on the server.
     */
    private String version;
    /**
     * The server's game port number.
     */
    private Integer port;
    /**
     * Server's SteamID.
     */
    private Long steamID;
    /**
     * Spectator port number for SourceTV.
     */
    private Integer sourceTVPort;
    /**
     * Name of the spectator server for SourceTV.
     */
    private String sourceTVName;
    /**
     * Tags that describe the game according to the server (for future use.)
     */
    private String keywords;
    /**
     * The server's 64-bit GameID. If this is present, a more accurate AppID is present in the low 24 bits.
     * The earlier AppID could have been truncated as it was forced into 16-bit storage.
     */
    private Long gameID;
    /**
     * the latency to the server.
     */
    private Long times;

}
