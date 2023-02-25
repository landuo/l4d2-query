package io.github.landuo.l4d2.utils;

import io.github.landuo.l4d2.entity.A2sPlayers;
import io.github.landuo.l4d2.entity.Player;
import io.github.landuo.l4d2.entity.SourceServerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author accidia
 */
public class L4d2Util {

    public static SourceServerInfo getServerInfo(String ip, Integer port, String rconPwd) {
        SourceServerInfo a2sInfo;
        a2sInfo = A2sUtil.getA2sInfo(ip + ":" + port);
        if (a2sInfo.getName() != null) {
            return a2sInfo;
        }
        if (rconPwd == null || "".equals(rconPwd)) {
            return null;
        }
        String response = RconUtil.send(ip, port, rconPwd);
        List<String> data = Arrays.asList(response.split("\\n"));
        a2sInfo = new SourceServerInfo();
        a2sInfo.setGame("Left 4 Dead 2");
        a2sInfo.setName(data.stream().filter(d -> d.startsWith("hostname")).findFirst().get().replace("hostname: ", ""));
        a2sInfo.setMap(data.stream().filter(d -> d.startsWith("map")).findFirst().get().replace("map     : ", ""));
        String players = data.stream().filter(d -> d.startsWith("players")).findFirst().get();
        a2sInfo.setPlayers(Integer.valueOf(players.substring(players.indexOf(":") + 1, players.indexOf("humans")).trim()));
        a2sInfo.setMaxPlayers(Integer.valueOf(players.substring(players.indexOf("(") + 1, players.indexOf("max")).trim()));
        a2sInfo.setVac(data.stream().filter(d -> d.startsWith("version")).findFirst().get().contains("insecure") ? "insecure" : "secure");
        a2sInfo.setTimes(A2sUtil.getPing(ip + ":" + port));
        return a2sInfo;
    }

    public static A2sPlayers getPlayers(String ip, Integer port, String rconPwd) {
        A2sPlayers a2sPlayers;
        a2sPlayers = A2sUtil.getPlayers(ip, port);
        if (a2sPlayers != null) {
            return a2sPlayers;
        }
        if (rconPwd == null || "".equals(rconPwd)) {
            return null;
        }
        String response = RconUtil.send(ip, port, rconPwd);
        List<String> data = Arrays.asList(response.split("\\n"));
        a2sPlayers = new A2sPlayers();
        a2sPlayers.setTotal(0);
        String playerStr = data.stream().filter(d -> d.startsWith("players")).findFirst().get();
        int humans = Integer.parseInt(playerStr.substring(playerStr.indexOf(":") + 1, playerStr.indexOf("humans")).trim());
        if (humans == 0) {
            return a2sPlayers;
        }
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < humans; i++) {
            data.stream().filter(d -> d.contains("STEAM_1:")).forEach(human -> {
                String[] strings = human.split("\\s+");
                Player player = new Player(Integer.valueOf(strings[2]), strings[3], Long.valueOf(strings[6]), strings[5]);
                players.add(player);
            });
        }
        a2sPlayers.setPlayers(players);
        return a2sPlayers;
    }

}
