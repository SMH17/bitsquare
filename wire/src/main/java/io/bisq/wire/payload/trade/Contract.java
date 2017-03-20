/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.wire.payload.trade;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import io.bisq.common.app.Version;
import io.bisq.common.monetary.Price;
import io.bisq.common.util.JsonExclude;
import io.bisq.wire.payload.Payload;
import io.bisq.wire.payload.crypto.PubKeyRing;
import io.bisq.wire.payload.offer.OfferPayload;
import io.bisq.wire.payload.p2p.NodeAddress;
import io.bisq.wire.payload.payment.PaymentAccountPayload;
import io.bisq.wire.proto.Messages;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Utils;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
@Immutable
public final class Contract implements Payload {
    // That object is sent over the wire, so we need to take care of version compatibility.
    @JsonExclude
    public static final long serialVersionUID = Version.P2P_NETWORK_VERSION;

    // Payload
    public final OfferPayload offerPayload;
    private final long tradeAmount;
    private final long tradePrice;
    public final String takeOfferFeeTxID;
    public final NodeAddress arbitratorNodeAddress;
    private final boolean isBuyerOffererAndSellerTaker;
    private final String offererAccountId;
    private final String takerAccountId;
    private final PaymentAccountPayload offererPaymentAccountPayload;
    private final PaymentAccountPayload takerPaymentAccountPayload;
    @JsonExclude
    private final PubKeyRing offererPubKeyRing;
    @JsonExclude
    private final PubKeyRing takerPubKeyRing;
    private final NodeAddress buyerNodeAddress;
    private final NodeAddress sellerNodeAddress;
    private final String offererPayoutAddressString;
    private final String takerPayoutAddressString;
    @JsonExclude
    private final byte[] offererMultiSigPubKey;
    @JsonExclude
    private final byte[] takerMultiSigPubKey;

    public Contract(OfferPayload offerPayload,
                    Coin tradeAmount,
                    Price tradePrice,
                    String takeOfferFeeTxID,
                    NodeAddress buyerNodeAddress,
                    NodeAddress sellerNodeAddress,
                    NodeAddress arbitratorNodeAddress,
                    boolean isBuyerOffererAndSellerTaker,
                    String offererAccountId,
                    String takerAccountId,
                    PaymentAccountPayload offererPaymentAccountPayload,
                    PaymentAccountPayload takerPaymentAccountPayload,
                    PubKeyRing offererPubKeyRing,
                    PubKeyRing takerPubKeyRing,
                    String offererPayoutAddressString,
                    String takerPayoutAddressString,
                    byte[] offererMultiSigPubKey,
                    byte[] takerMultiSigPubKey) {
        this.offerPayload = offerPayload;
        this.tradePrice = tradePrice.getValue();
        this.buyerNodeAddress = buyerNodeAddress;
        this.sellerNodeAddress = sellerNodeAddress;
        this.tradeAmount = tradeAmount.value;
        this.takeOfferFeeTxID = takeOfferFeeTxID;
        this.arbitratorNodeAddress = arbitratorNodeAddress;
        this.isBuyerOffererAndSellerTaker = isBuyerOffererAndSellerTaker;
        this.offererAccountId = offererAccountId;
        this.takerAccountId = takerAccountId;
        this.offererPaymentAccountPayload = offererPaymentAccountPayload;
        this.takerPaymentAccountPayload = takerPaymentAccountPayload;
        this.offererPubKeyRing = offererPubKeyRing;
        this.takerPubKeyRing = takerPubKeyRing;
        this.offererPayoutAddressString = offererPayoutAddressString;
        this.takerPayoutAddressString = takerPayoutAddressString;
        this.offererMultiSigPubKey = offererMultiSigPubKey;
        this.takerMultiSigPubKey = takerMultiSigPubKey;

        // PaymentMethod need to be the same
        Preconditions.checkArgument(offererPaymentAccountPayload.getPaymentMethodId()
                        .equals(takerPaymentAccountPayload.getPaymentMethodId()),
                "payment methods of maker and taker must be the same.\n" +
                        "offererPaymentMethodId=" + offererPaymentAccountPayload.getPaymentMethodId() + "\n" +
                        "takerPaymentMethodId=" + takerPaymentAccountPayload.getPaymentMethodId());
    }

    public boolean isBuyerOffererAndSellerTaker() {
        return isBuyerOffererAndSellerTaker;
    }

    public String getBuyerAccountId() {
        return isBuyerOffererAndSellerTaker ? offererAccountId : takerAccountId;
    }

    public String getSellerAccountId() {
        return isBuyerOffererAndSellerTaker ? takerAccountId : offererAccountId;
    }

    public String getBuyerPayoutAddressString() {
        return isBuyerOffererAndSellerTaker ? offererPayoutAddressString : takerPayoutAddressString;
    }

