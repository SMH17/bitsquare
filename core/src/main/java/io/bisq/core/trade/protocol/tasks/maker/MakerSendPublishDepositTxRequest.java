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

package io.bisq.core.trade.protocol.tasks.maker;

import io.bisq.common.taskrunner.TaskRunner;
import io.bisq.core.btc.AddressEntry;
import io.bisq.core.btc.wallet.BtcWalletService;
import io.bisq.core.trade.Trade;
import io.bisq.core.trade.messages.PublishDepositTxRequest;
import io.bisq.core.trade.protocol.tasks.TradeTask;
import io.bisq.network.p2p.SendMailboxMessageListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class MakerSendPublishDepositTxRequest extends TradeTask {
    @SuppressWarnings({"WeakerAccess", "unused"})
    public MakerSendPublishDepositTxRequest(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            BtcWalletService walletService = processModel.getWalletService();
            String id = processModel.getOffer().getId();

            Optional<AddressEntry> addressEntryOptional = walletService.getAddressEntry(id, AddressEntry.Context.MULTI_SIG);
            checkArgument(addressEntryOptional.isPresent(), "addressEntry must be set here.");
            AddressEntry makerPayoutAddressEntry = walletService.getOrCreateAddressEntry(id, AddressEntry.Context.TRADE_PAYOUT);
            byte[] makerMultiSigPubKey = processModel.getMyMultiSigPubKey();
            checkArgument(Arrays.equals(makerMultiSigPubKey,
                            addressEntryOptional.get().getPubKey()),
                    "makerMultiSigPubKey from AddressEntry must match the one from the trade data. trade id =" + id);

            PublishDepositTxRequest message = new PublishDepositTxRequest(
                    processModel.getId(),
                    processModel.getPaymentAccountPayload(trade),
                    processModel.getAccountId(),
                    makerMultiSigPubKey,
                    trade.getContractAsJson(),
                    trade.getMakerContractSignature(),
                    makerPayoutAddressEntry.getAddressString(),
                    processModel.getPreparedDepositTx(),
                    processModel.getRawTransactionInputs(),
                    processModel.getMyNodeAddress(),
                    UUID.randomUUID().toString()
            );
            trade.setState(Trade.State.MAKER_SENT_PUBLISH_DEPOSIT_TX_REQUEST);

            processModel.getP2PService().sendEncryptedMailboxMessage(
                    trade.getTradingPeerNodeAddress(),
                    processModel.tradingPeer.getPubKeyRing(),
                    message,
                    new SendMailboxMessageListener() {
                        @Override
                        public void onArrived() {
                            log.info("Message arrived at peer. tradeId={}, message{}", id, message);
                            trade.setState(Trade.State.MAKER_SAW_ARRIVED_PUBLISH_DEPOSIT_TX_REQUEST);
                            complete();
                        }

                        @Override
                        public void onStoredInMailbox() {
                            log.info("Message stored in mailbox. tradeId={}, message{}", id, message);
                            trade.setState(Trade.State.MAKER_STORED_IN_MAILBOX_PUBLISH_DEPOSIT_TX_REQUEST);
                            complete();
                        }

                        @Override
                        public void onFault(String errorMessage) {
                            trade.setState(Trade.State.MAKER_SEND_FAILED_PUBLISH_DEPOSIT_TX_REQUEST);
                            appendToErrorMessage("Sending message failed: message=" + message + "\nerrorMessage=" + errorMessage);
                            failed(errorMessage);
                        }
                    }
            );

        } catch (Throwable t) {
            failed(t);
        }
    }
}