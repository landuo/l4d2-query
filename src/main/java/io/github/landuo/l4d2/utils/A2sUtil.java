package io.github.landuo.l4d2.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.landuo.l4d2.entity.A2sPlayers;
import io.github.landuo.l4d2.entity.Player;
import io.github.landuo.l4d2.entity.SourceServerInfo;
import io.github.landuo.l4d2.exception.ServiceException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author accidia
 * @see <a href="https://developer.valvesoftware.com/wiki/Server_queries">Server Queries</a>
 */
public class A2sUtil {
    private static final String A2S_INFO = "FF FF FF FF 54 53 6F 75 72 63 65 20 45 6E 67 69 6E 65 20 51 75 65 72 79 00 FF FF FF FF";
    private static final String A2S_PLAYER = "FF FF FF FF 55 FF FF FF FF";
    private static final String A2S_RULE = "FF FF FF FF 56 FF FF FF FF";
    private static final String A2S_PING = "FF FF FF FF 69";

    public static Integer soTimeOut = 2000;

    /**
     * 计算服务器延迟
     *
     * @param ipPort ip:port
     * @return 服务器延迟, 结果为-1时表示连接不上服务器
     */
    public static Long getPing(String ipPort) {
        long current = System.currentTimeMillis();
        String[] split = ipPort.split(":");
        String[] payload = A2S_PING.split(" ");
        ByteBuffer buffer = ByteBuffer.allocate(payload.length);
        for (String s : payload) {
            buffer.put((byte) Integer.parseInt(s, 16));
        }
        DatagramSocket ds;
        try {
            ds = new DatagramSocket();
            ds.setSoTimeout(soTimeOut);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
            ds.connect(inetSocketAddress);
            // 请求challenge number
            DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.array().length, inetSocketAddress);
            ds.send(packet);
            byte[] challengeResponse = new byte[100];
            packet = new DatagramPacket(challengeResponse, challengeResponse.length);
            ds.receive(packet);
            ds.close();
        } catch (IOException e) {
            return -1L;
        }
        return (System.currentTimeMillis() - current);
    }

    /**
     * Returns the server rules, or configuration variables in name/value pairs.
     *
     * @param ipPort ip:port
     * @return server configuration variables
     */
    public static Map<String, String> getA2sRules(String ipPort) {
        List<byte[]> list;
        String[] split = ipPort.split(":");
        try {
            list = send(split[0], Integer.valueOf(split[1]), A2S_RULE);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        byte[] data = new byte[list.stream().mapToInt(e -> e.length).sum()];
        int tempIndex = 0;
        // 组装成一个大数组
        for (int i = 0; i < list.size(); i++) {
            final int thisIndex = i;
            Optional<byte[]> optional = list.stream().filter(e -> e[9] == thisIndex).findFirst();
            if (!optional.isPresent()) {
                continue;
            }
            byte[] response = optional.get();
            int j = (thisIndex == 0) ? 19 : 12;
            System.arraycopy(response, j, data, tempIndex, response.length - j);
            tempIndex += response.length - j;
        }
        Map<String, String> result = new LinkedHashMap<>();
        byte[] temp = new byte[100];
        String key = null;
        tempIndex = 0;
        for (byte datum : data) {
            if (datum == 0) {
                if ("".equals(key)) {
                    break;
                }
                // key为null时说明已经存放完一对键值对
                if (key == null) {
                    key = new String(temp, StandardCharsets.UTF_8).trim();
                } else {
                    result.put(key, new String(temp, StandardCharsets.UTF_8).trim());
                    key = null;
                }
                temp = new byte[100];
                // 重置缓存数组计数
                tempIndex = 0;
                continue;
            }
            temp[tempIndex++] = datum;
        }
        return result;
    }

    /**
     * 查询服务器信息
     *
     * @param ipPort ip:port
     * @return 新source server返回的服务器信息
     */
    public static SourceServerInfo getA2sInfo(String ipPort) {
        List<byte[]> list;
        String[] split = ipPort.split(":");
        try {
            list = send(split[0], Integer.valueOf(split[1]), A2S_INFO);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        Map<String, Object> result = new HashMap<>(16);
        byte[] response = list.get(0);
        if (response[4] == (byte) 0x49) {
            getA2sInfoResponseWithNewApi(result, response);
        }
        result.put("times", getPing(ipPort));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(result, SourceServerInfo.class);
    }

    /**
     * 获取服务器玩家信息
     *
     * @param ipPort ip:port
     * @return 服务器当前玩家信息
     */
    public static A2sPlayers getPlayers(String ipPort) {
        String[] arr = ipPort.split(":");
        List<byte[]> list = null;
        try {
            list = send(arr[0], Integer.valueOf(arr[1]), A2S_PLAYER);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        A2sPlayers a2sPlayers = new A2sPlayers();
        byte[] response = list.get(0);
        if (response[4] == (byte) 0x44) {
            int i = 5;
            int total = response[i++];
            a2sPlayers.setTotal(total);
            List<Player> players = new LinkedList<>();
            for (int j = 0; j < total; j++) {
                Player player = new Player();
                player.setIndex((int) response[i++]);
                byte[] tmp = new byte[100];
                int k = 0;
                while (response[i] != 0) {
                    tmp[k++] = response[i++];
                }
                i++;
                player.setName(new String(tmp, StandardCharsets.UTF_8).trim());

                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int z = 0; z < 4; z++) {
                    buffer.put(response[i++]);
                }
                player.setScore((long) buffer.getInt(0));
                buffer.clear();
                for (int z = 0; z < 4; z++) {
                    buffer.put(z, response[i++]);
                }
                player.setDuration(convertFloatToDurationTime(buffer.getFloat(0)));
                players.add(player);
            }
            players.sort(Comparator.comparing(Player::getScore).reversed());
            a2sPlayers.setPlayers(players);
            return a2sPlayers;
        }
        return null;
    }

    /**
     * float类型数据转化成 0h0m0s 格式
     */
    private static String convertFloatToDurationTime(Float duration) {
        int intValue = duration.intValue();
        if (intValue < 60) {
            return intValue + "s";
        }
        if (intValue <= 3600) {
            return (intValue / 60) + "m" + (intValue % 60) + "s";
        }
        int h = intValue / 3600;
        int m = intValue - (h * 3600);
        return h + "h" + (m / 60) + "m" + (m % 60) + "s";
    }

    /**
     * 组装服务器信息结果
     *
     * @param result   返回的结果
     * @param response 接收到的Source Server数据
     */
    private static void getA2sInfoResponseWithNewApi(Map<String, Object> result, byte[] response) {
        int i = 6;
        // 取前四个内容转ascii
        i = putStrToResult(response, i, result, "name");
        i = putStrToResult(response, i, result, "map");
        i = putStrToResult(response, i, result, "folder");
        i = putStrToResult(response, i, result, "game");

        // Steam Application ID of game.
        i = putNumberToResult(2, response, i, result, "id");
        // Number of players on the server.
        result.put("players", String.valueOf(response[i++]));
        // Maximum number of players the server reports it can hold.
        result.put("maxPlayers", String.valueOf(response[i++]));
        //Number of bots on the server.
        result.put("bots", String.valueOf(response[i++]));
        //Indicates the type of server:
        result.put("serverType", String.valueOf((char) response[i++]));
        //Indicates the operating system of the server:
        char serverType = (char) response[i++];
        result.put("environment", "l".equals(String.valueOf(serverType)) ? "Linux" : ("w".equals(String.valueOf(serverType)) ? "Windows" : "Mac"));
        //Indicates whether the server requires a password:
        result.put("visibility", "0".equals(String.valueOf(response[i++])) ? "public" : "private");
        //Indicates whether the server requires a password:
        result.put("vac", "0".equals(String.valueOf(response[i++])) ? "unsecured" : "secured");
        // Version of the game installed on the server.
        i = putStrToResult(response, i, result, "version");

        // 结束标识位
        byte EDF = response[i++];
        if ((EDF & (byte) 0x80) != 0) {
            i = putNumberToResult(2, response, i, result, "port");
        }
        if ((EDF & (byte) 0x10) != 0) {
            i = putNumberToResult(8, response, i, result, "steamID");
        }
        if ((EDF & (byte) 0x40) != 0) {
            i = putNumberToResult(2, response, i, result, "sourceTVPort");
            i = putStrToResult(response, i, result, "sourceTVName");
        }
        if ((EDF & (byte) 0x20) != 0) {
            i = putStrToResult(response, i, result, "keywords");
        }
        if ((EDF & (byte) 0x01) != 0) {
            i = putNumberToResult(8, response, i, result, "gameID");
        }
    }

    /**
     * 发送查询命令并接收结果
     */
    private static List<byte[]> send(String ip, Integer port, String request) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
        DatagramSocket ds = new DatagramSocket();
        ds.setSoTimeout(soTimeOut);
        ds.connect(inetSocketAddress);
        String[] payload = request.split(" ");
        ByteBuffer buffer = ByteBuffer.allocate(payload.length);
        for (String s : payload) {
            buffer.put((byte) Integer.parseInt(s, 16));
        }
        // 请求challenge number
        DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.array().length, inetSocketAddress);
        ds.send(packet);
        byte[] challengeResponse = new byte[9];
        packet = new DatagramPacket(challengeResponse, challengeResponse.length);
        ds.receive(packet);

        for (int i = 0; i < 4; i++) {
            buffer.put(payload.length - 4 + i, challengeResponse[i + 5]);
        }
        packet = new DatagramPacket(buffer.array(), 0, buffer.limit(), inetSocketAddress);
        ds.send(packet);
        List<byte[]> list = new LinkedList<>();
        byte[] response = new byte[4096];
        try {
            while (true) {
                packet = new DatagramPacket(response, response.length);
                ds.receive(packet);
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(response, 0, data, 0, packet.getLength());
                list.add(data);
                if (response[0] == (byte) 0xFF) {
                    return list;
                }
            }
        } catch (SocketTimeoutException ignored) {
            list.sort(Comparator.comparing(e -> e[9]));
        }
        ds.close();
        return list;
    }

    /**
     * 组装字符串类型结果
     */
    private static int putStrToResult(byte[] response, int i, Map<String, Object> result, String key) {
        int j = 0;
        byte[] bytes = new byte[100];
        for (; i < response.length; i++) {
            if (response[i] == 0) {
                result.put(key, new String(bytes).trim());
                break;
            }
            bytes[j++] = response[i];
        }
        return ++i;
    }

    /**
     * 组装数字类型结果
     */
    private static int putNumberToResult(Integer capacity, byte[] response, Integer i, Map<String, Object> result, String key) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int j = 0; j < capacity; j++) {
            buffer.put(response[i++]);
        }
        // 根据容量选择需要的数字类型
        String number = null;
        switch (capacity) {
            case 1:
                number = String.valueOf(buffer.get(0));
                break;
            case 2:
                number = String.valueOf(buffer.getShort(0));
                break;
            case 4:
                number = String.valueOf(buffer.getInt(0));
                break;
            //long long type
            case 8:
                number = String.valueOf(buffer.getLong(0));
                break;
            default:
                break;
        }
        result.put(key, number);
        return i;
    }

}
