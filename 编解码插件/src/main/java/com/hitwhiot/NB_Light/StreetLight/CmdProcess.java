package com.hitwhiot.NB_Light.StreetLight;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CmdProcess {

    //private String identifier = "123";
    private String msgType = "deviceReq";
    private String serviceId = "Light";
    private String cmd = "Control";
    private int hasMore = 0;
    private int errcode = 0;
    private int mid = 0;
    private JsonNode paras;


    public CmdProcess() {
    }

    public CmdProcess(ObjectNode input) {

        try {
            // this.identifier = input.get("identifier").asText();
            this.msgType = input.get("msgType").asText();
            /*
            平台收到设备上报消息，编码ACK
            {
                "identifier":"0",
                "msgType":"cloudRsp",
                "request": ***,//设备上报的码流
                "errcode":0,
                "hasMore":0
            }
            * */
            if (msgType.equals("cloudRsp")) {
                //在此组装ACK的值
                this.errcode = input.get("errcode").asInt();
                this.hasMore = input.get("hasMore").asInt();
            } else {
            /*
            
            * */
                //此处需要考虑兼容性，如果没有传mId，则不对其进行编码
                if (input.get("mid") != null) {
                    this.mid = input.get("mid").intValue();
                }
                this.cmd = input.get("cmd").asText();
                this.paras = input.get("paras");
                this.hasMore = input.get("hasMore").asInt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] toByte() {
        try {
            if (this.msgType.equals("cloudReq")) {
                /*
                应用服务器下发的控制命令，本例只有一条控制命令：Control
                如果有其他控制命令，增加判断即可。
                * */
            	/*
            	平台下发命令到设备，输入
                {
                    "identifier":0,
                    "msgType":"cloudReq",
                    "serviceId":"Light",
                    "cmd":"Control",
                    "mid":0,
                    "paras":{"LED":"ON"},
                    "hasMore":0
                }
                */
            	
                if (this.cmd.equals("Control")) {
                	byte[] downData = new byte[4];
                	String Data = paras.get("LED").asText();
                	downData = Data.getBytes();

                    //此处需要考虑兼容性，如果没有传mId，则不对其进行编码
                	/*
                    if (Utilty.getInstance().isValidofMid(mid)) {
                        byte[] byteMid = new byte[2];
                        byteMid = Utilty.getInstance().int2Bytes(mid, 2);
                        buf.writeByte(byteMid[0]);
                        buf.writeByte(byteMid[1]);
                    }
					*/
                    return downData;
                }
            }

            /*
            平台收到设备的上报数据，根据需要编码ACK，对设备进行响应，如果此处返回null，表示不需要对设备响应。
            * */
            else if (this.msgType.equals("cloudRsp")) {
                byte[] ack = new byte[4];
                ByteBufUtils buf = new ByteBufUtils(ack);
                buf.writeByte((byte) 0xAA);
                buf.writeByte((byte) 0xAA);
                buf.writeByte((byte) this.errcode);
                buf.writeByte((byte) this.hasMore);
                return ack;
            }
            return null;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

}
