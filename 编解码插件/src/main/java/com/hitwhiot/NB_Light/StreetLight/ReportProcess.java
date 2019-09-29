package com.hitwhiot.NB_Light.StreetLight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReportProcess {
    //private String identifier;

    private String msgType = "deviceReq";
    private int hasMore = 0;
    private int errcode = 0;
    private byte bDeviceReq = 0x00;
    private byte bDeviceRsp = 0x01;


    private int light = 0;               ////////////Bean define date



    private byte noMid = 0x00;
    private byte hasMid = 0x01;
    private boolean isContainMid = false;
    private int mid = 0;

    /**
     * @param (binaryData[0]<<2)+(binaryData[1]) 设备发送给平台coap报文的payload部分
     *                   本例入参：AA 72 00 00 32 08 8D 03 20 62 33 99
     *                   byte[0]--byte[1]:  AA 72 命令头
     *                   byte[2]:   00 mstType 00表示设备上报数据deviceReq
     *                   byte[3]:   00 hasMore  0表示没有后续数据，1表示有后续数据，不带按照0处理
     *                   byte[4]--byte[11]:服务数据，根据需要解析//如果是deviceRsp,byte[4]表示是否携带mid, byte[5]--byte[6]表示短命令Id
     * @return
     */
    public ReportProcess(byte[] binaryData) {
    	//public ReportProcess(byte[] binaryData) {
        // identifier参数可以根据入参的码流获得，本例指定默认值123
        // identifier = "123";

        /*
        如果是设备上报数据，返回格式为
        {
            "identifier":"123",
            "msgType":"deviceReq",
            "hasMore":0,
            "data":[{"serviceId":"Brightness",
                      "serviceData":{"brightness":50},
                      {
                      "serviceId":"Electricity",
                      "serviceData":{"voltage":218.9,"current":800,"frequency":50.1,"powerfactor":0.98},
                      {
                      "serviceId":"Temperature",
                      "serviceData":{"temperature":25},
                      ]
	    }
	    */
    	msgType = "deviceReq";        ////设备上报标志
    	light=0;
    	for (int i=0;i<5;i++) {
    		if (binaryData[i]==20) {
    			binaryData[i]=00;
    			//light=light*10+(int)binaryData[i];
    		}
    		else {
    			binaryData[i]=(byte) ((int)binaryData[i]&0x0f);
    		}
    			
    	}
    	light =(int) (((int)binaryData[0]*10000)+((int)binaryData[1]*1000)+((int)binaryData[2]*100)+((int)binaryData[3]*10)+((int)binaryData[4]));
    	//light =(int) (((int)binaryData[4]<<8)+((int)binaryData[3]<<6)+((int)binaryData[2]<<4)+((int)binaryData[1]<<2)+((int)binaryData[0]));
    	/**
        if (binaryData[2] == bDeviceReq) {
            msgType = "deviceReq";
            hasMore = binaryData[3];

            //serviceId=Brightness 数据解析
            brightness = binaryData[4];

            //serviceId=Electricity 数据解析
            voltage = (double) (((binaryData[5] << 8) + (binaryData[6] & 0xFF)) * 0.1f);
            current = (binaryData[7] << 8) + binaryData[8];
            powerfactor = (double) (binaryData[9] * 0.01);
            frequency = (double) binaryData[10] * 0.1f + 45;

            //serviceId=Temperature 数据解析
            temperature = (int) binaryData[11] & 0xFF - 128;
        }
        **/
        /*
        如果是设备对平台命令的应答，返回格式为：
       {
            "identifier":"123",
            "msgType":"deviceRsp",
            "errcode":0,
            "body" :{****} 特别注意该body体为一层json结构。
        }
	    */
    	/**
        else if (binaryData[2] == bDeviceRsp) {
            msgType = "deviceRsp";
            errcode = binaryData[3];
            //此处需要考虑兼容性，如果没有传mId，则不对其进行解码
            if (binaryData[4] == hasMid) {
                mid = Utilty.getInstance().bytes2Int(binaryData, 5, 2);
                if (Utilty.getInstance().isValidofMid(mid)) {
                    isContainMid = true;
                }

            }
        } else {
            return;
        }
**/

    }

    public ObjectNode toJsonNode() {
        try {
            //组装body体
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            root.put("msgType", "deviceReq");
            ArrayNode arrynode = mapper.createArrayNode();
            //serviceId=Battery 数据组装
            ObjectNode serviceNode = mapper.createObjectNode();
            ObjectNode serviceDataNode = mapper.createObjectNode();
            serviceDataNode.put("Light", this.light);
            serviceNode.put("serviceId", "Light");
            serviceNode.set("serviceData", serviceDataNode);
            arrynode.add(serviceNode);
            
            root.set("data", arrynode);
            return root;
            

/**
            } else {
                root.put("errcode", this.errcode);
                //此处需要考虑兼容性，如果没有传mid，则不对其进行解码
                if (isContainMid) {
                    root.put("mid", this.mid);//mid
                }
                //组装body体，只能为ObjectNode对象
                ObjectNode body = mapper.createObjectNode();
                body.put("result", 0);
                root.put("body", body);
            }
            return root;
            */
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}