package Gnetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class wisUserHandler extends ChannelHandlerAdapter {
	private static final Logger logger=Logger.getLogger(wisUserHandler.class.getName());
	private wisUser _owner;
	private wisUserParser _parser;
	
	public wisUser getOwner(){
		return _owner;
	}

	public wisUserHandler(wisUser owner,Map<ChannelHandlerContext,wisUser> mContextUser){
		this._owner = owner;
		this._mContextUser = mContextUser;
		this._parser = new wisUserParser(this);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		_mContextUser.put(ctx,_owner);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf srcBuf = (ByteBuf) msg;
		ByteBuf buf = srcBuf.order(ByteOrder.LITTLE_ENDIAN);  //trun to little endian.
		while(3*wisUser.def.INT_LEN  < buf.readableBytes()){
			buf.markReaderIndex();     //mark the readindex
			int checkSum = buf.readInt();
			int cmd = buf.readInt();
			int dataLen = buf.readInt();
			if(dataLen <= buf.readableBytes()){
				byte[] pack = new byte[dataLen];
				buf.readBytes(pack,0,pack.length);
				_parser.handlerResponse(ctx,checkSum,cmd,dataLen,pack);
			}
			else {
				buf.resetReaderIndex();  //reset readindex
				break;
			}
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		_mContextUser.remove(ctx);
		ctx.close();
		System.out.printf("客户端异常退出: ");
		cause.printStackTrace();
	}
	
	private Map<ChannelHandlerContext, wisUser> _mContextUser;
}
