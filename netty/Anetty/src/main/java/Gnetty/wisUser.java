package Gnetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class wisUser {
	/**
	 * 连接服务器
	 * @param port
	 * @param host
	 * @throws Exception
	 */
	public interface def{
		final int UUID_LEN = 32;
		final int PASSWORD_LEN = 24;
		final int CMD_LEN = 4;
		final int TOKEN_LEN = 72;
		final int INT_LEN = 4;
		final int DEVID_LEN = 32;
		final int CMD_INTERVAL = 1000;
	}
	
	public wisUser(String userId,String token,String password,String deviceId){
		this._userId = userId;
		this._token = token;
		this._password = password;
		this._deviceId = deviceId;
	}
	
	public String getUserId(){
		return _userId;
	}
	
	public String getToken(){
		return _token;
	}
	
	public String getPassword(){
		return _password;
	}
	public String getDeviceId(){
		return _deviceId;
	}

	public static void main(String[] args) throws Exception {
		if(args.length < 4){
			System.out.println("Usage:./wisUSer host port deviceUuid connectNum");
			System.exit(0);
		}
		final int connectNum = Integer.parseInt(args[3]);
		final int port = Integer.parseInt(args[1]);
		
		InetAddress addr = null;
		try{
			addr = InetAddress.getByName(args[0]);
		}catch(UnknownHostException e){
			e.printStackTrace();
		}
		final String host = addr.getHostAddress();		
		final String deviceUuid = args[2];
		
		final Map<ChannelHandlerContext,wisUser> mContextUser  = new HashMap<ChannelHandlerContext,wisUser>();
		
		EventLoopGroup group = new NioEventLoopGroup();
		ChannelFuture fArray[] = new ChannelFuture[connectNum];
		for(int i = 0;i < connectNum;i++){
			try {
				final String tmpUserID = "userID_" + i; 
				final wisUser _this = new wisUser(tmpUserID,"0123456789abcdefghi","password",deviceUuid);
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class)
						.option(ChannelOption.TCP_NODELAY, true)
						//.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
						.option(ChannelOption.SO_KEEPALIVE,true)
						.handler(new ChannelInitializer<SocketChannel>() {
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new wisUserHandler(_this,mContextUser));
							}
						});
				fArray[i] = b.connect(host, port);
				fArray[i].addListener(new ChannelFutureListener(){
					public void operationComplete(ChannelFuture arg0)
							throws Exception {
						if(arg0.isSuccess()){
							System.out.println(tmpUserID + ":连接成功");
						}
						else if(arg0.isCancelled()){
							System.out.println(tmpUserID + ":连接取消");
						}
						else if(arg0.cause() != null){
							System.out.println(tmpUserID + ":连接失败  " + arg0.cause());
						}
					}				
				});
				try{
					Thread.sleep(200);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}catch(Exception e){
				System.out.println("初始化客户端失败 ！！！");
				e.printStackTrace();
			}
			for(int t=0;t<1000000;t++){
				for(int h = 0 ;h<100;h++){
					
				}
			}
		}
		for(int i = 0;i< connectNum;i++){
			fArray[i].channel().closeFuture();
		}
		for(int s = 0;s < 20;s++){
			Thread th = new Thread(new cmdExecutor(mContextUser));  //start the thread to execute cmd
			th.start();
		}
		
		while(true);
	}
	private String _userId;
	private String _token;
	private String _password;
	private String _deviceId;
}