    public String getSellerPayoutAddressString() {
        return isBuyerOffererAndSellerTaker ? takerPayoutAddressString : offererPayoutAddressString;
    }

    public PubKeyRing getBuyerPubKeyRing() {
        return isBuyerOffererAndSellerTaker ? offererPubKeyRing : takerPubKeyRing;
    }

    public PubKeyRing getSellerPubKeyRing() {
        return isBuyerOffererAndSellerTaker ? takerPubKeyRing : offererPubKeyRing;
    }

    public byte[] getBuyerMultiSigPubKey() {
        return isBuyerOffererAndSellerTaker ? offererMultiSigPubKey : takerMultiSigPubKey;
    }

    public byte[] getSellerMultiSigPubKey() {
        return isBuyerOffererAndSellerTaker ? takerMultiSigPubKey : offererMultiSigPubKey;
    }

    public PaymentAccountPayload getBuyerPaymentAccountPayload() {
        return isBuyerOffererAndSellerTaker ? offererPaymentAccountPayload : takerPaymentAccountPayload;
    }

    public PaymentAccountPayload getSellerPaymentAccountPayload() {
        return isBuyerOffererAndSellerTaker ? takerPaymentAccountPayload : offererPaymentAccountPayload;
    }

    public String getPaymentMethodId() {
        return offererPaymentAccountPayload.getPaymentMethodId();
    }

    public Coin getTradeAmount() {
        return Coin.valueOf(tradeAmount);
    }

    public Price getTradePrice() {
        return Price.valueOf(offerPayload.getCurrencyCode(), tradePrice);
    }

    public NodeAddress getBuyerNodeAddress() {
        return buyerNodeAddress;
    }

