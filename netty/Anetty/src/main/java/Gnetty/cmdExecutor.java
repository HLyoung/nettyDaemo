
package Gnetty;

import java.util.ArrayList;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import Gnetty.wisUserParser.WisCmdID;

public class cmdExecutor implements Runnable{
	public int[] cmd = {wisUserParser.WisCmdID.WIS_CMD_USER_REGIST,
			wisUserParser.WisCmdID.WIS_CMD_USER_LOGIN,
			wisUserParser.WisCmdID.WIS_CMD_USER_BIND};
	
	public cmdExecutor(Map<ChannelHandlerContext,wisUser> mContextUser){     //命令执行器根据
		this._mContextUser = mContextUser;
	}
	
	public static  void handlerRegister(ChannelHandlerContext ctx,wisUser user){
		byte[] pack = new byte[2*wisUser.def.INT_LEN + wisUser.def.UUID_LEN + wisUser.def.PASSWORD_LEN];
		byte[] name = user.getUserId().getBytes();
		byte[] password = user.getPassword().getBytes();
		
		ByteIntConverter.convertInt2ByteArray(pack,0,WisCmdID.WIS_CMD_USER_REGIST);
		ByteIntConverter.convertInt2ByteArray(pack,wisUser.def.INT_LEN,wisUser.def.UUID_LEN + wisUser.def.PASSWORD_LEN);
		System.arraycopy(name,0,pack,2* wisUser.def.INT_LEN,name.length);
		System.arraycopy(password,0,pack,2*wisUser.def.INT_LEN + wisUser.def.UUID_LEN ,password.length);

		andCheckAndSend(pack,ctx);
	}
	
	public static void handlerLogin(ChannelHandlerContext ctx,wisUser user){
		byte[] pack = new byte[2*wisUser.def.INT_LEN + wisUser.def.UUID_LEN + wisUser.def.PASSWORD_LEN + wisUser.def.TOKEN_LEN];
		byte[] name = user.getUserId().getBytes();
		byte[] password = user.getPassword().getBytes();
		byte[] token = user.getToken().getBytes();
		
		ByteIntConverter.convertInt2ByteArray(pack,0,WisCmdID.WIS_CMD_USER_LOGIN);
		ByteIntConverter.convertInt2ByteArray(pack,wisUser.def.INT_LEN,wisUser.def.UUID_LEN + wisUser.def.PASSWORD_LEN + wisUser.def.TOKEN_LEN);
		System.arraycopy(name,0,pack,2*wisUser.def.INT_LEN,name.length);
		System.arraycopy(password,0,pack,2*wisUser.def.INT_LEN + wisUser.def.UUID_LEN ,password.length);
		System.arraycopy(token,0,pack,2*wisUser.def.INT_LEN + wisUser.def.UUID_LEN +wisUser.def.PASSWORD_LEN,token.length);
		  
		andCheckAndSend(pack,ctx);
	}

	public static void handlerBind(ChannelHandlerContext ctx,wisUser user){
		byte[] pack = new byte[2*wisUser.def.INT_LEN + wisUser.def.DEVID_LEN];
		byte[] devId = user.getDeviceId().getBytes();
		
		ByteIntConverter.convertInt2ByteArray(pack,0,WisCmdID.WIS_CMD_USER_BIND);
		ByteIntConverter.convertInt2ByteArray(pack,wisUser.def.INT_LEN,wisUser.def.DEVID_LEN);
		System.arraycopy(devId,0,pack,2*wisUser.def.INT_LEN,devId.length);
		
		andCheckAndSend(pack,ctx);
	}
	
	public static  void handlerHeartBeat(ChannelHandlerContext ctx,wisUser user){
		byte[] pack = new byte[2*wisUser.def.INT_LEN + wisUser.def.INT_LEN];
		
		ByteIntConverter.convertInt2ByteArray(pack, 0, WisCmdID.WIS_CMD_USER_HEART_BEAT);
		ByteIntConverter.convertInt2ByteArray(pack, wisUser.def.INT_LEN, wisUser.def.INT_LEN);
		ByteIntConverter.convertInt2ByteArray(pack,2*wisUser.def.INT_LEN,0);
		
		andCheckAndSend(pack,ctx);
	}
	
	public static void andCheckAndSend(byte[] srcByte,ChannelHandlerContext ctx){
		int checkSum = 0;
		for(int i = 0;i < srcByte.length;i++){
			int value = srcByte[i];
			checkSum += value & 0xFF;
		}	
		byte[] pack = new byte[srcByte.length + wisUser.def.INT_LEN];
		ByteIntConverter.convertInt2ByteArray(pack,0,checkSum);
		System.arraycopy(srcByte,0,pack,wisUser.def.INT_LEN,srcByte.length);
		
		ByteBuf regBuf = Unpooled.buffer(pack.length);
		regBuf.writeBytes(pack);
		ctx.writeAndFlush(regBuf);
//		cmd_startTime =  System.currentTimeMillis();
	}
	
	public void handlerCmd(int cmd,ChannelHandlerContext ctx,wisUser user)
	{
		switch(cmd){
			case WisCmdID.WIS_CMD_USER_REGIST:{
				handlerRegister(ctx,user);
				break;
			}
			case WisCmdID.WIS_CMD_USER_LOGIN:{
				handlerLogin(ctx,user);
				break;
			}
			case WisCmdID.WIS_CMD_USER_BIND:{
				handlerBind(ctx,user);
				break;
			}
			default:
				break;
		}
	}
	public void run(){
		while(true){
			for(ChannelHandlerContext ctx:_mContextUser.keySet()){
				wisUser user = _mContextUser.get(ctx);
				for(int _cmd:cmd){
					handlerCmd(_cmd,ctx,user);
					try{
						Thread.sleep(wisUser.def.CMD_INTERVAL);
					}catch(InterruptedException ex){
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	private Map<ChannelHandlerContext,wisUser> _mContextUser;
}