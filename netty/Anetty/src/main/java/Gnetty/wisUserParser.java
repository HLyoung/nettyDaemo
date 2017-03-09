
package Gnetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.io.*;
public class wisUserParser{
	
	public interface WisCmdID {
		int WIS_CMD_UNKNOWN = -1;
		int WIS_CMD_HEART_BEAT = 0x8000;
		int WIS_CMD_LOGIN = 0x8001;
		int WIS_CMD_POWEROFF = 0x8002;
		int WIS_CMD_REBOOT = 0x8003;
		int WIS_CMD_WEBSIGNAL = 0x8004;
		int WIS_CMD_RESULT = 0x8005;
		/* commands for pc client(Android/iOS/PC/...) */
		int WIS_CMD_USER_HEART_BEAT = 0x8000;
		int WIS_CMD_USER_PRIVATE = 0x8081;
		int WIS_CMD_USER_LOGIN = 0x8082;
		int WIS_CMD_USER_BIND = 0x8083;
		int WIS_CMD_USER_UNBIND = 0x8084;
		int WIS_CMD_USER_GET_DEVICES = 0x8085;
		int WIS_CMD_USER_GET_DEVICE_INFO = 0x8086;
		int WIS_CMD_USER_GET_DEVICE_STATUS = 0x8087;
		int WIS_CMD_USER_SIGN_UP = 0x8091;
		int WIS_CMD_USER_CHANGE_PASSWORD = 0x8092;
		int WIS_CMD_USER_RESET_PASSWORD = 0x8094;
		int WIS_CMD_USER_LOGIN_OUT = 0x8096;
		int WIS_CMD_USER_COM_LOGIN = 0X8095;
		/* command for wis_client(light) */
		int WIS_CMD_TX_LED_ON = 0x8200;
		int WIS_CMD_TX_LED_OFF = 0x8201;
		int WIS_CMD_RX_LED_ON = 0x8202;
		int WIS_CMD_RX_LED_OFF = 0x8203;
		/* command for wis_client(signal ) */
		int WIS_CMD_LIGHT_SIGNAL = 0x9000;
		/* command for gateway */
		int WIS_CMD_URL = 0x7000;
		int WIS_CMD_TO_USER = 0x8006;
		int WIS_CMD_TO_DEVICE_CHANGE = 0X2006;
		
		int WIS_CMD_USER_REGIST = 0x8091;
	}
	
	private ChannelHandlerAdapter _owner;
	private long cmd_startTime = 0;
	
	public wisUserParser(ChannelHandlerAdapter owner){
		this._owner = owner;
	}
	
	public void  handlerResponse(ChannelHandlerContext ctx, int checkSum,int cmd,int dataLen,byte []data){
		String userName = ((wisUserHandler)_owner).getOwner().getUserId();
		long time = System.currentTimeMillis() - cmd_startTime;
		cmd_startTime = System.currentTimeMillis();
		if(getCheckSum(cmd,dataLen,data) == checkSum){
			switch(cmd){
				case WisCmdID.WIS_CMD_USER_REGIST:{							
					System.out.println(userName + " 注册OK:" + time + " millseconds");
					break;
				}
				case WisCmdID.WIS_CMD_USER_LOGIN:{
					System.out.println(userName + " 登录OK:" + time + " millseconds");
					break;
				}
				
				case WisCmdID.WIS_CMD_USER_BIND:{
					System.out.println(userName + " 绑定OK:" + time + " millseconds");
					break;
				}
				case WisCmdID.WIS_CMD_HEART_BEAT:{
					System.out.println(userName + " Heart Beat Received.");  //send heart beat response immediate
					cmdExecutor.handlerHeartBeat(ctx,((wisUserHandler)_owner).getOwner());
					break;
				}
				default:
					System.out.println(userName + " 数据错误");
			}
		}else{
			System.out.println(userName + " 验证码错误");
		}				
	}
	
	
	private int getCheckSum(byte[] data) {
		int checkSumSend = 0;
		int byteSend = data.length;
		for (int i = 0; i < byteSend; i++) {
			int a = data[i];
			if (a < 0)
				a = (a & 0x7F) + 128;
			checkSumSend += a;  
		}
		return checkSumSend;
	}

	private int getCheckSum(int cmdId, int dataLen, byte[] data) {
		int checkSumSend = 0;
		byte[] tmp = new byte[4];
		ByteIntConverter.convertInt2ByteArray(tmp, 0, cmdId);
		checkSumSend += getCheckSum(tmp);
		ByteIntConverter.convertInt2ByteArray(tmp, 0, dataLen);
		checkSumSend += getCheckSum(tmp);
		checkSumSend += getCheckSum(data);
		return checkSumSend;
	}
}