    public NodeAddress getSellerNodeAddress() {
        return sellerNodeAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contract)) return false;

        Contract contract = (Contract) o;

        if (tradeAmount != contract.tradeAmount) return false;
        if (tradePrice != contract.tradePrice) return false;
        if (isBuyerOffererAndSellerTaker != contract.isBuyerOffererAndSellerTaker) return false;
        if (offerPayload != null ? !offerPayload.equals(contract.offerPayload) : contract.offerPayload != null)
            return false;
        if (takeOfferFeeTxID != null ? !takeOfferFeeTxID.equals(contract.takeOfferFeeTxID) : contract.takeOfferFeeTxID != null)
            return false;
        if (arbitratorNodeAddress != null ? !arbitratorNodeAddress.equals(contract.arbitratorNodeAddress) : contract.arbitratorNodeAddress != null)
            return false;
        if (offererAccountId != null ? !offererAccountId.equals(contract.offererAccountId) : contract.offererAccountId != null)
            return false;
        if (takerAccountId != null ? !takerAccountId.equals(contract.takerAccountId) : contract.takerAccountId != null)
            return false;
        if (!offererPaymentAccountPayload.equals(contract.offererPaymentAccountPayload))
            return false;
        if (!takerPaymentAccountPayload.equals(contract.takerPaymentAccountPayload))
            return false;
        if (offererPubKeyRing != null ? !offererPubKeyRing.equals(contract.offererPubKeyRing) : contract.offererPubKeyRing != null)
            return false;
        if (takerPubKeyRing != null ? !takerPubKeyRing.equals(contract.takerPubKeyRing) : contract.takerPubKeyRing != null)
            return false;
        if (buyerNodeAddress != null ? !buyerNodeAddress.equals(contract.buyerNodeAddress) : contract.buyerNodeAddress != null)
            return false;
        if (sellerNodeAddress != null ? !sellerNodeAddress.equals(contract.sellerNodeAddress) : contract.sellerNodeAddress != null)
            return false;
        if (offererPayoutAddressString != null ? !offererPayoutAddressString.equals(contract.offererPayoutAddressString) : contract.offererPayoutAddressString != null)
            return false;
        if (takerPayoutAddressString != null ? !takerPayoutAddressString.equals(contract.takerPayoutAddressString) : contract.takerPayoutAddressString != null)
            return false;
        if (!Arrays.equals(offererMultiSigPubKey, contract.offererMultiSigPubKey)) return false;
        return Arrays.equals(takerMultiSigPubKey, contract.takerMultiSigPubKey);

    }

    @Override
    public int hashCode() {
        int result = offerPayload != null ? offerPayload.hashCode() : 0;
        result = 31 * result + (int) (tradeAmount ^ (tradeAmount >>> 32));
        result = 31 * result + (int) (tradePrice ^ (tradePrice >>> 32));
        result = 31 * result + (takeOfferFeeTxID != null ? takeOfferFeeTxID.hashCode() : 0);
        result = 31 * result + (arbitratorNodeAddress != null ? arbitratorNodeAddress.hashCode() : 0);
        result = 31 * result + (isBuyerOffererAndSellerTaker ? 1 : 0);
        result = 31 * result + (offererAccountId != null ? offererAccountId.hashCode() : 0);
        result = 31 * result + (takerAccountId != null ? takerAccountId.hashCode() : 0);
        result = 31 * result + (offererPaymentAccountPayload.hashCode());
        result = 31 * result + (takerPaymentAccountPayload.hashCode());
        result = 31 * result + (offererPubKeyRing != null ? offererPubKeyRing.hashCode() : 0);
        result = 31 * result + (takerPubKeyRing != null ? takerPubKeyRing.hashCode() : 0);
        result = 31 * result + (buyerNodeAddress != null ? buyerNodeAddress.hashCode() : 0);
        result = 31 * result + (sellerNodeAddress != null ? sellerNodeAddress.hashCode() : 0);
        result = 31 * result + (offererPayoutAddressString != null ? offererPayoutAddressString.hashCode() : 0);
        result = 31 * result + (takerPayoutAddressString != null ? takerPayoutAddressString.hashCode() : 0);
        result = 31 * result + (offererMultiSigPubKey != null ? Arrays.hashCode(offererMultiSigPubKey) : 0);
        result = 31 * result + (takerMultiSigPubKey != null ? Arrays.hashCode(takerMultiSigPubKey) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "\n\toffer=" + offerPayload +
                "\n\ttradeAmount=" + tradeAmount +
                "\n\ttradePrice=" + tradePrice +
                "\n\ttakeOfferFeeTxID='" + takeOfferFeeTxID + '\'' +
                "\n\tarbitratorAddress=" + arbitratorNodeAddress +
                "\n\tisBuyerOffererAndSellerTaker=" + isBuyerOffererAndSellerTaker +
                "\n\toffererAccountId='" + offererAccountId + '\'' +
                "\n\ttakerAccountId='" + takerAccountId + '\'' +
                "\n\toffererPaymentAccountPayload=" + offererPaymentAccountPayload +
                "\n\ttakerPaymentAccountPayload=" + takerPaymentAccountPayload +
                "\n\toffererPubKeyRing=" + offererPubKeyRing +
                "\n\ttakerPubKeyRing=" + takerPubKeyRing +
                "\n\tbuyerAddress=" + buyerNodeAddress +
                "\n\tsellerAddress=" + sellerNodeAddress +
                "\n\toffererPayoutAddressString='" + offererPayoutAddressString + '\'' +
                "\n\ttakerPayoutAddressString='" + takerPayoutAddressString + '\'' +
                "\n\toffererMultiSigPubKey=" + Utils.HEX.encode(offererMultiSigPubKey) +
                "\n\ttakerMultiSigPubKey=" + Utils.HEX.encode(takerMultiSigPubKey) +
                "\n\tBuyerMultiSigPubKey=" + Utils.HEX.encode(getBuyerMultiSigPubKey()) +
                "\n\tSellerMultiSigPubKey=" + Utils.HEX.encode(getSellerMultiSigPubKey()) +
                '}';
    }

    @Override
    public Messages.Contract toProtoBuf() {
        return Messages.Contract.newBuilder()
                .setOfferPayload(offerPayload.toProtoBuf().getOfferPayload())
                .setTradeAmount(tradeAmount)
                .setTradePrice(tradePrice)
                .setTakeOfferFeeTxId(takeOfferFeeTxID)
                .setArbitratorNodeAddress(arbitratorNodeAddress.toProtoBuf())
                .setIsBuyerOffererAndSellerTaker(isBuyerOffererAndSellerTaker)
                .setOffererAccountId(offererAccountId)
                .setTakerAccountId(takerAccountId)
                .setOffererPaymentAccountPayload((Messages.PaymentAccountPayload) offererPaymentAccountPayload.toProtoBuf())
                .setTakerPaymentAccountPayload((Messages.PaymentAccountPayload) takerPaymentAccountPayload.toProtoBuf())
                .setOffererPubKeyRing(offererPubKeyRing.toProtoBuf())
                .setTakerPubKeyRing(takerPubKeyRing.toProtoBuf())
                .setBuyerNodeAddress(buyerNodeAddress.toProtoBuf())
                .setSellerNodeAddress(sellerNodeAddress.toProtoBuf())
                .setOffererPayoutAddressString(offererPayoutAddressString)
                .setTakerPayoutAddressString(takerPayoutAddressString)
                .setOffererBtcPubKey(ByteString.copyFrom(offererMultiSigPubKey))
                .setTakerBtcPubKey(ByteString.copyFrom(takerMultiSigPubKey)).build();
    }
}