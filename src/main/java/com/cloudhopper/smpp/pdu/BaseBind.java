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
import com.cloudhopper.smpp.type.NotEnoughDataInBufferException;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.util.ByteBufUtil;
import com.cloudhopper.smpp.util.PduUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
@Getter
@Setter
public abstract class BaseBind<R extends PduResponse> extends PduRequest<R> {

    private String systemId;
    private String password;
    private String systemType;
    private byte interfaceVersion;
    private Address addressRange;

    public BaseBind(int commandId, String name) {
        super(commandId, name);
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.systemId = ByteBufUtil.readNullTerminatedString(buffer);
        this.password = ByteBufUtil.readNullTerminatedString(buffer);
        this.systemType = ByteBufUtil.readNullTerminatedString(buffer);
        // at this point, we should have at least 3 bytes left
        if (buffer.readableBytes() < 3) {
            throw new NotEnoughDataInBufferException("After parsing systemId, password, and systemType", buffer.readableBytes(), 3);
        }
        this.interfaceVersion = buffer.readByte();
        this.addressRange = ByteBufUtil.readAddress(buffer);
    }
    
    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemId);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.password);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemType);
        bodyLength += 1; // interface version
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.addressRange);
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.systemId);
        ByteBufUtil.writeNullTerminatedString(buffer, this.password);
        ByteBufUtil.writeNullTerminatedString(buffer, this.systemType);
        buffer.writeByte(this.interfaceVersion);
        ByteBufUtil.writeAddress(buffer, this.addressRange);
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("systemId [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.systemId));
        buffer.append("] password [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.password));
        buffer.append("] systemType [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.systemType));
        buffer.append("] interfaceVersion [0x");
        buffer.append(HexUtil.toHexString(this.interfaceVersion));
        buffer.append("] addressRange (");
        if (this.addressRange == null) {
            buffer.append(SmppConstants.EMPTY_ADDRESS.toString());
        } else {
            buffer.append(StringUtil.toStringWithNullAsEmpty(this.addressRange));
        }
        buffer.append(")");
    }
}