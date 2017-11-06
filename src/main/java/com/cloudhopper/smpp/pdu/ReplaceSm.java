package com.cloudhopper.smpp.pdu;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.commons.util.StringUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.util.ByteBufUtil;
import com.cloudhopper.smpp.util.PduUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
public class ReplaceSm extends PduRequest<ReplaceSmResp> {

    @Setter private String messageId;
    @Setter private Address sourceAddress;
    @Setter private String scheduleDeliveryTime;
    @Setter private String validityPeriod;
    @Setter private byte registeredDelivery;
    @Setter private byte defaultMsgId;
            private byte[] shortMessage;
    
    public ReplaceSm() {
        super(SmppConstants.CMD_ID_REPLACE_SM, "replace_sm");
    }

    @Override
    public ReplaceSmResp createResponse() {
        ReplaceSmResp resp = new ReplaceSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<ReplaceSmResp> getResponseClass() {
        return ReplaceSmResp.class;
    }

    public int getShortMessageLength() {
        return Optional.ofNullable(shortMessage).map(s -> s.length).orElse(0);
    }

     public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
        if (value != null && value.length > 255) {
            throw new SmppInvalidArgumentException("A short message in a PDU can only be a max of 255 bytes [actual=" + value.length + "]; use optional parameter message_payload as an alternative");
        }
        this.shortMessage = value;
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.messageId = ByteBufUtil.readNullTerminatedString(buffer); 
        this.sourceAddress = ByteBufUtil.readAddress(buffer);
        this.scheduleDeliveryTime = ByteBufUtil.readNullTerminatedString(buffer);
        this.validityPeriod = ByteBufUtil.readNullTerminatedString(buffer);
        this.registeredDelivery = buffer.readByte();
        this.defaultMsgId = buffer.readByte();
        // this is always an unsigned version of the short message length
        short shortMessageLength = buffer.readUnsignedByte();
        this.shortMessage = new byte[shortMessageLength];
        buffer.readBytes(this.shortMessage);
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.messageId);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.scheduleDeliveryTime);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.validityPeriod);
        bodyLength += 3;    // regDelivery, defaultMsgId, messageLength bytes
        bodyLength += getShortMessageLength();
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.messageId);
        ByteBufUtil.writeAddress(buffer, this.sourceAddress);
        ByteBufUtil.writeNullTerminatedString(buffer, this.scheduleDeliveryTime);
        ByteBufUtil.writeNullTerminatedString(buffer, this.validityPeriod);
        buffer.writeByte(this.registeredDelivery);
        buffer.writeByte(this.defaultMsgId);
        buffer.writeByte((byte)getShortMessageLength());
        if (this.shortMessage != null) {
            buffer.writeBytes(this.shortMessage);
        }
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("( messageId [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.messageId));
        buffer.append("] sourceAddr [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.sourceAddress));
        buffer.append("] scheduleDeliveryTime [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.scheduleDeliveryTime));
        buffer.append("] validityPeriod [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.validityPeriod));
        buffer.append("] regDlvry [0x");
        buffer.append(HexUtil.toHexString(this.registeredDelivery));
        buffer.append("] message [");
        HexUtil.appendHexString(buffer, this.shortMessage);
        buffer.append("])");
    }

}