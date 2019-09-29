package com.polycis.tcpservice.tcp;

import com.polycis.tcpservice.repository.TcpRepository;
import com.polycis.tcpservice.repository.database.DevDataUp;
import com.polycis.tcpservice.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author : Wenyu Zhou
 * @version : v1.0
 * @date : 2019/7/29
 * description : 描述
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class DiscardServerHandler extends ChannelHandlerAdapter {

    private TcpRepository tcp;

    @Autowired
    public void setTcp(TcpRepository tcp) {
        this.tcp = tcp;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        try {
            ByteBuf in = (ByteBuf) msg;
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            StringBuilder sBuilder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sBuilder.append(ByteUtil.toHexString(ByteUtil.subBytes(bytes, i, 1)))
                        .append(i != bytes.length - 1 ? " " : "");
            }
            //log.info("\n传输内容是 " + bytes.length);
            log.info("原始包: " + sBuilder.toString());
            //判断是否连包
            int minLen = 8;
            while (bytes.length >= minLen) {
                int len = Integer.valueOf(ByteUtil.toHexString(ByteUtil.subBytes(bytes, 4, 2)), 16);
                if(bytes.length >= len + 8){
                    byte[] newBytes = ByteUtil.subBytes(bytes, 0, len + 8);
                    bytes = ByteUtil.subBytes(bytes, len + 8, bytes.length - (8 + len));
                    //log.info("分包: " + ByteUtil.toHexString(newBytes));
                    doData(newBytes);
                }else if(bytes[3] == 0x13 ){
                    //超限电流容易丢包，先这样处理（拦截标记）
                    doData(bytes);
                    break;
                }else {
                    log.info("该信息不符合协议！");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 数据处理
     *
     * @param bytes
     */
    private void doData(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(ByteUtil.toHexString(ByteUtil.subBytes(bytes, i, 1)))
                    .append(i != bytes.length - 1 ? " " : "");
        }
        DevDataUp data = new DevDataUp();
        data.setPlatform(3);
        data.setMac("");
        data.setDataInfo("");
        data.setPushStatus(0);
        data.setCreateTime(new Date());
        data.setReportTime(new Date());
        data.setModifyTime(new Date());
        if (bytes[0] == 0x22 && bytes[1] == 0x22) {
            //22 22 00 10 00 12 31 38 00 00 00 00 31 38 00 00 00 00 31 38 00 00 00 00 F6 45
            if(bytes[2] == 0x00){
                data.setDeviceUuid("ffffff2000018413");
            }else if(bytes[2] == 0x01){
                data.setDeviceUuid("ffffff2000018415");
            }else if(bytes[2] == 0x02){
                data.setDeviceUuid("ffffff2000018416");
            }
            data.setEncodeData(stringBuilder.toString());
            if (bytes[3] == 0x10) {
                String eleA = ByteUtil.toHexStringReverse(ByteUtil.subBytes(bytes, 6, 2)).trim();
                String eleB = ByteUtil.toHexStringReverse(ByteUtil.subBytes(bytes, 8, 2)).trim();
                String eleC = ByteUtil.toHexStringReverse(ByteUtil.subBytes(bytes, 10, 2)).trim();
                String dec = "A项电流:" + Integer.valueOf(eleA, 16) * 10
                        + "mA; B项电流:" + Integer.valueOf(eleB, 16) * 10
                        + "mA; C项电流:" + Integer.valueOf(eleC, 16) * 10 + "mA;";
                data.setDecodeData(dec);
            } else {
                data.setDecodeData(getDataType(bytes[3]));
            }
            tcp.save(data);

        } else if (bytes[0] == 0x33 && bytes[1] == 0x33) {
            if(bytes[2] == 0x00){
                data.setDeviceUuid("ffffff2000018414");
            }else if(bytes[2] == 0x01){
                data.setDeviceUuid("ffffff2000018417");
            }
            data.setEncodeData(stringBuilder.toString());
            if (bytes[3] == 0x10) {
                String eleA = ByteUtil.toHexStringReverse(ByteUtil.subBytes(bytes, 6, 2)).trim();
                data.setDecodeData("电流值:" + Integer.valueOf(eleA, 16) * 10 + "mA;");
            } else {
                data.setDecodeData(getDataType(bytes[3]));
            }
            tcp.save(data);
        }
    }

    private String getDataType(byte bytes) {
        String str = "";
        if (bytes == 0x16) {
            str = "负载离线告警";
        } else if (bytes == 0x12) {
            str = "一键报警";
        } else if (bytes == 0x13) {
            str = "超限电流上报";
        } else if (bytes == 0x14) {
            str = "负载状态变化上报";
        } else if (bytes == 0x15) {
            str = "人员上报";
        }
        return str;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 出现异常就关闭
        cause.printStackTrace();
        ctx.close();
    }
}
