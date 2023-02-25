package io.github.landuo.l4d2.utils;

import io.github.landuo.l4d2.entity.RconRequest;
import io.github.landuo.l4d2.enums.PacketTypeEnum;
import io.github.landuo.l4d2.exception.ServiceException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author accidia
 * @see <a href="https://developer.valvesoftware.com/wiki/Source_RCON_Protocol">Source_RCON_Protocol</a>
 */
public class RconUtil {

    protected static String send(String ip, Integer port, String password) {
        RconRequest request = new RconRequest(ip, port, password, "status");
        return send(request);
    }

    /**
     * 发送指令到服务器上
     *
     * @param request 请求对象
     * @return 指令执行后的结果
     */
    public static String send(RconRequest request) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(request.getIp(), request.getPort()), 2000);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        Random random = new Random();
        int id = random.nextInt();
        List<byte[]> array = new ArrayList<>();
        try {
            // 先进行认证
            write(socket.getOutputStream(), id, PacketTypeEnum.SERVERDATA_AUTH, request.getPasswd());
            read(id, socket.getInputStream());
            read(id, socket.getInputStream());
            id = random.nextInt();
            // 发送命令
            write(socket.getOutputStream(), id, PacketTypeEnum.SERVERDATA_EXECCOMMAND, request.getCmd());

            // 持续读数据
            socket.setSoTimeout(500);
            while (true) {
                array.add(read(id, socket.getInputStream()));
            }
        } catch (SocketTimeoutException socketTimeoutException) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new ServiceException("Failed to close socket.");
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        StringBuilder builder = new StringBuilder();
        for (byte[] data : array) {
            builder.append(new String(data, StandardCharsets.UTF_8));
        }
        return builder.toString();

    }

    /**
     * 写命令到服务器上
     *
     * @param out            输出流
     * @param id             数据包的ID
     * @param packetTypeEnum 数据包的类型
     * @param body           数据包内容
     */
    private static void write(OutputStream out, Integer id, PacketTypeEnum packetTypeEnum, String body) {
        ByteBuffer bb = ByteBuffer.allocate(4 + 4 + 4 + body.getBytes().length + 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        // 数据包大小
        bb.putInt(bb.limit() - 4);
        bb.putInt(id);
        bb.putInt(packetTypeEnum.getValue());
        bb.put(body.getBytes());
        //最后两位终止标志
        bb.put((byte) 0);
        bb.put((byte) 0);
        try {
            out.write(bb.array());
        } catch (IOException e) {
            throw new ServiceException("Failed to send cmd.");
        }
    }

    private static byte[] read(Integer id, InputStream in) throws IOException {
        byte[] header = new byte[4 * 3];
        in.read(header);
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int packetSize = bb.getInt();
        int requestId = bb.getInt();
        // 不知为啥0也是密码错误
        if (requestId == -1 || requestId == 0) {
            throw new ServiceException(400, "Password error.");
        }
        if (requestId == id) {
            byte[] payload = new byte[packetSize - 4 - 4 - 2];
            DataInputStream dataInputStream = new DataInputStream(in);
            dataInputStream.readFully(payload);
            // 读取最后两个终止字符
            dataInputStream.read(new byte[2]);
            return payload;
        }
        return null;
    }
}
