/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.malhartech.bufferserver;

import com.google.protobuf.ByteString;
import com.malhartech.bufferserver.Buffer.Data;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.channel.*;

/**
 * this class is called the last while reading the response from server.
 *
 * @author chetan
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            ClientHandler.class.getName());
    // Stateful properties
    private volatile Channel channel;

    public void publish(String identifier, String type) {
        Buffer.PublisherRequest.Builder prb = Buffer.PublisherRequest.newBuilder();
        prb.setIdentifier(identifier).setType(type);

        Data.Builder db = Data.newBuilder();
        db.setType(Data.DataType.PUBLISHER_REQUEST);
        db.setPublish(prb);

        final ChannelFutureListener cfl = new ChannelFutureListener() {

            public void operationComplete(ChannelFuture cf) throws Exception {
                Buffer.PartitionedData.Builder pdb = Buffer.PartitionedData.newBuilder();
                pdb.setWindowId(new Date().getTime());
                pdb.setData(ByteString.EMPTY);

                byte[] bytes = String.valueOf(new Random().nextInt() % 10).getBytes();
                pdb.setPartition(ByteString.copyFrom(bytes));


                Buffer.Data.Builder db = Data.newBuilder();
                db.setType(Data.DataType.PARTITIONED_DATA);
                db.setPartitioneddata(pdb);

                Thread.sleep(500);
                cf.getChannel().write(db).addListener(this);
            }
        };

        channel.write(db).addListener(cfl);
    }

    public void registerPartitions(String id, String down_type, String node, String type, Collection<String> partitions) {
        Buffer.SubscriberRequest.Builder srb = Buffer.SubscriberRequest.newBuilder();
        srb.setIdentifier(id);
        srb.setType(down_type);
        srb.setUpstreamIdentifier(node);
        srb.setUpstreamType(type);

        for (String c : partitions) {
            srb.addPartition(ByteString.copyFromUtf8(c));
        }

        Data.Builder builder = Data.newBuilder();
        builder.setType(Data.DataType.SUBSCRIBER_REQUEST);
        builder.setSubscribe(srb);

        channel.write(builder.build());
    }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }

        System.out.println(e);

        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        channel = e.getChannel();
        super.channelOpen(ctx, e);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, final MessageEvent e) {
        Data data = (Data) e.getMessage();
        System.out.println(data.getType());
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        //compiled code
        throw new RuntimeException("Compiled Code");
    }
}
