package rml.handler;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import rml.model.User;
import rml.server.HouseKeeper;
import rml.util.JsonMapper;

import java.util.Map;

/**
 * Simple to Introduction
 * author:Jianghui
 * date:2016/7/23 22:05
 */
public class AudioCloseRespHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message= (String) msg;
        JsonNode jsonNode= JsonMapper.getInstance().readValue(message, JsonNode.class);
        if("audioClose".equals(jsonNode.get("type").asText())){
            HouseKeeper.closeAudio(ctx.channel());//
            String stoken=HouseKeeper.getChannelRelation(ctx.channel().localAddress().toString()).get(ctx.channel().remoteAddress().toString());
            notifyAllUser(ctx,stoken);
        }else{
            ctx.fireChannelRead(msg);
        }

    }

    private void notifyAllUser(ChannelHandlerContext ctx,String token) {
        String msg="{\"type\":\"audioClose\",\"token\":\""+token+"\"}";
        Map<String,User> sessions= HouseKeeper.getSessions(ctx.channel().localAddress().toString());
        for(User user:sessions.values()){
            if(user.getToken().equals(token)){
                continue;
            }
            user.getNettyChannel().writeAndFlush(msg);
        }
    }
}
